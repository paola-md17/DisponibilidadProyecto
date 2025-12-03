package mx.edu.utng.jdrj.disponibilidad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle // <--- ÍCONO DE PERFIL
import androidx.compose.material.icons.filled.Event // <--- ÍCONO DE RESERVAS (Calendario)
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Logout
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
import kotlinx.coroutines.delay
import mx.edu.utng.jdrj.disponibilidad.ui.components.EspacioCard
import mx.edu.utng.jdrj.disponibilidad.viewmodel.HomeViewModel
import mx.edu.utng.jdrj.disponibilidad.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    loginViewModel: LoginViewModel? = null,
    onEspacioClick: (String) -> Unit,
    onNavigateToMisReservas: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit = {},
    onIrAPerfil: () -> Unit = {} // Parametro para ir al perfil
) {
    val todosLosEspacios = viewModel.listaEspacios
    val idsFavoritos = viewModel.idsFavoritos
    val mostrarSoloFavoritos = viewModel.mostrarSoloFavoritos
    val isLoading = viewModel.isLoading
    val usuario = loginViewModel?.usuarioActual

    val esAdmin = usuario?.rol == "admin"
    var mostrarDialogoSalir by remember { mutableStateOf(false) }

    // Carga inicial de datos
    LaunchedEffect(Unit) {
        if (usuario != null) viewModel.cargarFavoritos(usuario.idUsuario)
    }

    val espaciosA_Mostrar = if (mostrarSoloFavoritos) {
        todosLosEspacios.filter { it.idEspacio in idsFavoritos }
    } else {
        todosLosEspacios
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { }, // TÍTULO VACÍO (Para limpieza visual)
                navigationIcon = {
                    if (esAdmin) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                        }
                    }
                },
                actions = {
                    // 1. BOTÓN DE PERFIL (Ahora está aquí, a la derecha, junto a los demás)
                    IconButton(onClick = onIrAPerfil) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Mi Perfil", tint = Color.White)
                    }

                    // 2. Botón Actualizar
                    IconButton(onClick = {
                        viewModel.cargarEspacios()
                        if (usuario != null) viewModel.cargarFavoritos(usuario.idUsuario)
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                    }

                    // 3. Botón Filtro Favoritos
                    IconButton(onClick = { viewModel.toggleFiltro() }) {
                        Icon(
                            imageVector = if (mostrarSoloFavoritos) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Filtrar Favoritos",
                            tint = if (mostrarSoloFavoritos) Color(0xFFFFEB3B) else Color.White
                        )
                    }

                    // 4. Botón Mis Reservas (SOLO ÍCONO DE CALENDARIO)
                    IconButton(onClick = onNavigateToMisReservas) {
                        Icon(Icons.Default.Event, contentDescription = "Mis Reservas", tint = Color.White)
                    }

                    // 5. Botón Salir
                    IconButton(onClick = { mostrarDialogoSalir = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Salir", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (espaciosA_Mostrar.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (mostrarSoloFavoritos) "No tienes favoritos aún." else "No hay espacios disponibles.",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón de actualizar en pantalla vacía
                    Button(
                        onClick = { viewModel.cargarEspacios() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Actualizar")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp), // Quitamos padding superior para pegar el header
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // --- CABECERA PRO (HEADER) ---
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary, // Empieza azul
                                            MaterialTheme.colorScheme.background // Termina en gris fondo
                                        )
                                    )
                                )
                                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (mostrarSoloFavoritos) "Tus Favoritos ❤️" else "Espacios UTNG",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Reserva aulas y laboratorios",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // --- LISTA DE TARJETAS ---
                    items(espaciosA_Mostrar) { espacio ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            EspacioCard(
                                espacio = espacio,
                                esFavorito = espacio.idEspacio in idsFavoritos,
                                onClick = { onEspacioClick(espacio.idEspacio) }
                            )
                        }
                    }
                }
            }

            // Spinner de carga
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        // DIÁLOGO DE SALIDA
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
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}