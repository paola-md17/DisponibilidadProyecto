package mx.edu.utng.jdrj.disponibilidad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountCircle // <--- Perfil
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mx.edu.utng.jdrj.disponibilidad.data.model.Reserva
import mx.edu.utng.jdrj.disponibilidad.viewmodel.AdminViewModel
import mx.edu.utng.jdrj.disponibilidad.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    loginViewModel: LoginViewModel,
    onLogout: () -> Unit,
    onIrAEspacios: () -> Unit,
    onIrAUsuarios: () -> Unit,
    onIrAGestionEspacios: () -> Unit,
    onIrAPerfil: () -> Unit,
    onIrAEstadisticas: () -> Unit
) {
    val reservas = adminViewModel.reservasPendientes
    val isLoading = adminViewModel.isLoading
    val usuario = loginViewModel.usuarioActual

    var mostrarDialogoRechazo by remember { mutableStateOf(false) }
    var reservaSeleccionada by remember { mutableStateOf<Reserva?>(null) }
    var motivoRechazo by remember { mutableStateOf("") }
    var mostrarDialogoSalir by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        adminViewModel.cargarPendientes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { }, // Título vacío para limpieza visual
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    // 1. PERFIL (Aquí a la derecha para fácil acceso)
                    IconButton(onClick = onIrAPerfil) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Mi Perfil")
                    }

                    // 2. Actualizar
                    IconButton(onClick = { adminViewModel.cargarPendientes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }

                    // 3. Estadísticas
                    IconButton(onClick = onIrAEstadisticas) {
                        Icon(Icons.Default.BarChart, contentDescription = "Reportes")
                    }

                    // 4. Espacios
                    IconButton(onClick = onIrAGestionEspacios) {
                        Icon(Icons.Default.Domain, contentDescription = "Espacios")
                    }

                    // 5. Usuarios
                    IconButton(onClick = onIrAUsuarios) {
                        Icon(Icons.Default.People, contentDescription = "Usuarios")
                    }

                    // 6. Salir
                    IconButton(onClick = { mostrarDialogoSalir = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Salir")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onIrAEspacios,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Asignar Horario", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Gris Nube
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- CABECERA PRO ---
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Panel de Administración",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Hola, ${usuario?.nombre ?: "Admin"}",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Tarjeta Resumen
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(4.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Dashboard, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Solicitudes Pendientes", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text("${reservas.size}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(onClick = { adminViewModel.cargarPendientes() }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                    }
                }

                // --- LISTA ---
                if (reservas.isEmpty() && !isLoading) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color.LightGray, modifier = Modifier.size(60.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Todo al día", color = Color.Gray, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { adminViewModel.cargarPendientes() }) {
                                Text("Actualizar Lista")
                            }
                        }
                    }
                } else {
                    items(reservas) { reserva ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ReservaAdminItem(
                                reserva = reserva,
                                onAprobar = { adminViewModel.aprobarReserva(reserva.idReserva) },
                                onRechazarClick = {
                                    reservaSeleccionada = reserva
                                    motivoRechazo = ""
                                    mostrarDialogoRechazo = true
                                }
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        // Diálogos
        if (mostrarDialogoRechazo && reservaSeleccionada != null) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoRechazo = false },
                title = { Text("Rechazar Solicitud") },
                text = {
                    Column {
                        Text("Ingresa el motivo del rechazo:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = motivoRechazo,
                            onValueChange = { motivoRechazo = it },
                            label = { Text("Motivo") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (motivoRechazo.isNotEmpty()) {
                                adminViewModel.rechazarReserva(reservaSeleccionada!!.idReserva, motivoRechazo)
                                mostrarDialogoRechazo = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Confirmar") }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoRechazo = false }) { Text("Cancelar") }
                }
            )
        }

        if (mostrarDialogoSalir) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoSalir = false },
                title = { Text("Cerrar Sesión") },
                text = { Text("¿Estás seguro de que deseas salir?") },
                confirmButton = {
                    Button(onClick = { mostrarDialogoSalir = false; onLogout() }) { Text("Sí, salir") }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoSalir = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
fun ReservaAdminItem(
    reserva: Reserva,
    onAprobar: () -> Unit,
    onRechazarClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reserva.nombreEspacio,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = reserva.fecha,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val detalleEquipo = if (reserva.equiposReservados.isEmpty()) {
                "(Aula Completa)"
            } else {
                "(${reserva.equiposReservados.joinToString(", ") { "${it.cantidad}x ${it.nombreEquipo}" }})"
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${reserva.horaInicio} - ${reserva.horaFin}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = detalleEquipo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Motivo: ${reserva.proposito}", style = MaterialTheme.typography.bodyMedium, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            Text(text = "Solicitante ID: ${reserva.idUsuario.take(8)}...", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onRechazarClick) {
                    Text("Rechazar", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAprobar,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Aprobar")
                }
            }
        }
    }
}