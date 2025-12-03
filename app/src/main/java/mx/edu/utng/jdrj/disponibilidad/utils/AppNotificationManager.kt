package mx.edu.utng.jdrj.disponibilidad.utils

import android.content.Context
import mx.edu.utng.jdrj.disponibilidad.data.model.Usuario
import mx.edu.utng.jdrj.disponibilidad.data.repository.ReservasRepository
import mx.edu.utng.jdrj.disponibilidad.utils.Constants

class AppNotificationManager(context: Context) {
    private val notificationService = NotificationService(context)
    private val reservasRepo = ReservasRepository()

    // --- ESCUCHA PARA ALUMNOS (CERO SPAM) ---
    // Solo reacciona cuando una reserva cambia de estado (Pendiente -> Aprobada/Cancelada)
    fun iniciarEscuchaUsuario(usuario: Usuario) {
        if (usuario.rol == "admin") return

        reservasRepo.escucharCambiosMisReservas(usuario.idUsuario) { reservaModificada ->
            // Verificamos el estado final para mandar el mensaje correcto

            // 1. Si fue RECHAZADA y tiene motivo
            if (reservaModificada.estado == Constants.ESTADO_CANCELADA && reservaModificada.motivoRechazo.isNotEmpty()) {
                notificationService.mostrarNotificacion(
                    "Reserva Rechazada ❌",
                    "Motivo: ${reservaModificada.motivoRechazo}"
                )
            }
            // 2. Si fue APROBADA
            else if (reservaModificada.estado == Constants.ESTADO_APROBADA) {
                notificationService.mostrarNotificacion(
                    "Reserva Aprobada ✅",
                    "Tu reserva en ${reservaModificada.nombreEspacio} ha sido confirmada."
                )
            }
        }
    }

    // --- ESCUCHA PARA ADMIN (CERO SPAM) ---
    // Solo reacciona cuando llega una solicitud NUEVA (Added)
    fun iniciarEscuchaAdmin(usuario: Usuario) {
        if (usuario.rol != "admin") return

        reservasRepo.escucharNuevasSolicitudes { nuevaReserva ->
            // Al usar DocumentChange.Type.ADDED, esto saltará una vez por cada solicitud nueva real
            notificationService.mostrarNotificacion(
                "Nueva Solicitud 📅",
                "Nueva petición para: ${nuevaReserva.nombreEspacio}"
            )
        }
    }
}