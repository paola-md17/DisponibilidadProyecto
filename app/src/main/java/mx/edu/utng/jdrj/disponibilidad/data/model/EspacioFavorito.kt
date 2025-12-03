package mx.edu.utng.jdrj.disponibilidad.data.model

data class EspacioFavorito(
    val idFavorito: String = "",
    val idUsuario: String = "", // FK hacia Usuario
    val idEspacio: String = ""  // FK hacia Espacio
)