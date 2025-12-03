package mx.edu.utng.jdrj.disponibilidad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utng.jdrj.disponibilidad.data.model.Usuario
import mx.edu.utng.jdrj.disponibilidad.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    adminViewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val usuarios = adminViewModel.listaUsuarios
    val isLoading = adminViewModel.isLoading

    // Estado para el diálogo de borrar
    var mostrarDialogoBorrar by remember { mutableStateOf(false) }
    var usuarioABorrar by remember { mutableStateOf<Usuario?>(null) }

    // Cargar la lista de usuarios al entrar a la pantalla
    LaunchedEffect(Unit) {
        adminViewModel.cargarUsuarios()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A237E), // Azul Oscuro Institucional (Admin)
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
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
                    // Ordenamos: Primero los Admins, luego por nombre alfabéticamente
                    val listaOrdenada = usuarios.sortedWith(
                        compareByDescending<Usuario> { it.rol == "admin" }.thenBy { it.nombre }
                    )

                    items(listaOrdenada) { usuario ->
                        UserItem(
                            usuario = usuario,
                            onToggleAprobado = { adminViewModel.alternarAprobacion(usuario) },
                            onHacerAdmin = { adminViewModel.hacerAdmin(usuario) },
                            onQuitarAdmin = { adminViewModel.quitarAdmin(usuario) },
                            onEliminar = {
                                usuarioABorrar = usuario
                                mostrarDialogoBorrar = true
                            }
                        )
                    }
                }
            }
        }

        // --- DIÁLOGO DE CONFIRMACIÓN DE ELIMINACIÓN ---
        if (mostrarDialogoBorrar && usuarioABorrar != null) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoBorrar = false },
                title = { Text("Eliminar Usuario") },
                text = { Text("¿Estás seguro de que quieres eliminar a ${usuarioABorrar!!.nombre}? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            adminViewModel.eliminarUsuario(usuarioABorrar!!)
                            mostrarDialogoBorrar = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoBorrar = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
fun UserItem(
    usuario: Usuario,
    onToggleAprobado: () -> Unit,
    onHacerAdmin: () -> Unit,
    onQuitarAdmin: () -> Unit,
    onEliminar: () -> Unit
) {
    val esAdmin = usuario.rol == "admin"

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // INFO DEL USUARIO
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (esAdmin) Icons.Default.Security else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (esAdmin) Color(0xFF1A237E) else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${usuario.nombre} ${usuario.apellido}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (esAdmin) Color(0xFF1A237E) else Color.Black
                        )
                        val tipoUsuario = if (esAdmin) "Administrador" else "Usuario Normal"
                        Text(
                            text = tipoUsuario,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "ID: ${usuario.idInstitucional}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // SWITCH DE APROBACIÓN (ADMITIDO / PENDIENTE)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Switch(
                        checked = usuario.aprobado,
                        onCheckedChange = { onToggleAprobado() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4CAF50),
                            checkedTrackColor = Color(0xFFE8F5E9)
                        )
                    )
                    Text(
                        text = if (usuario.aprobado) "ADMITIDO" else "PENDIENTE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (usuario.aprobado) Color(0xFF4CAF50) else Color.Red
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // BOTONES DE ROL Y ELIMINAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                }

                if (!esAdmin) {
                    TextButton(onClick = onHacerAdmin) {
                        Text("Hacer Admin", color = Color(0xFF1A237E), fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = onQuitarAdmin) {
                        Text("Quitar Admin", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}