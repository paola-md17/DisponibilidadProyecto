package mx.edu.utng.jdrj.disponibilidad

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.edu.utng.jdrj.disponibilidad.ui.theme.DisponibilidadTheme
import mx.edu.utng.jdrj.disponibilidad.ui.screens.*
import mx.edu.utng.jdrj.disponibilidad.utils.AppNotificationManager
import mx.edu.utng.jdrj.disponibilidad.viewmodel.AdminViewModel
import mx.edu.utng.jdrj.disponibilidad.viewmodel.HomeViewModel
import mx.edu.utng.jdrj.disponibilidad.viewmodel.LoginViewModel
import mx.edu.utng.jdrj.disponibilidad.viewmodel.ReservaViewModel
import mx.edu.utng.jdrj.disponibilidad.viewmodel.StatisticsViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        setContent {
            DisponibilidadTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val notificationManager = remember { AppNotificationManager(context) }

    val loginViewModel: LoginViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val reservaViewModel: ReservaViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()
    val statisticsViewModel: StatisticsViewModel = viewModel() // ViewModel para gráficas

    val usuario = loginViewModel.usuarioActual
    LaunchedEffect(usuario) {
        if (usuario != null) {
            if (usuario.rol == "admin") {
                notificationManager.iniciarEscuchaAdmin(usuario)
            } else {
                notificationManager.iniciarEscuchaUsuario(usuario)
            }
        }
    }

    NavHost(navController = navController, startDestination = "login") {

        // 1. LOGIN
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    val user = loginViewModel.usuarioActual
                    if (user?.rol == "admin") {
                        navController.navigate("admin_dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        // 2. REGISTRO
        composable("register") {
            RegisterScreen(
                viewModel = loginViewModel,
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 3. HOME ALUMNO / ADMIN (Selector)
        composable("home") {
            HomeScreen(
                viewModel = homeViewModel,
                loginViewModel = loginViewModel,
                onEspacioClick = { id -> navController.navigate("detail/$id") },
                onNavigateToMisReservas = { navController.navigate("mis_reservas") },
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate("login") { popUpTo("home") { inclusive = true } }
                },
                onBack = { navController.popBackStack() },
                onIrAPerfil = { navController.navigate("profile") }
            )
        }

        // 4. ADMIN DASHBOARD
        composable("admin_dashboard") {
            AdminDashboardScreen(
                adminViewModel = adminViewModel,
                loginViewModel = loginViewModel,
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate("login") { popUpTo("admin_dashboard") { inclusive = true } }
                },
                onIrAEspacios = { navController.navigate("home") },
                onIrAUsuarios = { navController.navigate("admin_users") },
                onIrAGestionEspacios = { navController.navigate("admin_spaces") },
                onIrAPerfil = { navController.navigate("profile") },
                onIrAEstadisticas = { navController.navigate("statistics") } // Conectado
            )
        }

        // 7. USUARIOS
        composable("admin_users") {
            UserManagementScreen(
                adminViewModel = adminViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 8. PERFIL
        composable("profile") {
            ProfileScreen(
                loginViewModel = loginViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 9. GESTIÓN ESPACIOS
        composable("admin_spaces") {
            SpaceManagementScreen(
                adminViewModel = adminViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 10. ESTADÍSTICAS (Ruta que faltaba)
        composable("statistics") {
            // Calculamos al entrar
            LaunchedEffect(Unit) { statisticsViewModel.calcularEstadisticas() }

            StatisticsScreen(
                viewModel = statisticsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. DETALLE
        composable(
            route = "detail/{espacioId}",
            arguments = listOf(navArgument("espacioId") { type = NavType.StringType })
        ) { backStackEntry ->
            val espacioId = backStackEntry.arguments?.getString("espacioId") ?: ""
            DetailScreen(
                espacioId = espacioId,
                homeViewModel = homeViewModel,
                reservaViewModel = reservaViewModel,
                loginViewModel = loginViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. MIS RESERVAS
        composable("mis_reservas") {
            MyReservationsScreen(
                reservaViewModel = reservaViewModel,
                loginViewModel = loginViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}