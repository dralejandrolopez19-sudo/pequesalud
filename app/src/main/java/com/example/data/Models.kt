package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Locale

enum class WorkspaceMode(val displayName: String, val badgeText: String) {
    PARENT(displayName = "Escritorio de Padres", badgeText = "Modo Padres"),
    PEDIATRICIAN(displayName = "Estación Clínica del Pediatra", badgeText = "Modo Pediatra")
}

data class PediatricianProfile(
    val name: String = "Dr. Alejandro López",
    val licenseId: String = "Exequátur 482-19 • CMD 19402",
    val specialty: String = "Pediatría y Puericultura",
    val clinicName: String = "Centro Pediátrico PequeSalud",
    val clinicPhone: String = "809-555-9010"
)

@IgnoreExtraProperties
data class ChildProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val ageYears: Long = 0L,
    val birthDate: String = "Por registrar",
    val gender: String = "Femenino",
    val bloodType: String = "O+",
    val allergies: String = "Ninguna",
    val pediatrician: String = "Dr. Alejandro López",
    val emergencyContact: String = "Por registrar",
    val lastCheckupDate: String = "Recién registrado",
    val lastCheckupSummary: String = "Sin indicaciones registradas aún",
    val avatarColorHex: String = "#FDE2E4",
    val weightKg: Double = 0.0,
    val heightCm: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun familyLinkCode(): String {
        val cleanPrefix = name
            .trim()
            .filter { it.isLetter() }
            .take(3)
            .uppercase(Locale.ROOT)
            .padEnd(3, 'X')
        val numericCode = ((ageYears + 2L) * 137L + name.length * 19L) % 900L + 100L
        return "PEQUE-$cleanPrefix-$numericCode"
    }

    fun hasDrugAllergyWarning(drugName: String): Boolean {
        val lowerAllergies = allergies.lowercase(Locale.ROOT)
        val lowerDrug = drugName.lowercase(Locale.ROOT)
        if (lowerAllergies.contains("ninguna") || lowerAllergies.isBlank()) return false
        if (lowerAllergies.contains("penicilina") || lowerAllergies.contains("betalact")) {
            if (lowerDrug.contains("amoxicilina") || lowerDrug.contains("ampicilina") || lowerDrug.contains("penicilina") || lowerDrug.contains("cef")) {
                return true
            }
        }
        if (lowerAllergies.contains("aines") || lowerAllergies.contains("ibuprofeno")) {
            if (lowerDrug.contains("ibuprofeno") || lowerDrug.contains("naproxeno") || lowerDrug.contains("diclofenaco")) {
                return true
            }
        }
        return lowerAllergies.split(",", ";", " ").any { token ->
            token.length > 3 && lowerDrug.contains(token.trim())
        }
    }

    fun toCreateMap(ownerUid: String): Map<String, Any> = mapOf(
        "userId" to ownerUid,
        "name" to name.trim().ifEmpty { "Paciente" },
        "ageYears" to ageYears,
        "birthDate" to birthDate.trim().ifEmpty { "Por registrar" },
        "gender" to if (gender == "Masculino") "Masculino" else "Femenino",
        "bloodType" to bloodType.trim().ifEmpty { "O+" },
        "allergies" to allergies.trim().ifEmpty { "Ninguna" },
        "pediatrician" to pediatrician.trim().ifEmpty { "Dr. Alejandro López" },
        "emergencyContact" to emergencyContact.trim().ifEmpty { "Por registrar" },
        "lastCheckupDate" to lastCheckupDate.trim().ifEmpty { "Recién registrado" },
        "lastCheckupSummary" to lastCheckupSummary.trim().ifEmpty { "Sin indicaciones registradas aún" },
        "avatarColorHex" to avatarColorHex.trim().ifEmpty { "#E2ECFA" },
        "weightKg" to weightKg,
        "heightCm" to heightCm,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@IgnoreExtraProperties
data class HealthRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val childId: String = "",
    val childName: String = "",
    val category: String = "VACUNA",
    val title: String = "",
    val subtitle: String = "",
    val dateText: String = "",
    val status: String = "Aplicada",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(ownerUid: String): Map<String, Any> = mapOf(
        "userId" to ownerUid,
        "childId" to childId.trim().ifEmpty { "general" },
        "childName" to childName.trim().ifEmpty { "Paciente" },
        "category" to category,
        "title" to title.trim().ifEmpty { "Registro médico" },
        "subtitle" to subtitle.trim(),
        "dateText" to dateText.trim().ifEmpty { "Hoy" },
        "status" to status.trim().ifEmpty { "Aplicada" },
        "notes" to notes.trim().take(1450),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

/**
 * Catálogo farmacológico pediátrico para la Calculadora de Dosis Ponderal (mg/kg -> mL).
 */
data class PediatricDrugFormula(
    val id: String,
    val name: String,
    val presentationLabel: String,
    val mgPerMl: Double,
    val defaultMgPerKgPerDose: Double,
    val isDailyTotalDivided: Boolean,
    val defaultEveryHours: Int,
    val defaultDays: Int,
    val clinicalIndication: String,
    val parentFriendlyTip: String
)

val pediatricDrugCatalog = listOf(
    PediatricDrugFormula(
        id = "paracetamol_160",
        name = "Paracetamol (Acetaminofén)",
        presentationLabel = "Jarabe 160 mg / 5 mL",
        mgPerMl = 32.0,
        defaultMgPerKgPerDose = 15.0,
        isDailyTotalDivided = false,
        defaultEveryHours = 6,
        defaultDays = 3,
        clinicalIndication = "Antipirético y analgésico (10-15 mg/kg/dosis c/6h)",
        parentFriendlyTip = "Dar con jeringa dosificadora si hay fiebre mayor a 38.0 °C o dolor."
    ),
    PediatricDrugFormula(
        id = "paracetamol_gotas",
        name = "Paracetamol Gotas Pediátricas",
        presentationLabel = "Gotas 100 mg / 1 mL",
        mgPerMl = 100.0,
        defaultMgPerKgPerDose = 15.0,
        isDailyTotalDivided = false,
        defaultEveryHours = 6,
        defaultDays = 3,
        clinicalIndication = "Lactantes: 15 mg/kg/dosis (aprox. 3 gotas/kg/dosis)",
        parentFriendlyTip = "Medir con el gotero incluido antes de la toma de leche."
    ),
    PediatricDrugFormula(
        id = "ibuprofeno_100",
        name = "Ibuprofeno Pediátrico",
        presentationLabel = "Suspensión 100 mg / 5 mL",
        mgPerMl = 20.0,
        defaultMgPerKgPerDose = 10.0,
        isDailyTotalDivided = false,
        defaultEveryHours = 8,
        defaultDays = 3,
        clinicalIndication = "Antiinflamatorio y antipirético (5-10 mg/kg/dosis c/8h, >6 meses)",
        parentFriendlyTip = "Administrar siempre después de algún alimento o leche para cuidar su pancita."
    ),
    PediatricDrugFormula(
        id = "ibuprofeno_200",
        name = "Ibuprofeno Forte",
        presentationLabel = "Suspensión 200 mg / 5 mL",
        mgPerMl = 40.0,
        defaultMgPerKgPerDose = 10.0,
        isDailyTotalDivided = false,
        defaultEveryHours = 8,
        defaultDays = 3,
        clinicalIndication = "Niños mayores: 10 mg/kg/dosis c/8h",
        parentFriendlyTip = "Agitar bien el frasco antes de medir y dar después de comer."
    ),
    PediatricDrugFormula(
        id = "amoxicilina_250",
        name = "Amoxicilina Suspensión",
        presentationLabel = "Suspensión 250 mg / 5 mL",
        mgPerMl = 50.0,
        defaultMgPerKgPerDose = 25.0, // 75-80 mg/kg/día dividido c/8h (~25-27 mg/kg/dosis)
        isDailyTotalDivided = false,
        defaultEveryHours = 8,
        defaultDays = 7,
        clinicalIndication = "Antibiótico betalactámico (~80 mg/kg/día div. c/8h)",
        parentFriendlyTip = "Completar todos los días indicados por el pediatra aunque ya se sienta mejor."
    ),
    PediatricDrugFormula(
        id = "amoxicilina_clav_400",
        name = "Amoxicilina + Ác. Clavulánico",
        presentationLabel = "Suspensión 400 mg / 5 mL",
        mgPerMl = 80.0,
        defaultMgPerKgPerDose = 22.5, // 45 mg/kg/día div c/12h o 90 mg/kg/día en OMA
        isDailyTotalDivided = false,
        defaultEveryHours = 12,
        defaultDays = 7,
        clinicalIndication = "Otitis media / Sinusitis (45-90 mg/kg/día div. c/12h)",
        parentFriendlyTip = "Conservar en refrigeración una vez preparado y dar junto con las comidas."
    ),
    PediatricDrugFormula(
        id = "azitromicina_200",
        name = "Azitromicina Suspensión",
        presentationLabel = "Suspensión 200 mg / 5 mL",
        mgPerMl = 40.0,
        defaultMgPerKgPerDose = 10.0,
        isDailyTotalDivided = false,
        defaultEveryHours = 24,
        defaultDays = 5,
        clinicalIndication = "Macrólido (10 mg/kg/día cada 24h • Ideal en alergia a Penicilina)",
        parentFriendlyTip = "Dar una sola vez al día a la misma hora."
    ),
    PediatricDrugFormula(
        id = "cetirizina_5",
        name = "Cetirizina Jarabe",
        presentationLabel = "Jarabe 5 mg / 5 mL",
        mgPerMl = 1.0,
        defaultMgPerKgPerDose = 0.25,
        isDailyTotalDivided = false,
        defaultEveryHours = 24,
        defaultDays = 5,
        clinicalIndication = "Antihistamínico H1 (0.25 mg/kg/día c/24h)",
        parentFriendlyTip = "Administrar preferiblemente por la noche para aliviar picazón, rinitis o ronchas."
    ),
    PediatricDrugFormula(
        id = "prednisolona_15",
        name = "Prednisolona Solución",
        presentationLabel = "Solución 15 mg / 5 mL",
        mgPerMl = 3.0,
        defaultMgPerKgPerDose = 1.0,
        isDailyTotalDivided = false,
        defaultEveryHours = 24,
        defaultDays = 3,
        clinicalIndication = "Corticoide sistémico para crisis asmática/crup (1-2 mg/kg/día)",
        parentFriendlyTip = "Dar por la mañana después del desayuno según indicación estricta del pediatra."
    )
)

/**
 * Rangos fisiológicos de signos vitales pediátricos según grupo etario.
 */
data class PediatricVitalReference(
    val ageGroupLabel: String,
    val hrMin: Int,
    val hrMax: Int,
    val rrMin: Int,
    val rrMax: Int,
    val sysBpMin: Int,
    val sysBpMax: Int
)

fun getPediatricVitalReference(ageYears: Long): PediatricVitalReference = when {
    ageYears <= 1L -> PediatricVitalReference("Lactante (0-1 año)", hrMin = 100, hrMax = 160, rrMin = 30, rrMax = 50, sysBpMin = 70, sysBpMax = 100)
    ageYears <= 3L -> PediatricVitalReference("Preescolar temprano (1-3 años)", hrMin = 90, hrMax = 150, rrMin = 24, rrMax = 40, sysBpMin = 80, sysBpMax = 105)
    ageYears <= 5L -> PediatricVitalReference("Preescolar (4-5 años)", hrMin = 80, hrMax = 140, rrMin = 22, rrMax = 34, sysBpMin = 85, sysBpMax = 110)
    ageYears <= 11L -> PediatricVitalReference("Escolar (6-11 años)", hrMin = 70, hrMax = 120, rrMin = 18, rrMax = 30, sysBpMin = 90, sysBpMax = 115)
    else -> PediatricVitalReference("Adolescente (12+ años)", hrMin = 60, hrMax = 100, rrMin = 12, rrMax = 20, sysBpMin = 95, sysBpMax = 120)
}

enum class ClinicalModule(
    val categoryKey: String,
    val title: String,
    val subtitle: String,
    val bullets: List<String>,
    val addButtonLabel: String
) {
    HISTORIAL(
        categoryKey = "HISTORIAL",
        title = "Historial clínico",
        subtitle = "Línea de tiempo, diagnósticos y tratamientos",
        bullets = listOf("Línea de tiempo", "Consultas médicas", "Diagnósticos", "Tratamientos"),
        addButtonLabel = "Agregar antecedente"
    ),
    VACUNA(
        categoryKey = "VACUNA",
        title = "Vacunas",
        subtitle = "Calendario de vacunación y certificados",
        bullets = listOf("Calendario de vacunación", "Dosis aplicadas y pendientes", "Certificados", "Recordatorios"),
        addButtonLabel = "Agregar vacuna"
    ),
    CRECIMIENTO(
        categoryKey = "CRECIMIENTO",
        title = "Crecimiento y desarrollo",
        subtitle = "Peso, talla, IMC, percentiles y desarrollo psicomotor",
        bullets = listOf("Peso, talla, IMC", "Percentiles", "Desarrollo psicomotor", "Gráficas"),
        addButtonLabel = "Registrar medición"
    ),
    MEDICAMENTO(
        categoryKey = "MEDICAMENTO",
        title = "Medicamentos",
        subtitle = "Tratamientos actuales, dosis y alertas de alergias",
        bullets = listOf("Medicamentos actuales", "Historial", "Recordatorios", "Alertas de alergias"),
        addButtonLabel = "Agregar medicamento"
    ),
    LABORATORIO(
        categoryKey = "LABORATORIO",
        title = "Laboratorios",
        subtitle = "Resultados de análisis, adjuntos e interpretación",
        bullets = listOf("Resultados", "Documentos adjuntos", "Interpretación básica", "Historial"),
        addButtonLabel = "Agregar laboratorio"
    ),
    CONSULTA(
        categoryKey = "CONSULTA",
        title = "Consultas",
        subtitle = "Agenda pediátrica, especialistas y notas médicas",
        bullets = listOf("Agenda de citas", "Historial de consultas", "Especialistas", "Notas médicas"),
        addButtonLabel = "Agendar consulta"
    ),
    NUTRICION(
        categoryKey = "NUTRICION",
        title = "Nutrición",
        subtitle = "Plan alimentario, evaluación y recomendaciones",
        bullets = listOf("Plan alimentario", "Evaluación nutricional", "Recomendaciones", "Seguimiento"),
        addButtonLabel = "Agregar plan nutricional"
    ),
    DOCUMENTO(
        categoryKey = "DOCUMENTO",
        title = "Documentos",
        subtitle = "Estudios de imágenes, informes y certificados PDF/fotos",
        bullets = listOf("Estudios de imágenes", "Informes médicos", "Resultados de laboratorio", "Archivos PDF / fotos"),
        addButtonLabel = "Subir documento"
    ),
    CALENDARIO(
        categoryKey = "CALENDARIO",
        title = "Calendario",
        subtitle = "Citas médicas, vacunas, laboratorios y recordatorios",
        bullets = listOf("Citas médicas", "Vacunas", "Laboratorios", "Recordatorios"),
        addButtonLabel = "Agregar recordatorio"
    ),
    HOSPITALIZACION(
        categoryKey = "HOSPITALIZACION",
        title = "Hospitalizaciones y emergencias",
        subtitle = "Ingresos hospitalarios, cirugías y procedimientos",
        bullets = listOf("Ingresos hospitalarios", "Cirugías", "Procedimientos", "Transfusiones"),
        addButtonLabel = "Registrar ingreso/emergencia"
    )
}
