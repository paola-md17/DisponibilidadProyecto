package mx.edu.utng.jdrj.disponibilidad.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val FORMATO_FECHA = "dd/MM/yyyy"
    private const val FORMATO_HORA = "HH:mm"

    // Obtener la fecha de hoy como String (ej: "24/11/2023")
    fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat(FORMATO_FECHA, Locale.getDefault())
        return sdf.format(Date())
    }

    // Validar si el texto tiene formato de fecha correcto
    fun esFechaValida(fecha: String): Boolean {
        return try {
            val sdf = SimpleDateFormat(FORMATO_FECHA, Locale.getDefault())
            sdf.isLenient = false // No permitir fechas como 32/01/2023
            sdf.parse(fecha)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Validar formato de hora (ej: "14:30")
    fun esHoraValida(hora: String): Boolean {
        return try {
            val sdf = SimpleDateFormat(FORMATO_HORA, Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(hora)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Convertir una fecha String a objeto Calendar (útil para comparaciones)
    fun stringACalendar(fecha: String): Calendar? {
        return try {
            val sdf = SimpleDateFormat(FORMATO_FECHA, Locale.getDefault())
            val date = sdf.parse(fecha)
            Calendar.getInstance().apply {
                if (date != null) {
                    time = date
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}