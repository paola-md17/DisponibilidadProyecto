package mx.edu.utng.jdrj.disponibilidad.utils

object Constants {
    const val COLLECTION_USUARIOS = "usuarios"
    const val COLLECTION_ESPACIOS = "espacios"
    const val COLLECTION_RESERVAS = "reservas"

    // --- NUEVAS TABLAS PARA CAMINO B ---
    const val COLLECTION_EQUIPAMIENTO = "equipamiento"                 // Catálogo de equipos
    const val COLLECTION_ESPACIO_EQUIPAMIENTO = "espacio_equipamiento" // Tabla intermedia
    const val COLLECTION_FAVORITOS = "espacio_favorito"                // Tabla de likes

    const val ESTADO_PENDIENTE = "pendiente"
    const val ESTADO_APROBADA = "aprobada"
    const val ESTADO_CANCELADA = "cancelada"
}