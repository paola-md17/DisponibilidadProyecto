package mx.edu.utng.jdrj.disponibilidad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utng.jdrj.disponibilidad.utils.Constants
import mx.edu.utng.jdrj.disponibilidad.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit
) {
    val total = viewModel.totalReservas
    val topEspacio = viewModel.espacioMasSolicitado
    val horas = viewModel.horasTotalesReservadas
    val porEstado = viewModel.reservasPorEstado
    val isLoading = viewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes y Estadísticas", fontWeight = FontWeight.Bold) },
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // TARJETA PRINCIPAL: TOTALES
                    StatCard(
                        title = "Total de Solicitudes",
                        value = total.toString(),
                        icon = Icons.Default.BarChart,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        // TARJETA: APROBADAS
                        val aprobadas = porEstado[Constants.ESTADO_APROBADA] ?: 0
                        StatSmallCard(
                            title = "Aprobadas",
                            value = aprobadas.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // TARJETA: RECHAZADAS
                        val rechazadas = porEstado[Constants.ESTADO_CANCELADA] ?: 0
                        StatSmallCard(
                            title = "Canceladas",
                            value = rechazadas.toString(),
                            icon = Icons.Default.Cancel,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // TARJETA DESTACADA: EL ESPACIO MÁS POPULAR
                    if (topEspacio != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Espacio Más Solicitado", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFC107), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(topEspacio.first, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                Text("${topEspacio.second} reservas", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    }

                    // TARJETA: HORAS TOTALES
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Schedule, null, tint = Color(0xFF1565C0))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Uso Total del Edificio", fontSize = 14.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                                Text("$horas Horas", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D47A1))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(value, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            Icon(icon, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun StatSmallCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}