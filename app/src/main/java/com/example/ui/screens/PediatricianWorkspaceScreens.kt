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
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ChildProfile
import com.example.data.ClinicalModule
import com.example.data.HealthRecord
import com.example.data.PediatricianProfile
import com.example.data.getPediatricVitalReference
import com.example.data.pediatricDrugCatalog
import com.example.ui.AiAssistantViewModel
import com.example.ui.components.ChildAvatar
import com.example.ui.components.FeverThermalCurveCard
import com.example.ui.components.PediatricNotificationManager
import com.example.ui.components.PediatricPdfAndShareHelper
import com.example.ui.components.SendPediatricPushNotificationDialog
import com.example.ui.components.TannerTargetHeightCalculatorCard
import com.example.ui.components.WhoGrowthChartCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CardAiIcon
import com.example.ui.theme.PequeBlue
import com.example.ui.theme.PequeGreen
import com.example.ui.theme.PequeOrange
import com.example.ui.theme.PequePurple
import java.util.Locale
import kotlin.math.sqrt

/**
 * Calcula requerimiento hídrico basal por fórmula de Holliday-Segar (mL/día).
 */
private fun calculateHollidaySegarDailyMl(weightKg: Double): Double {
    if (weightKg <= 0.0) return 0.0
    return when {
        weightKg <= 10.0 -> weightKg * 100.0
        weightKg <= 20.0 -> 1000.0 + (weightKg - 10.0) * 50.0
        else -> 1500.0 + (weightKg - 20.0) * 20.0
    }
}

/**
 * Calcula Superficie Corporal por fórmula de Mosteller (m²).
 */
private fun calculateMostellerBsa(weightKg: Double, heightCm: Double): Double {
    if (weightKg <= 0.0 || heightCm <= 0.0) return 0.0
    return sqrt((weightKg * heightCm) / 3600.0)
}

@Composable
fun PediatricianPinDialog(
    expectedPin: String,
    pediatricianProfile: PediatricianProfile,
    initialConfigTab: Boolean = false,
    onDismiss: () -> Unit,
    onUnlockSuccess: () -> Unit,
    onUpdatePinAndProfile: (String, PediatricianProfile) -> Unit
) {
    val cleanExpected = expectedPin.trim().ifBlank { "1234" }
    var selectedTab by remember(initialConfigTab) { mutableIntStateOf(if (initialConfigTab) 1 else 0) }
    var enteredPin by remember(cleanExpected) { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var pinSavedMessage by remember { mutableStateOf<String?>(null) }

    var newPin by remember(cleanExpected) { mutableStateOf(cleanExpected) }
    var confirmNewPin by remember(cleanExpected) { mutableStateOf(cleanExpected) }
    var docName by remember(pediatricianProfile) { mutableStateOf(pediatricianProfile.name) }
    var docLicense by remember(pediatricianProfile) { mutableStateOf(pediatricianProfile.licenseId) }
    var docSpecialty by remember(pediatricianProfile) { mutableStateOf(pediatricianProfile.specialty) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = PequeBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (selectedTab == 0) "Acceso con PIN — Modo Pediatra" else "Configurar PIN de Modo Pediatra",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${pediatricianProfile.name} · ${pediatricianProfile.licenseId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pestañas superiores: Ingresar con PIN vs Configurar PIN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            pinError = null
                            pinSavedMessage = null
                        },
                        label = { Text("Ingresar PIN") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PequeBlue,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pin_dialog_tab_unlock")
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            pinError = null
                            pinSavedMessage = null
                        },
                        label = { Text("Configurar PIN") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PequeBlue,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pin_dialog_tab_configure")
                    )
                }

                if (selectedTab == 0) {
                    Text(
                        text = "Ingresa tu PIN de seguridad para cambiar al Escritorio del Pediatra (PIN actual: $cleanExpected).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Campo e indicador visual de dígitos del PIN
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { input ->
                            val digitsOnly = input.filter { it.isDigit() }.take(6)
                            enteredPin = digitsOnly
                            pinError = null
                            if (digitsOnly == cleanExpected) {
                                onUnlockSuccess()
                            }
                        },
                        label = { Text("PIN de acceso médico") },
                        placeholder = { Text("Ej. $cleanExpected") },
                        singleLine = true,
                        trailingIcon = {
                            TextButton(
                                onClick = {
                                    enteredPin = cleanExpected
                                    pinError = null
                                },
                                modifier = Modifier.testTag("pediatrician_pin_autofill")
                            ) {
                                Text(
                                    text = "Usar $cleanExpected",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pediatrician_pin_input")
                    )

                    // Teclado numérico en pantalla
                    val keypadRows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("C", "0", "⌫")
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        keypadRows.forEach { rowDigits ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowDigits.forEach { keyLabel ->
                                    Surface(
                                        onClick = {
                                            pinError = null
                                            when (keyLabel) {
                                                "C" -> enteredPin = ""
                                                "⌫" -> if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                }
                                                else -> {
                                                    if (enteredPin.length < 6) {
                                                        val updated = enteredPin + keyLabel
                                                        enteredPin = updated
                                                        if (updated == cleanExpected) {
                                                            onUnlockSuccess()
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = keyLabel,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (pinError != null) {
                        Text(
                            text = pinError!!,
                            style = MaterialTheme.typography.labelMedium,
                            color = AlertRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (pinSavedMessage != null) {
                        Text(
                            text = pinSavedMessage!!,
                            style = MaterialTheme.typography.labelMedium,
                            color = PequeGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Personaliza el código PIN (4 a 6 dígitos) y los datos profesionales para proteger el acceso al Modo Pediatra.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { input ->
                            newPin = input.filter { it.isDigit() }.take(6)
                            pinError = null
                        },
                        label = { Text("Nuevo PIN numérico (4 a 6 dígitos)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_new_pin_input")
                    )

                    OutlinedTextField(
                        value = confirmNewPin,
                        onValueChange = { input ->
                            confirmNewPin = input.filter { it.isDigit() }.take(6)
                            pinError = null
                        },
                        label = { Text("Confirmar nuevo PIN") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_confirm_pin_input")
                    )

                    HorizontalDivider()

                    OutlinedTextField(
                        value = docName,
                        onValueChange = { docName = it },
                        label = { Text("Nombre del Pediatra") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = docLicense,
                        onValueChange = { docLicense = it },
                        label = { Text("Cédula / Exequátur") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = docSpecialty,
                        onValueChange = { docSpecialty = it },
                        label = { Text("Especialidad") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (pinError != null) {
                        Text(
                            text = pinError!!,
                            style = MaterialTheme.typography.labelMedium,
                            color = AlertRed,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val cleanNew = newPin.trim()
                            val cleanConfirm = confirmNewPin.trim()
                            if (cleanNew.length < 4) {
                                pinError = "El PIN debe tener al menos 4 dígitos numéricos."
                            } else if (cleanNew != cleanConfirm) {
                                pinError = "La confirmación del PIN no coincide."
                            } else {
                                onUpdatePinAndProfile(
                                    cleanNew,
                                    pediatricianProfile.copy(
                                        name = docName.trim().ifBlank { "Dr. Alejandro López" },
                                        licenseId = docLicense.trim().ifBlank { "Exequátur 482-19" },
                                        specialty = docSpecialty.trim().ifBlank { "Pediatría y Puericultura" }
                                    )
                                )
                                enteredPin = cleanNew
                                pinSavedMessage = "PIN actualizado a $cleanNew correctamente."
                                selectedTab = 0
                            }
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_pin_only_button")
                    ) {
                        Text("Solo guardar nuevo PIN", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 0) {
                Button(
                    onClick = {
                        if (enteredPin.trim() == cleanExpected) {
                            onUnlockSuccess()
                        } else {
                            pinError = "PIN incorrecto. Ingresa $cleanExpected o toca 'Usar $cleanExpected'."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PequeBlue),
                    modifier = Modifier.testTag("pediatrician_unlock_confirm_button")
                ) {
                    Text("Cambiar a Modo Pediatra", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        val cleanNew = newPin.trim()
                        val cleanConfirm = confirmNewPin.trim()
                        if (cleanNew.length < 4) {
                            pinError = "El PIN debe tener al menos 4 dígitos numéricos."
                        } else if (cleanNew != cleanConfirm) {
                            pinError = "La confirmación del PIN no coincide."
                        } else {
                            onUpdatePinAndProfile(
                                cleanNew,
                                pediatricianProfile.copy(
                                    name = docName.trim().ifBlank { "Dr. Alejandro López" },
                                    licenseId = docLicense.trim().ifBlank { "Exequátur 482-19" },
                                    specialty = docSpecialty.trim().ifBlank { "Pediatría y Puericultura" }
                                )
                            )
                            onUnlockSuccess()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PequeBlue),
                    modifier = Modifier.testTag("save_pin_and_enter_button")
                ) {
                    Text("Guardar PIN y Entrar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/**
 * Cabecera Clínica Ejecutiva del Pediatra con Ficha de Telemetría del Paciente Activo.
 */
@Composable
fun PediatricianClinicalHeader(
    pediatricianProfile: PediatricianProfile,
    children: List<ChildProfile>,
    activeChild: ChildProfile?,
    showSearchField: Boolean = false,
    onSelectChild: (String) -> Unit,
    onAddChildClicked: () -> Unit,
    onEditActiveChild: (ChildProfile) -> Unit,
    onSwitchToParentMode: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showEditChildModal by remember { mutableStateOf(false) }

    val filteredChildren = remember(children, searchQuery) {
        if (searchQuery.isBlank()) children
        else children.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.familyLinkCode().contains(searchQuery, ignoreCase = true) ||
                it.allergies.contains(searchQuery, ignoreCase = true)
        }
    }

    if (showEditChildModal && activeChild != null) {
        AddOrEditChildDialog(
            initialChild = activeChild,
            onDismiss = { showEditChildModal = false },
            onConfirm = { updated ->
                onEditActiveChild(updated)
                showEditChildModal = false
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Fila 1: Identidad clínica del especialista + botón Vista Padres
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PequeBlue.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, PequeBlue.copy(alpha = 0.35f)),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = PequeBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pediatricianProfile.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${pediatricianProfile.specialty} · ${pediatricianProfile.licenseId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                onClick = onSwitchToParentMode,
                shape = RoundedCornerShape(50),
                color = PequeGreen.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, PequeGreen.copy(alpha = 0.45f)),
                modifier = Modifier.testTag("switch_workspace_mode_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = PequeGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vista Padres",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PequeGreen
                    )
                }
            }
        }

        if (showSearchField) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar paciente, alergia o código PEQUE-...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pediatrician_patient_search_input")
            )
        }

        // Selector horizontal de pacientes activos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            filteredChildren.forEach { child ->
                val isSelected = activeChild?.id == child.id
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) PequeBlue else MaterialTheme.colorScheme.surface,
                    border = if (isSelected) null else BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.clickable { onSelectChild(child.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChildAvatar(
                            name = child.name,
                            gender = child.gender,
                            colorHex = child.avatarColorHex,
                            size = 20.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${child.name.substringBefore(" ")} (${child.ageYears}a)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Surface(
                shape = if (children.isEmpty()) RoundedCornerShape(50) else CircleShape,
                color = if (children.isEmpty()) PequeBlue else MaterialTheme.colorScheme.surface,
                border = if (children.isEmpty()) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                modifier = Modifier
                    .let { if (children.isEmpty()) it else it.size(36.dp) }
                    .clickable { onAddChildClicked() }
                    .testTag("pediatrician_add_patient_button")
            ) {
                if (children.isEmpty()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nuevo paciente",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Registrar nuevo paciente",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nuevo paciente",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Ficha Clínica Ejecutiva del Paciente Activo
        if (activeChild != null) {
            val heightMeters = (activeChild.heightCm / 100.0).coerceAtLeast(0.5)
            val bmi = if (activeChild.weightKg > 0 && activeChild.heightCm > 0) {
                activeChild.weightKg / (heightMeters * heightMeters)
            } else {
                0.0
            }
            val hasAllergy = !activeChild.allergies.equals("Ninguna", ignoreCase = true) &&
                !activeChild.allergies.contains("Ninguna", ignoreCase = true) &&
                activeChild.allergies.isNotBlank()

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.2.dp,
                    if (hasAllergy) AlertRed.copy(alpha = 0.45f)
                    else PequeBlue.copy(alpha = 0.30f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    PequeBlue.copy(alpha = 0.08f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(14.dp),
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
                            ChildAvatar(
                                name = activeChild.name,
                                gender = activeChild.gender,
                                colorHex = activeChild.avatarColorHex,
                                size = 46.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeChild.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${activeChild.ageYears} años · ${activeChild.familyLinkCode()} · Grupo ${activeChild.bloodType}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val context = LocalContext.current
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = {
                                    PediatricPdfAndShareHelper.generateAndShareFullDossierPdf(
                                        context = context,
                                        child = activeChild,
                                        records = emptyList(),
                                        doctorName = pediatricianProfile.name
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = PequeBlue.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = "Exportar PDF",
                                        tint = PequeBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "PDF",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PequeBlue
                                    )
                                }
                            }

                            Surface(
                                onClick = { showEditChildModal = true },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar ficha clínica",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Ficha",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 4 Píldoras de Telemetría Clínica Rápida (Peso, Talla, IMC, Alergias)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClinicalTelemetryPill(
                            label = "Peso",
                            value = if (activeChild.weightKg > 0) "${activeChild.weightKg} kg" else "-- kg",
                            accentColor = PequeBlue,
                            modifier = Modifier.weight(1f)
                        )
                        ClinicalTelemetryPill(
                            label = "Talla",
                            value = if (activeChild.heightCm > 0) "${activeChild.heightCm} cm" else "-- cm",
                            accentColor = PequeGreen,
                            modifier = Modifier.weight(1f)
                        )
                        ClinicalTelemetryPill(
                            label = "IMC",
                            value = if (bmi > 0) String.format(Locale.US, "%.1f", bmi) else "--",
                            accentColor = PequePurple,
                            modifier = Modifier.weight(1f)
                        )
                        ClinicalTelemetryPill(
                            label = "Alergia",
                            value = if (hasAllergy) activeChild.allergies else "Ninguna",
                            accentColor = if (hasAllergy) AlertRed else PequeGreen,
                            isAlert = hasAllergy,
                            modifier = Modifier.weight(1.25f)
                        )
                    }

                    if (activeChild.lastCheckupSummary.isNotBlank() &&
                        !activeChild.lastCheckupSummary.contains("Sin consultas registradas", ignoreCase = true)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PequeBlue.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Última evolución (${activeChild.lastCheckupDate}): ${activeChild.lastCheckupSummary}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
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

@Composable
private fun ClinicalTelemetryPill(
    label: String,
    value: String,
    accentColor: Color,
    isAlert: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isAlert) AlertRed.copy(alpha = 0.12f) else accentColor.copy(alpha = 0.08f),
        border = BorderStroke(
            1.dp,
            if (isAlert) AlertRed.copy(alpha = 0.45f) else accentColor.copy(alpha = 0.22f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isAlert) AlertRed else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isAlert) AlertRed else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * ESTACIÓN 1 DEL PEDIATRA: Dashboard Ejecutivo de Guardia, Triaje de Casa y Acciones Clínicas.
 */
@Composable
fun PediatricianTriageAndPatientsScreen(
    pediatricianProfile: PediatricianProfile,
    children: List<ChildProfile>,
    activeChild: ChildProfile?,
    records: List<HealthRecord>,
    onSelectChild: (String) -> Unit,
    onAddChildClicked: () -> Unit,
    onEditActiveChild: (ChildProfile) -> Unit,
    onAddRecord: (HealthRecord) -> Unit,
    onReplyOrUpdateRecord: (String, String, String, String) -> Unit,
    onSwitchToParentMode: () -> Unit,
    onNavigateStation: (Int) -> Unit = {},
    onOpenAiAssistant: () -> Unit = {},
    onOpenHybridPwaHub: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Síntomas, 1=Dudas, 2=Diario/Estudios, 3=Indicaciones Enviadas

    val childRecords = remember(records, activeChild) {
        if (activeChild == null) emptyList()
        else records.filter { it.childId == activeChild.id || it.childName == activeChild.name }
    }

    val homeSymptoms = childRecords.filter { it.category == "SINTOMA_CASA" }
    val alertSymptomsCount = homeSymptoms.count {
        it.status.contains("Alerta", ignoreCase = true) ||
            it.status.contains("Pendiente", ignoreCase = true) ||
            !it.notes.contains("| Respuesta", ignoreCase = true)
    }
    val parentDoubts = childRecords.filter { it.category == "DUDA_PADRE" }
    val unansweredDoubtsCount = parentDoubts.count {
        !it.status.contains("Respondida", ignoreCase = true) &&
            !it.notes.contains("| Respuesta", ignoreCase = true)
    }
    val homeDiaryAndStudies = childRecords.filter { it.category == "DIARIO_BEBE" || it.category == "ESTUDIO_PADRE" }
    val activeMedsCount = childRecords.count { it.category == "MEDICAMENTO" }
    val context = LocalContext.current
    val doctorIndications = childRecords.filter {
        it.category in listOf("MEDICAMENTO", "CONSULTA", "CERTIFICADO", "CALENDARIO") ||
            it.status.contains("Push", ignoreCase = true) ||
            it.notes.contains("| Respuesta", ignoreCase = true)
    }

    val visibleRecords = when (selectedTab) {
        0 -> homeSymptoms
        1 -> parentDoubts
        2 -> homeDiaryAndStudies
        else -> doctorIndications
    }

    var recordToReply by remember { mutableStateOf<HealthRecord?>(null) }
    var doctorReplyInput by remember { mutableStateOf("") }
    var showDirectIndicationDialog by remember { mutableStateOf(false) }
    var showPushNotificationDialog by remember { mutableStateOf(false) }
    var showClinicalScalesDialog by remember { mutableStateOf(false) }

    if (showPushNotificationDialog && activeChild != null) {
        SendPediatricPushNotificationDialog(
            activeChild = activeChild,
            pediatricianProfile = pediatricianProfile,
            onDismiss = { showPushNotificationDialog = false },
            onSendPushRecord = { pushRecord ->
                onAddRecord(pushRecord)
                onEditActiveChild(
                    activeChild.copy(
                        lastCheckupDate = pushRecord.dateText,
                        lastCheckupSummary = "Push enviado: ${pushRecord.title}"
                    )
                )
                showPushNotificationDialog = false
                selectedTab = 3
            }
        )
    }

    if (showClinicalScalesDialog && activeChild != null) {
        PediatricScalesAndFluidsDialog(
            child = activeChild,
            pediatricianName = pediatricianProfile.name,
            onDismiss = { showClinicalScalesDialog = false },
            onSaveEvaluation = { rec ->
                onAddRecord(rec)
                onEditActiveChild(
                    activeChild.copy(
                        lastCheckupDate = "Hoy",
                        lastCheckupSummary = "${rec.title} · ${rec.subtitle}"
                    )
                )
                showClinicalScalesDialog = false
                selectedTab = 3
            }
        )
    }

    if (showDirectIndicationDialog && activeChild != null) {
        var indTitle by remember { mutableStateOf("") }
        var indDetails by remember { mutableStateOf("") }
        val quickPresets = listOf(
            "Control térmico e hidratación en casa" to "Ofrecer líquidos fraccionados frecuentes, mantener ambiente fresco y vigilar temperatura cada 6 horas.",
            "Plan de alimentación blanda y suero oral" to "Dieta astringente (arroz, manzana, guineo, pollo hervido) y suero de rehidratación oral tras cada evacuación.",
            "Vigilar signos de alarma respiratoria" to "Lavados nasales con solución salina antes de dormir y comer. Acudir a consulta si hay tiraje intercostal o fiebre >38.5 °C.",
            "Cuidados post-vacunación" to "Aplicar paños fríos en el sitio de punción si hay inflamación local. Administrar analgésico ponderal solo si presenta fiebre o dolor."
        )

        AlertDialog(
            onDismissRequest = { showDirectIndicationDialog = false },
            title = {
                Text(
                    text = "Nueva Indicación para Padres (${activeChild.name.substringBefore(" ")})",
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
                        text = "Esta indicación aparecerá de inmediato en la tarjeta 'Indicaciones de tu pediatra' del Escritorio de Padres.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickPresets.forEach { (presetTitle, presetBody) ->
                            FilterChip(
                                selected = indTitle == presetTitle,
                                onClick = {
                                    indTitle = presetTitle
                                    indDetails = presetBody
                                },
                                label = { Text(presetTitle.take(24) + "…") }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = indTitle,
                        onValueChange = { indTitle = it },
                        label = { Text("Título de la indicación") },
                        placeholder = { Text("Ej. Control térmico, dieta blanda...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("direct_indication_title_input")
                    )
                    OutlinedTextField(
                        value = indDetails,
                        onValueChange = { indDetails = it },
                        label = { Text("Instrucciones detalladas para mamá y papá") },
                        placeholder = { Text("Escribe las indicaciones claras que verán los padres en su escritorio...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("direct_indication_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (indTitle.isNotBlank()) {
                            val cleanNote = indDetails.trim().ifBlank { "Seguir indicaciones médicas en casa." }
                            onAddRecord(
                                HealthRecord(
                                    childId = activeChild.id,
                                    childName = activeChild.name,
                                    category = "CONSULTA",
                                    title = indTitle.trim(),
                                    subtitle = "Indicación de ${pediatricianProfile.name}",
                                    dateText = "Hoy",
                                    status = "Alerta Push",
                                    notes = "[PUSH] $cleanNote"
                                )
                            )
                            PediatricNotificationManager.dispatchPushNotification(
                                context = context,
                                title = "Nueva Indicación: ${indTitle.trim()}",
                                body = cleanNote,
                                childName = activeChild.name.substringBefore(" "),
                                isAppointment = false
                            )
                            onEditActiveChild(
                                activeChild.copy(
                                    lastCheckupDate = "Hoy",
                                    lastCheckupSummary = "${indTitle.trim()}: ${cleanNote.take(120)}"
                                )
                            )
                            showDirectIndicationDialog = false
                            selectedTab = 3
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PequeBlue),
                    modifier = Modifier.testTag("confirm_send_indication_button")
                ) {
                    Text("Enviar a los Padres", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectIndicationDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (recordToReply != null) {
        val target = recordToReply!!
        AlertDialog(
            onDismissRequest = { recordToReply = null },
            title = {
                Text(
                    text = "Responder a los padres",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = target.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = doctorReplyInput,
                        onValueChange = { doctorReplyInput = it },
                        label = { Text("Indicación del pediatra") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("doctor_reply_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (doctorReplyInput.isNotBlank()) {
                            val baseNote = target.notes.substringBefore("|").trim()
                            val cleanReply = doctorReplyInput.trim()
                            val combined = "$baseNote | Respuesta ${pediatricianProfile.name}: $cleanReply"
                            val nextStatus = if (target.category == "DUDA_PADRE") "Respondida" else "Revisado por Pediatra"
                            onReplyOrUpdateRecord(
                                target.id,
                                nextStatus,
                                combined,
                                "Indicación y alerta push enviadas al Escritorio de Padres"
                            )
                            PediatricNotificationManager.dispatchPushNotification(
                                context = context,
                                title = "Respuesta de ${pediatricianProfile.name}: ${target.title}",
                                body = cleanReply,
                                childName = activeChild?.name?.substringBefore(" ") ?: "",
                                isAppointment = false
                            )
                            if (activeChild != null) {
                                onEditActiveChild(
                                    activeChild.copy(
                                        lastCheckupDate = "Hoy",
                                        lastCheckupSummary = "Indicación Dr.: ${cleanReply.take(120)}"
                                    )
                                )
                            }
                            recordToReply = null
                            doctorReplyInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PequeBlue),
                    modifier = Modifier.testTag("confirm_doctor_reply_button")
                ) {
                    Text("Enviar a los Padres", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToReply = null }) { Text("Cancelar") }
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
            PediatricianClinicalHeader(
                pediatricianProfile = pediatricianProfile,
                children = children,
                activeChild = activeChild,
                showSearchField = true,
                onSelectChild = onSelectChild,
                onAddChildClicked = onAddChildClicked,
                onEditActiveChild = onEditActiveChild,
                onSwitchToParentMode = onSwitchToParentMode
            )
        }

        if (activeChild != null) {
            // Resumen Ejecutivo de Guardia y Triaje (4 KPIs interactivos)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monitor de Guardia · ${activeChild.name.substringBefore(" ")}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${childRecords.size} eventos clínicos",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = PequeBlue
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClinicalKpiMetricCard(
                            title = "Síntomas Casa",
                            metricValue = "${homeSymptoms.size}",
                            subtitle = if (alertSymptomsCount > 0) "$alertSymptomsCount por revisar" else "Al día",
                            icon = Icons.Default.Notifications,
                            accentColor = if (alertSymptomsCount > 0) AlertRed else PequeOrange,
                            isSelected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        ClinicalKpiMetricCard(
                            title = "Dudas Padres",
                            metricValue = "${parentDoubts.size}",
                            subtitle = if (unansweredDoubtsCount > 0) "$unansweredDoubtsCount sin responder" else "Respondidas",
                            icon = Icons.Default.QuestionAnswer,
                            accentColor = PequePurple,
                            isSelected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClinicalKpiMetricCard(
                            title = "Diario y Estudios",
                            metricValue = "${homeDiaryAndStudies.size}",
                            subtitle = "Bitácora en casa",
                            icon = Icons.Default.Straighten,
                            accentColor = PequeGreen,
                            isSelected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            modifier = Modifier.weight(1f)
                        )
                        ClinicalKpiMetricCard(
                            title = "Indicaciones Dr.",
                            metricValue = "${doctorIndications.size}",
                            subtitle = "$activeMedsCount recetas activas",
                            icon = Icons.Default.Medication,
                            accentColor = PequeBlue,
                            isSelected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Bento Grid 2x2 de Herramientas Clínicas Rápidas + Botón de Indicación Directa
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Acciones Clínicas Rápidas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showDirectIndicationDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PequeBlue),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_direct_indication_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Enviar Indicación",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = { showPushNotificationDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PequeOrange),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_push_notification_dialog_button")
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Alerta / Cita Push",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClinicalQuickToolTile(
                            title = "Dosis mg/kg y SOAP",
                            subtitle = "Calculadora ponderal",
                            icon = Icons.Default.Calculate,
                            accentColor = PequeBlue,
                            onClick = { onNavigateStation(1) },
                            modifier = Modifier.weight(1f)
                        )
                        ClinicalQuickToolTile(
                            title = "Curvas OMS y PAI",
                            subtitle = "Percentiles y vacunas",
                            icon = Icons.Default.Straighten,
                            accentColor = PequeGreen,
                            onClick = { onNavigateStation(2) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClinicalQuickToolTile(
                            title = "Estudios y Certificados",
                            subtitle = "Laboratorio y alta",
                            icon = Icons.Default.MedicalServices,
                            accentColor = PequeOrange,
                            onClick = { onNavigateStation(3) },
                            modifier = Modifier.weight(1f)
                        )
                        ClinicalQuickToolTile(
                            title = "Síntesis Clínica",
                            subtitle = "Soporte diferencial",
                            icon = Icons.Default.AutoAwesome,
                            accentColor = CardAiIcon,
                            onClick = onOpenAiAssistant,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Acceso minimalista a Escalas de Guardia, TEP y Líquidos Holliday-Segar / Plan B OMS
                    OutlinedButton(
                        onClick = { showClinicalScalesDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, PequePurple.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = PequePurple,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Escalas de Urgencia (TEP / Wood-Downes) y Líquidos Holliday-Segar",
                            color = PequePurple,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Bandeja de Triaje de Casa con Pestañas
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Bandeja Bidireccional con Padres",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Síntomas (${homeSymptoms.size})",
                        "Dudas (${parentDoubts.size})",
                        "Diario y Estudios (${homeDiaryAndStudies.size})",
                        "Indicaciones Enviadas (${doctorIndications.size})"
                    ).forEachIndexed { idx, label ->
                        FilterChip(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }

        if (selectedTab == 0 && homeSymptoms.isNotEmpty() && activeChild != null) {
            item {
                FeverThermalCurveCard(
                    childName = activeChild.name,
                    symptomRecords = homeSymptoms
                )
            }
        }

        if (visibleRecords.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Sin registros en esta bandeja para ${activeChild?.name ?: "este paciente"}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Cuando mamá o papá registren síntomas, dudas o mediciones desde casa, aparecerán aquí en tiempo real para que puedas responderles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            itemsIndexed(visibleRecords, key = { idx, rec -> "${rec.id.ifBlank { "triage" }}_$idx" }) { _, rec ->
                val isAlert = rec.status.contains("Alerta", ignoreCase = true)
                ClinicalParentInboxCard(
                    record = rec,
                    badgeText = "${rec.category} · ${rec.status}",
                    badgeColor = if (isAlert) AlertRed else PequeBlue,
                    actionLabel = if (rec.notes.contains("| Respuesta")) "Actualizar indicación" else "Responder a padres",
                    onReplyClicked = {
                        doctorReplyInput = rec.notes.substringAfter("Respuesta ${pediatricianProfile.name}:", "").trim()
                        recordToReply = rec
                    }
                )
            }
        }
    }
}

@Composable
private fun ClinicalKpiMetricCard(
    title: String,
    metricValue: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = metricValue,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.14f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClinicalQuickToolTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.14f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
private fun ClinicalParentInboxCard(
    record: HealthRecord,
    badgeText: String,
    badgeColor: Color,
    actionLabel: String,
    onReplyClicked: () -> Unit
) {
    val parentReportText = remember(record.notes) {
        record.notes.substringBefore("| Respuesta").trim()
    }
    val doctorReplyText = remember(record.notes) {
        if (record.notes.contains("| Respuesta")) {
            record.notes.substringAfter("| ").trim()
        } else {
            ""
        }
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = record.dateText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = record.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (record.subtitle.isNotBlank()) {
                Text(
                    text = record.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = PequeBlue
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = parentReportText.ifBlank { record.notes },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (doctorReplyText.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PequeGreen.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, PequeGreen.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PequeGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = doctorReplyText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onReplyClicked,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(actionLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * ESTACIÓN 2 DEL PEDIATRA: Calculadora Ponderal (mg/kg -> mL) y Consulta SOAP (Separados por pestaña limpia).
 */
@Composable
fun PediatricianConsultationAndDoseScreen(
    pediatricianProfile: PediatricianProfile,
    children: List<ChildProfile>,
    activeChild: ChildProfile?,
    records: List<HealthRecord>,
    onSelectChild: (String) -> Unit,
    onAddChildClicked: () -> Unit,
    onEditActiveChild: (ChildProfile) -> Unit,
    onAddRecord: (HealthRecord) -> Unit,
    onSwitchToParentMode: () -> Unit,
    clinicalSupportViewModel: AiAssistantViewModel = viewModel()
) {
    val context = LocalContext.current
    var activeToolTab by remember { mutableIntStateOf(0) } // 0 = Calculadora Dosis, 1 = Nota SOAP y Signos Vitales
    var showPushReminderModal by remember { mutableStateOf(false) }
    var isGeneratingSoapDraft by remember { mutableStateOf(false) }
    val vitalRef = getPediatricVitalReference(activeChild?.ageYears ?: 5L)

    if (showPushReminderModal && activeChild != null) {
        SendPediatricPushNotificationDialog(
            activeChild = activeChild,
            pediatricianProfile = pediatricianProfile,
            onDismiss = { showPushReminderModal = false },
            onSendPushRecord = { pushRecord ->
                onAddRecord(pushRecord)
                onEditActiveChild(
                    activeChild.copy(
                        lastCheckupDate = pushRecord.dateText,
                        lastCheckupSummary = "Push enviado: ${pushRecord.title}"
                    )
                )
                showPushReminderModal = false
            }
        )
    }

    val childRecords = remember(records, activeChild) {
        if (activeChild == null) emptyList()
        else records.filter { it.childId == activeChild.id || it.childName == activeChild.name }
    }
    val childMeds = remember(childRecords) { childRecords.filter { it.category == "MEDICAMENTO" } }
    val childConsultations = remember(childRecords) {
        childRecords.filter { it.category == "CONSULTA" || it.category == "HISTORIAL" }
    }

    // Signos vitales (vacíos para captura directa)
    var hrInput by remember { mutableStateOf("") }
    var rrInput by remember { mutableStateOf("") }
    var tempInput by remember { mutableStateOf("") }
    var spo2Input by remember { mutableStateOf("") }
    var bpInput by remember { mutableStateOf("") }
    var painOrGlucoseInput by remember { mutableStateOf("") }

    val physicalExamChips = listOf(
        "Hidratado y activo",
        "Fontanela normotensa",
        "Otoscopia normal",
        "Hiperemia faríngea",
        "Murmullo vesicular normal",
        "Abdomen blando"
    )
    var selectedExamFindings by remember {
        mutableStateOf(emptySet<String>())
    }
    val cie10Presets = listOf(
        "Z00.1 - Control de niño sano",
        "J00 - Rinofaringitis aguda",
        "J02.9 - Faringoamigdalitis aguda",
        "H66.9 - Otitis media aguda",
        "A09 - Gastroenteritis aguda"
    )
    var diagnosisCie10 by remember { mutableStateOf("") }
    var clinicalSoapNotes by remember { mutableStateOf("") }

    // Calculadora Farmacológica Ponderal (mg/kg -> mL)
    var selectedDrug by remember { mutableStateOf(pediatricDrugCatalog.first()) }
    var customWeightKg by remember(activeChild) {
        val childW = activeChild?.weightKg ?: 0.0
        mutableStateOf(if (childW > 0.0) childW.toString() else "")
    }
    var mgPerKgDose by remember(selectedDrug) {
        mutableStateOf(selectedDrug.defaultMgPerKgPerDose.toString())
    }
    var everyHours by remember(selectedDrug) {
        mutableStateOf(selectedDrug.defaultEveryHours.toString())
    }
    var durationDays by remember(selectedDrug) {
        mutableStateOf(selectedDrug.defaultDays.toString())
    }

    val weightNum = customWeightKg.toDoubleOrNull() ?: (activeChild?.weightKg ?: 0.0)
    val mgKgNum = mgPerKgDose.toDoubleOrNull() ?: selectedDrug.defaultMgPerKgPerDose
    val totalMgPerDose = weightNum * mgKgNum
    val exactMlPerDose = if (selectedDrug.mgPerMl > 0) totalMgPerDose / selectedDrug.mgPerMl else 0.0
    val exactDropsPerDose = exactMlPerDose * 20.0
    val hasAllergyWarning = activeChild?.hasDrugAllergyWarning(selectedDrug.name) == true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PediatricianClinicalHeader(
                pediatricianProfile = pediatricianProfile,
                children = children,
                activeChild = activeChild,
                onSelectChild = onSelectChild,
                onAddChildClicked = onAddChildClicked,
                onEditActiveChild = onEditActiveChild,
                onSwitchToParentMode = onSwitchToParentMode
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeToolTab == 0,
                    onClick = { activeToolTab = 0 },
                    label = { Text("Calculadora Dosis (mg/kg)") }
                )
                FilterChip(
                    selected = activeToolTab == 1,
                    onClick = { activeToolTab = 1 },
                    label = { Text("Consulta SOAP y Vitales") }
                )
                if (activeChild != null) {
                    FilterChip(
                        selected = false,
                        onClick = { showPushReminderModal = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = PequeOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Recordatorio Push") }
                    )
                }
            }
        }

        if (activeToolTab == 0) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = PequeBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dosis Ponderal (mg/kg → mL)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pediatricDrugCatalog.forEach { drug ->
                                FilterChip(
                                    selected = selectedDrug.id == drug.id,
                                    onClick = {
                                        selectedDrug = drug
                                        mgPerKgDose = drug.defaultMgPerKgPerDose.toString()
                                        everyHours = drug.defaultEveryHours.toString()
                                        durationDays = drug.defaultDays.toString()
                                    },
                                    label = { Text("${drug.name} (${drug.presentationLabel})") }
                                )
                            }
                        }

                        if (hasAllergyWarning && activeChild != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AlertRed.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = AlertRed, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Alerta: Alergia a ${activeChild.allergies}. Evitar betalactámicos.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AlertRed
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customWeightKg,
                                onValueChange = { customWeightKg = it },
                                label = { Text("Peso (kg)") },
                                placeholder = { Text("Ej. 14.5") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = mgPerKgDose,
                                onValueChange = { mgPerKgDose = it },
                                label = { Text("mg/kg") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = everyHours,
                                onValueChange = { everyHours = it },
                                label = { Text("Cada (h)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = durationDays,
                                onValueChange = { durationDays = it },
                                label = { Text("Días") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        val formattedMg = String.format(Locale.US, "%.1f", totalMgPerDose)
                        val formattedMl = String.format(Locale.US, "%.1f", exactMlPerDose)
                        val formattedDrops = String.format(Locale.US, "%.0f", exactDropsPerDose)
                        val childHeightCm = activeChild?.heightCm ?: 0.0
                        val bsaM2 = calculateMostellerBsa(weightNum, childHeightCm)
                        val dailyFluidsMl = calculateHollidaySegarDailyMl(weightNum)
                        val hourlyFluidsMl = dailyFluidsMl / 24.0

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PequeBlue.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (selectedDrug.id.contains("gotas")) {
                                        "Dar $formattedMl mL ($formattedDrops gotas) cada $everyHours h por $durationDays días"
                                    } else {
                                        "Dar $formattedMl mL ($formattedMg mg) cada $everyHours h por $durationDays días"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PequeBlue
                                )
                                Text(
                                    text = selectedDrug.parentFriendlyTip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (weightNum > 0.0) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = PequeBlue.copy(alpha = 0.2f)
                                    )
                                    Text(
                                        text = "SC Mosteller: ${
                                            if (bsaM2 > 0) String.format(Locale.US, "%.2f m²", bsaM2) else "-- m²"
                                        } · Líquidos Holliday-Segar: ${
                                            String.format(Locale.US, "%.0f mL/día (%.1f mL/h)", dailyFluidsMl, hourlyFluidsMl)
                                        }",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (activeChild == null) {
                                    onAddChildClicked()
                                } else {
                                    val doseText = if (selectedDrug.id.contains("gotas")) {
                                        "Dar $formattedMl mL ($formattedDrops gotas) cada $everyHours horas por $durationDays días"
                                    } else {
                                        "Dar $formattedMl mL cada $everyHours horas por $durationDays días"
                                    }
                                    onAddRecord(
                                        HealthRecord(
                                            childId = activeChild.id,
                                            childName = activeChild.name,
                                            category = "MEDICAMENTO",
                                            title = "${selectedDrug.name} (${selectedDrug.presentationLabel})",
                                            subtitle = doseText,
                                            dateText = "Por $durationDays días",
                                            status = "Activo",
                                            notes = "[PUSH] Indicado por ${pediatricianProfile.name}: ${selectedDrug.parentFriendlyTip} · ($mgKgNum mg/kg para $weightNum kg)."
                                        )
                                    )
                                    PediatricNotificationManager.dispatchPushNotification(
                                        context = context,
                                        title = "Nueva Receta: ${selectedDrug.name}",
                                        body = "$doseText · ${selectedDrug.parentFriendlyTip}",
                                        childName = activeChild.name.substringBefore(" "),
                                        isAppointment = false
                                    )
                                    onEditActiveChild(
                                        activeChild.copy(
                                            weightKg = if (weightNum > 0.0) weightNum else activeChild.weightKg,
                                            lastCheckupDate = "Hoy",
                                            lastCheckupSummary = "Receta: ${selectedDrug.name} · $doseText"
                                        )
                                    )
                                }
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasAllergyWarning) AlertRed else PequeBlue
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("emit_prescription_button")
                        ) {
                            Icon(Icons.Default.Medication, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar Receta a los Padres", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (childMeds.isNotEmpty()) {
                item {
                    Text(
                        text = "Recetas activas visibles en el Escritorio de Padres (${childMeds.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                itemsIndexed(childMeds, key = { idx, med -> "${med.id.ifBlank { "med_doc" }}_$idx" }) { _, med ->
                    ClinicalRecordSimpleCard(med)
                }
            }
        } else {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Signos Vitales (${vitalRef.ageGroupLabel})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = hrInput,
                                onValueChange = { hrInput = it },
                                label = { Text("FC lpm") },
                                placeholder = { Text("92") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = rrInput,
                                onValueChange = { rrInput = it },
                                label = { Text("FR rpm") },
                                placeholder = { Text("22") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = tempInput,
                                onValueChange = { tempInput = it },
                                label = { Text("Temp °C") },
                                placeholder = { Text("36.7") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = spo2Input,
                                onValueChange = { spo2Input = it },
                                label = { Text("SpO₂ %") },
                                placeholder = { Text("99") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = bpInput,
                                onValueChange = { bpInput = it },
                                label = { Text("TA (mmHg)") },
                                placeholder = { Text("Ej. 95/60") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = painOrGlucoseInput,
                                onValueChange = { painOrGlucoseInput = it },
                                label = { Text("EVA Dolor / Glucemia") },
                                placeholder = { Text("Ej. EVA 0/10 · 88 mg/dL") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                        Text(
                            text = "Exploración Física y Diagnóstico CIE-10",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            physicalExamChips.forEach { finding ->
                                val isSel = finding in selectedExamFindings
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        selectedExamFindings = if (isSel) selectedExamFindings - finding else selectedExamFindings + finding
                                    },
                                    label = { Text(finding) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            cie10Presets.forEach { preset ->
                                FilterChip(
                                    selected = diagnosisCie10 == preset,
                                    onClick = { diagnosisCie10 = preset },
                                    label = { Text(preset) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = diagnosisCie10,
                            onValueChange = { diagnosisCie10 = it },
                            label = { Text("Diagnóstico CIE-10") },
                            placeholder = { Text("Escribe el diagnóstico o toca un código arriba...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = clinicalSoapNotes,
                            onValueChange = { clinicalSoapNotes = it },
                            label = { Text("Nota de Evolución y Plan (SOAP)") },
                            placeholder = { Text("Escribe la evolución clínica y las indicaciones para los padres...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedButton(
                            onClick = {
                                if (!isGeneratingSoapDraft) {
                                    isGeneratingSoapDraft = true
                                    val vitalsText = "FC: ${hrInput.ifBlank { "N/R" }} lpm, FR: ${rrInput.ifBlank { "N/R" }} rpm, Temp: ${tempInput.ifBlank { "N/R" }} °C, SpO2: ${spo2Input.ifBlank { "N/R" }}%, TA: ${bpInput.ifBlank { "N/R" }}"
                                    val examSummary = selectedExamFindings.joinToString(", ").ifBlank { "Sin hallazgos patológicos" }
                                    clinicalSupportViewModel.generateSoapEvolutionDraft(
                                        child = activeChild,
                                        diagnosisCie10 = diagnosisCie10.ifBlank { "Z00.1 - Control de niño sano" },
                                        vitalsText = vitalsText,
                                        examFindings = examSummary
                                    ) { draft ->
                                        clinicalSoapNotes = draft
                                        isGeneratingSoapDraft = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("draft_soap_evolution_button")
                        ) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isGeneratingSoapDraft) "Redactando evolución clínica..." else "Sugerir evolución clínica (SOAP)",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                if (activeChild == null) {
                                    onAddChildClicked()
                                } else {
                                    val cleanDiag = diagnosisCie10.trim().ifBlank { "Consulta Pediátrica" }
                                    val examSummary = selectedExamFindings.joinToString(", ").ifBlank { "Exploración física general" }
                                    val extraVitals = buildString {
                                        if (bpInput.isNotBlank()) append(", TA: $bpInput mmHg")
                                        if (painOrGlucoseInput.isNotBlank()) append(", $painOrGlucoseInput")
                                    }
                                    val vitalsText = "FC: ${hrInput.ifBlank { "-" }} lpm, FR: ${rrInput.ifBlank { "-" }} rpm, Temp: ${tempInput.ifBlank { "-" }} °C, SpO2: ${spo2Input.ifBlank { "-" }}%$extraVitals"
                                    val planSummary = clinicalSoapNotes.trim().ifBlank { "Seguir indicaciones médicas en casa." }
                                    onAddRecord(
                                        HealthRecord(
                                            childId = activeChild.id,
                                            childName = activeChild.name,
                                            category = "CONSULTA",
                                            title = cleanDiag,
                                            subtitle = "${pediatricianProfile.name} · Plan e Indicaciones",
                                            dateText = "Hoy",
                                            status = "Alerta Push",
                                            notes = "[PUSH] Vitales: $vitalsText. | EF: $examSummary. | Plan para Padres: $planSummary"
                                        )
                                    )
                                    PediatricNotificationManager.dispatchPushNotification(
                                        context = context,
                                        title = "Consulta Pediátrica: $cleanDiag",
                                        body = planSummary,
                                        childName = activeChild.name.substringBefore(" "),
                                        isAppointment = false
                                    )
                                    onEditActiveChild(
                                        activeChild.copy(
                                            lastCheckupDate = "Hoy",
                                            lastCheckupSummary = "$cleanDiag · ${planSummary.take(120)}"
                                        )
                                    )
                                    diagnosisCie10 = ""
                                    clinicalSoapNotes = ""
                                    hrInput = ""
                                    rrInput = ""
                                    tempInput = ""
                                    spo2Input = ""
                                    bpInput = ""
                                    painOrGlucoseInput = ""
                                }
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = PequeGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_soap_consultation_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Consulta y Enviar Plan a Padres", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (childConsultations.isNotEmpty()) {
                item {
                    Text(
                        text = "Consultas e Indicaciones Registradas (${childConsultations.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                itemsIndexed(childConsultations, key = { idx, con -> "${con.id.ifBlank { "con_doc" }}_$idx" }) { _, con ->
                    ClinicalRecordSimpleCard(con)
                }
            }
        }
    }
}

/**
 * ESTACIÓN 3 DEL PEDIATRA: Somatometría OMS y Vacunación Oficial (Minimalista con pestañas).
 */
@Composable
fun PediatricianGrowthAndVaccinesScreen(
    pediatricianProfile: PediatricianProfile,
    children: List<ChildProfile>,
    activeChild: ChildProfile?,
    records: List<HealthRecord>,
    onSelectChild: (String) -> Unit,
    onAddChildClicked: () -> Unit,
    onEditActiveChild: (ChildProfile) -> Unit,
    onAddRecord: (HealthRecord) -> Unit,
    onToggleRecordStatus: (HealthRecord) -> Unit,
    onDeleteRecord: (String) -> Unit,
    onSwitchToParentMode: () -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = Somatometría, 1 = Vacunación PAI

    var weightInput by remember(activeChild) {
        val w = activeChild?.weightKg ?: 0.0
        mutableStateOf(if (w > 0.0) w.toString() else "")
    }
    var heightInput by remember(activeChild) {
        val h = activeChild?.heightCm ?: 0.0
        mutableStateOf(if (h > 0.0) h.toString() else "")
    }
    var headCircInput by remember { mutableStateOf("") }
    var percentileChoice by remember { mutableStateOf("Percentil 50-75 OMS") }

    var vaccineName by remember { mutableStateOf("") }
    var vaccineDose by remember { mutableStateOf("") }
    var vaccineLot by remember { mutableStateOf("") }

    val childVaccines = remember(records, activeChild) {
        records.filter {
            it.category == "VACUNA" &&
                (activeChild == null || it.childId == activeChild.id || it.childName == activeChild.name)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PediatricianClinicalHeader(
                pediatricianProfile = pediatricianProfile,
                children = children,
                activeChild = activeChild,
                onSelectChild = onSelectChild,
                onAddChildClicked = onAddChildClicked,
                onEditActiveChild = onEditActiveChild,
                onSwitchToParentMode = onSwitchToParentMode
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    label = { Text("Somatometría y Percentiles") }
                )
                FilterChip(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    label = { Text("Vacunación PAI (${childVaccines.size})") }
                )
            }
        }

        if (selectedSubTab == 0) {
            item {
                val liveW = weightInput.replace(",", ".").toDoubleOrNull()
                val liveH = heightInput.replace(",", ".").toDoubleOrNull()
                val livePc = headCircInput.replace(",", ".").toDoubleOrNull()

                WhoGrowthChartCard(
                    activeChild = activeChild,
                    records = records,
                    isPediatricianMode = true,
                    liveWeightKg = liveW,
                    liveHeightCm = liveH,
                    liveHeadCircCm = livePc,
                    onAutoPercentileComputed = { autoChannel ->
                        percentileChoice = autoChannel
                    }
                )
            }

            item {
                val w = weightInput.replace(",", ".").toDoubleOrNull() ?: (activeChild?.weightKg ?: 0.0)
                val h = heightInput.replace(",", ".").toDoubleOrNull() ?: (activeChild?.heightCm ?: 0.0)
                val hm = (h / 100.0).coerceAtLeast(0.4)
                val bmi = if (w > 0.0 && h > 0.0) w / (hm * hm) else 0.0
                val formattedBmi = String.format(Locale.US, "%.1f", bmi)

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Medición Antropométrica OMS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                label = { Text("Peso (kg)") },
                                placeholder = { Text("Ej. 14.5") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = heightInput,
                                onValueChange = { heightInput = it },
                                label = { Text("Talla (cm)") },
                                placeholder = { Text("Ej. 98") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = headCircInput,
                                onValueChange = { headCircInput = it },
                                label = { Text("PC (cm)") },
                                placeholder = { Text("Ej. 49.5") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "Percentil 50-85 OMS (Eutrófico)",
                                "Percentil 15-50 OMS (Eutrófico)",
                                "Percentil 85-97 OMS (Vigilancia)",
                                "Percentil 3-15 OMS (Vigilancia)",
                                "Percentil <3 OMS (Alerta)",
                                "Percentil >97 OMS (Alerta)"
                            ).forEach { p ->
                                FilterChip(
                                    selected = percentileChoice == p,
                                    onClick = { percentileChoice = p },
                                    label = { Text(p) }
                                )
                            }
                        }

                        if (bmi > 0.0) {
                            val nutritionalStatus = when {
                                bmi < 13.8 -> "Riesgo de bajo peso · Vigilar ganancia ponderal"
                                bmi <= 18.2 -> "Estado Nutricional Eutrófico (Normal OMS)"
                                else -> "Sobrepeso / Percentil alto · Ajustar plan nutricional"
                            }
                            val statusColor = if (bmi in 13.8..18.2) PequeGreen else PequeOrange
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = statusColor.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = nutritionalStatus,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "IMC $formattedBmi",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = statusColor
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (activeChild == null) {
                                    onAddChildClicked()
                                } else if (w > 0.0 || h > 0.0) {
                                    val pcLabel = if (headCircInput.isNotBlank()) " · PC: $headCircInput cm" else ""
                                    onAddRecord(
                                        HealthRecord(
                                            childId = activeChild.id,
                                            childName = activeChild.name,
                                            category = "CRECIMIENTO",
                                            title = "Peso: $w kg · Talla: $h cm$pcLabel",
                                            subtitle = "IMC: $formattedBmi kg/m² · $percentileChoice",
                                            dateText = "Hoy",
                                            status = "Percentil Normal",
                                            notes = "Evaluado por ${pediatricianProfile.name}."
                                        )
                                    )
                                    onEditActiveChild(
                                        activeChild.copy(
                                            weightKg = w,
                                            heightCm = h,
                                            lastCheckupDate = "Hoy",
                                            lastCheckupSummary = "Control crecimiento: $w kg, $h cm ($percentileChoice)"
                                        )
                                    )
                                }
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = PequeGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guardar Medición", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (activeChild != null) {
                item {
                    TannerTargetHeightCalculatorCard(
                        activeChild = activeChild,
                        onSaveTargetHeightRecord = { rec -> onAddRecord(rec) }
                    )
                }
            }

            val childGrowthList = records.filter {
                it.category == "CRECIMIENTO" &&
                    (activeChild == null || it.childId == activeChild.id || it.childName == activeChild.name)
            }
            if (childGrowthList.isNotEmpty()) {
                item {
                    Text(
                        text = "Historial de Somatometría (${childGrowthList.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                itemsIndexed(childGrowthList, key = { idx, gr -> "${gr.id.ifBlank { "grow_doc" }}_$idx" }) { _, gr ->
                    ClinicalRecordSimpleCard(gr)
                }
            }
        } else {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Aplicar Vacuna Oficial o Esquema PAI por Edad",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Influenza Estacional", "SRP (Triple Viral)", "DPT Refuerzo", "Neumococo 13V", "Rotavirus", "Hexavalente", "BCG + Hep B", "VPH").forEach { v ->
                                FilterChip(
                                    selected = vaccineName == v,
                                    onClick = { vaccineName = v },
                                    label = { Text(v) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = vaccineName,
                                onValueChange = { vaccineName = it },
                                label = { Text("Vacuna") },
                                placeholder = { Text("Ej. SRP / Influenza") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = vaccineLot,
                                onValueChange = { vaccineLot = it },
                                label = { Text("Lote Oficial") },
                                placeholder = { Text("Ej. LOTE-2026") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = vaccineDose,
                            onValueChange = { vaccineDose = it },
                            label = { Text("Dosis / Esquema") },
                            placeholder = { Text("Ej. 1ra dosis, 2da dosis o Refuerzo anual") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (activeChild == null) {
                                        onAddChildClicked()
                                    } else if (vaccineName.isNotBlank()) {
                                        onAddRecord(
                                            HealthRecord(
                                                childId = activeChild.id,
                                                childName = activeChild.name,
                                                category = "VACUNA",
                                                title = vaccineName.trim(),
                                                subtitle = vaccineDose.trim().ifBlank { "Dosis aplicada" },
                                                dateText = "Hoy",
                                                status = "Aplicada",
                                                notes = "Lote: ${vaccineLot.ifBlank { "Registro clínico" }} · ${pediatricianProfile.name}."
                                            )
                                        )
                                        vaccineName = ""
                                        vaccineDose = ""
                                        vaccineLot = ""
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = PequeBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Certificar Aplicada", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    if (activeChild == null) {
                                        onAddChildClicked()
                                    } else if (vaccineName.isNotBlank()) {
                                        onAddRecord(
                                            HealthRecord(
                                                childId = activeChild.id,
                                                childName = activeChild.name,
                                                category = "VACUNA",
                                                title = vaccineName.trim(),
                                                subtitle = vaccineDose.trim().ifBlank { "Próxima dosis PAI" },
                                                dateText = "Programada",
                                                status = "Pendiente",
                                                notes = "Programada en cartilla PAI por ${pediatricianProfile.name}."
                                            )
                                        )
                                        vaccineName = ""
                                        vaccineDose = ""
                                        vaccineLot = ""
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Programar Próxima", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            itemsIndexed(childVaccines, key = { idx, vac -> "${vac.id.ifBlank { "vac" }}_$idx" }) { _, vac ->
                val isApplied = vac.status.equals("Aplicada", ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${vac.title} (${vac.subtitle})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${vac.dateText} · ${vac.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { onToggleRecordStatus(vac) }) {
                            Text(
                                text = if (isApplied) "Aplicada" else "Pendiente",
                                color = if (isApplied) PequeGreen else PequeOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { onDeleteRecord(vac.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * ESTACIÓN 4 DEL PEDIATRA: Perinatal, Neurodesarrollo, Certificados y Soporte Clínico.
 */
@Composable
fun PediatricianClinicalSuiteScreen(
    pediatricianProfile: PediatricianProfile,
    children: List<ChildProfile>,
    activeChild: ChildProfile?,
    records: List<HealthRecord>,
    onSelectChild: (String) -> Unit,
    onAddChildClicked: () -> Unit,
    onEditActiveChild: (ChildProfile) -> Unit,
    onAddRecord: (HealthRecord) -> Unit,
    onOpenModule: (ClinicalModule) -> Unit,
    onOpenAiAssistant: () -> Unit,
    onSwitchToParentMode: () -> Unit
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0=Perinatal, 1=Neuro, 2=Laboratorio/Imagen, 3=Nutrición, 4=Certificados, 5=Módulos

    var sdgInput by remember { mutableStateOf("") }
    var apgarInput by remember { mutableStateOf("") }
    var screeningsSummary by remember { mutableStateOf("") }

    var neuroMilestoneTitle by remember { mutableStateOf("") }
    var neuroDetails by remember { mutableStateOf("") }

    var labOrderTitle by remember { mutableStateOf("") }
    var labOrderNotes by remember { mutableStateOf("") }

    var nutritionPlanTitle by remember { mutableStateOf("") }
    var nutritionPlanNotes by remember { mutableStateOf("") }

    var certType by remember { mutableStateOf("") }
    var certBody by remember(activeChild) { mutableStateOf("") }

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
            PediatricianClinicalHeader(
                pediatricianProfile = pediatricianProfile,
                children = children,
                activeChild = activeChild,
                onSelectChild = onSelectChild,
                onAddChildClicked = onAddChildClicked,
                onEditActiveChild = onEditActiveChild,
                onSwitchToParentMode = onSwitchToParentMode
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAiAssistant() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = CardAiIcon, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Síntesis y Soporte Clínico",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Síntesis SOAP, dosis mg/kg y diagnóstico diferencial",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            val sections = listOf(
                "Perinatal",
                "Neurodesarrollo",
                "Lab y Gabinete",
                "Nutrición y Lactancia",
                "Certificados",
                "Módulos"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sections.forEachIndexed { idx, label ->
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
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Historia Perinatal y Tamizajes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = sdgInput,
                                onValueChange = { sdgInput = it },
                                label = { Text("SDG y Vía de Nacimiento") },
                                placeholder = { Text("Ej. 39.0 SDG · Parto Eutócico / Cesárea") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = apgarInput,
                                onValueChange = { apgarInput = it },
                                label = { Text("APGAR y Somatometría Neonatal") },
                                placeholder = { Text("Ej. APGAR 9/10 · Peso: 3,250 g · Talla: 50 cm") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = screeningsSummary,
                                onValueChange = { screeningsSummary = it },
                                label = { Text("Tamizajes Neonatales") },
                                placeholder = { Text("Ej. Metabólico, Auditivo, Cardiológico, Visual y Cadera: Normales") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    if (activeChild == null) {
                                        onAddChildClicked()
                                    } else if (sdgInput.isNotBlank() || apgarInput.isNotBlank() || screeningsSummary.isNotBlank()) {
                                        onAddRecord(
                                            HealthRecord(
                                                childId = activeChild.id,
                                                childName = activeChild.name,
                                                category = "PERINATAL",
                                                title = sdgInput.ifBlank { "Antecedente Perinatal" },
                                                subtitle = apgarInput.ifBlank { "Registro neonatal" },
                                                dateText = "Perinatal",
                                                status = "Normal",
                                                notes = screeningsSummary.ifBlank { "Tamizajes registrados." }
                                            )
                                        )
                                        sdgInput = ""
                                        apgarInput = ""
                                        screeningsSummary = ""
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = PequeBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Guardar Perinatal", color = Color.White)
                            }
                        }
                    }
                }
                itemsIndexed(childRecords.filter { it.category == "PERINATAL" }, key = { idx, rec -> "${rec.id.ifBlank { "peri" }}_$idx" }) { _, rec ->
                    ClinicalRecordSimpleCard(rec)
                }
            }

            1 -> {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Evaluación del Neurodesarrollo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = neuroMilestoneTitle,
                                onValueChange = { neuroMilestoneTitle = it },
                                label = { Text("Etapa / Hito evaluado") },
                                placeholder = { Text("Ej. Control de 6 meses / 12 meses / 2 años") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = neuroDetails,
                                onValueChange = { neuroDetails = it },
                                label = { Text("Hitos observados") },
                                placeholder = { Text("Ej. Motor grueso, fino, lenguaje y social acordes a la edad...") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    if (activeChild == null) {
                                        onAddChildClicked()
                                    } else if (neuroMilestoneTitle.isNotBlank() || neuroDetails.isNotBlank()) {
                                        onAddRecord(
                                            HealthRecord(
                                                childId = activeChild.id,
                                                childName = activeChild.name,
                                                category = "NEURODESARROLLO",
                                                title = neuroMilestoneTitle.ifBlank { "Evaluación de Neurodesarrollo" },
                                                subtitle = pediatricianProfile.name,
                                                dateText = "Hoy",
                                                status = "Acorde a edad",
                                                notes = neuroDetails.ifBlank { "Sin signos de alarma." }
                                            )
                                        )
                                        neuroMilestoneTitle = ""
                                        neuroDetails = ""
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = PequePurple),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Guardar Evaluación", color = Color.White)
                            }
                        }
                    }
                }
                itemsIndexed(childRecords.filter { it.category == "NEURODESARROLLO" }, key = { idx, rec -> "${rec.id.ifBlank { "neuro" }}_$idx" }) { _, rec ->
                    ClinicalRecordSimpleCard(rec)
                }
            }

            2 -> {
                item {
                    val labPresets = listOf(
                        "Biometría Hemática Completa",
                        "Examen General de Orina + Urocultivo",
                        "Coproparasitoscópico (3 muestras)",
                        "PCR, VSG y Procalcitonina",
                        "Perfil Férrico y Ferritina",
                        "Panel Viral Respiratorio",
                        "Radiografía de Tórax AP y Lateral"
                    )
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Emitir Orden de Laboratorio o Gabinete", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                labPresets.forEach { preset ->
                                    FilterChip(
                                        selected = labOrderTitle == preset,
                                        onClick = {
                                            labOrderTitle = preset
                                            if (labOrderNotes.isBlank()) {
                                                labOrderNotes = "Acudir en ayuno según indicación de laboratorio y subir resultados en el Escritorio de Padres."
                                            }
                                        },
                                        label = { Text(preset) }
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = labOrderTitle,
                                onValueChange = { labOrderTitle = it },
                                label = { Text("Estudio solicitado") },
                                placeholder = { Text("Ej. Biometría Hemática, Urocultivo, Rx...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = labOrderNotes,
                                onValueChange = { labOrderNotes = it },
                                label = { Text("Indicaciones clínicas y preparación") },
                                placeholder = { Text("Ej. Ayuno de 4 horas, muestra matutina...") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    if (activeChild == null) {
                                        onAddChildClicked()
                                    } else if (labOrderTitle.isNotBlank()) {
                                        onAddRecord(
                                            HealthRecord(
                                                childId = activeChild.id,
                                                childName = activeChild.name,
                                                category = "LABORATORIO",
                                                title = "Orden: ${labOrderTitle.trim()}",
                                                subtitle = "Solicitado por ${pediatricianProfile.name}",
                                                dateText = "Hoy",
                                                status = "Solicitado",
                                                notes = labOrderNotes.trim().ifBlank { "Realizar estudio y adjuntar resultado en la app." }
                                            )
                                        )
                                        labOrderTitle = ""
                                        labOrderNotes = ""
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = PequeOrange),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Enviar Orden a los Padres", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                itemsIndexed(
                    childRecords.filter { it.category == "LABORATORIO" || it.category == "ESTUDIO_PADRE" },
                    key = { idx, rec -> "${rec.id.ifBlank { "lab" }}_$idx" }
                ) { _, rec ->
                    ClinicalRecordSimpleCard(rec)
                }
            }

            3 -> {
                item {
                    val nutritionPresets = listOf(
                        "Lactancia materna a libre demanda" to "Asegurar tomas frecuentes (8-12 al día), buen agarre y suplementación con Vitamina D (400 UI/día).",
                        "Inicio de Alimentación Complementaria (6m)" to "Introducir alimentos ricos en hierro (carne, legumbres, verduras y frutas naturales) en texturas progresivas, regla de 3 días para alérgenos, sin sal ni azúcar añadida.",
                        "Plan de Dieta Astringente e Hidratación" to "Ofrecer suero de rehidratación oral tras cada evacuación y dieta blanda (arroz, pollo hervido, manzana, plátano).",
                        "Plato Saludable Escolar y Hierro" to "3 comidas principales y 2 colaciones saludables; priorizar proteína magra, vegetales verdes, frutas enteras y agua natural."
                    )
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Plan Nutricional, Lactancia y Ablactación", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                nutritionPresets.forEach { (title, body) ->
                                    FilterChip(
                                        selected = nutritionPlanTitle == title,
                                        onClick = {
                                            nutritionPlanTitle = title
                                            nutritionPlanNotes = body
                                        },
                                        label = { Text(title.take(26) + "…") }
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = nutritionPlanTitle,
                                onValueChange = { nutritionPlanTitle = it },
                                label = { Text("Título del Plan Nutricional") },
                                placeholder = { Text("Ej. Alimentación complementaria 6 meses...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = nutritionPlanNotes,
                                onValueChange = { nutritionPlanNotes = it },
                                label = { Text("Pautas detalladas para casa") },
                                placeholder = { Text("Alimentos permitidos, horarios, suplementos...") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    if (activeChild == null) {
                                        onAddChildClicked()
                                    } else if (nutritionPlanTitle.isNotBlank()) {
                                        onAddRecord(
                                            HealthRecord(
                                                childId = activeChild.id,
                                                childName = activeChild.name,
                                                category = "NUTRICION",
                                                title = nutritionPlanTitle.trim(),
                                                subtitle = "Plan Nutricional · ${pediatricianProfile.name}",
                                                dateText = "Hoy",
                                                status = "Activo",
                                                notes = nutritionPlanNotes.trim().ifBlank { "Seguir pauta nutricional indicada." }
                                            )
                                        )
                                        nutritionPlanTitle = ""
                                        nutritionPlanNotes = ""
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = PequeGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Enviar Plan Nutricional a Padres", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                itemsIndexed(
                    childRecords.filter { it.category == "NUTRICION" },
                    key = { idx, rec -> "${rec.id.ifBlank { "nutri" }}_$idx" }
                ) { _, rec ->
                    ClinicalRecordSimpleCard(rec)
                }
            }

            4 -> {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Emitir Certificado o Interconsulta", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "Certificado de Buena Salud Escolar",
                                    "Constancia de Aptitud Física",
                                    "Justificante de Asistencia Médica",
                                    "Referimiento a Subespecialidad"
                                ).forEach { preset ->
                                    FilterChip(
                                        selected = certType == preset,
                                        onClick = {
                                            certType = preset
                                            if (certBody.isBlank()) {
                                                certBody = "Se hace constar que ${activeChild?.name ?: "el/la paciente"} fue evaluado(a) clínicamente encontrándose en adecuadas condiciones de salud."
                                            }
                                        },
                                        label = { Text(preset) }
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = certType,
                                onValueChange = { certType = it },
                                label = { Text("Tipo de Documento") },
                                placeholder = { Text("Ej. Certificado Escolar, Referimiento...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = certBody,
                                onValueChange = { certBody = it },
                                label = { Text("Contenido") },
                                placeholder = { Text("Escribe el texto del certificado o interconsulta...") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    if (activeChild == null) {
                                        onAddChildClicked()
                                    } else if (certType.isNotBlank() || certBody.isNotBlank()) {
                                        onAddRecord(
                                            HealthRecord(
                                                childId = activeChild.id,
                                                childName = activeChild.name,
                                                category = "CERTIFICADO",
                                                title = certType.ifBlank { "Certificado Pediátrico" },
                                                subtitle = "${pediatricianProfile.name} · ${pediatricianProfile.licenseId}",
                                                dateText = "Hoy",
                                                status = "Emitido",
                                                notes = certBody.ifBlank { "Documento médico oficial." }
                                            )
                                        )
                                        certType = ""
                                        certBody = ""
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = PequeGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Firmar y Enviar a Padres", color = Color.White)
                            }
                        }
                    }
                }
                itemsIndexed(childRecords.filter { it.category == "CERTIFICADO" || it.category == "DOCUMENTO" }, key = { idx, rec -> "${rec.id.ifBlank { "cert" }}_$idx" }) { _, rec ->
                    ClinicalRecordSimpleCard(rec)
                }
            }

            5 -> {
                items(ClinicalModule.entries) { mod ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenModule(mod) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mod.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(mod.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClinicalRecordSimpleCard(rec: HealthRecord) {
    val context = LocalContext.current
    val cleanNotes = remember(rec.notes) { rec.notes.replace("[PUSH]", "").trim() }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rec.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${rec.subtitle} · ${rec.dateText}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PequeBlue
                    )
                }
                TextButton(
                    onClick = {
                        PediatricPdfAndShareHelper.generateAndShareRecordPdf(
                            context = context,
                            child = null,
                            record = rec
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Exportar PDF",
                        tint = PequeBlue,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PDF",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PequeBlue
                    )
                }
            }
            if (cleanNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = cleanNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Evaluador de Escalas Pediátricas de Guardia (TEP, Wood-Downes, Deshidratación)
 * y Calculadora de Líquidos Holliday-Segar + Plan B OMS.
 */
@Composable
fun PediatricScalesAndFluidsDialog(
    child: ChildProfile,
    pediatricianName: String,
    onDismiss: () -> Unit,
    onSaveEvaluation: (HealthRecord) -> Unit
) {
    var tepStatus by remember { mutableStateOf("TEP Estable (3 lados normales)") }
    var respScale by remember { mutableStateOf("Wood-Downes 0 pts (Sin dificultad)") }
    var dehydrationScale by remember { mutableStateOf("Hidratado · Plan A OMS") }
    var weightInput by remember(child) {
        mutableStateOf(if (child.weightKg > 0.0) child.weightKg.toString() else "")
    }

    val w = weightInput.replace(",", ".").toDoubleOrNull() ?: child.weightKg
    val dailyFluids = calculateHollidaySegarDailyMl(w)
    val hourlyFluids = dailyFluids / 24.0
    val planBOrs4Hours = w * 75.0 // 75 mL/kg en 4 horas OMS
    val bsa = calculateMostellerBsa(w, child.heightCm)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Escalas Clínicas y Líquidos (${child.name.substringBefore(" ")})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Peso para cálculo hídrico (kg)") },
                    placeholder = { Text("Ej. 14.5") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PequeBlue.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, PequeBlue.copy(alpha = 0.30f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Holliday-Segar: ${String.format(Locale.US, "%.0f mL/día (%.1f mL/h)", dailyFluids, hourlyFluids)}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = PequeBlue
                        )
                        Text(
                            text = "Plan B SRO OMS (75 mL/kg/4h): ${String.format(Locale.US, "%.0f mL en 4 horas", planBOrs4Hours)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (bsa > 0.0) {
                            Text(
                                text = "Superficie Corporal Mosteller: ${String.format(Locale.US, "%.2f m²", bsa)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text("1. Triángulo de Evaluación Pediátrica (TEP)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "TEP Estable (3 lados normales)",
                        "Dificultad Respiratoria",
                        "Choque Compensado",
                        "Disfunción SNC"
                    ).forEach { opt ->
                        FilterChip(
                            selected = tepStatus == opt,
                            onClick = { tepStatus = opt },
                            label = { Text(opt) }
                        )
                    }
                }

                Text("2. Escala Respiratoria (Wood-Downes / Westley)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Wood-Downes 0 pts (Sin dificultad)",
                        "Leve (1-3 pts · Vigilancia)",
                        "Moderada (4-7 pts · Nebulización)",
                        "Grave (>7 pts · Urgencias)"
                    ).forEach { opt ->
                        FilterChip(
                            selected = respScale == opt,
                            onClick = { respScale = opt },
                            label = { Text(opt) }
                        )
                    }
                }

                Text("3. Hidratación (Gorelick / Plan OMS)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Hidratado · Plan A OMS",
                        "Deshidratación Leve-Mod · Plan B (75 mL/kg)",
                        "Deshidratación Grave · Plan C IV"
                    ).forEach { opt ->
                        FilterChip(
                            selected = dehydrationScale == opt,
                            onClick = { dehydrationScale = opt },
                            label = { Text(opt) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val summaryNotes = "TEP: $tepStatus | Resp: $respScale | Hidratación: $dehydrationScale | " +
                        "Holliday-Segar: ${String.format(Locale.US, "%.0f mL/día (%.1f mL/h)", dailyFluids, hourlyFluids)} · " +
                        "Plan B SRO: ${String.format(Locale.US, "%.0f mL/4h", planBOrs4Hours)}."
                    onSaveEvaluation(
                        HealthRecord(
                            childId = child.id,
                            childName = child.name,
                            category = "CONSULTA",
                            title = "Valoración Clínica y Plan Hídrico",
                            subtitle = "$tepStatus · $dehydrationScale",
                            dateText = "Hoy",
                            status = "Indicación activa",
                            notes = "Evaluado por $pediatricianName: $summaryNotes"
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PequePurple)
            ) {
                Text("Guardar y Enviar a Padres", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
