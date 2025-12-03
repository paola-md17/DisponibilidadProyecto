package mx.edu.utng.jdrj.disponibilidad.data.model

// Modelo para mostrar equipos en la UI con su cantidad total disponible
data class EquipoUI(
    val idEquipo: String,
    val nombre: String,
    val cantidadTotal: Int
)