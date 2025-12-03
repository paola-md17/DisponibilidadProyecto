package mx.edu.utng.jdrj.disponibilidad.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mx.edu.utng.jdrj.disponibilidad.data.model.EspacioFavorito
import mx.edu.utng.jdrj.disponibilidad.utils.Constants

class FavoritosRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection(Constants.COLLECTION_FAVORITOS)

    // Verificar si UN espacio es favorito (Ya lo teníamos)
    suspend fun esFavorito(idUsuario: String, idEspacio: String): Boolean {
        val snapshot = collection
            .whereEqualTo("idUsuario", idUsuario)
            .whereEqualTo("idEspacio", idEspacio)
            .get().await()
        return !snapshot.isEmpty
    }

    // Toggle (Dar/Quitar like) (Ya lo teníamos)
    suspend fun toggleFavorito(idUsuario: String, idEspacio: String): Boolean {
        val query = collection
            .whereEqualTo("idUsuario", idUsuario)
            .whereEqualTo("idEspacio", idEspacio)
            .get().await()

        return if (query.isEmpty) {
            val doc = collection.document()
            val fav = EspacioFavorito(doc.id, idUsuario, idEspacio)
            doc.set(fav).await()
            true
        } else {
            for (doc in query.documents) {
                doc.reference.delete().await()
            }
            false
        }
    }

    // --- NUEVA FUNCIÓN: Obtener TODOS los IDs de espacios favoritos del usuario ---
    suspend fun obtenerMisFavoritos(idUsuario: String): List<String> {
        return try {
            val snapshot = collection
                .whereEqualTo("idUsuario", idUsuario)
                .get().await()

            // Regresamos solo la lista de IDs de espacios (ej: ["aula_1", "lab_2"])
            snapshot.toObjects(EspacioFavorito::class.java).map { it.idEspacio }
        } catch (e: Exception) {
            emptyList()
        }
    }
}