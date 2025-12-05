# 📅 Disponibilidad - Gestión de Espacios TICS

**Disponibilidad** es una aplicación móvil nativa desarrollada para optimizar la gestión y reserva de espacios institucionales. El proyecto permite consultar disponibilidad en tiempo real y realizar reservas de manera eficiente.

🔗 **Sitio Web:** [https://JOSTHONS.github.io/disponibilidad-tics/](https://JOSTHONS.github.io/disponibilidad-tics/)

## 🚀 Características Principales
* **🔐 Autenticación Segura:** Login y registro con **Firebase Auth**.
* **📅 Reservas en Tiempo Real:** Visualización instantánea mediante **Cloud Firestore**.
* **🔔 Notificaciones:** Alertas de confirmación (Android 13+).
* **👤 Roles de Usuario:** Interfaz para Admin y Estudiante.
* **📊 Dashboard:** Métricas de ocupación.

## 🛠 Stack Tecnológico
| Categoría | Tecnología |
| :--- | :--- |
| **Lenguaje** | Kotlin |
| **UI** | Jetpack Compose (Material 3) |
| **Backend** | Firebase |
| **Patrón** | MVVM |

## 📂 Estructura del Proyecto
El código fuente está organizado siguiendo los principios de **Clean Architecture** y **MVVM** para asegurar la escalabilidad:

```text
mx.edu.utng.jdrj.disponibilidad
├── 📂 ui              # Capa de Presentación (Vistas)
│   ├── 📂 screens     # Pantallas Composable (Login, Home, Reserva)
│   └── 📂 theme       # Tema y Tipografía (Material Design 3)
├── 📂 viewmodel       # Capa de Lógica de Negocio (State Management)
├── 📂 data            # Capa de Datos (Modelos y Repositorios)
│   └── 📂 firebase    # Conexión con Firestore y Auth
└── 📂 utils           # Clases utilitarias (NotificationManager, Constantes)
```

## 📸 Capturas de Pantalla
| Login | Home | Reserva |
|:---:|:---:|:---:|
| ![Login](docs/screenshots/login.png) | ![Home](docs/screenshots/home.png) | ![Reserva](docs/screenshots/reserva.png) |

## 🔧 Instalación
1. Clonar repositorio.
2. Agregar `google-services.json`.
3. Compilar en Android Studio.

## 📄 Documentación KDoc (Ejemplos)

### 1. Navegación
```kotlin
/**
 * Gestiona el grafo de navegación y ViewModels.
 * @see LoginViewModel
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Implementación...
}

```

### 2. Verificación de Permisos

```kotlin
/**
 * Valida permiso POST_NOTIFICATIONS en Android 13+.
 * Si no se tiene, lanza el requestPermissionLauncher.
 */
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
}

```
## 🌟 Validación con Usuarios
El proyecto fue sometido a pruebas de usabilidad con **10 usuarios reales**, obteniendo los siguientes resultados:

| Métrica | Puntuación |
| :--- | :--- |
| **Satisfacción General** | ⭐⭐⭐⭐⭐ (4.8/5.0) |
| **Facilidad de Uso** | 95% |
| **Estabilidad** | 100% (Sin errores críticos) |

## 📺 Demostración
¡Mira la app en funcionamiento!
[**Ver Video en YouTube**](https://youtu.be/dQS_hPHYwmw)

## 👥 Equipo de Desarrollo

| Nombre | Rol |
| :--- | :--- |
| **Paola Moya Díaz** | Desarrollador Android |
| **Josthyn Daniel Rodríguez de Jesús** | Desarrollador Android |
