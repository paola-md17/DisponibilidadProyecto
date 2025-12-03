package mx.edu.utng.jdrj.disponibilidad.data.model

data class Usuario(
    val idUsuario: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val rol: String = "alumno",        // "alumno", "admin"
    val idInstitucional: String = "",

    // NUEVO: Control de acceso.
    // Por defecto es FALSE (nadie entra hasta que el admin lo apruebe, excepto el primer admin manual)
    val aprobado: Boolean = false
)