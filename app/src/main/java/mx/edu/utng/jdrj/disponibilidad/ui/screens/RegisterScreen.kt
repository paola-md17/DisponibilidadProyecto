package mx.edu.utng.jdrj.disponibilidad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utng.jdrj.disponibilidad.viewmodel.LoginViewModel

@Composable
fun RegisterScreen(
    viewModel: LoginViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // Estados locales para el formulario
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var idInstitucional by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Estados para mensajes de error de validación local
    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorApellido by remember { mutableStateOf<String?>(null) }
    var errorId by remember { mutableStateOf<String?>(null) }
    var errorEmail by remember { mutableStateOf<String?>(null) }
    var errorPassword by remember { mutableStateOf<String?>(null) }

    // Función auxiliar para validar email (Más flexible: solo requiere @)
    fun esEmailValido(email: String): Boolean {
        return email.contains("@")
    }

    // Scroll por si el teclado tapa los campos en pantallas pequeñas
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crear Cuenta",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- NOMBRE ---
        OutlinedTextField(
            value = nombre,
            onValueChange = { input ->
                // Validación en tiempo real: Solo letras y espacios
                if (input.all { it.isLetter() || it.isWhitespace() }) {
                    nombre = input
                    errorNombre = null // Limpiar error si corrige
                }
            },
            label = { Text("Nombre") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorNombre != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        if (errorNombre != null) {
            Text(text = errorNombre!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- APELLIDO ---
        OutlinedTextField(
            value = apellido,
            onValueChange = { input ->
                if (input.all { it.isLetter() || it.isWhitespace() }) {
                    apellido = input
                    errorApellido = null
                }
            },
            label = { Text("Apellido") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorApellido != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        if (errorApellido != null) {
            Text(text = errorApellido!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- ID INSTITUCIONAL / MATRÍCULA ---
        OutlinedTextField(
            value = idInstitucional,
            onValueChange = { input ->
                // Validación: Solo números
                if (input.all { it.isDigit() }) {
                    idInstitucional = input
                    errorId = null
                }
            },
            label = { Text("ID Institucional / Matrícula") },
            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorId != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        if (errorId != null) {
            Text(text = errorId!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- EMAIL ---
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorEmail = null
            },
            label = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = errorEmail != null
        )
        if (errorEmail != null) {
            Text(text = errorEmail!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- PASSWORD ---
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorPassword = null
            },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = errorPassword != null
        )
        if (errorPassword != null) {
            Text(text = errorPassword!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
        }

        // Mostrar error general de Firebase
        if (viewModel.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = viewModel.errorMessage!!,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    // --- VALIDACIONES FINALES ---
                    var hayErrores = false

                    if (nombre.isBlank()) {
                        errorNombre = "El nombre es obligatorio"
                        hayErrores = true
                    }
                    if (apellido.isBlank()) {
                        errorApellido = "El apellido es obligatorio"
                        hayErrores = true
                    }
                    if (idInstitucional.isBlank()) {
                        errorId = "La matrícula es obligatoria"
                        hayErrores = true
                    }
                    if (email.isBlank()) {
                        errorEmail = "El correo es obligatorio"
                        hayErrores = true
                    } else if (!esEmailValido(email)) {
                        errorEmail = "El correo debe contener un @"
                        hayErrores = true
                    }
                    if (password.length < 6) {
                        errorPassword = "Mínimo 6 caracteres"
                        hayErrores = true
                    }

                    if (!hayErrores) {
                        viewModel.registro(
                            email, password, nombre, apellido, idInstitucional, onRegisterSuccess
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("REGISTRARME")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateBack) {
            Text("Volver al Login")
        }
    }
}