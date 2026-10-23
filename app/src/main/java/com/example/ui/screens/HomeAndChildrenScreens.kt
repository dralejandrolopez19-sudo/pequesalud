package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChildProfile
import com.example.data.ClinicalModule
import com.example.data.HealthRecord
import com.example.ui.components.ChildAvatar
import com.example.ui.components.MedicalQrCardDialog
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CardAiBg
import com.example.ui.theme.CardAiIcon
import com.example.ui.theme.CardCalendarioBg
import com.example.ui.theme.CardCalendarioIcon
import com.example.ui.theme.CardConsultasBg
import com.example.ui.theme.CardConsultasIcon
import com.example.ui.theme.CardCrecimientoBg
import com.example.ui.theme.CardCrecimientoIcon
import com.example.ui.theme.CardDocumentosBg
import com.example.ui.theme.CardDocumentosIcon
import com.example.ui.theme.CardHistorialBg
import com.example.ui.theme.CardHistorialIcon
import com.example.ui.theme.CardHospitalBg
import com.example.ui.theme.CardHospitalIcon
import com.example.ui.theme.CardLaboratoriosBg
import com.example.ui.theme.CardLaboratoriosIcon
import com.example.ui.theme.CardMedicamentosBg
import com.example.ui.theme.CardMedicamentosIcon
import com.example.ui.theme.CardNutricionBg
import com.example.ui.theme.CardNutricionIcon
import com.example.ui.theme.CardVacunasBg
import com.example.ui.theme.CardVacunasIcon
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.PequeBlue
import com.example.ui.theme.PequeGreen
import com.example.ui.theme.PequeLightBlue
import com.example.ui.theme.adaptiveCardTint

data class QuickAccessItem(
    val module: ClinicalModule,
    val label: String,
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color
)

val mainGridModules = listOf(
    QuickAccessItem(
        module = ClinicalModule.HISTORIAL,
        label = "Historial\nclínico",
        icon = Icons.Default.Description,
        bgColor = CardHistorialBg,
        iconColor = CardHistorialIcon
    ),
    QuickAccessItem(
        module = ClinicalModule.VACUNA,
        label = "Vacunas",
        icon = Icons.Default.Vaccines,
        bgColor = CardVacunasBg,
        iconColor = CardVacunasIcon
    ),
    QuickAccessItem(
        module = ClinicalModule.CRECIMIENTO,
        label = "Crecimiento\ny desarrollo",
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        bgColor = CardCrecimientoBg,
        iconColor = CardCrecimientoIcon
    ),
    QuickAccessItem(
        module = ClinicalModule.MEDICAMENTO,
        label = "Medicamentos",
        icon = Icons.Default.Medication,
        bgColor = CardMedicamentosBg,
        iconColor = CardMedicamentosIcon
    ),
    QuickAccessItem(
        module = ClinicalModule.LABORATORIO,
        label = "Laboratorios",
        icon = Icons.Default.Science,
        bgColor = CardLaboratoriosBg,
        iconColor = CardLaboratoriosIcon
    ),
    QuickAccessItem(
        module = ClinicalModule.CONSULTA,
        label = "Consultas",
        icon = Icons.Default.Groups,
        bgColor = CardConsultasBg,
        iconColor = CardConsultasIcon
    ),
    QuickAccessItem(
        module = ClinicalModule.NUTRICION,
        label = "Nutrición",
        icon = Icons.Default.Eco,
        bgColor = CardNutricionBg,
        iconColor = CardNutricionIcon
    ),
    QuickAccessItem(
        module = ClinicalModule.DOCUMENTO,
        label = "Documentos",
        icon = Icons.Default.FolderCopy,
        bgColor = CardDocumentosBg,
        iconColor = CardDocumentosIcon
    ),
    QuickAccessItem(
        module = ClinicalModule.CALENDARIO,
        label = "Calendario",
        icon = Icons.Default.CalendarMonth,
        bgColor = CardCalendarioBg,
        iconColor = CardCalendarioIcon
    )
)

@Composable
fun HomeDashboardScreen(
    parentDisplayName: String,
    children: List<ChildProfile>,
    selectedChildId: String,
    records: List<HealthRecord>,
    onSelectChild: (String) -> Unit,
    onOpenChildProfile: (ChildProfile) -> Unit,
    onAddChildClicked: () -> Unit,
    onOpenModule: (ClinicalModule) -> Unit,
    onOpenAiAssistant: () -> Unit
) {
    val activeChild = children.find { it.id == selectedChildId } ?: children.firstOrNull()
    val pendingVaccines = records.count {
        it.category == "VACUNA" && it.status.equals("Pendiente", ignoreCase = true) &&
            (activeChild == null || it.childId == activeChild.id)
    }
    val isDark = LocalIsDarkTheme.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header: "Hola, Alejandro" / "Familia López" + Avatar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hola, $parentDisplayName",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Familia López",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                ChildAvatar(
                    name = parentDisplayName,
                    gender = "Masculino",
                    colorHex = "#DCEBFE",
                    size = 48.dp
                )
            }
        }

        // Selector horizontal de hijos (Sofía, Mateo, Valentina, + Agregar hijo)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        children.forEach { child ->
                            val isSelected = child.id == activeChild?.id
                            val shortName = child.name.substringBefore(" ")
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .width(86.dp)
                                    .clickable {
                                        if (isSelected) {
                                            onOpenChildProfile(child)
                                        } else {
                                            onSelectChild(child.id)
                                        }
                                    }
                                    .testTag("child_selector_${child.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    ChildAvatar(
                                        name = child.name,
                                        gender = child.gender,
                                        colorHex = child.avatarColorHex,
                                        size = 48.dp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = shortName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${child.ageYears} años",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Botón "+ Agregar hijo"
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .width(86.dp)
                                .clickable { onAddChildClicked() }
                                .testTag("add_child_card_button")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Agregar hijo",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Agregar hijo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    if (activeChild != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onOpenChildProfile(activeChild) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Viendo a: ${activeChild.name} (${activeChild.bloodType} • Alergia: ${activeChild.allergies.ifBlank { "Ninguna" }})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Ver perfil",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Grid 3x3 de Accesos Rápidos (adaptativo para modo claro y oscuro)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                mainGridModules.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = adaptiveCardTint(item.bgColor, item.iconColor),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(106.dp)
                                    .clickable { onOpenModule(item.module) }
                                    .testTag("module_card_${item.module.categoryKey}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isDark) Color.White.copy(alpha = 0.14f)
                                                else Color.White.copy(alpha = 0.85f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.module.title,
                                            tint = if (isDark) Color.White else item.iconColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Accesos directos a Hospitalizaciones y Resumen de consulta
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = adaptiveCardTint(CardHospitalBg, CardHospitalIcon),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenModule(ClinicalModule.HOSPITALIZACION) }
                        .testTag("module_card_HOSPITALIZACION")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isDark) Color.White.copy(alpha = 0.14f) else Color.White
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = "Hospitalizaciones",
                                tint = if (isDark) Color.White else CardHospitalIcon
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Hospitalizaciones",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "y emergencias",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = adaptiveCardTint(CardAiBg, CardAiIcon),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenAiAssistant() }
                        .testTag("module_card_RESUMEN_CONSULTA")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isDark) Color.White.copy(alpha = 0.14f) else Color.White
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Resumen de consulta",
                                tint = if (isDark) Color.White else CardAiIcon
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Resumen de consulta",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Dudas e historial",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Tarjeta de resumen: Último control y Alertas de Vacunas
        if (activeChild != null) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenChildProfile(activeChild) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Último control de ${activeChild.name.substringBefore(" ")}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${activeChild.lastCheckupDate} • ${activeChild.lastCheckupSummary}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (pendingVaccines > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• $pendingVaccines vacuna(s) pendiente(s) en calendario",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChildProfileScreen(
    child: ChildProfile,
    allChildren: List<ChildProfile>,
    records: List<HealthRecord>,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onSelectChild: (String) -> Unit,
    onAddChildClicked: () -> Unit,
    onEditChild: (ChildProfile) -> Unit,
    onOpenVaccines: () -> Unit
) {
    if (showBackButton) {
        BackHandler { onBack() }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showQrModal by remember { mutableStateOf(false) }
    var showEditModal by remember { mutableStateOf(false) }

    val childRecords = records.filter { it.childId == child.id || it.childName == child.name }

    if (showQrModal) {
        MedicalQrCardDialog(
            child = child,
            onDismiss = { showQrModal = false }
        )
    }

    if (showEditModal) {
        AddOrEditChildDialog(
            initialChild = child,
            onDismiss = { showEditModal = false },
            onConfirm = { updated ->
                onEditChild(updated)
                showEditModal = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top App Bar: < | Perfil del niño | QR Card icon
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButton) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Text(
                    text = "Perfil del niño",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { showQrModal = true },
                    modifier = Modifier.testTag("qr_medical_card_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "Tarjeta médica QR",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Selector rápido entre hermanos cuando estamos en la pestaña "Mis hijos"
        if (!showBackButton && allChildren.size > 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allChildren.forEach { sibling ->
                        FilterChip(
                            selected = sibling.id == child.id,
                            onClick = { onSelectChild(sibling.id) },
                            label = { Text(sibling.name.substringBefore(" ")) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PequeBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    FilterChip(
                        selected = false,
                        onClick = onAddChildClicked,
                        label = { Text("+ Agregar hijo") }
                    )
                }
            }
        }

        // Foto circular grande + Nombre + Edad/Sexo + Botón "Editar perfil"
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ChildAvatar(
                    name = child.name,
                    gender = child.gender,
                    colorHex = child.avatarColorHex,
                    size = 96.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = child.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${child.ageYears} años • ${child.gender}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showEditModal = true },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("edit_child_profile_button")
                ) {
                    Text(
                        text = "Editar perfil",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4 Tarjetas de datos clínicos clave
        item {
            ProfileDataFieldCard(
                icon = Icons.Default.WaterDrop,
                iconTint = AlertRed,
                iconBg = adaptiveCardTint(Color(0xFFFEECEB), AlertRed),
                label = "Grupo sanguíneo",
                value = child.bloodType
            )
        }
        item {
            ProfileDataFieldCard(
                icon = Icons.Default.Warning,
                iconTint = AlertRed,
                iconBg = adaptiveCardTint(Color(0xFFFEECEB), AlertRed),
                label = "Alergias",
                value = child.allergies.ifBlank { "Ninguna registrada" }
            )
        }
        item {
            ProfileDataFieldCard(
                icon = Icons.Default.Person,
                iconTint = PequeBlue,
                iconBg = adaptiveCardTint(Color(0xFFE3F2FD), PequeBlue),
                label = "Pediatra",
                value = child.pediatrician
            )
        }
        item {
            ProfileDataFieldCard(
                icon = Icons.Default.Phone,
                iconTint = PequeGreen,
                iconBg = adaptiveCardTint(Color(0xFFE0F6EE), PequeGreen),
                label = "Contacto de emergencia",
                value = child.emergencyContact
            )
        }

        // Pestañas (Resumen | Crecimiento | Historial)
        item {
            val tabs = listOf("Resumen", "Crecimiento", "Historial")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { idx, title ->
                    val active = selectedTab == idx
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        border = if (active) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = idx }
                            .testTag("profile_tab_$idx")
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenVaccines() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Último control",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = child.lastCheckupDate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = child.lastCheckupSummary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showQrModal = true },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tarjeta QR")
                        }
                        Button(
                            onClick = onOpenVaccines,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PequeBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Vaccines, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ver vacunas", color = Color.White)
                        }
                    }
                }
            }

            1 -> {
                item {
                    GrowthSummaryCard(child = child)
                }
            }

            2 -> {
                val timeline = childRecords.filter {
                    it.category == "HISTORIAL" || it.category == "CONSULTA" || it.category == "HOSPITALIZACION"
                }
                if (timeline.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Aún no hay consultas en el historial clínico de ${child.name}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(timeline, key = { it.id }) { rec ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = rec.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = rec.dateText,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = rec.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (rec.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = rec.notes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
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

@Composable
private fun ProfileDataFieldCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    label: String,
    value: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GrowthSummaryCard(child: ChildProfile) {
    val heightM = (child.heightCm / 100.0).coerceAtLeast(0.5)
    val bmi = child.weightKg / (heightM * heightM)
    val bmiFormatted = String.format(java.util.Locale.US, "%.1f", bmi)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Antropometría y Percentiles OMS",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricPill(
                    label = "Peso",
                    value = "${child.weightKg} kg",
                    bgColor = adaptiveCardTint(Color(0xFFE3F2FD), PequeBlue),
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "Talla",
                    value = "${child.heightCm} cm",
                    bgColor = adaptiveCardTint(Color(0xFFE0F6EE), PequeGreen),
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "IMC",
                    value = "$bmiFormatted kg/m²",
                    bgColor = adaptiveCardTint(Color(0xFFEDE8FD), CardVacunasIcon),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Curva de crecimiento (Percentil 60-65 • Eutrófico)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            ) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(0f, h * 0.85f)
                    cubicTo(w * 0.3f, h * 0.65f, w * 0.6f, h * 0.40f, w, h * 0.18f)
                }
                drawPath(
                    path = path,
                    color = PequeGreen,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
                listOf(
                    Offset(0f, h * 0.85f),
                    Offset(w * 0.33f, h * 0.62f),
                    Offset(w * 0.66f, h * 0.39f),
                    Offset(w, h * 0.18f)
                ).forEach { pt ->
                    drawCircle(color = PequeBlue, radius = 8f, center = pt)
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AddOrEditChildDialog(
    initialChild: ChildProfile? = null,
    onDismiss: () -> Unit,
    onConfirm: (ChildProfile) -> Unit
) {
    var name by remember { mutableStateOf(initialChild?.name ?: "") }
    var ageYears by remember { mutableStateOf(initialChild?.ageYears?.toString() ?: "4") }
    var birthDate by remember { mutableStateOf(initialChild?.birthDate ?: "15/05/2022") }
    var gender by remember { mutableStateOf(initialChild?.gender ?: "Femenino") }
    var bloodType by remember { mutableStateOf(initialChild?.bloodType ?: "O+") }
    var allergies by remember { mutableStateOf(initialChild?.allergies ?: "Ninguna") }
    var pediatrician by remember { mutableStateOf(initialChild?.pediatrician ?: "Dra. María González") }
    var emergencyContact by remember { mutableStateOf(initialChild?.emergencyContact ?: "809-555-1234") }
    var weightKg by remember { mutableStateOf(initialChild?.weightKg?.toString() ?: "16.5") }
    var heightCm by remember { mutableStateOf(initialChild?.heightCm?.toString() ?: "102.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialChild == null) "Agregar hijo/a" else "Editar perfil de ${initialChild.name}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageYears,
                        onValueChange = { ageYears = it },
                        label = { Text("Edad (años)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bloodType,
                        onValueChange = { bloodType = it },
                        label = { Text("Grupo sanguíneo") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = gender == "Femenino",
                        onClick = { gender = "Femenino" },
                        label = { Text("Femenino") }
                    )
                    FilterChip(
                        selected = gender == "Masculino",
                        onClick = { gender = "Masculino" },
                        label = { Text("Masculino") }
                    )
                }
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("Fecha de nacimiento (DD/MM/AAAA)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Alergias") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pediatrician,
                    onValueChange = { pediatrician = it },
                    label = { Text("Pediatra de cabecera") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text("Contacto de emergencia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weightKg,
                        onValueChange = { weightKg = it },
                        label = { Text("Peso (kg)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = heightCm,
                        onValueChange = { heightCm = it },
                        label = { Text("Talla (cm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAge = ageYears.toLongOrNull()?.coerceIn(0L, 25L) ?: 4L
                    val parsedWeight = weightKg.toDoubleOrNull()?.coerceIn(1.0, 200.0) ?: 16.5
                    val parsedHeight = heightCm.toDoubleOrNull()?.coerceIn(30.0, 220.0) ?: 102.0
                    val profile = (initialChild ?: ChildProfile()).copy(
                        name = name.ifBlank { "Nuevo Hijo" },
                        ageYears = parsedAge,
                        birthDate = birthDate.ifBlank { "01/01/2022" },
                        gender = gender,
                        bloodType = bloodType.ifBlank { "O+" },
                        allergies = allergies.ifBlank { "Ninguna" },
                        pediatrician = pediatrician.ifBlank { "Dra. María González" },
                        emergencyContact = emergencyContact.ifBlank { "809-555-1234" },
                        weightKg = parsedWeight,
                        heightCm = parsedHeight,
                        avatarColorHex = initialChild?.avatarColorHex
                            ?: if (gender == "Femenino") "#FDE2E4" else "#D8F3DC"
                    )
                    onConfirm(profile)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PequeBlue)
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
