package mx.edu.utng.jdrj.disponibilidad.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mx.edu.utng.jdrj.disponibilidad.data.model.Reserva
import mx.edu.utng.jdrj.disponibilidad.data.model.EspacioEquipamiento
import mx.edu.utng.jdrj.disponibilidad.utils.Constants

class ReservasRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection(Constants.COLLECTION_RESERVAS)

    // --- USUARIO NORMAL ---

    suspend fun crearReserva(reserva: Reserva): Result<Boolean> {
        return try {
            val errorConflicto = verificarDisponibilidadInteligente(reserva)
            if (errorConflicto != null) {
                return Result.failure(Exception(errorConflicto))
            }

            val docRef = collection.document()
            val nuevaReserva = reserva.copy(idReserva = docRef.id, timestamp = System.currentTimeMillis())
            docRef.set(nuevaReserva).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerMisReservas(usuarioId: String): List<Reserva> {
        return try {
            val snapshot = collection.whereEqualTo("idUsuario", usuarioId).get().await()
            snapshot.toObjects(Reserva::class.java).sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- NUEVO: OBTENER TODO EL HISTORIAL (PARA ESTADÍSTICAS) ---
    suspend fun obtenerTodasLasReservas(): List<Reserva> {
        return try {
            // Descargamos TODO para procesarlo en la app (conteo, gráficas, etc.)
            val snapshot = collection.get().await()
            snapshot.toObjects(Reserva::class.java)
        } catch (e: Exception) {
            Log.e("ReservasRepo", "Error al obtener estadísticas", e)
            emptyList()
        }
    }

    // --- LÓGICA MAESTRA DE INVENTARIO ---
    private suspend fun verificarDisponibilidadInteligente(nueva: Reserva): String? {
        try {
            val snapshot = collection
                .whereEqualTo("idEspacio", nueva.idEspacio)
                .whereEqualTo("fecha", nueva.fecha)
                .get().await()

            val reservasDelDia = snapshot.toObjects(Reserva::class.java)
                .filter { it.estado != Constants.ESTADO_CANCELADA }

            val inicioNuevo = convertirHoraAMinutos(nueva.horaInicio)
            val finNuevo = convertirHoraAMinutos(nueva.horaFin)

            val reservasEnConflicto = reservasDelDia.filter { r ->
                val inicioExistente = convertirHoraAMinutos(r.horaInicio)
                val finExistente = convertirHoraAMinutos(r.horaFin)
                (inicioNuevo < finExistente) && (inicioExistente < finNuevo)
            }

            // CASO 1: AULA COMPLETA
            if (nueva.equiposReservados.isEmpty()) {
                if (reservasEnConflicto.isNotEmpty()) {
                    return "El espacio o sus equipos ya están ocupados en este horario."
                }
                return null
            }

            // CASO 2: RESERVA DE EQUIPOS
            else {
                if (reservasEnConflicto.any { it.equiposReservados.isEmpty() }) {
                    return "El aula está reservada completa para un evento/clase."
                }

                for (itemNuevo in nueva.equiposReservados) {
                    var cantidadOcupada = 0
                    for (reservaExistente in reservasEnConflicto) {
                        val itemEncontrado = reservaExistente.equiposReservados.find { it.idEquipo == itemNuevo.idEquipo }
                        if (itemEncontrado != null) {
                            cantidadOcupada += itemEncontrado.cantidad
                        }
                    }

                    val relacionQuery = db.collection(Constants.COLLECTION_ESPACIO_EQUIPAMIENTO)
                        .whereEqualTo("idEspacio", nueva.idEspacio)
                        .whereEqualTo("idEquipo", itemNuevo.idEquipo)
                        .get().await()

                    val relacion = relacionQuery.toObjects(EspacioEquipamiento::class.java).firstOrNull()
                    val capacidadTotal = relacion?.cantidad ?: 0

                    if ((cantidadOcupada + itemNuevo.cantidad) > capacidadTotal) {
                        val disponibles = capacidadTotal - cantidadOcupada
                        return "No hay suficientes '${itemNuevo.nombreEquipo}'. (Pides ${itemNuevo.cantidad}, quedan $disponibles)."
                    }
                }
                return null
            }

        } catch (e: Exception) {
            return "Error al verificar disponibilidad: ${e.message}"
        }
    }

    private fun convertirHoraAMinutos(horaTexto: String): Int {
        try {
            val partes = horaTexto.split(" ", ":")
            if (partes.size < 2) return 0
            var horas = partes[0].toInt()
            val minutos = partes[1].toInt()
            if (partes.size >= 3) {
                val periodo = partes[2].uppercase()
                if (periodo == "PM" && horas != 12) horas += 12
                if (periodo == "AM" && horas == 12) horas = 0
            }
            return (horas * 60) + minutos
        } catch (e: Exception) { return 0 }
    }

    // --- RESTO DE FUNCIONES ---

    suspend fun eliminarReservaDefinitivamente(idReserva: String): Result<Boolean> {
        return try {
            collection.document(idReserva).delete().await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun cancelarReserva(idReserva: String): Result<Boolean> {
        return rechazarReserva(idReserva, "Cancelada por el usuario")
    }

    suspend fun obtenerReservasPorEspacioYFecha(idEspacio: String, fecha: String): List<Reserva> {
        return try {
            val snapshot = collection.whereEqualTo("idEspacio", idEspacio).whereEqualTo("fecha", fecha).get().await()
            snapshot.toObjects(Reserva::class.java).filter { it.estado != Constants.ESTADO_CANCELADA }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun obtenerReservasPendientes(): List<Reserva> {
        return try {
            val snapshot = collection.whereEqualTo("estado", Constants.ESTADO_PENDIENTE).get().await()
            snapshot.toObjects(Reserva::class.java).sortedBy { it.timestamp }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun aprobarReserva(idReserva: String): Result<Boolean> {
        return try {
            collection.document(idReserva).update("estado", Constants.ESTADO_APROBADA).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun rechazarReserva(idReserva: String, motivo: String): Result<Boolean> {
        return try {
            collection.document(idReserva).update(mapOf("estado" to Constants.ESTADO_CANCELADA, "motivoRechazo" to motivo)).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun escucharCambiosMisReservas(idUsuario: String, onModificacion: (Reserva) -> Unit) {
        collection.whereEqualTo("idUsuario", idUsuario).addSnapshotListener { snapshot, _ ->
            snapshot?.documentChanges?.forEach { if (it.type == DocumentChange.Type.MODIFIED) onModificacion(it.document.toObject(Reserva::class.java)) }
        }
    }

    fun escucharNuevasSolicitudes(onNueva: (Reserva) -> Unit) {
        collection.whereEqualTo("estado", Constants.ESTADO_PENDIENTE).addSnapshotListener { snapshot, _ ->
            snapshot?.documentChanges?.forEach { if (it.type == DocumentChange.Type.ADDED) onNueva(it.document.toObject(Reserva::class.java)) }
        }
    }
}