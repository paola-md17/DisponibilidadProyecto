package mx.edu.utng.jdrj.disponibilidad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh // Icono para actualizar
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utng.jdrj.disponibilidad.data.model.Reserva
import mx.edu.utng.jdrj.disponibilidad.utils.Constants
import mx.edu.utng.jdrj.disponibilidad.viewmodel.LoginViewModel
import mx.edu.utng.jdrj.disponibilidad.viewmodel.ReservaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(
    reservaViewModel: ReservaViewModel,
    loginViewModel: LoginViewModel,
    onNavigateBack: () -> Unit
) {
    val usuario = loginViewModel.usuarioActual
    val esAdmin = usuario?.rol == "admin"

    val reservas = reservaViewModel.reservasVisibles
    val isLoading = reservaViewModel.isLoading
    val mostrandoTodo = reservaViewModel.mostrarHistorialCompleto

    // Carga inicial
    LaunchedEffect(Unit) {
        if (usuario != null) {
            reservaViewModel.cargarMisReservas(usuario.idUsuario)
        }
    }

    val tituloPantalla = when {
        mostrandoTodo -> "Historial Completo"
        esAdmin -> "Horarios Asignados"
        else -> "Mis Reservas Activas"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tituloPantalla, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    // --- BOTÓN ACTUALIZAR (Manual y Seguro) ---
                    IconButton(onClick = {
                        if (usuario != null) reservaViewModel.cargarMisReservas(usuario.idUsuario)
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = MaterialTheme.colorScheme.onPrimary)
                    }

                    // Botón Historial
                    TextButton(onClick = { reservaViewModel.toggleMostrarTodo() }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (mostrandoTodo) Icons.Default.Visibility else Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (mostrandoTodo) "Ver Recientes" else "Ver Historial", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            if (reservas.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (mostrandoTodo) "Historial vacío." else "No hay registros activos.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón grande para actualizar si está vacío
                    Button(
                        onClick = { if (usuario != null) reservaViewModel.cargarMisReservas(usuario.idUsuario) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Actualizar")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(reservas) { reserva ->
                        ReservaItem(
                            reserva = reserva,
                            esModoHistorial = mostrandoTodo,
                            onCancel = {
                                if (usuario != null) {
                                    reservaViewModel.cancelarReserva(reserva.idReserva, usuario.idUsuario)
                                }
                            },
                            onDelete = {
                                if (usuario != null) {
                                    reservaViewModel.eliminarReserva(reserva.idReserva, usuario.idUsuario)
                                }
                            }
                        )
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.secondary)
            }

            if (reservaViewModel.mensajeExito != null) {
                Text(
                    text = reservaViewModel.mensajeExito!!,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .padding(bottom = 32.dp)
                        .background(Color(0xFF4CAF50), shape = RoundedCornerShape(8.dp))
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ReservaItem(
    reserva: Reserva,
    esModoHistorial: Boolean,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val colorEstado = when (reserva.estado) {
        Constants.ESTADO_APROBADA -> Color(0xFF2E7D32) // Verde
        Constants.ESTADO_CANCELADA -> Color.Red
        else -> Color(0xFFF57C00) // Naranja
    }

    // --- LÓGICA CORREGIDA PARA EL CARRITO ---
    val descripcionReserva = if (reserva.equiposReservados.isEmpty()) {
        "(Aula Completa)"
    } else {
        // Muestra: (1x PC, 2x Cables)
        "(" + reserva.equiposReservados.joinToString(", ") { "${it.cantidad}x ${it.nombreEquipo}" } + ")"
    }

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reserva.nombreEspacio,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Aquí se muestra qué pidió
                    Text(
                        text = descripcionReserva,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = colorEstado.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = reserva.estado.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = colorEstado,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Fecha: ${reserva.fecha}", color = Color.Gray)
            Text("Horario: ${reserva.horaInicio} - ${reserva.horaFin}", fontWeight = FontWeight.Medium)

            if (reserva.motivoRechazo.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Motivo: ${reserva.motivoRechazo}",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.align(Alignment.End)) {
                if (reserva.estado == Constants.ESTADO_CANCELADA && esModoHistorial) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar")
                    }
                } else if (reserva.estado != Constants.ESTADO_CANCELADA) {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}