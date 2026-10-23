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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CardAiIcon
import com.example.ui.theme.PequeBlue
import com.example.ui.theme.PequeGreen
import com.example.ui.theme.PequeOrange
import com.example.ui.theme.PequePurple
import java.util.Locale

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
 * Cabecera Clínica Minimalista del Pediatra.
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
        // Fila 1: Título clínico + botón Vista Padres
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pediatricianProfile.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Estación Pediátrica · ${pediatricianProfile.licenseId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                placeholder = { Text("Buscar paciente o código PEQUE-...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pediatrician_patient_search_input")
            )
        }

        // Selector horizontal minimalista de pacientes
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

        // Banda compacta de datos clínicos esenciales
        if (activeChild != null) {
            val heightMeters = (activeChild.heightCm / 100.0).coerceAtLeast(0.5)
            val bmi = activeChild.weightKg / (heightMeters * heightMeters)
            val hasAllergy = !activeChild.allergies.equals("Ninguna", ignoreCase = true) &&
                !activeChild.allergies.contains("Ninguna", ignoreCase = true)

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (hasAllergy) AlertRed.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${activeChild.name} · ${activeChild.familyLinkCode()} · Grupo ${activeChild.bloodType}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${activeChild.weightKg} kg · ${activeChild.heightCm} cm · IMC ${
                                String.format(Locale.US, "%.1f", bmi)
                            } · Alergia: ${activeChild.allergies}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (hasAllergy) FontWeight.Bold else FontWeight.Normal,
                            color = if (hasAllergy) AlertRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showEditChildModal = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar paciente",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * ESTACIÓN 1 DEL PEDIATRA: Pacientes y Triaje de Casa (Minimalista con pestañas limpias).
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
    onSwitchToParentMode: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Síntomas, 1=Dudas, 2=Diario/Estudios, 3=Indicaciones Enviadas

    val childRecords = remember(records, activeChild) {
        if (activeChild == null) emptyList()
        else records.filter { it.childId == activeChild.id || it.childName == activeChild.name }
    }

    val homeSymptoms = childRecords.filter { it.category == "SINTOMA_CASA" }
    val parentDoubts = childRecords.filter { it.category == "DUDA_PADRE" }
    val homeDiaryAndStudies = childRecords.filter { it.category == "DIARIO_BEBE" || it.category == "ESTUDIO_PADRE" }
    val doctorIndications = childRecords.filter {
        it.category in listOf("MEDICAMENTO", "CONSULTA", "CERTIFICADO") ||
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
                            onAddRecord(
                                HealthRecord(
                                    childId = activeChild.id,
                                    childName = activeChild.name,
                                    category = "CONSULTA",
                                    title = indTitle.trim(),
                                    subtitle = "Indicación de ${pediatricianProfile.name}",
                                    dateText = "Hoy",
                                    status = "Indicación activa",
                                    notes = indDetails.trim().ifBlank { "Seguir indicaciones médicas en casa." }
                                )
                            )
                            onEditActiveChild(
                                activeChild.copy(
                                    lastCheckupDate = "Hoy",
                                    lastCheckupSummary = "${indTitle.trim()}: ${indDetails.trim().take(120)}"
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
                                "Indicación enviada al Escritorio de Padres"
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

        // Botón rápido para enviar una indicación directa al Escritorio de Padres
        if (activeChild != null) {
            item {
                Button(
                    onClick = { showDirectIndicationDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PequeBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("send_direct_indication_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enviar Indicación Médica a los Padres (${activeChild.name.substringBefore(" ")})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
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

        if (visibleRecords.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sin registros en esta categoría para ${activeChild?.name ?: "este paciente"}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
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
private fun ClinicalParentInboxCard(
    record: HealthRecord,
    badgeText: String,
    badgeColor: Color,
    actionLabel: String,
    onReplyClicked: () -> Unit
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
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
                Text(
                    text = record.dateText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = record.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = record.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    var activeToolTab by remember { mutableIntStateOf(0) } // 0 = Calculadora Dosis, 1 = Nota SOAP y Signos Vitales
    var isGeneratingSoapDraft by remember { mutableStateOf(false) }
    val vitalRef = getPediatricVitalReference(activeChild?.ageYears ?: 5L)

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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PequeBlue.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
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
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedDrug.parentFriendlyTip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                                            notes = "Indicado por ${pediatricianProfile.name}: ${selectedDrug.parentFriendlyTip} · ($mgKgNum mg/kg para $weightNum kg)."
                                        )
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
                                    val vitalsText = "FC: ${hrInput.ifBlank { "N/R" }} lpm, FR: ${rrInput.ifBlank { "N/R" }} rpm, Temp: ${tempInput.ifBlank { "N/R" }} °C, SpO2: ${spo2Input.ifBlank { "N/R" }}%"
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
                                    val vitalsText = "FC: ${hrInput.ifBlank { "-" }} lpm, FR: ${rrInput.ifBlank { "-" }} rpm, Temp: ${tempInput.ifBlank { "-" }} °C, SpO2: ${spo2Input.ifBlank { "-" }}%"
                                    val planSummary = clinicalSoapNotes.trim().ifBlank { "Seguir indicaciones médicas en casa." }
                                    onAddRecord(
                                        HealthRecord(
                                            childId = activeChild.id,
                                            childName = activeChild.name,
                                            category = "CONSULTA",
                                            title = cleanDiag,
                                            subtitle = "${pediatricianProfile.name} · Plan e Indicaciones",
                                            dateText = "Hoy",
                                            status = "Completada",
                                            notes = "Vitales: $vitalsText. | EF: $examSummary. | Plan para Padres: $planSummary"
                                        )
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
                val w = weightInput.toDoubleOrNull() ?: (activeChild?.weightKg ?: 0.0)
                val h = heightInput.toDoubleOrNull() ?: (activeChild?.heightCm ?: 0.0)
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
                            listOf("Percentil 50-75 OMS", "Percentil 25-50 OMS", "Percentil 85-95 OMS", "Percentil <15 OMS").forEach { p ->
                                FilterChip(
                                    selected = percentileChoice == p,
                                    onClick = { percentileChoice = p },
                                    label = { Text(p) }
                                )
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
                            text = "Aplicar Vacuna Oficial",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Influenza Estacional", "SRP (Triple Viral)", "DPT Refuerzo", "Neumococo 13V", "Rotavirus", "VPH").forEach { v ->
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
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Certificar Vacuna", color = Color.White, fontWeight = FontWeight.Bold)
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
    var selectedSection by remember { mutableIntStateOf(0) } // 0=Perinatal, 1=Neuro, 2=Certificados, 3=Módulos

    var sdgInput by remember { mutableStateOf("") }
    var apgarInput by remember { mutableStateOf("") }
    var screeningsSummary by remember { mutableStateOf("") }

    var neuroMilestoneTitle by remember { mutableStateOf("") }
    var neuroDetails by remember { mutableStateOf("") }

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
            val sections = listOf("Perinatal", "Neurodesarrollo", "Certificados", "Módulos")
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

            3 -> {
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
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
            if (rec.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = rec.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
