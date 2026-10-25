package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ChildProfile
import com.example.data.HealthRecord
import com.example.data.pediatricDrugCatalog
import com.example.ui.components.ChildAvatar
import com.example.ui.components.FeverThermalCurveCard
import com.example.ui.components.LactationTimerAndDiaperCounterSection
import com.example.ui.components.MedicalQrCardDialog
import com.example.ui.components.ParentPushNotificationsBannerCard
import com.example.ui.components.PediatricPdfAndShareHelper
import com.example.ui.components.TannerTargetHeightCalculatorCard
import com.example.ui.components.WhoGrowthChartCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CardAiIcon
import com.example.ui.theme.PequeBlue
import com.example.ui.theme.PequeGreen
import com.example.ui.theme.PequeOrange
import com.example.ui.theme.PequePurple
import java.util.Locale

/**
 * Conmutador minimalista de modo (solo se usa en la pestaña Más o encabezados secundarios).
 */
@Composable
fun WorkspaceRoleBanner(
    isPediatricianMode: Boolean,
    pediatricianName: String,
    currentPin: String = "1234",
    onRequestSwitchMode: () -> Unit,
    onOpenPinSettings: () -> Unit = {}
) {
    val accentColor = if (isPediatricianMode) PequeBlue else PequeGreen

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isPediatricianMode) Icons.Default.MedicalServices else Icons.Default.Shield,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isPediatricianMode) "Modo Pediatra Activo" else "Escritorio de Padres",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isPediatricianMode) {
                                "$pediatricianName · PIN activo: $currentPin"
                            } else {
                                "Protegido con PIN médico ($currentPin)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    onClick = onRequestSwitchMode,
                    shape = RoundedCornerShape(50),
                    color = accentColor.copy(alpha = 0.14f),
                    modifier = Modifier.testTag("switch_workspace_mode_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPediatricianMode) Icons.Default.Shield else Icons.Default.Lock,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isPediatricianMode) "Vista Padres" else "Modo Pediatra",
                            style = MaterialTheme.typography.labelMedium,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configuración de PIN para Modo Pediatra",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onOpenPinSettings,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("open_pin_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = PequeBlue,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Configurar PIN",
                        style = MaterialTheme.typography.labelMedium,
                        color = PequeBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * ESCRITORIO DE PADRES — Dashboard Ejecutivo Familiar, cálido, visual y sin ruido.
 */
@Composable
fun ParentHomeDashboardScreen(
    parentDisplayName: String,
    children: List<ChildProfile>,
    selectedChildId: String,
    records: List<HealthRecord>,
    onSelectChild: (String) -> Unit,
    onAddChildClicked: () -> Unit,
    onAddRecord: (HealthRecord) -> Unit,
    onOpenTrackerWithFilter: (Int) -> Unit,
    onOpenReadOnlySection: (Int) -> Unit,
    onOpenAiAssistant: () -> Unit,
    onRequestPediatricianMode: () -> Unit,
    onOpenPinSettings: () -> Unit = {},
    onEditActiveChild: (ChildProfile) -> Unit = {},
    onConfirmPushNotification: (HealthRecord) -> Unit = {},
    onOpenHybridPwaHub: () -> Unit = {}
) {
    val activeChild = children.find { it.id == selectedChildId } ?: children.firstOrNull()
    val formattedParentName = remember(parentDisplayName) {
        parentDisplayName.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    val childRecords = remember(records, activeChild) {
        if (activeChild == null) emptyList()
        else records.filter { it.childId == activeChild.id || it.childName == activeChild.name }
    }

    val activeMeds = childRecords.filter { it.category == "MEDICAMENTO" && it.status.equals("Activo", ignoreCase = true) }
    val appliedVaccines = childRecords.filter { it.category == "VACUNA" && it.status.equals("Aplicada", ignoreCase = true) }
    val pendingVaccines = childRecords.filter { it.category == "VACUNA" && it.status.equals("Pendiente", ignoreCase = true) }
    val homeSymptoms = childRecords.filter { it.category == "SINTOMA_CASA" }
    val homeDiary = childRecords.filter { it.category == "DIARIO_BEBE" }
    val parentDoubts = childRecords.filter { it.category == "DUDA_PADRE" }
    val parentStudies = childRecords.filter { it.category == "ESTUDIO_PADRE" }

    val doctorDirectIndications = childRecords.filter {
        it.category == "CONSULTA" || it.category == "CERTIFICADO"
    }
    val answeredParentReports = childRecords.filter {
        it.category in listOf("SINTOMA_CASA", "DUDA_PADRE", "DIARIO_BEBE", "ESTUDIO_PADRE") &&
            (it.notes.contains("|") || it.status.contains("Respondida", ignoreCase = true) || it.status.contains("Revisado", ignoreCase = true))
    }

    var showSymptomDialog by remember { mutableStateOf(false) }
    var showBabyDiaryDialog by remember { mutableStateOf(false) }
    var showParentDoubtDialog by remember { mutableStateOf(false) }
    var showUploadStudyDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showEditChildDialog by remember { mutableStateOf(false) }
    var showFeverDoseDialog by remember { mutableStateOf(false) }
    var showAlarmTrafficLightDialog by remember { mutableStateOf(false) }
    var showMilestonesDialog by remember { mutableStateOf(false) }
    var showNextAppointmentDialog by remember { mutableStateOf(false) }

    if (showQrDialog && activeChild != null) {
        MedicalQrCardDialog(
            child = activeChild,
            onDismiss = { showQrDialog = false }
        )
    }

    if (showEditChildDialog && activeChild != null) {
        AddOrEditChildDialog(
            initialChild = activeChild,
            onDismiss = { showEditChildDialog = false },
            onConfirm = { updated ->
                onEditActiveChild(updated)
                showEditChildDialog = false
            }
        )
    }

    if (showSymptomDialog && activeChild != null) {
        ParentAddSymptomDialog(
            child = activeChild,
            onDismiss = { showSymptomDialog = false },
            onSave = { rec ->
                onAddRecord(rec)
                showSymptomDialog = false
            }
        )
    }

    if (showBabyDiaryDialog && activeChild != null) {
        ParentAddBabyDiaryDialog(
            child = activeChild,
            onDismiss = { showBabyDiaryDialog = false },
            onSave = { rec ->
                onAddRecord(rec)
                showBabyDiaryDialog = false
            }
        )
    }

    if (showParentDoubtDialog && activeChild != null) {
        ParentAddDoubtDialog(
            child = activeChild,
            onDismiss = { showParentDoubtDialog = false },
            onSave = { rec ->
                onAddRecord(rec)
                showParentDoubtDialog = false
            }
        )
    }

    if (showUploadStudyDialog && activeChild != null) {
        ParentUploadStudyDialog(
            child = activeChild,
            onDismiss = { showUploadStudyDialog = false },
            onSave = { rec ->
                onAddRecord(rec)
                showUploadStudyDialog = false
            }
        )
    }

    if (showFeverDoseDialog && activeChild != null) {
        ParentFeverDoseCalculatorDialog(
            child = activeChild,
            onDismiss = { showFeverDoseDialog = false },
            onLogDoseRecord = { rec ->
                onAddRecord(rec)
                showFeverDoseDialog = false
            }
        )
    }

    if (showAlarmTrafficLightDialog && activeChild != null) {
        ParentAlarmTrafficLightDialog(
            child = activeChild,
            onDismiss = { showAlarmTrafficLightDialog = false },
            onReportUrgentSymptom = { rec ->
                onAddRecord(rec)
                showAlarmTrafficLightDialog = false
            }
        )
    }

    if (showMilestonesDialog && activeChild != null) {
        ParentDevelopmentalMilestonesDialog(
            child = activeChild,
            onDismiss = { showMilestonesDialog = false },
            onSaveMilestoneRecord = { rec ->
                onAddRecord(rec)
                showMilestonesDialog = false
            }
        )
    }

    if (showNextAppointmentDialog && activeChild != null) {
        ParentScheduleAppointmentDialog(
            child = activeChild,
            onDismiss = { showNextAppointmentDialog = false },
            onSaveAppointment = { rec ->
                onAddRecord(rec)
                showNextAppointmentDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Cabecera Ejecutiva del Escritorio de Padres
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PequeGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ESCRITORIO DE PADRES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = PequeGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Hola, $formattedParentName",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (activeChild != null) {
                        IconButton(
                            onClick = { showQrDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .testTag("parent_qr_code_chip")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "Código QR",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenPinSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PequeBlue.copy(alpha = 0.1f))
                            .testTag("parent_header_pin_config_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Configurar PIN Modo Pediatra",
                            tint = PequeBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Surface(
                        onClick = onRequestPediatricianMode,
                        shape = RoundedCornerShape(50),
                        color = PequeBlue.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, PequeBlue.copy(alpha = 0.45f)),
                        modifier = Modifier.testTag("switch_workspace_mode_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = PequeBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Modo Pediatra",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PequeBlue
                            )
                        }
                    }
                }
            }
        }

        // 2. Selector de Hijos + Tarjeta Hero del Paciente Activo
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    children.forEach { child ->
                        val selected = activeChild?.id == child.id
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (selected) PequeGreen else MaterialTheme.colorScheme.surface,
                            border = if (selected) null else BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier
                                .clickable { onSelectChild(child.id) }
                                .testTag("parent_select_child_${child.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChildAvatar(
                                    name = child.name,
                                    gender = child.gender,
                                    colorHex = child.avatarColorHex,
                                    size = 22.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = child.name.substringBefore(" "),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Surface(
                        shape = if (children.isEmpty()) RoundedCornerShape(50) else CircleShape,
                        color = if (children.isEmpty()) PequeGreen else MaterialTheme.colorScheme.surface,
                        border = if (children.isEmpty()) null else BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .then(if (children.isEmpty()) Modifier else Modifier.size(38.dp))
                            .clickable { onAddChildClicked() }
                            .testTag("parent_add_child_button")
                    ) {
                        if (children.isEmpty()) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Registrar hijo/a",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Registrar hijo/a",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Agregar hijo",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Tarjeta Hero Ejecutiva del Niño/a Activo
                if (activeChild != null) {
                    val hasAllergy = !activeChild.allergies.equals("Ninguna", ignoreCase = true) &&
                        !activeChild.allergies.contains("Ninguna", ignoreCase = true)
                    val weightText = if (activeChild.weightKg > 0.0) "${activeChild.weightKg} kg" else "Sin peso"
                    val heightText = if (activeChild.heightCm > 0.0) "${activeChild.heightCm} cm" else "Sin talla"

                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, PequeGreen.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            PequeGreen.copy(alpha = 0.08f),
                                            PequeBlue.copy(alpha = 0.04f)
                                        )
                                    )
                                )
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ChildAvatar(
                                        name = activeChild.name,
                                        gender = activeChild.gender,
                                        colorHex = activeChild.avatarColorHex,
                                        size = 54.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = activeChild.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${activeChild.ageYears} años · Grupo ${activeChild.bloodType} · ${activeChild.familyLinkCode()}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (hasAllergy) AlertRed.copy(alpha = 0.12f) else PequeGreen.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = if (hasAllergy) "Alergia: ${activeChild.allergies}" else "Sin alergias reportadas",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (hasAllergy) AlertRed else PequeGreen,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { showEditChildDialog = true },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar datos del niño",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // 3 KPIs rápidos del estado del niño
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ParentKpiPill(
                                    label = "Peso y Talla",
                                    value = "$weightText · $heightText",
                                    accentColor = PequeBlue,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onOpenReadOnlySection(2) }
                                )
                                ParentKpiPill(
                                    label = "Tratamientos",
                                    value = if (activeMeds.isEmpty()) "0 activos" else "${activeMeds.size} en curso",
                                    accentColor = PequeOrange,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onOpenReadOnlySection(0) }
                                )
                                ParentKpiPill(
                                    label = "Vacunas PAI",
                                    value = if (pendingVaccines.isEmpty()) "${appliedVaccines.size} al día" else "${pendingVaccines.size} pend.",
                                    accentColor = PequeGreen,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onOpenReadOnlySection(1) }
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, PequeGreen.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddChildClicked() }
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(PequeGreen.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = PequeGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Comienza registrando a tu hijo/a",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Coloca su nombre, fecha de nacimiento, peso y alergias para activar su expediente y conectarlo con el pediatra.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2.5 Centro de Notificaciones Push y Recordatorios Automáticos del Pediatra
        if (activeChild != null) {
            val hasPushOrAppointments = childRecords.any {
                it.status.contains("Push", ignoreCase = true) ||
                    it.notes.contains("[PUSH]", ignoreCase = true) ||
                    it.category == "CALENDARIO"
            }
            if (hasPushOrAppointments) {
                item {
                    ParentPushNotificationsBannerCard(
                        activeChild = activeChild,
                        records = childRecords,
                        onConfirmNotification = onConfirmPushNotification,
                        onOpenReadOnlySection = onOpenReadOnlySection
                    )
                }
            }
        }

        // 3. Tarjeta Ejecutiva Destacada: "Indicaciones y Plan de tu Pediatra" (Sincronizada en tiempo real)
        if (activeChild != null) {
            item {
                val totalIndicationsCount = doctorDirectIndications.size + activeMeds.size + answeredParentReports.size
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Indicaciones de tu pediatra",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (totalIndicationsCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = PequeBlue.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "$totalIndicationsCount",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PequeBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Ver recetas y plan",
                            style = MaterialTheme.typography.labelLarge,
                            color = PequeBlue,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onOpenReadOnlySection(0) }
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, PequeBlue.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Cabecera del médico tratante
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MedicalServices,
                                        contentDescription = null,
                                        tint = PequeBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = activeChild.pediatrician,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PequeBlue
                                    )
                                }
                                Text(
                                    text = "Última revisión: ${activeChild.lastCheckupDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )

                            var hasRenderedItem = false

                            // 1) Indicaciones directas o planes de consulta del pediatra
                            doctorDirectIndications.take(2).forEach { ind ->
                                if (hasRenderedItem) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    )
                                }
                                hasRenderedItem = true
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenReadOnlySection(0) }
                                        .testTag("parent_doctor_indication_row_${ind.id}")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(PequeBlue.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MedicalServices,
                                            contentDescription = null,
                                            tint = PequeBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = ind.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = ind.dateText,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PequeBlue,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        if (ind.subtitle.isNotBlank()) {
                                            Text(
                                                text = ind.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        if (ind.notes.isNotBlank()) {
                                            Text(
                                                text = ind.notes.replace("[PUSH]", "").trim(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            // 2) Recetas y medicamentos activos (con botón 1-toque para confirmar toma en casa)
                            activeMeds.take(2).forEach { med ->
                                if (hasRenderedItem) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    )
                                }
                                hasRenderedItem = true
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenReadOnlySection(0) }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(PequeOrange.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Medication,
                                            contentDescription = null,
                                            tint = PequeOrange,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = med.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = med.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PequeBlue
                                        )
                                        if (med.notes.isNotBlank()) {
                                            Text(
                                                text = med.notes.replace("[PUSH]", "").trim(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        onClick = {
                                            onAddRecord(
                                                HealthRecord(
                                                    childId = activeChild.id,
                                                    childName = activeChild.name,
                                                    category = "DIARIO_BEBE",
                                                    title = "Toma administrada: ${med.title.substringBefore(" (")}",
                                                    subtitle = med.subtitle,
                                                    dateText = "Hoy",
                                                    status = "Administrada",
                                                    notes = "Dosis confirmada en casa por mamá/papá (${med.subtitle})."
                                                )
                                            )
                                        },
                                        shape = RoundedCornerShape(50),
                                        color = PequeGreen.copy(alpha = 0.14f),
                                        border = BorderStroke(1.dp, PequeGreen.copy(alpha = 0.35f))
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
                                                text = "Toma dada",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = PequeGreen
                                            )
                                        }
                                    }
                                }
                            }

                            // 3) Respuestas del pediatra a síntomas, dudas, diario o estudios de casa
                            answeredParentReports.take(2).forEach { rep ->
                                if (hasRenderedItem) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    )
                                }
                                hasRenderedItem = true
                                val filterIdx = when (rep.category) {
                                    "SINTOMA_CASA" -> 1
                                    "DIARIO_BEBE" -> 2
                                    "DUDA_PADRE" -> 3
                                    "ESTUDIO_PADRE" -> 4
                                    else -> 0
                                }
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenTrackerWithFilter(filterIdx) }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(PequeGreen.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = PequeGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Respuesta médica · ${rep.title}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PequeGreen
                                        )
                                        Text(
                                            text = rep.notes.substringAfter("|", rep.notes).trim(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            if (!hasRenderedItem) {
                                Text(
                                    text = "Nota actual: ${activeChild.lastCheckupSummary}. Cuando el pediatra envíe una receta, plan o respuesta desde su escritorio, aparecerá aquí al instante.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Cuadrícula Bento 2x2: "Registrar en casa" con contadores en vivo + Píldoras de Herramientas de Cuidado
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Registrar en casa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Abrir bitácora",
                        style = MaterialTheme.typography.labelLarge,
                        color = PequeGreen,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onOpenTrackerWithFilter(0) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MinimalActionCard(
                        title = "Síntomas",
                        subtitle = if (homeSymptoms.isEmpty()) "Fiebre o malestar" else "${homeSymptoms.size} registrado(s)",
                        icon = Icons.Default.Thermostat,
                        accentColor = AlertRed,
                        testTag = "parent_action_symptom",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (activeChild == null) onAddChildClicked()
                            else showSymptomDialog = true
                        }
                    )
                    MinimalActionCard(
                        title = "Diario",
                        subtitle = if (homeDiary.isEmpty()) "Sueño y comida" else "${homeDiary.size} nota(s)",
                        icon = Icons.Default.Bedtime,
                        accentColor = PequeGreen,
                        testTag = "parent_action_diary",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (activeChild == null) onAddChildClicked()
                            else showBabyDiaryDialog = true
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MinimalActionCard(
                        title = "Mis Dudas",
                        subtitle = if (parentDoubts.isEmpty()) "Para el pediatra" else "${parentDoubts.size} anotada(s)",
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        accentColor = PequeBlue,
                        testTag = "parent_action_doubt",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (activeChild == null) onAddChildClicked()
                            else showParentDoubtDialog = true
                        }
                    )
                    MinimalActionCard(
                        title = "Estudios",
                        subtitle = if (parentStudies.isEmpty()) "Subir foto o lab" else "${parentStudies.size} adjunto(s)",
                        icon = Icons.Default.AddAPhoto,
                        accentColor = CardAiIcon,
                        testTag = "parent_action_study",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (activeChild == null) onAddChildClicked()
                            else showUploadStudyDialog = true
                        }
                    )
                }

                // Barra minimalista de 4 Herramientas Rápidas de Cuidado en Casa
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ParentQuickToolChip(
                        label = "Dosis Fiebre",
                        icon = Icons.Default.Calculate,
                        accentColor = PequeOrange,
                        onClick = {
                            if (activeChild == null) onAddChildClicked()
                            else showFeverDoseDialog = true
                        }
                    )
                    ParentQuickToolChip(
                        label = "Semáforo Urgencias",
                        icon = Icons.Default.Warning,
                        accentColor = AlertRed,
                        onClick = {
                            if (activeChild == null) onAddChildClicked()
                            else showAlarmTrafficLightDialog = true
                        }
                    )
                    ParentQuickToolChip(
                        label = "Hitos y Nutrición",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        accentColor = PequePurple,
                        onClick = {
                            if (activeChild == null) onAddChildClicked()
                            else showMilestonesDialog = true
                        }
                    )
                    ParentQuickToolChip(
                        label = "Próxima Cita",
                        icon = Icons.Default.CalendarMonth,
                        accentColor = PequeBlue,
                        onClick = {
                            if (activeChild == null) onAddChildClicked()
                            else showNextAppointmentDialog = true
                        }
                    )
                }
            }
        }

        // 5. Consultas Rápidas en Lista Ejecutiva (Vacunas, Crecimiento y Guía de Consulta)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    MinimalListRow(
                        title = "Vacunas y esquema PAI",
                        subtitle = if (pendingVaccines.isEmpty()) "${appliedVaccines.size} aplicada(s) · Esquema al día" else "${pendingVaccines.size} próxima(s) por aplicar",
                        icon = Icons.Default.Vaccines,
                        iconTint = PequeGreen,
                        onClick = { onOpenReadOnlySection(1) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    MinimalListRow(
                        title = "Crecimiento y certificados",
                        subtitle = activeChild?.let {
                            if (it.weightKg > 0.0 || it.heightCm > 0.0) "${it.weightKg} kg · ${it.heightCm} cm" else "Ver mediciones y constancias"
                        } ?: "Ver resumen",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconTint = PequeBlue,
                        onClick = { onOpenReadOnlySection(2) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    MinimalListRow(
                        title = "Guía y resumen para consulta",
                        subtitle = "Explicación sencilla de recetas, vacunas y dudas",
                        icon = Icons.Default.MedicalServices,
                        iconTint = CardAiIcon,
                        testTag = "parent_ai_assistant_card",
                        onClick = onOpenAiAssistant
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentKpiPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MinimalActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun MinimalListRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    testTag: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Pestaña 2 del Escritorio de Padres: Bitácora minimalista en Casa.
 */
@Composable
fun ParentHomeTrackerScreen(
    activeChild: ChildProfile?,
    allChildren: List<ChildProfile>,
    records: List<HealthRecord>,
    initialFilterIndex: Int = 0,
    onSelectChild: (String) -> Unit,
    onAddRecord: (HealthRecord) -> Unit,
    onDeleteRecord: (String) -> Unit,
    onAddChildClicked: () -> Unit = {}
) {
    var selectedFilter by remember(initialFilterIndex) { mutableIntStateOf(initialFilterIndex) }
    var showSymptomDialog by remember { mutableStateOf(false) }
    var showBabyDiaryDialog by remember { mutableStateOf(false) }
    var showParentDoubtDialog by remember { mutableStateOf(false) }
    var showUploadStudyDialog by remember { mutableStateOf(false) }

    val parentCategories = listOf("SINTOMA_CASA", "DIARIO_BEBE", "DUDA_PADRE", "ESTUDIO_PADRE")
    val childRecords = remember(records, activeChild, selectedFilter) {
        val base = records.filter {
            it.category in parentCategories &&
                (activeChild == null || it.childId == activeChild.id || it.childName == activeChild.name)
        }
        when (selectedFilter) {
            1 -> base.filter { it.category == "SINTOMA_CASA" }
            2 -> base.filter { it.category == "DIARIO_BEBE" }
            3 -> base.filter { it.category == "DUDA_PADRE" }
            4 -> base.filter { it.category == "ESTUDIO_PADRE" }
            else -> base
        }
    }

    if (showSymptomDialog && activeChild != null) {
        ParentAddSymptomDialog(
            child = activeChild,
            onDismiss = { showSymptomDialog = false },
            onSave = {
                onAddRecord(it)
                showSymptomDialog = false
            }
        )
    }
    if (showBabyDiaryDialog && activeChild != null) {
        ParentAddBabyDiaryDialog(
            child = activeChild,
            onDismiss = { showBabyDiaryDialog = false },
            onSave = {
                onAddRecord(it)
                showBabyDiaryDialog = false
            }
        )
    }
    if (showParentDoubtDialog && activeChild != null) {
        ParentAddDoubtDialog(
            child = activeChild,
            onDismiss = { showParentDoubtDialog = false },
            onSave = {
                onAddRecord(it)
                showParentDoubtDialog = false
            }
        )
    }
    if (showUploadStudyDialog && activeChild != null) {
        ParentUploadStudyDialog(
            child = activeChild,
            onDismiss = { showUploadStudyDialog = false },
            onSave = {
                onAddRecord(it)
                showUploadStudyDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bitácora en casa",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = activeChild?.name ?: "Registros familiares",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = {
                        if (activeChild == null) {
                            onAddChildClicked()
                        } else {
                            when (selectedFilter) {
                                2 -> showBabyDiaryDialog = true
                                3 -> showParentDoubtDialog = true
                                4 -> showUploadStudyDialog = true
                                else -> showSymptomDialog = true
                            }
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = PequeBlue),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (activeChild == null) "Registrar hijo/a" else "Nuevo", color = Color.White)
                }
            }
        }

        if (allChildren.size > 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allChildren.forEach { child ->
                        FilterChip(
                            selected = activeChild?.id == child.id,
                            onClick = { onSelectChild(child.id) },
                            label = { Text(child.name.substringBefore(" ")) }
                        )
                    }
                }
            }
        }

        // Filtros limpios
        item {
            val filters = listOf("Todo", "Síntomas", "Diario", "Dudas", "Estudios")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = selectedFilter == idx,
                        onClick = { selectedFilter = idx },
                        label = { Text(label) }
                    )
                }
            }
        }

        if (selectedFilter == 0 || selectedFilter == 1) {
            val symptomList = records.filter {
                it.category == "SINTOMA_CASA" &&
                    (activeChild == null || it.childId == activeChild.id || it.childName == activeChild.name)
            }
            if (symptomList.isNotEmpty()) {
                item {
                    FeverThermalCurveCard(
                        childName = activeChild?.name ?: "Paciente",
                        symptomRecords = symptomList
                    )
                }
            }
        }

        if (childRecords.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sin registros en esta categoría",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Toca \"+ Nuevo\" para agregar una nota para el pediatra.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            itemsIndexed(childRecords, key = { idx, rec -> "${rec.id.ifBlank { "home" }}_$idx" }) { _, rec ->
                val accentColor = when (rec.category) {
                    "SINTOMA_CASA" -> AlertRed
                    "DIARIO_BEBE" -> PequeGreen
                    "DUDA_PADRE" -> PequeBlue
                    else -> CardAiIcon
                }
                val parentNotePart = rec.notes.substringBefore("|").trim()
                val doctorReplyPart = if (rec.notes.contains("|")) rec.notes.substringAfter("|").trim() else null

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rec.dateText,
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { onDeleteRecord(rec.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rec.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (parentNotePart.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = parentNotePart,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (doctorReplyPart != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PequeGreen.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = doctorReplyPart,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Pestaña 3 del Escritorio de Padres: Salud en modo lectura minimalista (Recetas, Vacunas, Crecimiento y Certificados).
 */
@Composable
fun ParentSimpleReadOnlyScreen(
    activeChild: ChildProfile?,
    allChildren: List<ChildProfile>,
    records: List<HealthRecord>,
    initialSection: Int = 0,
    onSelectChild: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedSection by remember(initialSection) { mutableIntStateOf(initialSection) }
    val childRecords = remember(records, activeChild) {
        if (activeChild == null) emptyList()
        else records.filter { it.childId == activeChild.id || it.childName == activeChild.name }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Salud de ${activeChild?.name?.substringBefore(" ") ?: "tu hijo/a"}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Datos actualizados por tu pediatra",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (activeChild != null) {
                    Surface(
                        onClick = {
                            PediatricPdfAndShareHelper.generateAndShareFullDossierPdf(
                                context = context,
                                child = activeChild,
                                records = childRecords,
                                doctorName = activeChild.pediatrician
                            )
                        },
                        shape = RoundedCornerShape(50),
                        color = PequeBlue.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, PequeBlue.copy(alpha = 0.35f)),
                        modifier = Modifier.testTag("parent_export_full_pdf_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Exportar PDF",
                                tint = PequeBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Exportar PDF",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PequeBlue
                            )
                        }
                    }
                }
            }
        }

        if (allChildren.size > 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allChildren.forEach { child ->
                        FilterChip(
                            selected = activeChild?.id == child.id,
                            onClick = { onSelectChild(child.id) },
                            label = { Text(child.name.substringBefore(" ")) }
                        )
                    }
                }
            }
        }

        item {
            val tabs = listOf("Indicaciones y Recetas", "Vacunas", "Crecimiento", "Documentos")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = selectedSection == idx,
                        onClick = { selectedSection = idx },
                        label = { Text(label) }
                    )
                }
            }
        }

        when (selectedSection) {
            0 -> {
                val medsAndIndications = childRecords.filter {
                    it.category in listOf("MEDICAMENTO", "CONSULTA", "NUTRICION", "HISTORIAL") ||
                        (it.category in listOf("SINTOMA_CASA", "DUDA_PADRE", "DIARIO_BEBE", "ESTUDIO_PADRE") && it.notes.contains("|"))
                }
                if (medsAndIndications.isEmpty()) {
                    item {
                        MinimalReadOnlyCard(
                            title = "Resumen médico actual",
                            subtitle = activeChild?.lastCheckupSummary ?: "Sin indicaciones activas",
                            meta = activeChild?.name ?: "Pediatría",
                            notes = "Cuando tu pediatra registre una indicación, receta o respuesta a tus reportes, aparecerá aquí de inmediato.",
                            accentColor = PequeBlue
                        )
                    }
                } else {
                    itemsIndexed(medsAndIndications, key = { idx, rec -> "${rec.id.ifBlank { "med" }}_$idx" }) { _, rec ->
                        val isDoctorReply = rec.category in listOf("SINTOMA_CASA", "DUDA_PADRE", "DIARIO_BEBE", "ESTUDIO_PADRE")
                        MinimalReadOnlyCard(
                            title = if (isDoctorReply) "Respuesta a: ${rec.title}" else rec.title,
                            subtitle = rec.subtitle,
                            meta = "${rec.dateText} · ${rec.status}",
                            notes = if (isDoctorReply) rec.notes.substringAfter("|", rec.notes).trim() else rec.notes.replace("[PUSH]", "").trim(),
                            accentColor = if (isDoctorReply) PequeGreen else PequeBlue,
                            onSharePdf = {
                                PediatricPdfAndShareHelper.generateAndShareRecordPdf(
                                    context = context,
                                    child = activeChild,
                                    record = rec,
                                    doctorName = activeChild?.pediatrician ?: "Pediatría"
                                )
                            }
                        )
                    }
                }
            }

            1 -> {
                val vaccines = childRecords.filter { it.category == "VACUNA" }
                itemsIndexed(vaccines, key = { idx, vac -> "${vac.id.ifBlank { "vac" }}_$idx" }) { _, vac ->
                    val isApplied = vac.status.equals("Aplicada", ignoreCase = true)
                    MinimalReadOnlyCard(
                        title = vac.title,
                        subtitle = vac.subtitle,
                        meta = if (isApplied) "Al día (${vac.dateText})" else "Próxima (${vac.dateText})",
                        notes = vac.notes,
                        accentColor = if (isApplied) PequeGreen else PequeOrange,
                        onSharePdf = {
                            PediatricPdfAndShareHelper.generateAndShareRecordPdf(
                                context = context,
                                child = activeChild,
                                record = vac,
                                doctorName = activeChild?.pediatrician ?: "Pediatría"
                            )
                        }
                    )
                }
            }

            2 -> {
                item {
                    WhoGrowthChartCard(
                        activeChild = activeChild,
                        records = childRecords,
                        isPediatricianMode = false
                    )
                }
                if (activeChild != null) {
                    item {
                        TannerTargetHeightCalculatorCard(activeChild = activeChild)
                    }
                }
                val growthRecords = childRecords.filter { it.category == "CRECIMIENTO" || it.category == "NEURODESARROLLO" }
                itemsIndexed(growthRecords, key = { idx, rec -> "${rec.id.ifBlank { "grow" }}_$idx" }) { _, rec ->
                    MinimalReadOnlyCard(
                        title = rec.title,
                        subtitle = rec.subtitle,
                        meta = rec.dateText,
                        notes = rec.notes,
                        accentColor = PequeGreen
                    )
                }
            }

            3 -> {
                val docs = childRecords.filter {
                    it.category in listOf("CERTIFICADO", "DOCUMENTO", "CONSULTA", "CALENDARIO", "LABORATORIO")
                }
                itemsIndexed(docs, key = { idx, rec -> "${rec.id.ifBlank { "doc" }}_$idx" }) { _, rec ->
                    MinimalReadOnlyCard(
                        title = rec.title,
                        subtitle = rec.subtitle,
                        meta = "${rec.dateText} · ${rec.status}",
                        notes = rec.notes.replace("[PUSH]", "").trim(),
                        accentColor = PequeBlue,
                        onSharePdf = {
                            PediatricPdfAndShareHelper.generateAndShareRecordPdf(
                                context = context,
                                child = activeChild,
                                record = rec,
                                doctorName = activeChild?.pediatrician ?: "Pediatría"
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MinimalReadOnlyCard(
    title: String,
    subtitle: String,
    meta: String,
    notes: String,
    accentColor: Color,
    onSharePdf: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onSharePdf != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onSharePdf,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Exportar PDF",
                            tint = accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "PDF / Compartir",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParentAddSymptomDialog(
    child: ChildProfile,
    onDismiss: () -> Unit,
    onSave: (HealthRecord) -> Unit
) {
    var temperature by remember { mutableStateOf("") }
    val symptomOptions = listOf("Fiebre", "Tos", "Mocos", "Dolor de oído", "Dolor de garganta", "Vómito", "Diarrea", "Ronchas")
    var selectedSymptoms by remember { mutableStateOf(emptySet<String>()) }
    var homeNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Registrar síntoma (${child.name.substringBefore(" ")})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("36.5", "37.5", "38.0", "38.5", "39.0").forEach { preset ->
                        FilterChip(
                            selected = temperature == preset,
                            onClick = { temperature = preset },
                            label = { Text("$preset°C") }
                        )
                    }
                }
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("Temperatura (°C)") },
                    placeholder = { Text("Ej. 38.2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    symptomOptions.forEach { sym ->
                        val isSel = sym in selectedSymptoms
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                selectedSymptoms = if (isSel) selectedSymptoms - sym else selectedSymptoms + sym
                            },
                            label = { Text(sym) }
                        )
                    }
                }

                OutlinedTextField(
                    value = homeNotes,
                    onValueChange = { homeNotes = it },
                    label = { Text("Nota breve para el pediatra") },
                    placeholder = { Text("Describe qué observaste en casa...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanTemp = temperature.replace(",", ".").trim()
                    val tempVal = cleanTemp.toDoubleOrNull() ?: 36.8
                    val symText = if (selectedSymptoms.isEmpty()) "Malestar en casa" else selectedSymptoms.joinToString(", ")
                    val statusLabel = if (tempVal >= 38.0) "Alerta en casa" else "Observación"
                    val titleText = if (cleanTemp.isNotBlank()) "$symText ($cleanTemp °C)" else symText
                    onSave(
                        HealthRecord(
                            childId = child.id,
                            childName = child.name,
                            category = "SINTOMA_CASA",
                            title = titleText,
                            subtitle = "Registrado en casa",
                            dateText = "Hoy",
                            status = statusLabel,
                            notes = homeNotes.trim().ifBlank {
                                if (cleanTemp.isNotBlank()) "Temperatura: $cleanTemp °C. Síntomas: $symText."
                                else "Síntomas observados: $symText."
                            }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PequeBlue)
            ) {
                Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun ParentAddBabyDiaryDialog(
    child: ChildProfile,
    onDismiss: () -> Unit,
    onSave: (HealthRecord) -> Unit
) {
    var sleepHours by remember { mutableStateOf("") }
    var appetite by remember { mutableStateOf("Comió bien") }
    var mood by remember { mutableStateOf("Tranquilo") }
    var lactationAndDiaperSummary by remember { mutableStateOf("") }
    var extraNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Diario de hoy (${child.name.substringBefore(" ")})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Comió bien", "Regular", "Poco apetito", "Leche/Líquidos").forEach { opt ->
                        FilterChip(
                            selected = appetite == opt,
                            onClick = { appetite = opt },
                            label = { Text(opt) }
                        )
                    }
                }

                LactationTimerAndDiaperCounterSection(
                    onSummaryChanged = { lactationAndDiaperSummary = it }
                )

                OutlinedTextField(
                    value = sleepHours,
                    onValueChange = { sleepHours = it },
                    label = { Text("Horas de sueño") },
                    placeholder = { Text("Ej. 10") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = extraNotes,
                    onValueChange = { extraNotes = it },
                    label = { Text("Notas de alimentación, sueño o pañales") },
                    placeholder = { Text("Escribe cómo estuvo durante el día...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanSleep = sleepHours.trim()
                    val sleepLabel = if (cleanSleep.isNotBlank()) "Sueño: ${cleanSleep}h · $appetite" else "Alimentación: $appetite"
                    val baseNotes = extraNotes.trim().ifBlank {
                        if (cleanSleep.isNotBlank()) "Alimentación: $appetite. Sueño: $cleanSleep h."
                        else "Alimentación: $appetite."
                    }
                    val fullNotes = if (lactationAndDiaperSummary.isNotBlank()) {
                        "$lactationAndDiaperSummary · $baseNotes"
                    } else {
                        baseNotes
                    }
                    onSave(
                        HealthRecord(
                            childId = child.id,
                            childName = child.name,
                            category = "DIARIO_BEBE",
                            title = sleepLabel,
                            subtitle = if (lactationAndDiaperSummary.isNotBlank()) lactationAndDiaperSummary else "Ánimo: $mood",
                            dateText = "Hoy",
                            status = "Normal",
                            notes = fullNotes
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PequeGreen)
            ) {
                Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun ParentAddDoubtDialog(
    child: ChildProfile,
    onDismiss: () -> Unit,
    onSave: (HealthRecord) -> Unit
) {
    var questionTitle by remember { mutableStateOf("") }
    var questionDetail by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nueva duda para el pediatra",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = questionTitle,
                    onValueChange = { questionTitle = it },
                    label = { Text("Pregunta") },
                    placeholder = { Text("¿Qué deseas preguntarle al pediatra?") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = questionDetail,
                    onValueChange = { questionDetail = it },
                    label = { Text("Detalles (opcional)") },
                    placeholder = { Text("Contexto o detalles adicionales...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (questionTitle.isNotBlank()) {
                        onSave(
                            HealthRecord(
                                childId = child.id,
                                childName = child.name,
                                category = "DUDA_PADRE",
                                title = questionTitle.trim(),
                                subtitle = "Pregunta para consulta",
                                dateText = "Hoy",
                                status = "Pendiente",
                                notes = questionDetail.trim().ifBlank { "Anotada por los padres." }
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PequeBlue)
            ) {
                Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun ParentUploadStudyDialog(
    child: ChildProfile,
    onDismiss: () -> Unit,
    onSave: (HealthRecord) -> Unit
) {
    var studyTitle by remember { mutableStateOf("") }
    var studyNotes by remember { mutableStateOf("") }
    var selectedImageUriText by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUriText = uri.lastPathSegment ?: "Imagen_Adjunta.jpg"
            if (studyTitle.isBlank()) {
                studyTitle = "Foto clínica / Resultado de estudio"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Adjuntar estudio o foto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = PequeBlue.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, PequeBlue.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("parent_photo_picker_button")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Seleccionar foto",
                            tint = PequeBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedImageUriText != null) {
                                    "Foto seleccionada: $selectedImageUriText"
                                } else {
                                    "Seleccionar foto o imagen de galería"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = PequeBlue
                            )
                            Text(
                                text = "Selector seguro de Android sin permisos invasivos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = studyTitle,
                    onValueChange = { studyTitle = it },
                    label = { Text("Nombre del estudio o foto") },
                    placeholder = { Text("Ej. Hemograma, Radiografía, Foto...") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = studyNotes,
                    onValueChange = { studyNotes = it },
                    label = { Text("Observación o resultados") },
                    placeholder = { Text("Anota los resultados o comentarios...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (studyTitle.isNotBlank() || selectedImageUriText != null) {
                        val attachInfo = if (selectedImageUriText != null) {
                            "[Imagen adjunta: $selectedImageUriText] "
                        } else {
                            ""
                        }
                        onSave(
                            HealthRecord(
                                childId = child.id,
                                childName = child.name,
                                category = "ESTUDIO_PADRE",
                                title = studyTitle.trim().ifBlank { "Foto / Estudio adjunto" },
                                subtitle = if (selectedImageUriText != null) "Imagen adjunta · Subido por padres" else "Subido por los padres",
                                dateText = "Hoy",
                                status = "Adjunto",
                                notes = "$attachInfo${studyNotes.trim().ifBlank { "Enviado desde casa." }}"
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PequeBlue)
            ) {
                Text("Subir", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ParentQuickToolChip(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Calculadora Rápida de Fiebre y Dosis Segura por Peso para Padres.
 */
@Composable
fun ParentFeverDoseCalculatorDialog(
    child: ChildProfile,
    onDismiss: () -> Unit,
    onLogDoseRecord: (HealthRecord) -> Unit
) {
    val antipyreticOptions = remember {
        pediatricDrugCatalog.filter {
            it.name.contains("Paracetamol", ignoreCase = true) ||
                it.name.contains("Ibuprofeno", ignoreCase = true)
        }
    }
    var selectedDrug by remember { mutableStateOf(antipyreticOptions.first()) }
    var weightText by remember(child) {
        mutableStateOf(if (child.weightKg > 0.0) child.weightKg.toString() else "")
    }

    val weightKg = weightText.replace(",", ".").toDoubleOrNull() ?: child.weightKg
    val totalMg = weightKg * selectedDrug.defaultMgPerKgPerDose
    val exactMl = if (selectedDrug.mgPerMl > 0) totalMg / selectedDrug.mgPerMl else 0.0
    val exactDrops = exactMl * 20.0
    val formattedMl = String.format(Locale.US, "%.1f", exactMl)
    val formattedDrops = String.format(Locale.US, "%.0f", exactDrops)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Guía de Dosis para Fiebre (${child.name.substringBefore(" ")})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Cálculo orientativo basado en el peso actual del niño/a. Confirma siempre la concentración del frasco.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Peso actual (kg)") },
                    placeholder = { Text("Ej. 14.5") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    antipyreticOptions.forEach { drug ->
                        FilterChip(
                            selected = selectedDrug.id == drug.id,
                            onClick = { selectedDrug = drug },
                            label = { Text("${drug.name} (${drug.presentationLabel})") }
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PequeOrange.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, PequeOrange.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (weightKg <= 0.0) {
                                "Ingresa el peso en kg arriba para calcular la dosis"
                            } else if (selectedDrug.id.contains("gotas")) {
                                "Dosis: $formattedMl mL ($formattedDrops gotas) cada ${selectedDrug.defaultEveryHours} horas"
                            } else {
                                "Dosis: $formattedMl mL cada ${selectedDrug.defaultEveryHours} horas"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = PequeOrange
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedDrug.parentFriendlyTip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (weightKg > 0.0) {
                        onLogDoseRecord(
                            HealthRecord(
                                childId = child.id,
                                childName = child.name,
                                category = "DIARIO_BEBE",
                                title = "Toma antipirético: ${selectedDrug.name}",
                                subtitle = "$formattedMl mL (${selectedDrug.presentationLabel})",
                                dateText = "Hoy",
                                status = "Administrada",
                                notes = "Administrado en casa para control térmico ($formattedMl mL para $weightKg kg)."
                            )
                        )
                    } else {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PequeOrange)
            ) {
                Text("Registrar Toma", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

/**
 * Semáforo Pediátrico de Signos de Alarma y Urgencias en Casa.
 */
@Composable
fun ParentAlarmTrafficLightDialog(
    child: ChildProfile,
    onDismiss: () -> Unit,
    onReportUrgentSymptom: (HealthRecord) -> Unit
) {
    var selectedAlarm by remember { mutableStateOf("") }
    val redFlags = listOf(
        "Fiebre > 38.0 °C en menor de 3 meses",
        "Hundimiento de costillas o respiración rápida",
        "Decaimiento extremo o irritabilidad inconsolable",
        "Vómitos persistentes o boca seca sin lágrimas",
        "Manchas rojas/violáceas en piel que no desaparecen"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Semáforo de Urgencias Pediátricas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PequeGreen.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, PequeGreen.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("VERDE · Vigilancia en casa", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = PequeGreen)
                        Text("Fiebre < 38.5 °C que baja con antipirético, niño activo que juega y toma líquidos.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PequeOrange.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, PequeOrange.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("ÁMBAR · Consultar al pediatra hoy", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = PequeOrange)
                        Text("Fiebre > 48 horas, dolor de oído, rechazo parcial de alimentos o diarrea moderada.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AlertRed.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("ROJO · Acudir a Urgencias / Avisar al Pediatra", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = AlertRed)
                        Text("Selecciona si observas alguno de estos signos para enviar alerta inmediata al escritorio del pediatra:", style = MaterialTheme.typography.bodySmall)
                        redFlags.forEach { flag ->
                            FilterChip(
                                selected = selectedAlarm == flag,
                                onClick = { selectedAlarm = flag },
                                label = { Text(flag, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedAlarm.isNotBlank()) {
                Button(
                    onClick = {
                        onReportUrgentSymptom(
                            HealthRecord(
                                childId = child.id,
                                childName = child.name,
                                category = "SINTOMA_CASA",
                                title = "ALERTA: $selectedAlarm",
                                subtitle = "Semáforo Rojo en Casa",
                                dateText = "Hoy",
                                status = "Alerta en casa",
                                notes = "Signo de alarma reportado por los padres desde el Semáforo de Urgencias: $selectedAlarm."
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("Notificar Alerta al Pediatra", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

/**
 * Registro de Hitos del Desarrollo y Alimentación desde Casa.
 */
@Composable
fun ParentDevelopmentalMilestonesDialog(
    child: ChildProfile,
    onDismiss: () -> Unit,
    onSaveMilestoneRecord: (HealthRecord) -> Unit
) {
    val milestoneOptions = listOf(
        "Sostén cefálico y sonrisa social",
        "Se sienta sin apoyo y gira",
        "Gateo o primeros pasos",
        "Dice primeras palabras / frases",
        "Control de esfínteres diurno",
        "Buena aceptación de sólidos y verduras"
    )
    var selectedMilestone by remember { mutableStateOf(milestoneOptions.first()) }
    var parentObservation by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Hitos y Nutrición (${child.name.substringBefore(" ")})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Comparte los avances del desarrollo o alimentación de tu hijo/a para su expediente clínico.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                milestoneOptions.forEach { item ->
                    FilterChip(
                        selected = selectedMilestone == item,
                        onClick = { selectedMilestone = item },
                        label = { Text(item) }
                    )
                }
                OutlinedTextField(
                    value = parentObservation,
                    onValueChange = { parentObservation = it },
                    label = { Text("Observación de mamá / papá") },
                    placeholder = { Text("Ej. Ya dice 10 palabras nuevas y come frutas...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveMilestoneRecord(
                        HealthRecord(
                            childId = child.id,
                            childName = child.name,
                            category = "NEURODESARROLLO",
                            title = selectedMilestone,
                            subtitle = "Logro reportado en casa",
                            dateText = "Hoy",
                            status = "Acorde a edad",
                            notes = parentObservation.trim().ifBlank { "Hito confirmado por los padres en casa." }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PequePurple)
            ) {
                Text("Guardar en Expediente", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/**
 * Solicitud o Registro de Próxima Cita / Control de Niño Sano.
 */
@Composable
fun ParentScheduleAppointmentDialog(
    child: ChildProfile,
    onDismiss: () -> Unit,
    onSaveAppointment: (HealthRecord) -> Unit
) {
    val reasonPresets = listOf(
        "Control de niño sano y peso/talla",
        "Aplicación de vacuna pendiente",
        "Revisión de estudios de laboratorio",
        "Seguimiento de síntomas en casa"
    )
    var selectedReason by remember { mutableStateOf(reasonPresets.first()) }
    var preferredDateText by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Agendar Próxima Consulta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                reasonPresets.forEach { reason ->
                    FilterChip(
                        selected = selectedReason == reason,
                        onClick = { selectedReason = reason },
                        label = { Text(reason) }
                    )
                }
                OutlinedTextField(
                    value = preferredDateText,
                    onValueChange = { preferredDateText = it },
                    label = { Text("Fecha u horario deseado") },
                    placeholder = { Text("Ej. Viernes 15 Nov · 16:00 hrs") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Nota para el consultorio") },
                    placeholder = { Text("Comentarios adicionales...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveAppointment(
                        HealthRecord(
                            childId = child.id,
                            childName = child.name,
                            category = "CALENDARIO",
                            title = selectedReason,
                            subtitle = "Cita con ${child.pediatrician}",
                            dateText = preferredDateText.trim().ifBlank { "Próxima cita" },
                            status = "Programada",
                            notes = notesInput.trim().ifBlank { "Cita agendada desde el Escritorio de Padres." }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PequeBlue)
            ) {
                Text("Guardar Cita", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
