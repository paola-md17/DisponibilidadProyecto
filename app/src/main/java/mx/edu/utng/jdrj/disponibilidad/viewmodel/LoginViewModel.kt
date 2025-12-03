package mx.edu.utng.jdrj.disponibilidad.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.edu.utng.jdrj.disponibilidad.data.model.Usuario
import mx.edu.utng.jdrj.disponibilidad.data.repository.AuthRepository

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        verificarSesion()
    }

    private fun verificarSesion() {
        if (repository.estaLogueado()) {
            viewModelScope.launch {
                isLoading = true
                usuarioActual = repository.obtenerUsuarioActual()
                isLoading = false
            }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = repository.login(email, pass)
            if (result.isSuccess) {
                usuarioActual = repository.obtenerUsuarioActual()
                onSuccess()
            } else {
                errorMessage = "Error: ${result.exceptionOrNull()?.message}"
            }
            isLoading = false
        }
    }

    fun registro(email: String, pass: String, nombre: String, apellido: String, idInst: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = repository.registro(email, pass, nombre, apellido, idInst)
            if (result.isSuccess) {
                // No logueamos automático porque requiere aprobación
                onSuccess()
            } else {
                errorMessage = "Error al registrarse: ${result.exceptionOrNull()?.message}"
            }
            isLoading = false
        }
    }

    fun logout() {
        repository.logout()
        usuarioActual = null
    }

    // --- NUEVO: ACTUALIZAR PERFIL ---
    fun actualizarPerfil(nombre: String, apellido: String, onSuccess: () -> Unit) {
        val user = usuarioActual ?: return

        viewModelScope.launch {
            isLoading = true
            val datos = mapOf(
                "nombre" to nombre,
                "apellido" to apellido
            )
            val result = repository.actualizarDatosPerfil(user.idUsuario, datos)

            if (result.isSuccess) {
                // Actualizamos la memoria local para ver el cambio sin recargar
                usuarioActual = user.copy(nombre = nombre, apellido = apellido)
                onSuccess()
            } else {
                errorMessage = "No se pudo actualizar el perfil."
            }
            isLoading = false
        }
    }
}