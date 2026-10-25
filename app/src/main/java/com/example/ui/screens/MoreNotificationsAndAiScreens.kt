package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ChildProfile
import com.example.data.ClinicalModule
import com.example.data.HealthRecord
import com.example.ui.AiAssistantUiState
import com.example.ui.AiAssistantViewModel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CardAiBg
import com.example.ui.theme.CardAiIcon
import com.example.ui.theme.CardAjustesBg
import com.example.ui.theme.CardAjustesIcon
import com.example.ui.theme.CardCalendarioIcon
import com.example.ui.theme.CardCrecimientoIcon
import com.example.ui.theme.CardDocumentosIcon
import com.example.ui.theme.CardHospitalBg
import com.example.ui.theme.CardHospitalIcon
import com.example.ui.theme.CardLaboratoriosIcon
import com.example.ui.theme.CardMedicamentosIcon
import com.example.ui.theme.CardVacunasIcon
import com.example.ui.theme.PequeBlue
import com.example.ui.theme.PequeGreen
import com.example.ui.theme.PequePurple
import com.example.ui.theme.adaptiveCardTint

@Composable
fun AiAssistantScreen(
    activeChild: ChildProfile?,
    allChildren: List<ChildProfile>,
    records: List<HealthRecord>,
    onBack: () -> Unit,
    onSelectChild: (String) -> Unit,
    isPediatricianMode: Boolean = false,
    aiViewModel: AiAssistantViewModel = viewModel()
) {
    BackHandler { onBack() }

    val aiState by aiViewModel.uiState.collectAsStateWithLifecycle()
    var questionInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Column {
                    Text(
                        text = if (isPediatricianMode) "Soporte de Decisión Clínica" else "Guía y Resumen de Consulta",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isPediatricianMode) {
                            "Síntesis clínica, dosis mg/kg y evolución SOAP"
                        } else {
                            "Explicaciones en lenguaje sencillo para mamá y papá"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        if (activeChild != null && allChildren.size > 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allChildren.forEach { child ->
                        FilterChip(
                            selected = child.id == activeChild.id,
                            onClick = { onSelectChild(child.id) },
                            label = { Text(child.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CardAiIcon,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = adaptiveCardTint(CardAiBg, CardAiIcon)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Contexto activo: ${activeChild?.name ?: "Familia López"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Preguntas sobre el historial\n• Resumen para consulta médica\n• Información rápida (solo con datos registrados)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            aiViewModel.generateConsultationSummary(activeChild, records, isPediatricianMode)
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = CardAiIcon),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_consultation_summary_button")
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPediatricianMode) "Generar Síntesis Clínica SOAP" else "Generar Resumen Sencillo para Consulta",
                            color = Color.White
                        )
                    }
                }
            }
        }

        item {
            val quickQuestions = if (isPediatricianMode) {
                listOf(
                    "Integra los síntomas reportados en casa con sus antecedentes y alergias",
                    "Calcula opciones terapéuticas seguras considerando su peso y alergias",
                    "Resume su curva somatometrica, estado vacunal PAI y tamizajes"
                )
            } else {
                listOf(
                    "Explícame en palabras sencillas las recetas e indicaciones del pediatra",
                    "¿Qué cuidados en casa y signos de alarma debo vigilar hoy?",
                    "¿Qué vacunas tiene al día y cuál le toca próximamente?"
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isPediatricianMode) "Consultas clínicas rápidas:" else "Preguntas frecuentes de mamá y papá:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                quickQuestions.forEach { q ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                questionInput = q
                                aiViewModel.askQuestionStream(q, activeChild, records, isPediatricianMode)
                            }
                    ) {
                        Text(
                            text = q,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = questionInput,
                    onValueChange = { questionInput = it },
                    placeholder = {
                        Text(
                            if (isPediatricianMode) "Consulta clínica sobre ${activeChild?.name ?: "el paciente"}..."
                            else "Escribe tu duda sobre ${activeChild?.name?.substringBefore(" ") ?: "tu hijo"}..."
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_question_input")
                )
                IconButton(
                    onClick = {
                        aiViewModel.askQuestionStream(questionInput, activeChild, records, isPediatricianMode)
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(PequeBlue)
                        .testTag("ai_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar pregunta",
                        tint = Color.White
                    )
                }
            }
        }

        item {
            when (val st = aiState) {
                is AiAssistantUiState.Idle -> {}
                is AiAssistantUiState.Loading -> {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = CardAiIcon,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Analizando los registros clínicos de ${activeChild?.name ?: "tu familia"}...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                is AiAssistantUiState.Streaming -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Respuesta en tiempo real",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = st.partialText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                is AiAssistantUiState.Success -> {
                    val structured = st.structuredSummary
                    if (structured != null) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Resumen Ejecutivo para Consulta Pediátrica",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = structured.resumenGeneral,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (structured.alertasImportantes.isNotEmpty()) {
                                    Text(
                                        text = "Alertas y Puntos Clave:",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = AlertRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                    structured.alertasImportantes.forEach { alerta ->
                                        Text(
                                            text = "• $alerta",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                if (structured.preguntasSugeridasPediatra.isNotEmpty()) {
                                    Text(
                                        text = "Preguntas sugeridas para la pediatra:",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    structured.preguntasSugeridasPediatra.forEach { preg ->
                                        Text(
                                            text = "• $preg",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Análisis del Expediente",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = st.responseText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                is AiAssistantUiState.Error -> {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = adaptiveCardTint(Color(0xFFFEECEB), AlertRed)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = st.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AlertRed,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsTabScreen(
    children: List<ChildProfile>,
    records: List<HealthRecord>,
    onOpenVaccines: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    val pendingItems = records.filter {
        it.status.equals("Pendiente", ignoreCase = true) ||
            it.status.equals("Programada", ignoreCase = true) ||
            it.status.equals("Activo", ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Notificaciones y Alertas",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Recordatorios de vacunas, próximas citas y alergias",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(children.filter { it.allergies.isNotBlank() && !it.allergies.equals("Ninguna", true) }, key = { "allergy_${it.id}" }) { child ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = adaptiveCardTint(Color(0xFFFEECEB), AlertRed)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AlertRed
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Alerta de Alergia: ${child.name}",
                            style = MaterialTheme.typography.titleSmall,
                            color = AlertRed,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Alergia activa a: ${child.allergies} • Grupo sanguíneo ${child.bloodType}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        items(pendingItems, key = { it.id }) { rec ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (rec.category == "VACUNA") onOpenVaccines() else onOpenCalendar()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (rec.category == "VACUNA") Icons.Default.Vaccines else Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${rec.childName.substringBefore(" ")} • ${rec.title}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${rec.subtitle} • ${rec.dateText}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = adaptiveCardTint(Color(0xFFFFF4DC), CardMedicamentosIcon)
                    ) {
                        Text(
                            text = rec.status,
                            style = MaterialTheme.typography.labelSmall,
                            color = CardMedicamentosIcon,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

data class FullStructureCardData(
    val title: String,
    val icon: ImageVector,
    val iconBg: Color,
    val cardBg: Color,
    val bullets: List<String>,
    val onClick: () -> Unit
)

/**
 * Pestaña 4 de la barra inferior ("Más"):
 * Incluye el control destacado de **Modo Oscuro** arriba y en Ajustes, además de la
 * "Estructura completa de PequeSalud" y opciones de cuenta.
 */
@Composable
fun MoreAndFullStructureScreen(
    userEmail: String,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateChildren: () -> Unit,
    onOpenModule: (ClinicalModule) -> Unit,
    onOpenAiAssistant: () -> Unit,
    onSignOut: () -> Unit,
    isPediatricianMode: Boolean = false,
    pediatricianName: String = "Dr. Alejandro López",
    currentPin: String = "1234",
    onRequestSwitchWorkspace: () -> Unit = {},
    onOpenPinSettings: () -> Unit = {},
    onClearAllData: () -> Unit = {},
    onOpenHybridPwaHub: () -> Unit = {}
) {
    var cloudSyncEnabled by remember { mutableStateOf(true) }
    var biometricPrivacyEnabled by remember { mutableStateOf(true) }
    var showConfirmResetDialog by remember { mutableStateOf(false) }

    if (showConfirmResetDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmResetDialog = false },
            title = {
                Text(
                    text = "Reiniciar expediente en blanco",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Deseas eliminar todos los pacientes y registros actuales para dejar la aplicación completamente limpia y empezar desde cero?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmResetDialog = false
                        onClearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    modifier = Modifier.testTag("confirm_clear_all_data_button")
                ) {
                    Text("Sí, dejar en blanco", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmResetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val structureCards = listOf(
        FullStructureCardData(
            title = "Versión Web (PWA)",
            icon = Icons.Default.CloudDone,
            iconBg = PequeBlue,
            cardBg = Color(0xFFF0F6FF),
            bullets = listOf("Vista web instalable", "Escritorio Padres y Pediatra", "Curvas OMS"),
            onClick = onOpenHybridPwaHub
        ),
        FullStructureCardData(
            title = "Inicio",
            icon = Icons.Default.Home,
            iconBg = PequeBlue,
            cardBg = Color(0xFFF0F6FF),
            bullets = listOf("Resumen general", "Accesos rápidos", "Próximas citas", "Alertas y recordatorios"),
            onClick = onNavigateHome
        ),
        FullStructureCardData(
            title = "Mis hijos",
            icon = Icons.Default.Groups,
            iconBg = PequeGreen,
            cardBg = Color(0xFFEAFBF3),
            bullets = listOf("Perfiles de cada niño", "Datos personales", "Contacto de emergencia", "Tarjeta médica (QR)"),
            onClick = onNavigateChildren
        ),
        FullStructureCardData(
            title = "Historial clínico",
            icon = Icons.Default.Description,
            iconBg = CardVacunasIcon,
            cardBg = Color(0xFFF3F0FF),
            bullets = listOf("Línea de tiempo", "Consultas médicas", "Diagnósticos", "Tratamientos"),
            onClick = { onOpenModule(ClinicalModule.HISTORIAL) }
        ),
        FullStructureCardData(
            title = "Vacunas",
            icon = Icons.Default.Vaccines,
            iconBg = CardMedicamentosIcon,
            cardBg = Color(0xFFFFF8EB),
            bullets = listOf("Calendario de vacunación", "Dosis aplicadas y pendientes", "Certificados", "Recordatorios"),
            onClick = { onOpenModule(ClinicalModule.VACUNA) }
        ),
        FullStructureCardData(
            title = "Crecimiento y desarrollo",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            iconBg = CardCrecimientoIcon,
            cardBg = Color(0xFFEAFBF3),
            bullets = listOf("Peso, talla, IMC", "Percentiles", "Desarrollo psicomotor", "Gráficas"),
            onClick = { onOpenModule(ClinicalModule.CRECIMIENTO) }
        ),
        FullStructureCardData(
            title = "Medicamentos",
            icon = Icons.Default.Medication,
            iconBg = CardCalendarioIcon,
            cardBg = Color(0xFFFFF0F3),
            bullets = listOf("Medicamentos actuales", "Historial", "Recordatorios", "Alertas de alergias"),
            onClick = { onOpenModule(ClinicalModule.MEDICAMENTO) }
        ),
        FullStructureCardData(
            title = "Laboratorios",
            icon = Icons.Default.Science,
            iconBg = CardLaboratoriosIcon,
            cardBg = Color(0xFFF0F6FF),
            bullets = listOf("Resultados", "Documentos adjuntos", "Interpretación básica", "Historial"),
            onClick = { onOpenModule(ClinicalModule.LABORATORIO) }
        ),
        FullStructureCardData(
            title = "Consultas",
            icon = Icons.Default.Groups,
            iconBg = PequeGreen,
            cardBg = Color(0xFFEAFBF3),
            bullets = listOf("Agenda de citas", "Historial de consultas", "Especialistas", "Notas médicas"),
            onClick = { onOpenModule(ClinicalModule.CONSULTA) }
        ),
        FullStructureCardData(
            title = "Nutrición",
            icon = Icons.Default.Eco,
            iconBg = CardVacunasIcon,
            cardBg = Color(0xFFF3F0FF),
            bullets = listOf("Plan alimentario", "Evaluación nutricional", "Recomendaciones", "Seguimiento"),
            onClick = { onOpenModule(ClinicalModule.NUTRICION) }
        ),
        FullStructureCardData(
            title = "Documentos",
            icon = Icons.Default.FolderCopy,
            iconBg = CardDocumentosIcon,
            cardBg = Color(0xFFF0F6FF),
            bullets = listOf("Estudios de imágenes", "Informes médicos", "Resultados de laboratorio", "Archivos PDF / fotos"),
            onClick = { onOpenModule(ClinicalModule.DOCUMENTO) }
        ),
        FullStructureCardData(
            title = "Calendario",
            icon = Icons.Default.CalendarMonth,
            iconBg = CardCalendarioIcon,
            cardBg = Color(0xFFFFF0F3),
            bullets = listOf("Citas médicas", "Vacunas", "Laboratorios", "Recordatorios"),
            onClick = { onOpenModule(ClinicalModule.CALENDARIO) }
        ),
        FullStructureCardData(
            title = "Hospitalizaciones y emergencias",
            icon = Icons.Default.LocalHospital,
            iconBg = CardHospitalIcon,
            cardBg = CardHospitalBg,
            bullets = listOf("Ingresos hospitalarios", "Cirugías", "Procedimientos", "Transfusiones"),
            onClick = { onOpenModule(ClinicalModule.HOSPITALIZACION) }
        ),
        FullStructureCardData(
            title = "Resumen de consulta",
            icon = Icons.Default.Description,
            iconBg = CardAiIcon,
            cardBg = CardAiBg,
            bullets = listOf("Preguntas sobre el historial", "Resumen para consulta", "Información rápida", "(solo con datos registrados)"),
            onClick = onOpenAiAssistant
        ),
        FullStructureCardData(
            title = "Ajustes y PIN",
            icon = Icons.Default.Settings,
            iconBg = CardAjustesIcon,
            cardBg = CardAjustesBg,
            bullets = listOf("Configurar PIN Pediatra", "Cambiar de escritorio", "Privacidad y seguridad", "Modo oscuro"),
            onClick = onOpenPinSettings
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selector de Escritorio (Padres <-> Pediatra con PIN) en la parte superior de "Más"
        item {
            WorkspaceRoleBanner(
                isPediatricianMode = isPediatricianMode,
                pediatricianName = pediatricianName,
                currentPin = currentPin,
                onRequestSwitchMode = onRequestSwitchWorkspace,
                onOpenPinSettings = onOpenPinSettings
            )
        }

        // Tarjeta destacada de Modo Oscuro en la parte superior de la pestaña "Más"
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleDarkTheme(!isDarkTheme) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDarkTheme) PequePurple.copy(alpha = 0.25f)
                                    else Color(0xFFE3F2FD)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Modo oscuro",
                                tint = if (isDarkTheme) Color(0xFFC4B5FD) else PequeBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Modo oscuro",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isDarkTheme) "Activado • Descanso visual nocturno" else "Desactivado • Tema claro PequeSalud",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleDarkTheme(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PequeBlue
                        ),
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }
        }

        item {
            Text(
                text = "Módulos de PequeSalud",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        // Grid 2 columnas minimalista con las secciones de PequeSalud
        items(structureCards.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { card ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { card.onClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(card.iconBg.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = card.icon,
                                    contentDescription = card.title,
                                    tint = card.iconBg,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = card.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Text(
                                    text = card.bullets.firstOrNull() ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Panel de Ajustes interactivo
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ajustes de Cuenta y Familia",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cuenta activa: $userEmail",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = PequePurple)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Modo oscuro", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                Text("Cambiar apariencia clara u oscura", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = isDarkTheme, onCheckedChange = { onToggleDarkTheme(it) })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = PequeGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Sincronización en la nube", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                Text("Respaldo en tiempo real con Firebase", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = cloudSyncEnabled, onCheckedChange = { cloudSyncEnabled = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = PequeBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Privacidad y seguridad", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                Text("Tus datos pediátricos están aislados y protegidos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = biometricPrivacyEnabled, onCheckedChange = { biometricPrivacyEnabled = it })
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = CardVacunasIcon)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Soporte y Cuidadores", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            Text("Familia López • Crece sano. Crece acompañado.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PequeBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("PIN de Modo Pediatra", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                Text("PIN actual: $currentPin · Protege el escritorio clínico", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        OutlinedButton(
                            onClick = onOpenPinSettings,
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("settings_configure_pin_button")
                        ) {
                            Text("Configurar PIN", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onRequestSwitchWorkspace,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPediatricianMode) PequeGreen else PequeBlue
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_switch_mode_with_pin_button")
                    ) {
                        Icon(
                            imageVector = if (isPediatricianMode) Icons.Default.Home else Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPediatricianMode) "Volver al Escritorio de Padres" else "Cambiar a Modo Pediatra con PIN",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = { showConfirmResetDialog = true },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_all_data_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reiniciar expediente en blanco",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_out_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar sesión", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
