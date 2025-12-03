package mx.edu.utng.jdrj.disponibilidad.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.edu.utng.jdrj.disponibilidad.data.model.Reserva
import mx.edu.utng.jdrj.disponibilidad.data.repository.ReservasRepository
import mx.edu.utng.jdrj.disponibilidad.utils.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReservaViewModel : ViewModel() {
    private val repository = ReservasRepository()

    // --- VARIABLES DE ESTADO ---

    // 1. Para la pantalla "Mis Reservas" (Historial)
    private var todasLasReservas = listOf<Reserva>()
    var reservasVisibles by mutableStateOf<List<Reserva>>(emptyList())
        private set
    var mostrarHistorialCompleto by mutableStateOf(false)
        private set

    // 2. NUEVO: Para la Agenda Visual en el Detalle
    // Esta lista se llena con las reservas que estorban en la fecha seleccionada
    var reservasEnFechaSeleccionada by mutableStateOf<List<Reserva>>(emptyList())
        private set

    // 3. Generales
    var isLoading by mutableStateOf(false)
        private set
    var mensajeExito by mutableStateOf<String?>(null)
    var mensajeError by mutableStateOf<String?>(null)


    // --- MÉTODOS DE AGENDA VISUAL (NUEVO PASO 2) ---

    fun cargarAgenda(idEspacio: String, fecha: String) {
        viewModelScope.launch {
            // Consultamos al repo y guardamos la lista de ocupados
            // No activamos isLoading global para no bloquear la pantalla completa, solo queremos actualizar la lista
            reservasEnFechaSeleccionada = repository.obtenerReservasPorEspacioYFecha(idEspacio, fecha)
        }
    }


    // --- MÉTODOS DE CREACIÓN ---

    fun crearReserva(reserva: Reserva, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            limpiarMensajes()
            val result = repository.crearReserva(reserva)
            if (result.isSuccess) {
                mensajeExito = "¡Acción completada con éxito!"
                // Actualizamos la agenda inmediatamente para ver el nuevo hueco ocupado
                cargarAgenda(reserva.idEspacio, reserva.fecha)
                onSuccess()
            } else {
                mensajeError = "Error: ${result.exceptionOrNull()?.message}"
            }
            isLoading = false
        }
    }

    // Creación Recurrente (Para Admin)
    fun crearReservaRecurrente(reservaBase: Reserva, fechaFinRepeticion: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            limpiarMensajes()

            val listaFechas = generarFechasSemanales(reservaBase.fecha, fechaFinRepeticion)
            var exitos = 0
            var conflictos = 0

            listaFechas.forEach { fechaIteracion ->
                val nuevaReserva = reservaBase.copy(fecha = fechaIteracion)
                val result = repository.crearReserva(nuevaReserva)
                if (result.isSuccess) exitos++ else conflictos++
            }

            if (exitos > 0 && conflictos == 0) {
                mensajeExito = "¡Se asignaron las $exitos fechas del cuatrimestre correctamente!"
                // Actualizamos la agenda del día actual
                cargarAgenda(reservaBase.idEspacio, reservaBase.fecha)
                onSuccess()
            } else if (exitos > 0 && conflictos > 0) {
                mensajeExito = "Se asignaron $exitos fechas. $conflictos no se pudieron porque ya estaban ocupadas."
            } else {
                mensajeError = "No se pudo asignar ninguna fecha. Todo ocupado."
            }
            isLoading = false
        }
    }

    private fun generarFechasSemanales(fechaInicio: String, fechaFin: String): List<String> {
        val lista = mutableListOf<String>()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val cal = Calendar.getInstance()
            cal.time = sdf.parse(fechaInicio) ?: return emptyList()
            val dateFin = sdf.parse(fechaFin) ?: return emptyList()

            while (!cal.time.after(dateFin)) {
                lista.add(sdf.format(cal.time))
                cal.add(Calendar.DAY_OF_YEAR, 7)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lista
    }

    // --- MÉTODOS DE HISTORIAL ---

    fun cargarMisReservas(usuarioId: String) {
        viewModelScope.launch {
            isLoading = true
            todasLasReservas = repository.obtenerMisReservas(usuarioId)
            aplicarFiltro()
            isLoading = false
        }
    }

    fun toggleMostrarTodo() {
        mostrarHistorialCompleto = !mostrarHistorialCompleto
        aplicarFiltro()
    }

    private fun aplicarFiltro() {
        if (mostrarHistorialCompleto) {
            reservasVisibles = todasLasReservas
        } else {
            reservasVisibles = todasLasReservas.filter {
                it.estado == Constants.ESTADO_PENDIENTE || it.estado == Constants.ESTADO_APROBADA
            }
        }
    }

    fun cancelarReserva(idReserva: String, usuarioId: String) {
        viewModelScope.launch {
            isLoading = true
            val result = repository.cancelarReserva(idReserva)
            if (result.isSuccess) {
                cargarMisReservas(usuarioId)
                mensajeExito = "Reserva cancelada."
            } else {
                mensajeError = "Error al cancelar."
            }
            isLoading = false
        }
    }

    fun eliminarReserva(idReserva: String, usuarioId: String) {
        viewModelScope.launch {
            isLoading = true
            val result = repository.eliminarReservaDefinitivamente(idReserva)
            if (result.isSuccess) {
                cargarMisReservas(usuarioId)
                mensajeExito = "Registro eliminado."
            } else {
                mensajeError = "Error al eliminar."
            }
            isLoading = false
        }
    }

    fun limpiarMensajes() {
        mensajeExito = null
        mensajeError = null
    }
}