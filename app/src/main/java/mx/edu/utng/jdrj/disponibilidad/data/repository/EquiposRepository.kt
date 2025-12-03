package mx.edu.utng.jdrj.disponibilidad.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mx.edu.utng.jdrj.disponibilidad.data.model.Equipamiento
import mx.edu.utng.jdrj.disponibilidad.data.model.EspacioEquipamiento
import mx.edu.utng.jdrj.disponibilidad.data.model.EquipoUI
import mx.edu.utng.jdrj.disponibilidad.utils.Constants

class EquiposRepository {
    private val db = FirebaseFirestore.getInstance()

    // --- FUNCIÓN CLAVE ACTUALIZADA ---
    // Antes devolvía List<String>, ahora devuelve List<EquipoUI>
    suspend fun obtenerEquiposDeEspacio(idEspacio: String): List<EquipoUI> {
        val listaEquipos = mutableListOf<EquipoUI>()
        try {
            // 1. Buscamos en la tabla intermedia qué equipos tiene este espacio
            val relaciones = db.collection(Constants.COLLECTION_ESPACIO_EQUIPAMIENTO)
                .whereEqualTo("idEspacio", idEspacio)
                .get().await()
                .toObjects(EspacioEquipamiento::class.java)

            // 2. Por cada relación encontrada, buscamos el nombre del equipo en el catálogo
            for (rel in relaciones) {
                val equipoDoc = db.collection(Constants.COLLECTION_EQUIPAMIENTO)
                    .document(rel.idEquipo).get().await()
                val equipo = equipoDoc.toObject(Equipamiento::class.java)

                if (equipo != null) {
                    // AQUÍ ESTÁ LA MAGIA:
                    // Guardamos el objeto completo con su ID y la cantidad real
                    listaEquipos.add(
                        EquipoUI(
                            idEquipo = equipo.idEquipo,  // Necesario para la reserva
                            nombre = equipo.nombre,      // Para mostrar en pantalla
                            cantidadTotal = rel.cantidad // Para validar el stock (tope máximo)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("EquiposRepo", "Error al traer equipos", e)
        }
        return listaEquipos
    }

    // --- SEEDER DE DATOS (Mantenemos la lógica de creación igual que antes) ---
    // Esta parte se encarga de llenar la base de datos si está vacía
    suspend fun inicializarEquiposSiVacio() {
        val colEquipos = db.collection(Constants.COLLECTION_EQUIPAMIENTO)
        val snapshot = colEquipos.limit(1).get().await()
        if (snapshot.isEmpty) {
            crearCatalogoYRelaciones()
        }
    }

    private fun crearCatalogoYRelaciones() {
        val batch = db.batch()

        // 1. CREAR CATÁLOGO (Tipos de equipos)
        val equipos = listOf(
            Equipamiento("eq_tv", "Smart TV 55\"", 1),
            Equipamiento("eq_pizarron", "Pizarrón Blanco", 1),
            Equipamiento("eq_proyector", "Proyector HD", 1),
            Equipamiento("eq_pc", "PC de Escritorio", 1),
            Equipamiento("eq_restirador", "Mesa de Dibujo", 1),
            Equipamiento("eq_camara", "Cámara de Video 4K", 1),
            Equipamiento("eq_green_screen", "Pantalla Verde (Chroma)", 1),
            Equipamiento("eq_luces", "Kit de Iluminación", 1),
            Equipamiento("eq_mic", "Micrófono Boom", 1),
            Equipamiento("eq_mesa_juntas", "Mesa Ejecutiva", 1),

            // NUEVOS EQUIPOS QUE FALTABAN
            Equipamiento("eq_birrete", "Birrete (Banco Alto)", 1),
            Equipamiento("eq_router", "Router Cisco", 1),
            Equipamiento("eq_switch", "Switch 24 Puertos", 1),
            Equipamiento("eq_cable_utp", "Cable UTP (Red)", 1),
            Equipamiento("eq_cable_consola", "Cable Consola (Azul)", 1),
            Equipamiento("eq_cable_serial", "Cable Serial (Rojo)", 1),
            Equipamiento("eq_cable_fibra", "Cable Fibra Óptica", 1),
            Equipamiento("eq_servidor", "Servidor Rack", 1)
        )

        for (eq in equipos) {
            batch.set(db.collection(Constants.COLLECTION_EQUIPAMIENTO).document(eq.idEquipo), eq)
        }

        // 2. RELACIONES (Asignar equipos a aulas)

        // A) Aulas 1-12
        for (i in 1..12) {
            agregarRelacion(batch, "aula_$i", "eq_tv", 1)
            agregarRelacion(batch, "aula_$i", "eq_pizarron", 1)
        }

        // B) Laboratorios 1-4
        for (i in 1..4) {
            agregarRelacion(batch, "lab_$i", "eq_pizarron", 1)
            agregarRelacion(batch, "lab_$i", "eq_proyector", 1)
            agregarRelacion(batch, "lab_$i", "eq_pc", 25) // 25 PCs disponibles
        }

        // C) Talleres
        for (i in 1..2) {
            agregarRelacion(batch, "taller_dibujo_$i", "eq_tv", 1)
            agregarRelacion(batch, "taller_dibujo_$i", "eq_pizarron", 1)
            agregarRelacion(batch, "taller_dibujo_$i", "eq_proyector", 1)
            agregarRelacion(batch, "taller_dibujo_$i", "eq_restirador", 30)
            agregarRelacion(batch, "taller_dibujo_$i", "eq_birrete", 30) // NUEVO
        }

        // D) Sala Audiovisual
        agregarRelacion(batch, "sala_audiovisual", "eq_proyector", 1)
        agregarRelacion(batch, "sala_audiovisual", "eq_pizarron", 1)

        // E) Sala de Rodajes (Cine)
        agregarRelacion(batch, "sala_rodajes", "eq_camara", 3)
        agregarRelacion(batch, "sala_rodajes", "eq_green_screen", 1)
        agregarRelacion(batch, "sala_rodajes", "eq_luces", 4) // Actualizado a 4
        agregarRelacion(batch, "sala_rodajes", "eq_mic", 2)

        // F) Sala de Juntas
        agregarRelacion(batch, "sala_juntas", "eq_mesa_juntas", 1)

        // G) Laboratorio WAN (NUEVO)
        agregarRelacion(batch, "lab_wan", "eq_pc", 10)
        agregarRelacion(batch, "lab_wan", "eq_router", 10)
        agregarRelacion(batch, "lab_wan", "eq_switch", 17)
        agregarRelacion(batch, "lab_wan", "eq_cable_utp", 4)
        agregarRelacion(batch, "lab_wan", "eq_cable_consola", 4)
        agregarRelacion(batch, "lab_wan", "eq_cable_serial", 4)
        agregarRelacion(batch, "lab_wan", "eq_cable_fibra", 4)

        // H) Laboratorio Seguridad (NUEVO)
        agregarRelacion(batch, "lab_seguridad", "eq_pc", 10)
        agregarRelacion(batch, "lab_seguridad", "eq_servidor", 1)

        batch.commit()
    }

    private fun agregarRelacion(batch: com.google.firebase.firestore.WriteBatch, idEspacio: String, idEquipo: String, cantidad: Int) {
        val docRef = db.collection(Constants.COLLECTION_ESPACIO_EQUIPAMIENTO).document()
        val relacion = EspacioEquipamiento(docRef.id, idEspacio, idEquipo, cantidad)
        batch.set(docRef, relacion)
    }
}