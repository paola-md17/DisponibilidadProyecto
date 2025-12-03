package mx.edu.utng.jdrj.disponibilidad.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close // <--- IMPORTANTE
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utng.jdrj.disponibilidad.data.model.Reserva
import mx.edu.utng.jdrj.disponibilidad.data.model.EquipoUI
import mx.edu.utng.jdrj.disponibilidad.data.model.ItemReserva
import mx.edu.utng.jdrj.disponibilidad.viewmodel.HomeViewModel
import mx.edu.utng.jdrj.disponibilidad.viewmodel.LoginViewModel
import mx.edu.utng.jdrj.disponibilidad.viewmodel.ReservaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import mx.edu.utng.jdrj.disponibilidad.data.repository.EquiposRepository
import mx.edu.utng.jdrj.disponibilidad.data.repository.FavoritosRepository
import mx.edu.utng.jdrj.disponibilidad.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    espacioId: String,
    homeViewModel: HomeViewModel,
    reservaViewModel: ReservaViewModel,
    loginViewModel: LoginViewModel,
    onNavigateBack: () -> Unit
) {
    val espacio = homeViewModel.listaEspacios.find { it.idEspacio == espacioId }
    val usuario = loginViewModel.usuarioActual
    val esAdmin = usuario?.rol == "admin"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val equiposRepo = remember { EquiposRepository() }
    val favoritosRepo = remember { FavoritosRepository() }

    // Estado de equipos
    var listaEquipos by remember { mutableStateOf<List<EquipoUI>>(emptyList()) }
    var esFavorito by remember { mutableStateOf(false) }

    // Agenda Visual
    val agendaOcupada = reservaViewModel.reservasEnFechaSeleccionada

    LaunchedEffect(espacioId) {
        listaEquipos = equiposRepo.obtenerEquiposDeEspacio(espacioId)
        if (usuario != null) esFavorito = favoritosRepo.esFavorito(usuario.idUsuario, espacioId)
        reservaViewModel.cargarAgenda(espacioId, "")
    }

    var fecha by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
    var proposito by remember { mutableStateOf("") }
    var repetirSemanalmente by remember { mutableStateOf(false) }
    var fechaFinRepeticion by remember { mutableStateOf("") }

    var equipoSeleccionado by remember { mutableStateOf<EquipoUI?>(null) }
    var cantidadSolicitada by remember { mutableIntStateOf(1) }

    var expandirInicio by remember { mutableStateOf(false) }
    var expandirFin by remember { mutableStateOf(false) }

    val horasDisponibles = remember {
        (8..22).map { hora ->
            val periodo = if (hora < 12) "AM" else "PM"
            val hora12 = if (hora > 12) hora - 12 else hora
            "${hora12.toString().padStart(2, '0')}:00 $periodo"
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val dia = dayOfMonth.toString().padStart(2, '0')
            val mes = (month + 1).toString().padStart(2, '0')
            val nuevaFecha = "$dia/$mes/$year"
            fecha = nuevaFecha
            reservaViewModel.cargarAgenda(espacioId, nuevaFecha)
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = calendar.timeInMillis

    val datePickerRepeticion = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val dia = dayOfMonth.toString().padStart(2, '0')
            val mes = (month + 1).toString().padStart(2, '0')
            fechaFinRepeticion = "$dia/$mes/$year"
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerRepeticion.datePicker.minDate = calendar.timeInMillis + 86400000

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(espacio?.nombre ?: "Detalle", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (usuario != null) {
                            esFavorito = !esFavorito
                            scope.launch { favoritosRepo.toggleFavorito(usuario.idUsuario, espacioId) }
                        }
                    }) {
                        Icon(
                            imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (esFavorito) Color(0xFFFFEB3B) else Color.White
                        )
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
        if (espacio != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // TARJETA DE INFO
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Ubicación:", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Text(text = espacio.planta, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Descripción:", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Text(text = espacio.descripcion, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        if (listaEquipos.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Equipamiento Disponible:", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))

                            listaEquipos.forEach { item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${item.cantidadTotal}x ${item.nombre}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            equipoSeleccionado = item
                                            cantidadSolicitada = 1
                                            proposito = "Reserva de equipo: ${item.nombre}"
                                        },
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        colors = if (equipoSeleccionado == item)
                                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                        else
                                            ButtonDefaults.outlinedButtonColors()
                                    ) {
                                        Text(if (equipoSeleccionado == item) "Seleccionado" else "Reservar", fontSize = 12.sp)
                                    }
                                }
                            }

                            if (equipoSeleccionado != null) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Seleccionaste: ${equipoSeleccionado!!.nombre}",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Cantidad:", modifier = Modifier.padding(end = 16.dp))
                                            IconButton(
                                                onClick = { if (cantidadSolicitada > 1) cantidadSolicitada-- },
                                                enabled = cantidadSolicitada > 1
                                            ) {
                                                Icon(Icons.Filled.Remove, "Menos")
                                            }
                                            Text(
                                                text = cantidadSolicitada.toString(),
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                            IconButton(
                                                onClick = { if (cantidadSolicitada < equipoSeleccionado!!.cantidadTotal) cantidadSolicitada++ },
                                                enabled = cantidadSolicitada < equipoSeleccionado!!.cantidadTotal
                                            ) {
                                                Icon(Icons.Filled.Add, "Más")
                                            }
                                        }
                                        Text("Máx: ${equipoSeleccionado!!.cantidadTotal}", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                                    }
                                }

                                // --- AQUÍ ESTÁ EL BOTÓN ROJO QUE PEDISTE ---
                                OutlinedButton(
                                    onClick = {
                                        equipoSeleccionado = null
                                        proposito = ""
                                        cantidadSolicitada = 1
                                    },
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(top = 8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cancelar selección")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // TÍTULO DE ACCIÓN
                val textoAccion = if (esAdmin) "Asignar Horario (Admin)" else "Reservar"
                val objetoReserva = if (equipoSeleccionado != null) "${cantidadSolicitada}x ${equipoSeleccionado!!.nombre}" else "Espacio Completo"

                Text(
                    text = "$textoAccion: $objetoReserva",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // CAMPO FECHA
                OutlinedTextField(
                    value = fecha, onValueChange = {}, label = { Text("Fecha") },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }, enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                if (fecha.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    // AGENDA VISUAL
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Disponibilidad:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0D47A1))
                            Spacer(modifier = Modifier.height(8.dp))
                            if (agendaOcupada.isEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Todo el día libre ✅", fontSize = 13.sp, color = Color(0xFF2E7D32))
                                }
                            } else {
                                agendaOcupada.forEach { reserva ->
                                    val esBloqueoTotal = reserva.equiposReservados.isEmpty()
                                    val detalle = if (esBloqueoTotal) "(Aula Completa)" else "(${reserva.equiposReservados.joinToString { "${it.cantidad}x ${it.nombreEquipo}" }})"
                                    val colorEstado = if (esBloqueoTotal) MaterialTheme.colorScheme.error else Color(0xFFF57C00)
                                    val icono = if (esBloqueoTotal) Icons.Filled.Block else Icons.Filled.Info

                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                                        Icon(icono, null, tint = colorEstado, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("${reserva.horaInicio} - ${reserva.horaFin} $detalle", fontSize = 13.sp, color = colorEstado)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SELECTORES DE HORA
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = horaInicio, onValueChange = {}, label = { Text("Inicio") },
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                            leadingIcon = { Icon(Icons.Filled.AccessTime, null) },
                            modifier = Modifier.fillMaxWidth().clickable { expandirInicio = true }, enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = MaterialTheme.colorScheme.outline, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        DropdownMenu(expanded = expandirInicio, onDismissRequest = { expandirInicio = false }, modifier = Modifier.heightIn(max = 200.dp)) {
                            horasDisponibles.forEach { hora ->
                                DropdownMenuItem(text = { Text(hora) }, onClick = { horaInicio = hora; expandirInicio = false; val indexInicio = horasDisponibles.indexOf(hora); val indexFinActual = horasDisponibles.indexOf(horaFin); if (horaFin.isEmpty() || indexFinActual <= indexInicio) if (indexInicio + 1 < horasDisponibles.size) horaFin = horasDisponibles[indexInicio + 1] else horaFin = "" })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val habilitarFin = horaInicio.isNotEmpty()
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = horaFin, onValueChange = {}, label = { Text("Fin") },
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                            leadingIcon = { Icon(Icons.Filled.AccessTime, null) },
                            modifier = Modifier.fillMaxWidth().clickable(enabled = habilitarFin) { expandirFin = true }, enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = MaterialTheme.colorScheme.outline, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant, disabledContainerColor = if (habilitarFin) Color.Transparent else Color.LightGray.copy(alpha = 0.2f))
                        )
                        DropdownMenu(expanded = expandirFin, onDismissRequest = { expandirFin = false }, modifier = Modifier.heightIn(max = 200.dp)) {
                            val indexInicio = horasDisponibles.indexOf(horaInicio); horasDisponibles.forEachIndexed { index, hora -> if (horaInicio.isEmpty() || index > indexInicio) DropdownMenuItem(text = { Text(hora) }, onClick = { horaFin = hora; expandirFin = false }) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = proposito, onValueChange = { proposito = it }, label = { Text("Propósito") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                if (esAdmin) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = repetirSemanalmente, onCheckedChange = { repetirSemanalmente = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
                                Text("Repetir todo el cuatrimestre")
                            }
                            if (repetirSemanalmente) {
                                OutlinedTextField(value = fechaFinRepeticion, onValueChange = {}, label = { Text("Fecha Fin") }, leadingIcon = { Icon(Icons.Filled.DateRange, null) }, modifier = Modifier.fillMaxWidth().clickable { datePickerRepeticion.show() }, enabled = false, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (reservaViewModel.mensajeExito != null) Text(reservaViewModel.mensajeExito!!, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                if (reservaViewModel.mensajeError != null) Text(reservaViewModel.mensajeError!!, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val camposValidos = usuario != null && fecha.isNotEmpty() && horaInicio.isNotEmpty() && horaFin.isNotEmpty()
                        val repeticionValida = if (repetirSemanalmente) fechaFinRepeticion.isNotEmpty() else true

                        if (camposValidos && repeticionValida) {
                            val estadoInicial = if (esAdmin) Constants.ESTADO_APROBADA else Constants.ESTADO_PENDIENTE

                            // CREACIÓN DE LISTA DE EQUIPOS (CARRITO)
                            val listaItems = if (equipoSeleccionado != null) {
                                listOf(ItemReserva(equipoSeleccionado!!.idEquipo, equipoSeleccionado!!.nombre, cantidadSolicitada))
                            } else {
                                emptyList()
                            }

                            val nuevaReserva = Reserva(
                                idUsuario = usuario!!.idUsuario,
                                idEspacio = espacio.idEspacio,
                                nombreEspacio = espacio.nombre,
                                equiposReservados = listaItems, // <--- CARRITO
                                fecha = fecha,
                                horaInicio = horaInicio,
                                horaFin = horaFin,
                                proposito = proposito,
                                estado = estadoInicial
                            )

                            if (esAdmin && repetirSemanalmente) {
                                reservaViewModel.crearReservaRecurrente(nuevaReserva, fechaFinRepeticion) { }
                            } else {
                                reservaViewModel.crearReserva(nuevaReserva) { }
                            }
                        } else {
                            if (usuario == null) reservaViewModel.mensajeError = "Error crítico: Usuario no cargado."
                            else reservaViewModel.mensajeError = "Por favor completa todos los campos."
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !reservaViewModel.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (reservaViewModel.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        val textoBoton = if (esAdmin && repetirSemanalmente) "ASIGNAR TODO EL CUATRI" else if (esAdmin) "ASIGNAR HORARIO" else "CONFIRMAR RESERVA"
                        Text(textoBoton, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Espacio no encontrado") }
        }
    }
}