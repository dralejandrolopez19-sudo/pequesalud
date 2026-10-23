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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ChildProfile
import com.example.data.HealthRecord
import com.example.ui.components.ChildAvatar
import com.example.ui.components.MedicalQrCardDialog
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CardAiIcon
import com.example.ui.theme.PequeBlue
import com.example.ui.theme.PequeGreen
import com.example.ui.theme.PequeOrange
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
 * ESCRITORIO DE PADRES — Diseño minimalista, limpio y sin ruido visual.
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
    onOpenPinSettings: () -> Unit = {}
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
    val pendingVaccines = childRecords.filter { it.category == "VACUNA" && it.status.equals("Pendiente", ignoreCase = true) }
    val answeredDoubts = childRecords.filter {
        it.category == "DUDA_PADRE" && (it.status.contains("Respondida", ignoreCase = true) || it.notes.contains("Respuesta", ignoreCase = true))
    }

    var showSymptomDialog by remember { mutableStateOf(false) }
    var showBabyDiaryDialog by remember { mutableStateOf(false) }
    var showParentDoubtDialog by remember { mutableStateOf(false) }
    var showUploadStudyDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }

    if (showQrDialog && activeChild != null) {
        MedicalQrCardDialog(
            child = activeChild,
            onDismiss = { showQrDialog = false }
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Cabecera Minimalista Integrada (Saludo + Chip discreto Modo Pediatra + QR)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hola, $formattedParentName",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Escritorio de Padres",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

        // 2. Selector limpio de hijos + línea de resumen sutil
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            color = if (selected) PequeBlue else MaterialTheme.colorScheme.surface,
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
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { onAddChildClicked() }
                            .testTag("parent_add_child_button")
                    ) {
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

                if (activeChild != null) {
                    val hasAllergy = !activeChild.allergies.equals("Ninguna", ignoreCase = true) &&
                        !activeChild.allergies.contains("Ninguna", ignoreCase = true)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${activeChild.ageYears} años · ${activeChild.weightKg} kg · ${activeChild.heightCm} cm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (hasAllergy) {
                            Text(
                                text = "· Alergia: ${activeChild.allergies}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AlertRed
                            )
                        }
                    }
                }
            }
        }

        // 3. Cuadrícula Minimalista 2x2: "Registrar en casa" (Tarjetas blancas limpias)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Registrar en casa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MinimalActionCard(
                        title = "Síntomas",
                        subtitle = "Fiebre o malestar",
                        icon = Icons.Default.Thermostat,
                        accentColor = AlertRed,
                        testTag = "parent_action_symptom",
                        modifier = Modifier.weight(1f),
                        onClick = { showSymptomDialog = true }
                    )
                    MinimalActionCard(
                        title = "Diario",
                        subtitle = "Sueño y comida",
                        icon = Icons.Default.Bedtime,
                        accentColor = PequeGreen,
                        testTag = "parent_action_diary",
                        modifier = Modifier.weight(1f),
                        onClick = { showBabyDiaryDialog = true }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MinimalActionCard(
                        title = "Mis Dudas",
                        subtitle = "Para el pediatra",
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        accentColor = PequeBlue,
                        testTag = "parent_action_doubt",
                        modifier = Modifier.weight(1f),
                        onClick = { showParentDoubtDialog = true }
                    )
                    MinimalActionCard(
                        title = "Estudios",
                        subtitle = "Subir foto o lab",
                        icon = Icons.Default.AddAPhoto,
                        accentColor = CardAiIcon,
                        testTag = "parent_action_study",
                        modifier = Modifier.weight(1f),
                        onClick = { showUploadStudyDialog = true }
                    )
                }
            }
        }

        // 4. Tarjeta Única y Limpia: "Indicaciones del Pediatra"
        if (activeChild != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Indicaciones de tu pediatra",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Ver todo",
                            style = MaterialTheme.typography.labelLarge,
                            color = PequeBlue,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onOpenReadOnlySection(0) }
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (activeMeds.isEmpty() && answeredDoubts.isEmpty()) {
                                Text(
                                    text = "Sin tratamientos activos hoy · ${activeChild.lastCheckupSummary}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                activeMeds.take(2).forEachIndexed { idx, med ->
                                    if (idx > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(PequeBlue.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Medication,
                                                contentDescription = null,
                                                tint = PequeBlue,
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
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                if (answeredDoubts.isNotEmpty()) {
                                    val latest = answeredDoubts.first()
                                    if (activeMeds.isNotEmpty()) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.clickable { onOpenTrackerWithFilter(3) }
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
                                                text = "Respuesta a tu duda",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = PequeGreen
                                            )
                                            Text(
                                                text = latest.notes.substringAfter("|", latest.notes).trim(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
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

        // 5. Consultas Rápidas en Lista Minimalista (Vacunas, Crecimiento y Guía de Consulta)
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
                        title = "Vacunas",
                        subtitle = if (pendingVaccines.isEmpty()) "Esquema al día" else "${pendingVaccines.size} próxima(s)",
                        icon = Icons.Default.Vaccines,
                        iconTint = PequeGreen,
                        onClick = { onOpenReadOnlySection(1) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    MinimalListRow(
                        title = "Crecimiento y certificados",
                        subtitle = activeChild?.let { "${it.weightKg} kg · ${it.heightCm} cm" } ?: "Ver resumen",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconTint = PequeBlue,
                        onClick = { onOpenReadOnlySection(2) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    MinimalListRow(
                        title = "Guía y resumen para consulta",
                        subtitle = "Preparar próxima visita y resolver dudas",
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
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
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
            Spacer(modifier = Modifier.width(12.dp))
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
    onDeleteRecord: (String) -> Unit
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
                        when (selectedFilter) {
                            2 -> showBabyDiaryDialog = true
                            3 -> showParentDoubtDialog = true
                            4 -> showUploadStudyDialog = true
                            else -> showSymptomDialog = true
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = PequeBlue),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo", color = Color.White)
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
            Column {
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
        }

        item {
            val tabs = listOf("Recetas", "Vacunas", "Crecimiento", "Documentos")
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
                val meds = childRecords.filter { it.category == "MEDICAMENTO" || it.category == "NUTRICION" }
                itemsIndexed(meds, key = { idx, rec -> "${rec.id.ifBlank { "med" }}_$idx" }) { _, rec ->
                    MinimalReadOnlyCard(
                        title = rec.title,
                        subtitle = rec.subtitle,
                        meta = "${rec.dateText} · ${rec.status}",
                        notes = rec.notes,
                        accentColor = PequeBlue
                    )
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
                        notes = "",
                        accentColor = if (isApplied) PequeGreen else PequeOrange
                    )
                }
            }

            2 -> {
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
                    it.category in listOf("CERTIFICADO", "DOCUMENTO", "CONSULTA", "CALENDARIO")
                }
                itemsIndexed(docs, key = { idx, rec -> "${rec.id.ifBlank { "doc" }}_$idx" }) { _, rec ->
                    MinimalReadOnlyCard(
                        title = rec.title,
                        subtitle = rec.subtitle,
                        meta = rec.dateText,
                        notes = rec.notes,
                        accentColor = PequeBlue
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
    accentColor: Color
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
        }
    }
}

@Composable
fun ParentAddSymptomDialog(
    child: ChildProfile,
    onDismiss: () -> Unit,
    onSave: (HealthRecord) -> Unit
) {
    var temperature by remember { mutableStateOf("37.8") }
    val symptomOptions = listOf("Fiebre", "Tos", "Mocos", "Dolor de oído", "Dolor de garganta", "Vómito", "Ronchas")
    var selectedSymptoms by remember { mutableStateOf(setOf("Fiebre")) }
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
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("36.6", "37.5", "38.2", "39.0").forEach { preset ->
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tempVal = temperature.toDoubleOrNull() ?: 37.0
                    val symText = if (selectedSymptoms.isEmpty()) "Malestar" else selectedSymptoms.joinToString(", ")
                    val statusLabel = if (tempVal >= 38.0) "Alerta en casa" else "Observación"
                    onSave(
                        HealthRecord(
                            childId = child.id,
                            childName = child.name,
                            category = "SINTOMA_CASA",
                            title = "$symText ($temperature °C)",
                            subtitle = "Registrado en casa",
                            dateText = "Hoy",
                            status = statusLabel,
                            notes = homeNotes.ifBlank { "Temperatura: $temperature °C. Síntomas: $symText." }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PequeBlue)
            ) {
                Text("Guardar", color = Color.White)
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
    var sleepHours by remember { mutableStateOf("10") }
    var appetite by remember { mutableStateOf("Comió bien") }
    var mood by remember { mutableStateOf("Tranquilo") }
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

                OutlinedTextField(
                    value = sleepHours,
                    onValueChange = { sleepHours = it },
                    label = { Text("Horas de sueño") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = extraNotes,
                    onValueChange = { extraNotes = it },
                    label = { Text("Notas opcionales") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        HealthRecord(
                            childId = child.id,
                            childName = child.name,
                            category = "DIARIO_BEBE",
                            title = "Sueño: ${sleepHours}h · $appetite",
                            subtitle = "Ánimo: $mood",
                            dateText = "Hoy",
                            status = "Normal",
                            notes = extraNotes.ifBlank { "Alimentación: $appetite. Sueño: $sleepHours h." }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PequeGreen)
            ) {
                Text("Guardar", color = Color.White)
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
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = questionDetail,
                    onValueChange = { questionDetail = it },
                    label = { Text("Detalles (opcional)") },
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
                Text("Guardar", color = Color.White)
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
                OutlinedTextField(
                    value = studyTitle,
                    onValueChange = { studyTitle = it },
                    label = { Text("Nombre del estudio o foto") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = studyNotes,
                    onValueChange = { studyNotes = it },
                    label = { Text("Observación") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (studyTitle.isNotBlank()) {
                        onSave(
                            HealthRecord(
                                childId = child.id,
                                childName = child.name,
                                category = "ESTUDIO_PADRE",
                                title = studyTitle.trim(),
                                subtitle = "Subido por los padres",
                                dateText = "Hoy",
                                status = "Adjunto",
                                notes = studyNotes.trim().ifBlank { "Enviado desde casa." }
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PequeBlue)
            ) {
                Text("Subir", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
