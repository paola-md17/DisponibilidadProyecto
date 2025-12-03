package mx.edu.utng.jdrj.disponibilidad.data.model

data class EspacioEquipamiento(
    val idRelacion: String = "",
    val idEspacio: String = "", // FK hacia Espacio
    val idEquipo: String = "",  // FK hacia Equipamiento
    val cantidad: Int = 1
)