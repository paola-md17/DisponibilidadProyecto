package mx.edu.utng.jdrj.disponibilidad.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.edu.utng.jdrj.disponibilidad.data.model.Espacio
import mx.edu.utng.jdrj.disponibilidad.data.model.Reserva
import mx.edu.utng.jdrj.disponibilidad.data.model.Usuario
import mx.edu.utng.jdrj.disponibilidad.data.repository.AuthRepository
import mx.edu.utng.jdrj.disponibilidad.data.repository.EspaciosRepository
import mx.edu.utng.jdrj.disponibilidad.data.repository.ReservasRepository

class AdminViewModel : ViewModel() {
    private val reservasRepository = ReservasRepository()
    private val authRepository = AuthRepository()
    private val espaciosRepository = EspaciosRepository() // <--- NUEVO REPO

    // --- ESTADOS ---
    var reservasPendientes by mutableStateOf<List<Reserva>>(emptyList())
        private set
    var listaUsuarios by mutableStateOf<List<Usuario>>(emptyList())
        private set

    // NUEVO: Lista de Espacios para administrar
    var listaEspacios by mutableStateOf<List<Espacio>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        cargarPendientes()
        // Los espacios y usuarios se cargan bajo demanda al entrar a sus pantallas
    }

    // --- GESTIÓN DE RESERVAS (Ya existía) ---
    fun cargarPendientes() {
        viewModelScope.launch {
            isLoading = true
            reservasPendientes = reservasRepository.obtenerReservasPendientes()
            isLoading = false
        }
    }

    fun aprobarReserva(idReserva: String) {
        viewModelScope.launch {
            reservasRepository.aprobarReserva(idReserva)
            cargarPendientes()
        }
    }

    fun rechazarReserva(idReserva: String, motivo: String) {
        viewModelScope.launch {
            reservasRepository.rechazarReserva(idReserva, motivo)
            cargarPendientes()
        }
    }

    // --- GESTIÓN DE USUARIOS (Ya existía) ---
    fun cargarUsuarios() {
        viewModelScope.launch {
            isLoading = true
            listaUsuarios = authRepository.obtenerTodosLosUsuarios()
            isLoading = false
        }
    }

    fun alternarAprobacion(usuario: Usuario) {
        viewModelScope.launch {
            val result = authRepository.actualizarEstadoUsuario(usuario.idUsuario, !usuario.aprobado)
            if (result.isSuccess) cargarUsuarios()
        }
    }

    fun hacerAdmin(usuario: Usuario) {
        viewModelScope.launch {
            val result = authRepository.cambiarRolUsuario(usuario.idUsuario, "admin")
            if (result.isSuccess) {
                if (!usuario.aprobado) authRepository.actualizarEstadoUsuario(usuario.idUsuario, true)
                cargarUsuarios()
            }
        }
    }

    fun quitarAdmin(usuario: Usuario) {
        viewModelScope.launch {
            val result = authRepository.cambiarRolUsuario(usuario.idUsuario, "alumno")
            if (result.isSuccess) cargarUsuarios()
        }
    }

    fun eliminarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            val result = authRepository.eliminarUsuario(usuario.idUsuario)
            if (result.isSuccess) cargarUsuarios()
        }
    }

    // --- NUEVO: GESTIÓN DE ESPACIOS (CRUD) ---

    fun cargarEspaciosAdmin() {
        viewModelScope.launch {
            isLoading = true
            listaEspacios = espaciosRepository.obtenerEspacios()
            isLoading = false
        }
    }

    fun agregarEspacio(espacio: Espacio, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            val result = espaciosRepository.agregarEspacio(espacio)
            if (result.isSuccess) {
                cargarEspaciosAdmin() // Recargar lista
                onSuccess()
            }
            isLoading = false
        }
    }

    fun actualizarEspacio(espacio: Espacio, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            val result = espaciosRepository.actualizarEspacio(espacio)
            if (result.isSuccess) {
                cargarEspaciosAdmin()
                onSuccess()
            }
            isLoading = false
        }
    }

    fun eliminarEspacio(idEspacio: String) {
        viewModelScope.launch {
            isLoading = true
            val result = espaciosRepository.eliminarEspacio(idEspacio)
            if (result.isSuccess) {
                cargarEspaciosAdmin()
            }
            isLoading = false
        }
    }
}