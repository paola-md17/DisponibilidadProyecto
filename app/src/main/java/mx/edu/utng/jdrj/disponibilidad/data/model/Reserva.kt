package mx.edu.utng.jdrj.disponibilidad.data.model

data class Reserva(
    val idReserva: String = "",
    val idUsuario: String = "",
    val idEspacio: String = "",
    val nombreEspacio: String = "",

    // --- CARRITO DE COMPRAS (NUEVO) ---
    // Lista de equipos reservados. Si está vacía = Reserva de Aula completa.
    val equiposReservados: List<ItemReserva> = emptyList(),

    // Campos de fecha y estado
    val fecha: String = "",
    val horaInicio: String = "",
    val horaFin: String = "",
    val estado: String = "pendiente",
    val proposito: String = "",
    val motivoRechazo: String = "",
    val timestamp: Long = System.currentTimeMillis()
)