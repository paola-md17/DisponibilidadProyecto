package mx.edu.utng.jdrj.disponibilidad.utils

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.utng.jdrj.disponibilidad.data.model.Reserva
import mx.edu.utng.jdrj.disponibilidad.data.model.Usuario
import mx.edu.utng.jdrj.disponibilidad.data.repository.ReservasRepository

class AppNotificationManager(private val context: Context) {
    private val notificationService = NotificationService(context)
    private val reservasRepo = ReservasRepository()
    private val db = FirebaseFirestore.getInstance()

    private var esPrimeraCargaUsuario = true
    private var esPrimeraCargaAdmin = true

    // Variable para recordar el estado anterior y detectar cambios reales
    private var estadoAprobacionAnterior: Boolean? = null

    // --- 1. ESCUCHA DE RESERVAS (ALUMNO) ---
    fun iniciarEscuchaUsuario(usuario: Usuario) {
        if (usuario.rol == "admin") return

        reservasRepo.escucharCambiosMisReservas(usuario.idUsuario) { reserva, tipo ->
            if (esPrimeraCargaUsuario && tipo == DocumentChange.Type.ADDED) return@escucharCambiosMisReservas

            when (tipo) {
                DocumentChange.Type.MODIFIED -> {
                    if (reserva.estado == Constants.ESTADO_CANCELADA && reserva.motivoRechazo.isNotEmpty()) {
                        notificationService.mostrarNotificacion("❌ Reserva Rechazada", "Motivo: ${reserva.motivoRechazo}")
                    } else if (reserva.estado == Constants.ESTADO_APROBADA) {
                        notificationService.mostrarNotificacion("✅ Reserva Aprobada", "Tu solicitud para ${reserva.nombreEspacio} fue aceptada.")
                    }
                }
                DocumentChange.Type.ADDED -> {
                    if (reserva.estado == Constants.ESTADO_APROBADA) {
                        notificationService.mostrarNotificacion("📅 Nueva Asignación", "El administrador te asignó: ${reserva.nombreEspacio}")
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    notificationService.mostrarNotificacion("🗑️ Reserva Eliminada", "Tu reserva en ${reserva.nombreEspacio} fue eliminada.")
                }
            }
        }

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ esPrimeraCargaUsuario = false }, 3000)
    }

    // --- 2. ESCUCHA DE SOLICITUDES (ADMIN) ---
    fun iniciarEscuchaAdmin(usuario: Usuario) {
        if (usuario.rol != "admin") return

        reservasRepo.escucharNuevasSolicitudes { nuevaReserva ->
            if (!esPrimeraCargaAdmin) {
                notificationService.mostrarNotificacion("🔔 Nueva Solicitud", "Pendiente: ${nuevaReserva.nombreEspacio}")
            }
        }
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ esPrimeraCargaAdmin = false }, 3000)
    }

    // --- 3. ESCUCHA DE PERFIL (CORREGIDA) ---
    fun iniciarEscuchaPerfil(idUsuario: String) {
        Log.d("Notificaciones", "Iniciando escucha de perfil para: $idUsuario")

        db.collection(Constants.COLLECTION_USUARIOS).document(idUsuario)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Notificaciones", "Error escuchando perfil", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val usuarioActualizado = snapshot.toObject(Usuario::class.java) ?: return@addSnapshotListener
                    val aprobadoActual = usuarioActualizado.aprobado

                    // Si es la primera vez que leemos, solo guardamos el estado inicial
                    if (estadoAprobacionAnterior == null) {
                        estadoAprobacionAnterior = aprobadoActual
                        return@addSnapshotListener
                    }

                    // Detectar CAMBIO DE ESTADO: De Falso a Verdadero
                    if (estadoAprobacionAnterior == false && aprobadoActual == true) {
                        Log.d("Notificaciones", "¡Usuario Aprobado! Lanzando notificación.")
                        notificationService.mostrarNotificacion(
                            "🎉 Cuenta Aprobada",
                            "¡Felicidades! El administrador ha aprobado tu acceso."
                        )
                    }
                    // Detectar CAMBIO DE ESTADO: De Verdadero a Falso (Bloqueo)
                    else if (estadoAprobacionAnterior == true && aprobadoActual == false) {
                        notificationService.mostrarNotificacion(
                            "⛔ Cuenta Suspendida",
                            "El administrador ha revocado tu acceso."
                        )
                    }

                    // Actualizamos el estado guardado
                    estadoAprobacionAnterior = aprobadoActual
                }
            }
    }

    fun notificarBienvenida() {
        notificationService.mostrarNotificacion("👋 ¡Bienvenido!", "Registro exitoso. Espera aprobación del administrador.")
    }
}