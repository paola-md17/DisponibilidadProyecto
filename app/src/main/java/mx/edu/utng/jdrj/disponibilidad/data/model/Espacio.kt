package mx.edu.utng.jdrj.disponibilidad.data.model

data class Espacio(
    val idEspacio: String = "",
    val nombre: String = "",           // Ej: "Aula 1", "Laboratorio de Redes"
    val tipo: String = "",             // Ej: "Aula", "Laboratorio", "Auditorio"
    val capacidad: Int = 0,
    val planta: String = "",           // Ej: "Planta Baja", "Primer Piso"
    val descripcion: String = "",      // Detalles extra
    val imagenUrl: String = "",        // URL de la foto (opcional)
    val activo: Boolean = true         // Por si un aula entra en mantenimiento
)