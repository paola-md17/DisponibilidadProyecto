package mx.edu.utng.jdrj.disponibilidad.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mx.edu.utng.jdrj.disponibilidad.data.model.Espacio
import mx.edu.utng.jdrj.disponibilidad.utils.Constants

class EspaciosRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection(Constants.COLLECTION_ESPACIOS)

    // --- LEER (READ) ---
    suspend fun obtenerEspacios(): List<Espacio> {
        return try {
            val snapshot = collection.get().await()
            snapshot.toObjects(Espacio::class.java).sortedBy { it.nombre }
        } catch (e: Exception) {
            Log.e("EspaciosRepo", "Error al obtener espacios", e)
            emptyList()
        }
    }

    // --- CREAR / AGREGAR (CREATE) ---
    suspend fun agregarEspacio(espacio: Espacio): Result<Boolean> {
        return try {
            // Si no tiene ID, generamos uno automático
            val idFinal = if (espacio.idEspacio.isEmpty()) collection.document().id else espacio.idEspacio
            val espacioFinal = espacio.copy(idEspacio = idFinal)

            collection.document(idFinal).set(espacioFinal).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- ACTUALIZAR / EDITAR (UPDATE) ---
    suspend fun actualizarEspacio(espacio: Espacio): Result<Boolean> {
        return try {
            collection.document(espacio.idEspacio).set(espacio).await() // Sobrescribe con los nuevos datos
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- ELIMINAR (DELETE) ---
    suspend fun eliminarEspacio(idEspacio: String): Result<Boolean> {
        return try {
            collection.document(idEspacio).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- INICIALIZAR DATOS (Solo si está vacío) ---
    suspend fun inicializarEspaciosSiEstaVacio() {
        try {
            val snapshot = collection.limit(1).get().await()
            if (snapshot.isEmpty) {
                Log.d("EspaciosRepo", "No hay espacios. Creando datos iniciales...")
                crearDatosIniciales()
            }
        } catch (e: Exception) {
            Log.e("EspaciosRepo", "Error al verificar espacios", e)
        }
    }

    private fun crearDatosIniciales() {
        val batch = db.batch()
        val listaEspacios = mutableListOf<Espacio>()

        // 1. AULAS
        for (i in 1..12) {
            val ubicacion = if (i == 12) "Edificio F - Planta Baja" else "Edificio F - Planta Alta"
            listaEspacios.add(Espacio("aula_$i", "Aula $i", "Aula", 30, ubicacion, "Aula académica estándar con Smart TV y Pizarrón."))
        }
        // 2. LABORATORIOS
        for (i in 1..4) {
            listaEspacios.add(Espacio("lab_$i", "Laboratorio $i", "Laboratorio", 25, "Edificio F - Planta Baja", "Laboratorio de cómputo general."))
        }
        // 3. TALLERES
        listaEspacios.add(Espacio("taller_dibujo_1", "Taller de Dibujo 1", "Taller", 30, "Edificio F - Planta Alta", "Espacio para dibujo técnico."))
        listaEspacios.add(Espacio("taller_dibujo_2", "Taller de Dibujo 2", "Taller", 30, "Edificio F - Planta Baja", "Espacio para dibujo técnico."))
        // 4. SALAS
        listaEspacios.add(Espacio("sala_audiovisual", "Sala Audiovisual", "Auditorio", 50, "Edificio F - Planta Baja", "Auditorio para conferencias."))
        listaEspacios.add(Espacio("sala_juntas", "Sala de Juntas", "Sala", 20, "Edificio F - Planta Alta", "Espacio ejecutivo."))
        listaEspacios.add(Espacio("sala_rodajes", "Estudio de Producción", "Estudio", 5, "Edificio F - Planta Baja", "Estudio profesional de grabación."))
        // 5. NUEVOS
        listaEspacios.add(Espacio("lab_wan", "Laboratorio WAN", "Laboratorio", 20, "Edificio F - Planta Baja", "Laboratorio especializado en redes."))
        listaEspacios.add(Espacio("lab_seguridad", "Laboratorio de Seguridad", "Laboratorio", 15, "Edificio F - Planta Alta", "Laboratorio de Ciberseguridad."))

        for (espacio in listaEspacios) {
            batch.set(collection.document(espacio.idEspacio), espacio)
        }
        batch.commit()
    }
}