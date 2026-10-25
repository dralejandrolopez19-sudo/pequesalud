package com.example.ui.components

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.ChildProfile
import com.example.data.HealthRecord
import com.example.data.PediatricianProfile
import com.example.ui.theme.AlertRed
import com.example.ui.theme.PequeBlue
import com.example.ui.theme.PequeGreen
import com.example.ui.theme.PequeOrange
import com.example.ui.theme.PequePurple

object PediatricNotificationManager {
    const val CHANNEL_MEDICAL_ALERTS = "pequesalud_medical_alerts"
    const val CHANNEL_APPOINTMENT_REMINDERS = "pequesalud_appointment_reminders"
    private const val PREFS_PUSH_TRACKER = "pequesalud_push_tracker_prefs"
    private const val KEY_SEEN_SIGNATURES = "seen_push_signatures"
    private const val KEY_INITIALIZED = "push_observer_initialized"

    fun ensureNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            val medicalChannel = NotificationChannel(
                CHANNEL_MEDICAL_ALERTS,
                "Indicaciones y Alertas del Pediatra",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones instantáneas cuando el pediatra envía nuevas indicaciones médicas, recetas o respuestas urgentes."
                enableVibration(true)
            }

            val appointmentChannel = NotificationChannel(
                CHANNEL_APPOINTMENT_REMINDERS,
                "Recordatorios de Citas y Vacunas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios automáticos de próximas consultas pediátricas, controles de crecimiento y vacunas PAI."
                enableVibration(true)
            }

            manager.createNotificationChannel(medicalChannel)
            manager.createNotificationChannel(appointmentChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun dispatchPushNotification(
        context: Context,
        title: String,
        body: String,
        childName: String = "",
        isAppointment: Boolean = false,
        notificationId: Int = (System.currentTimeMillis() and 0xFFFFFFF).toInt()
    ): Boolean {
        ensureNotificationChannels(context)
        if (!hasNotificationPermission(context)) {
            return false
        }

        return try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(context, notificationId, intent, pendingIntentFlags)

            val channelId = if (isAppointment) CHANNEL_APPOINTMENT_REMINDERS else CHANNEL_MEDICAL_ALERTS
            val cleanHeader = if (childName.isNotBlank()) "[$childName] $title" else title

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(cleanHeader)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(
                    if (isAppointment) NotificationCompat.CATEGORY_REMINDER
                    else NotificationCompat.CATEGORY_MESSAGE
                )
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            true
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Evalúa registros clínicos entrantes y dispara notificaciones push automáticas
     * cuando el pediatra registra una nueva cita, receta, indicación o alerta push.
     */
    fun observeAndDispatchIncomingRecords(
        context: Context,
        records: List<HealthRecord>
    ) {
        if (records.isEmpty()) return
        val prefs = context.getSharedPreferences(PREFS_PUSH_TRACKER, Context.MODE_PRIVATE)
        val isInitialized = prefs.getBoolean(KEY_INITIALIZED, false)
        val seenSet = prefs.getStringSet(KEY_SEEN_SIGNATURES, emptySet())?.toMutableSet() ?: mutableSetOf()

        val pushCandidateRecords = records.filter { rec ->
            rec.status.contains("Push", ignoreCase = true) ||
                rec.notes.contains("[PUSH]", ignoreCase = true) ||
                rec.category in listOf("CALENDARIO", "CONSULTA", "MEDICAMENTO") ||
                rec.notes.contains("| Respuesta", ignoreCase = true)
        }

        if (!isInitialized) {
            pushCandidateRecords.forEach { rec ->
                seenSet.add("${rec.id}_${rec.status}_${rec.notes.hashCode()}")
            }
            prefs.edit()
                .putBoolean(KEY_INITIALIZED, true)
                .putStringSet(KEY_SEEN_SIGNATURES, seenSet)
                .apply()
            return
        }

        var updated = false
        pushCandidateRecords.forEach { rec ->
            val signature = "${rec.id}_${rec.status}_${rec.notes.hashCode()}"
            if (rec.id.isNotBlank() && signature !in seenSet) {
                seenSet.add(signature)
                updated = true

                // Evitar disparar notificación cuando son los padres quienes confirman
                if (!rec.status.contains("Confirmado", ignoreCase = true)) {
                    val isAppointment = rec.category == "CALENDARIO" || rec.category == "VACUNA"
                    val pushTitle = when {
                        rec.status.contains("Urgente", ignoreCase = true) -> "Alerta Médica Pediátrica: ${rec.title}"
                        isAppointment -> "Recordatorio de Cita: ${rec.title}"
                        rec.category == "MEDICAMENTO" -> "Nueva Receta Médica: ${rec.title}"
                        rec.notes.contains("| Respuesta", ignoreCase = true) -> "Respuesta de tu Pediatra: ${rec.title}"
                        else -> "Nueva Indicación Médica: ${rec.title}"
                    }
                    val pushBody = when {
                        rec.notes.contains("| Respuesta", ignoreCase = true) ->
                            rec.notes.substringAfter("| ").trim()
                        rec.subtitle.isNotBlank() && rec.notes.isNotBlank() ->
                            "${rec.subtitle} · ${rec.notes.replace("[PUSH]", "").trim()}"
                        rec.notes.isNotBlank() ->
                            rec.notes.replace("[PUSH]", "").trim()
                        else ->
                            rec.subtitle
                    }
                    dispatchPushNotification(
                        context = context,
                        title = pushTitle,
                        body = pushBody.ifBlank { "Revisa el Escritorio de Padres en PequeSalud." },
                        childName = rec.childName.substringBefore(" "),
                        isAppointment = isAppointment,
                        notificationId = rec.id.hashCode()
                    )
                }
            }
        }

        if (updated) {
            prefs.edit().putStringSet(KEY_SEEN_SIGNATURES, seenSet).apply()
        }
    }
}

/**
 * Observador en tiempo real y gestor de permiso POST_NOTIFICATIONS (Android 13+).
 */
@Composable
fun PediatricPushPermissionAndRealtimeObserver(
    records: List<HealthRecord>
) {
    val context = LocalContext.current
    var hasRequestedPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        PediatricNotificationManager.ensureNotificationChannels(context)
    }

    LaunchedEffect(Unit) {
        PediatricNotificationManager.ensureNotificationChannels(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !PediatricNotificationManager.hasNotificationPermission(context) &&
            !hasRequestedPermission
        ) {
            hasRequestedPermission = true
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(records) {
        PediatricNotificationManager.observeAndDispatchIncomingRecords(
            context = context,
            records = records
        )
    }
}

private data class PushTemplateOption(
    val typeIndex: Int, // 0 = Cita Próxima, 1 = Nueva Indicación, 2 = Vacuna PAI, 3 = Alerta Urgente
    val label: String,
    val title: String,
    val subtitle: String,
    val scheduleText: String,
    val message: String
)

/**
 * Modal Clínico del Pediatra para enviar Notificaciones Push y Recordatorios Automáticos a los Padres.
 */
@Composable
fun SendPediatricPushNotificationDialog(
    activeChild: ChildProfile,
    pediatricianProfile: PediatricianProfile,
    onDismiss: () -> Unit,
    onSendPushRecord: (HealthRecord) -> Unit
) {
    val context = LocalContext.current
    val childShortName = activeChild.name.substringBefore(" ")

    // 0 = Cita Próxima, 1 = Indicación Médica, 2 = Recordatorio Vacuna, 3 = Alerta Urgente
    var selectedType by remember { mutableIntStateOf(0) }

    val templates = remember(childShortName, pediatricianProfile.name) {
        listOf(
            PushTemplateOption(
                typeIndex = 0,
                label = "Cita de control mañana",
                title = "Recordatorio de Cita Pediátrica",
                subtitle = "Consulta con ${pediatricianProfile.name}",
                scheduleText = "Mañana · 10:00 AM",
                message = "Hola mamá y papá, les recordamos la cita de control pediátrico y antropometría OMS de $childShortName mañana a las 10:00 AM. Traer carnet de vacunación."
            ),
            PushTemplateOption(
                typeIndex = 0,
                label = "Control de peso y talla",
                title = "Próximo Control de Crecimiento OMS",
                subtitle = "Seguimiento antropométrico · ${pediatricianProfile.name}",
                scheduleText = "Próxima semana",
                message = "Recordatorio automático para agendar o asistir a la evaluación de peso, talla y percentiles OMS de $childShortName."
            ),
            PushTemplateOption(
                typeIndex = 1,
                label = "Nueva indicación en casa",
                title = "Nueva Indicación Médica Actualizada",
                subtitle = "Plan de cuidado · ${pediatricianProfile.name}",
                scheduleText = "Hoy · Inmediato",
                message = "He actualizado las indicaciones médicas de $childShortName. Por favor revisen el plan de hidratación, control de temperatura y horarios de medicación en su Escritorio."
            ),
            PushTemplateOption(
                typeIndex = 1,
                label = "Recordatorio de toma",
                title = "Recordatorio de Horario de Medicamento",
                subtitle = "Adherencia terapéutica · ${pediatricianProfile.name}",
                scheduleText = "Cada 8 horas",
                message = "Recuerden administrar la dosis ponderal indicada a $childShortName y marcar 'Toma dada' en el Escritorio de Padres para llevar el registro."
            ),
            PushTemplateOption(
                typeIndex = 2,
                label = "Próxima vacuna PAI",
                title = "Recordatorio de Vacunación PAI",
                subtitle = "Esquema de inmunización · ${pediatricianProfile.name}",
                scheduleText = "Próxima dosis",
                message = "Recordatorio automático: $childShortName tiene pendiente su refuerzo del esquema PAI. Acudir con tarjeta de vacunación y vigilar febrícula post-vacunal."
            ),
            PushTemplateOption(
                typeIndex = 3,
                label = "Vigilar signos de alarma",
                title = "Alerta de Seguimiento Clínico",
                subtitle = "Prioridad alta · ${pediatricianProfile.name}",
                scheduleText = "Atención inmediata",
                message = "Por favor reporten en la bandeja de Síntomas cómo ha evolucionado la temperatura y el estado de hidratación de $childShortName en las últimas 4 horas."
            )
        )
    }

    var pushTitle by remember { mutableStateOf(templates[0].title) }
    var pushSchedule by remember { mutableStateOf(templates[0].scheduleText) }
    var pushMessage by remember { mutableStateOf(templates[0].message) }
    var autoReminder24h by remember { mutableStateOf(true) }
    var dispatchSystemPushNow by remember { mutableStateOf(true) }

    val scheduleChips = listOf(
        "Hoy · Inmediato",
        "Mañana · 10:00 AM",
        "24h antes de cita",
        "En 48 horas",
        "Próxima semana"
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = PequeBlue.copy(alpha = 0.14f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = PequeBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Notificación Push a Padres",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Paciente: ${activeChild.name} · ${activeChild.familyLinkCode()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Envía una alerta push instantánea al teléfono de los padres y programa un recordatorio interactivo en su Escritorio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Selector de categoría de notificación push
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val types = listOf(
                        Triple(0, "Cita Próxima", PequeBlue),
                        Triple(1, "Nueva Indicación", PequeGreen),
                        Triple(2, "Vacuna PAI", PequePurple),
                        Triple(3, "Alerta Urgente", AlertRed)
                    )
                    types.forEach { (idx, label, color) ->
                        FilterChip(
                            selected = selectedType == idx,
                            onClick = {
                                selectedType = idx
                                val matching = templates.firstOrNull { it.typeIndex == idx }
                                if (matching != null) {
                                    pushTitle = matching.title
                                    pushSchedule = matching.scheduleText
                                    pushMessage = matching.message
                                }
                            },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("push_type_chip_$idx")
                        )
                    }
                }

                // Plantillas rápidas de 1 toque
                Text(
                    text = "Plantillas automáticas rápidas:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    templates.filter { it.typeIndex == selectedType }.forEach { tpl ->
                        FilterChip(
                            selected = pushTitle == tpl.title,
                            onClick = {
                                pushTitle = tpl.title
                                pushSchedule = tpl.scheduleText
                                pushMessage = tpl.message
                            },
                            label = { Text(tpl.label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = pushTitle,
                    onValueChange = { pushTitle = it },
                    label = { Text("Título de la notificación push") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("push_notification_title_input")
                )

                // Selector de momento / recordatorio
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    scheduleChips.forEach { chip ->
                        FilterChip(
                            selected = pushSchedule == chip,
                            onClick = { pushSchedule = chip },
                            label = { Text(chip) }
                        )
                    }
                }

                OutlinedTextField(
                    value = pushSchedule,
                    onValueChange = { pushSchedule = it },
                    label = { Text("Fecha / hora de cita o recordatorio") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("push_notification_schedule_input")
                )

                OutlinedTextField(
                    value = pushMessage,
                    onValueChange = { pushMessage = it },
                    label = { Text("Mensaje para mamá y papá") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("push_notification_message_input")
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PequeBlue.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, PequeBlue.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Notificación Push en barra de estado",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Sonar y mostrar aviso emergente en el dispositivo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = dispatchSystemPushNow,
                                onCheckedChange = { checked ->
                                    dispatchSystemPushNow = checked
                                    if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        !PediatricNotificationManager.hasNotificationPermission(context)
                                    ) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            )
                        }

                        if (selectedType == 0 || selectedType == 2) {
                            HorizontalDivider(color = PequeBlue.copy(alpha = 0.18f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Activar recordatorio automático 24h antes",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PequeBlue,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = autoReminder24h,
                                    onCheckedChange = { autoReminder24h = it }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanTitle = pushTitle.trim().ifBlank { "Aviso del Pediatra" }.take(140)
                    val cleanSchedule = pushSchedule.trim().ifBlank { "Hoy" }.take(110)
                    val cleanMsg = pushMessage.trim().ifBlank { "Revisa las indicaciones en PequeSalud." }

                    val category = when (selectedType) {
                        0 -> "CALENDARIO"
                        2 -> "VACUNA"
                        else -> "CONSULTA"
                    }
                    val statusLabel = when (selectedType) {
                        0 -> "Recordatorio Push"
                        2 -> "Recordatorio Push"
                        3 -> "Urgente Push"
                        else -> "Alerta Push"
                    }
                    val autoTag = if ((selectedType == 0 || selectedType == 2) && autoReminder24h) {
                        " · Recordatorio auto 24h activo"
                    } else {
                        ""
                    }

                    val record = HealthRecord(
                        childId = activeChild.id,
                        childName = activeChild.name,
                        category = category,
                        title = cleanTitle,
                        subtitle = ("${pediatricianProfile.name}$autoTag").take(190),
                        dateText = cleanSchedule,
                        status = statusLabel,
                        notes = "[PUSH] $cleanMsg".take(1450)
                    )

                    if (dispatchSystemPushNow) {
                        PediatricNotificationManager.dispatchPushNotification(
                            context = context,
                            title = cleanTitle,
                            body = "$cleanSchedule — $cleanMsg",
                            childName = childShortName,
                            isAppointment = selectedType == 0 || selectedType == 2
                        )
                    }

                    onSendPushRecord(record)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == 3) AlertRed else PequeBlue
                ),
                modifier = Modifier.testTag("confirm_send_push_notification_button")
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Enviar Alerta Push",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Centro de Alertas y Recordatorios Push del Pediatra en el Escritorio de Padres.
 * Permite ver citas próximas y nuevas indicaciones con confirmación de lectura en 1 toque.
 */
@Composable
fun ParentPushNotificationsBannerCard(
    activeChild: ChildProfile,
    records: List<HealthRecord>,
    onConfirmNotification: (HealthRecord) -> Unit,
    onOpenReadOnlySection: (Int) -> Unit
) {
    val context = LocalContext.current
    val childPushRecords = remember(records, activeChild.id) {
        records.filter { rec ->
            (rec.childId == activeChild.id || rec.childName == activeChild.name) &&
                (rec.status.contains("Push", ignoreCase = true) ||
                    rec.notes.contains("[PUSH]", ignoreCase = true) ||
                    rec.category == "CALENDARIO")
        }
    }

    val unconfirmedCount = remember(childPushRecords) {
        childPushRecords.count { !it.status.contains("Confirmado", ignoreCase = true) }
    }

    if (childPushRecords.isEmpty()) return

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.2.dp,
            if (unconfirmedCount > 0) PequeOrange.copy(alpha = 0.55f)
            else PequeGreen.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("parent_push_notifications_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (unconfirmedCount > 0) PequeOrange.copy(alpha = 0.15f)
                                else PequeGreen.copy(alpha = 0.14f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (unconfirmedCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (unconfirmedCount > 0) PequeOrange else PequeGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Avisos y Citas del Pediatra",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (unconfirmedCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = PequeOrange.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "$unconfirmedCount nuevo(s)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PequeOrange,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Notificaciones push y recordatorios automáticos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            childPushRecords.take(3).forEachIndexed { idx, pushRec ->
                if (idx > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                }
                val isUrgent = pushRec.status.contains("Urgente", ignoreCase = true)
                val isAppointment = pushRec.category == "CALENDARIO" || pushRec.category == "VACUNA"
                val isConfirmed = pushRec.status.contains("Confirmado", ignoreCase = true)
                val accent = when {
                    isUrgent -> AlertRed
                    isAppointment -> PequeBlue
                    else -> PequeGreen
                }
                val cleanBody = pushRec.notes.replace("[PUSH]", "").trim()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (pushRec.category == "VACUNA") onOpenReadOnlySection(1)
                            else onOpenReadOnlySection(0)
                        },
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = when {
                                    isUrgent -> Icons.Default.Warning
                                    pushRec.category == "VACUNA" -> Icons.Default.Vaccines
                                    isAppointment -> Icons.Default.CalendarMonth
                                    else -> Icons.Default.MedicalServices
                                },
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = pushRec.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = accent.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = pushRec.dateText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (cleanBody.isNotBlank()) {
                        Text(
                            text = cleanBody,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pushRec.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    PediatricNotificationManager.dispatchPushNotification(
                                        context = context,
                                        title = pushRec.title,
                                        body = "${pushRec.dateText} — $cleanBody",
                                        childName = activeChild.name.substringBefore(" "),
                                        isAppointment = isAppointment
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Probar aviso push",
                                    tint = PequeBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ver aviso",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PequeBlue
                                )
                            }

                            if (!isConfirmed) {
                                Surface(
                                    onClick = { onConfirmNotification(pushRec) },
                                    shape = RoundedCornerShape(50),
                                    color = PequeGreen.copy(alpha = 0.14f),
                                    border = BorderStroke(1.dp, PequeGreen.copy(alpha = 0.4f)),
                                    modifier = Modifier.testTag("confirm_push_alert_${pushRec.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = PequeGreen,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isAppointment) "Confirmar cita" else "Enterado",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = PequeGreen
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = PequeGreen.copy(alpha = 0.10f)
                                ) {
                                    Text(
                                        text = "Confirmado",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PequeGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
