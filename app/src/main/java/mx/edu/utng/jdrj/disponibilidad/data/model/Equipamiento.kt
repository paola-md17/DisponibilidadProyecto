package mx.edu.utng.jdrj.disponibilidad.data.model

data class Equipamiento(
    val idEquipo: String = "",
    val nombre: String = "",           // Ej: "Proyector", "PC Maestro"
    val cantidad: Int = 1
)