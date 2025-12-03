package mx.edu.utng.jdrj.disponibilidad.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.edu.utng.jdrj.disponibilidad.data.model.Espacio
import mx.edu.utng.jdrj.disponibilidad.data.repository.EquiposRepository // <--- Importante
import mx.edu.utng.jdrj.disponibilidad.data.repository.EspaciosRepository
import mx.edu.utng.jdrj.disponibilidad.data.repository.FavoritosRepository

class HomeViewModel : ViewModel() {
    private val repository = EspaciosRepository()
    // Instanciamos el repositorio que sabe crear los equipos
    private val equiposRepo = EquiposRepository()
    private val favoritosRepo = FavoritosRepository()

    // Lista completa de espacios
    var listaEspacios by mutableStateOf<List<Espacio>>(emptyList())
        private set

    // Lista de IDs favoritos
    var idsFavoritos by mutableStateOf<List<String>>(emptyList())
        private set

    // Interruptor para el filtro
    var mostrarSoloFavoritos by mutableStateOf(false)

    var isLoading by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            // 1. Ordenar crear Aulas (Si no existen)
            repository.inicializarEspaciosSiEstaVacio()

            // 2. ¡AQUÍ ESTÁ LA MAGIA!
            // Ordenar crear Equipos y Relaciones (Si no existen)
            equiposRepo.inicializarEquiposSiVacio()

            // 3. Descargar la lista para mostrarla
            cargarEspacios()
        }
    }

    fun cargarEspacios() {
        viewModelScope.launch {
            isLoading = true
            listaEspacios = repository.obtenerEspacios()
            isLoading = false
        }
    }

    // Funciones de Favoritos
    fun cargarFavoritos(idUsuario: String) {
        viewModelScope.launch {
            idsFavoritos = favoritosRepo.obtenerMisFavoritos(idUsuario)
        }
    }

    fun toggleFiltro() {
        mostrarSoloFavoritos = !mostrarSoloFavoritos
    }
}