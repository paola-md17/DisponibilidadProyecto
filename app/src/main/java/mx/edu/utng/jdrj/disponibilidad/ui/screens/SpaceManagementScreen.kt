package mx.edu.utng.jdrj.disponibilidad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utng.jdrj.disponibilidad.data.model.Espacio
import mx.edu.utng.jdrj.disponibilidad.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceManagementScreen(
    adminViewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val espacios = adminViewModel.listaEspacios
    val isLoading = adminViewModel.isLoading

    // Estados para los diálogos
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Espacio seleccionado para editar o eliminar (null si es nuevo)
    var selectedEspacio by remember { mutableStateOf<Espacio?>(null) }

    // Campos del formulario
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }
    var planta by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    // Cargar espacios al entrar
    LaunchedEffect(Unit) {
        adminViewModel.cargarEspaciosAdmin()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Espacios", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A237E), // Azul Admin
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Preparar para AGREGAR nuevo
                    selectedEspacio = null
                    nombre = ""
                    tipo = ""
                    capacidad = ""
                    planta = ""
                    descripcion = ""
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Espacio")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(espacios) { espacio ->
                        EspacioAdminCard(
                            espacio = espacio,
                            onEdit = {
                                // Preparar para EDITAR
                                selectedEspacio = espacio
                                nombre = espacio.nombre
                                tipo = espacio.tipo
                                capacidad = espacio.capacidad.toString()
                                planta = espacio.planta
                                descripcion = espacio.descripcion
                                showAddEditDialog = true
                            },
                            onDelete = {
                                selectedEspacio = espacio
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        // --- DIÁLOGO AGREGAR / EDITAR ---
        if (showAddEditDialog) {
            AlertDialog(
                onDismissRequest = { showAddEditDialog = false },
                title = { Text(if (selectedEspacio == null) "Nuevo Espacio" else "Editar Espacio") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre (Ej: Aula 1)") })
                        OutlinedTextField(value = tipo, onValueChange = { tipo = it }, label = { Text("Tipo (Ej: Aula, Laboratorio)") })
                        OutlinedTextField(
                            value = capacidad,
                            onValueChange = { if (it.all { char -> char.isDigit() }) capacidad = it },
                            label = { Text("Capacidad") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(value = planta, onValueChange = { planta = it }, label = { Text("Ubicación / Planta") })
                        OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, maxLines = 3)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (nombre.isNotEmpty() && capacidad.isNotEmpty()) {
                                val espacioFinal = Espacio(
                                    idEspacio = selectedEspacio?.idEspacio ?: "", // Si es nuevo, ID vacío (el repo lo genera)
                                    nombre = nombre,
                                    tipo = tipo,
                                    capacidad = capacidad.toIntOrNull() ?: 0,
                                    planta = planta,
                                    descripcion = descripcion
                                )

                                if (selectedEspacio == null) {
                                    adminViewModel.agregarEspacio(espacioFinal) { showAddEditDialog = false }
                                } else {
                                    adminViewModel.actualizarEspacio(espacioFinal) { showAddEditDialog = false }
                                }
                            }
                        }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddEditDialog = false }) { Text("Cancelar") }
                }
            )
        }

        // --- DIÁLOGO ELIMINAR ---
        if (showDeleteDialog && selectedEspacio != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Eliminar Espacio") },
                text = { Text("¿Estás seguro de eliminar '${selectedEspacio!!.nombre}'? Esto no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            adminViewModel.eliminarEspacio(selectedEspacio!!.idEspacio)
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
fun EspacioAdminCard(
    espacio: Espacio,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.MeetingRoom, null, tint = Color(0xFF1A237E), modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = espacio.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "${espacio.tipo} • Cap: ${espacio.capacidad}", fontSize = 12.sp, color = Color.Gray)
                    Text(text = espacio.planta, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar", tint = Color(0xFF1A237E))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}