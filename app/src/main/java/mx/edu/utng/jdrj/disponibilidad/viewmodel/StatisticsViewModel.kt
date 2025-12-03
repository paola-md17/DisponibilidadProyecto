package mx.edu.utng.jdrj.disponibilidad.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.edu.utng.jdrj.disponibilidad.data.model.Reserva
import mx.edu.utng.jdrj.disponibilidad.data.repository.ReservasRepository
import mx.edu.utng.jdrj.disponibilidad.utils.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatisticsViewModel : ViewModel() {
    private val repository = ReservasRepository()

    // --- ESTADOS PARA LA UI ---
    var totalReservas by mutableStateOf(0)
        private set

    var espacioMasSolicitado by mutableStateOf<Pair<String, Int>?>(null) // Nombre, Cantidad
        private set

    var horasTotalesReservadas by mutableStateOf(0)
        private set

    var reservasPorEstado by mutableStateOf<Map<String, Int>>(emptyMap()) // Aprobadas vs Canceladas
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        calcularEstadisticas()
    }

    fun calcularEstadisticas() {
        viewModelScope.launch {
            isLoading = true

            // 1. Obtenemos TODO el historial
            val todas = repository.obtenerTodasLasReservas()

            if (todas.isEmpty()) {
                isLoading = false
                return@launch
            }

            // 2. Cálculos Matemáticos
            totalReservas = todas.size

            // A) Espacio más popular
            // Agrupamos por nombre y contamos cuál tiene más
            val conteoPorEspacio = todas.groupingBy { it.nombreEspacio }.eachCount()
            val topEspacio = conteoPorEspacio.maxByOrNull { it.value }
            if (topEspacio != null) {
                espacioMasSolicitado = Pair(topEspacio.key, topEspacio.value)
            }

            // B) Horas Totales (Aprox)
            // Calculamos la diferencia entre inicio y fin de cada reserva aprobada
            var minutosTotales = 0
            todas.filter { it.estado == Constants.ESTADO_APROBADA }.forEach { r ->
                minutosTotales += calcularDuracionEnMinutos(r.horaInicio, r.horaFin)
            }
            horasTotalesReservadas = minutosTotales / 60

            // C) Estado (Aprobadas vs Canceladas)
            reservasPorEstado = todas.groupingBy { it.estado }.eachCount()

            isLoading = false
        }
    }

    // Función auxiliar para restar horas "10:00" - "12:00"
    private fun calcularDuracionEnMinutos(inicio: String, fin: String): Int {
        try {
            val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
            // Limpiamos AM/PM para cálculo simple si es necesario, o usamos un parser más robusto
            // Asumimos formato simple HH:mm para este ejemplo rápido, o usamos la lógica de minutos del repo
            // Simplificación:
            val minInicio = convertirAMinutos(inicio)
            val minFin = convertirAMinutos(fin)
            return if (minFin > minInicio) minFin - minInicio else 0
        } catch (e: Exception) {
            return 0
        }
    }

    private fun convertirAMinutos(hora: String): Int {
        // Reutilizamos lógica simple: "10:30" -> 630 min
        val partes = hora.split(":", " ")
        if (partes.size < 2) return 0
        var h = partes[0].toIntOrNull() ?: 0
        val m = partes[1].toIntOrNull() ?: 0
        // Ajuste simple AM/PM si existe
        if (hora.contains("PM", true) && h != 12) h += 12
        if (hora.contains("AM", true) && h == 12) h = 0
        return (h * 60) + m
    }
}