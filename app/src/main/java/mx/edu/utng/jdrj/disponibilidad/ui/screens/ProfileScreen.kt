package mx.edu.utng.jdrj.disponibilidad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utng.jdrj.disponibilidad.viewmodel.LoginViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    loginViewModel: LoginViewModel,
    onNavigateBack: () -> Unit
) {
    val usuario = loginViewModel.usuarioActual
    val isLoading = loginViewModel.isLoading
    val errorMsg = loginViewModel.errorMessage

    // Estados para edición
    var isEditing by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var apellido by remember { mutableStateOf(usuario?.apellido ?: "") }

    // Estado para mensaje de éxito
    var showSuccessMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
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
            if (usuario == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- AVATAR ---
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${usuario.nombre.firstOrNull() ?: ""}${usuario.apellido.firstOrNull() ?: ""}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${usuario.nombre} ${usuario.apellido}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        color = if (usuario.rol == "admin") Color(0xFF1A237E) else MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = if (usuario.rol == "admin") "Administrador" else "Usuario Normal",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- TARJETA DE DATOS ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (!isEditing) {
                                // MODO VISUALIZACIÓN
                                ProfileItem("Nombre", usuario.nombre, Icons.Default.Person)
                                Spacer(modifier = Modifier.height(12.dp))
                                ProfileItem("Apellido", usuario.apellido, Icons.Default.Person)
                            } else {
                                // MODO EDICIÓN
                                OutlinedTextField(
                                    value = nombre,
                                    onValueChange = { nombre = it },
                                    label = { Text("Nombre") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = apellido,
                                    onValueChange = { apellido = it },
                                    label = { Text("Apellido") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // DATOS NO EDITABLES
                            ProfileItem("Correo", usuario.email, Icons.Default.Email)
                            Spacer(modifier = Modifier.height(12.dp))
                            ProfileItem("ID / Matrícula", usuario.idInstitucional, Icons.Default.Badge)
                            Spacer(modifier = Modifier.height(12.dp))
                            ProfileItem(
                                "Estado",
                                if (usuario.aprobado) "Cuenta Verificada" else "Pendiente",
                                Icons.Default.VerifiedUser,
                                colorTexto = if (usuario.aprobado) Color(0xFF2E7D32) else Color.Red
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // MENSAJES
                    if (errorMsg != null) {
                        Text(errorMsg, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (showSuccessMessage) {
                        Text("¡Perfil actualizado correctamente!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        LaunchedEffect(Unit) {
                            delay(3000)
                            showSuccessMessage = false
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // BOTONES DE ACCIÓN
                    if (!isEditing) {
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Editar Perfil")
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    // Cancelar: Revertimos cambios
                                    isEditing = false
                                    nombre = usuario.nombre
                                    apellido = usuario.apellido
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(
                                onClick = {
                                    if (nombre.isNotEmpty() && apellido.isNotEmpty()) {
                                        loginViewModel.actualizarPerfil(nombre, apellido) {
                                            isEditing = false
                                            showSuccessMessage = true
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Guardar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, colorTexto: Color = Color.Black) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 16.sp, color = colorTexto, fontWeight = FontWeight.Medium)
        }
    }
}