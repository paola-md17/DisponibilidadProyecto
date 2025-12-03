package mx.edu.utng.jdrj.disponibilidad.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mx.edu.utng.jdrj.disponibilidad.data.model.Usuario
import mx.edu.utng.jdrj.disponibilidad.utils.Constants

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // --- LOGIN ---
    suspend fun login(email: String, pass: String): Result<Boolean> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val userId = authResult.user?.uid ?: throw Exception("Error de autenticación")

            val snapshot = db.collection(Constants.COLLECTION_USUARIOS)
                .document(userId)
                .get()
                .await()

            val usuario = snapshot.toObject(Usuario::class.java)

            if (usuario == null) {
                auth.signOut()
                return Result.failure(Exception("Usuario no encontrado en la base de datos."))
            }

            if (!usuario.aprobado && usuario.rol != "admin") {
                auth.signOut()
                return Result.failure(Exception("Tu cuenta está pendiente de aprobación por el administrador."))
            }

            Result.success(true)
        } catch (e: Exception) {
            auth.signOut()
            Result.failure(e)
        }
    }

    // --- REGISTRO ---
    suspend fun registro(
        email: String,
        pass: String,
        nombre: String,
        apellido: String,
        idInstitucional: String
    ): Result<Boolean> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val userId = authResult.user?.uid ?: throw Exception("Error al obtener ID")

            val nuevoUsuario = Usuario(
                idUsuario = userId,
                nombre = nombre,
                apellido = apellido,
                email = email,
                rol = "alumno",
                idInstitucional = idInstitucional,
                aprobado = false
            )

            db.collection(Constants.COLLECTION_USUARIOS)
                .document(userId)
                .set(nuevoUsuario)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error en registro", e)
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun obtenerUsuarioActual(): Usuario? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val snapshot = db.collection(Constants.COLLECTION_USUARIOS)
                .document(userId)
                .get()
                .await()
            snapshot.toObject(Usuario::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun estaLogueado(): Boolean = auth.currentUser != null

    // --- GESTIÓN ADMIN ---

    suspend fun obtenerTodosLosUsuarios(): List<Usuario> {
        return try {
            val snapshot = db.collection(Constants.COLLECTION_USUARIOS).get().await()
            snapshot.toObjects(Usuario::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun actualizarEstadoUsuario(idUsuario: String, aprobado: Boolean): Result<Boolean> {
        return try {
            db.collection(Constants.COLLECTION_USUARIOS).document(idUsuario)
                .update("aprobado", aprobado).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun cambiarRolUsuario(idUsuario: String, nuevoRol: String): Result<Boolean> {
        return try {
            db.collection(Constants.COLLECTION_USUARIOS).document(idUsuario)
                .update("rol", nuevoRol).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun eliminarUsuario(idUsuario: String): Result<Boolean> {
        return try {
            db.collection(Constants.COLLECTION_USUARIOS).document(idUsuario).delete().await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- NUEVO: ACTUALIZAR PERFIL (Nombre/Apellido) ---
    suspend fun actualizarDatosPerfil(idUsuario: String, nuevosDatos: Map<String, Any>): Result<Boolean> {
        return try {
            db.collection(Constants.COLLECTION_USUARIOS).document(idUsuario)
                .update(nuevosDatos)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}