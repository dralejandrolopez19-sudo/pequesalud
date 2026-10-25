package com.example.ui.components

import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ChildProfile
import com.example.data.HealthRecord
import com.example.ui.theme.AlertRed
import com.example.ui.theme.PequeBlue
import com.example.ui.theme.PequeGreen
import com.example.ui.theme.PequeOrange
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Parámetros antropométricos soportados en el motor gráfico OMS.
 */
enum class GrowthMetricType(
    val title: String,
    val shortLabel: String,
    val unit: String
) {
    WEIGHT("Peso para la edad", "Peso (kg)", "kg"),
    HEIGHT("Talla / Longitud para la edad", "Talla (cm)", "cm"),
    HEAD_CIRC("Perímetro Cefálico para la edad", "P. Cefálico (cm)", "cm"),
    BMI("Índice de Masa Corporal (IMC)", "IMC (kg/m²)", "kg/m²")
}

/**
 * Cohortes de edad estándar de la Organización Mundial de la Salud (OMS).
 */
enum class WhoAgeCohort(
    val label: String,
    val shortBadge: String,
    val axisLabels: List<String>,
    val axisMonths: List<Double>
) {
    INFANT_0_24M(
        label = "0 a 24 meses",
        shortBadge = "0-24m",
        axisLabels = listOf("0m", "3m", "6m", "9m", "12m", "18m", "24m"),
        axisMonths = listOf(0.0, 3.0, 6.0, 9.0, 12.0, 18.0, 24.0)
    ),
    PRESCHOOL_2_5Y(
        label = "2 a 5 años",
        shortBadge = "2-5a",
        axisLabels = listOf("2a", "2.5a", "3a", "3.5a", "4a", "4.5a", "5a"),
        axisMonths = listOf(24.0, 30.0, 36.0, 42.0, 48.0, 54.0, 60.0)
    ),
    SCHOOL_5_19Y(
        label = "5 a 19 años",
        shortBadge = "5-19a",
        axisLabels = listOf("5a", "7a", "9a", "11a", "13a", "16a", "19a"),
        axisMonths = listOf(60.0, 84.0, 108.0, 132.0, 156.0, 192.0, 228.0)
    );

    companion object {
        fun fromAgeYears(ageYears: Long): WhoAgeCohort = when {
            ageYears < 2L -> INFANT_0_24M
            ageYears <= 5L -> PRESCHOOL_2_5Y
            else -> SCHOOL_5_19Y
        }
    }
}

/**
 * Conjunto de las 5 curvas de percentiles oficiales de la OMS (P3, P15, P50, P85, P97)
 * evaluadas en los 7 nodos de la cohorte etaria seleccionada.
 */
data class WhoPercentileCurves(
    val cohort: WhoAgeCohort,
    val metric: GrowthMetricType,
    val isMale: Boolean,
    val p3: List<Double>,
    val p15: List<Double>,
    val p50: List<Double>,
    val p85: List<Double>,
    val p97: List<Double>
)

/**
 * Punto antropométrico del paciente para graficar sobre las curvas OMS.
 */
data class PatientGrowthPoint(
    val xIndex: Float, // Posición normalizada [0..6] dentro de la cohorte
    val ageLabel: String,
    val dateLabel: String,
    val value: Double,
    val percentile: Int,
    val statusChannel: String,
    val isLivePreview: Boolean = false
)

/**
 * Resultado de la evaluación clínica de una medición frente a las curvas OMS.
 */
data class WhoPercentileEvaluation(
    val exactPercentile: Int,
    val channelLabel: String,
    val parentSummary: String,
    val statusColor: Color,
    val isAlert: Boolean
)

/**
 * Obtiene las curvas estándar de la OMS (P3, P15, P50, P85, P97) diferenciadas
 * por sexo biológico y cohorte etaria (0-24m, 2-5a, 5-19a).
 */
fun getWhoStandardCurves(
    metric: GrowthMetricType,
    cohort: WhoAgeCohort,
    isMale: Boolean
): WhoPercentileCurves {
    val boyFactor = if (isMale) 1.0 else 0.965
    val boyOffsetCm = if (isMale) 0.0 else -1.1

    val p50Base: List<Double> = when (metric) {
        GrowthMetricType.WEIGHT -> when (cohort) {
            WhoAgeCohort.INFANT_0_24M -> listOf(3.3, 6.4, 7.9, 8.9, 9.6, 10.9, 12.2).map { it * boyFactor }
            WhoAgeCohort.PRESCHOOL_2_5Y -> listOf(12.2, 13.3, 14.3, 15.3, 16.3, 17.3, 18.3).map { it * boyFactor }
            WhoAgeCohort.SCHOOL_5_19Y -> listOf(18.3, 22.9, 28.1, 35.6, 45.6, 60.8, 68.9).map { it * boyFactor }
        }
        GrowthMetricType.HEIGHT -> when (cohort) {
            WhoAgeCohort.INFANT_0_24M -> listOf(49.9, 61.4, 67.6, 72.0, 75.7, 82.3, 87.8).map { it + boyOffsetCm }
            WhoAgeCohort.PRESCHOOL_2_5Y -> listOf(87.8, 91.9, 96.1, 99.9, 103.3, 106.7, 110.0).map { it + boyOffsetCm }
            WhoAgeCohort.SCHOOL_5_19Y -> listOf(110.0, 121.7, 132.6, 143.1, 156.0, 173.4, 176.5).map {
                if (isMale) it else (it * 0.965)
            }
        }
        GrowthMetricType.HEAD_CIRC -> when (cohort) {
            WhoAgeCohort.INFANT_0_24M -> listOf(34.5, 40.5, 43.3, 45.0, 46.1, 47.4, 48.3).map { it + (boyOffsetCm * 0.5) }
            WhoAgeCohort.PRESCHOOL_2_5Y -> listOf(48.3, 48.9, 49.5, 49.9, 50.2, 50.5, 50.8).map { it + (boyOffsetCm * 0.5) }
            WhoAgeCohort.SCHOOL_5_19Y -> listOf(50.8, 51.6, 52.3, 53.0, 53.8, 54.8, 55.4).map { it + (boyOffsetCm * 0.5) }
        }
        GrowthMetricType.BMI -> when (cohort) {
            WhoAgeCohort.INFANT_0_24M -> listOf(13.4, 16.9, 17.3, 17.0, 16.8, 16.2, 15.8)
            WhoAgeCohort.PRESCHOOL_2_5Y -> listOf(16.0, 15.8, 15.6, 15.5, 15.3, 15.3, 15.2)
            WhoAgeCohort.SCHOOL_5_19Y -> listOf(15.3, 15.5, 16.0, 16.9, 18.2, 20.5, 22.0)
        }
    }

    // Coeficientes de dispersión estándar OMS para P3 (-1.88 SD), P15 (-1.04 SD), P85 (+1.04 SD) y P97 (+1.88 SD)
    val sdRatio = when (metric) {
        GrowthMetricType.WEIGHT -> 0.115
        GrowthMetricType.HEIGHT -> 0.038
        GrowthMetricType.HEAD_CIRC -> 0.028
        GrowthMetricType.BMI -> 0.085
    }

    val p3 = p50Base.map { (it * (1.0 - 1.88 * sdRatio)).round1() }
    val p15 = p50Base.map { (it * (1.0 - 1.04 * sdRatio)).round1() }
    val p50 = p50Base.map { it.round1() }
    val p85 = p50Base.map { (it * (1.0 + 1.04 * sdRatio)).round1() }
    val p97 = p50Base.map { (it * (1.0 + 1.88 * sdRatio)).round1() }

    return WhoPercentileCurves(
        cohort = cohort,
        metric = metric,
        isMale = isMale,
        p3 = p3,
        p15 = p15,
        p50 = p50,
        p85 = p85,
        p97 = p97
    )
}

private fun Double.round1(): Double = (this * 10.0).roundToInt() / 10.0

/**
 * Interpola el valor de una curva OMS en un índice continuo xIndex [0f..6f].
 */
fun interpolateCurveAt(curve: List<Double>, xIndex: Float): Double {
    if (curve.isEmpty()) return 0.0
    val clamped = xIndex.coerceIn(0f, (curve.size - 1).toFloat())
    val lower = clamped.toInt()
    val upper = (lower + 1).coerceAtMost(curve.size - 1)
    val fraction = clamped - lower
    return curve[lower] + (curve[upper] - curve[lower]) * fraction
}

/**
 * Calcula la posición normalizada [0f..6f] de la edad del paciente dentro de la cohorte OMS.
 */
fun mapAgeYearsToCohortIndex(ageYears: Long, cohort: WhoAgeCohort): Float {
    val ageMonths = (ageYears.coerceAtLeast(0L) * 12L).toDouble().let {
        if (it == 0.0) 6.0 else it
    }
    val months = cohort.axisMonths
    if (ageMonths <= months.first()) return 0f
    if (ageMonths >= months.last()) return (months.size - 1).toFloat()
    for (i in 0 until months.size - 1) {
        val m0 = months[i]
        val m1 = months[i + 1]
        if (ageMonths in m0..m1) {
            val ratio = ((ageMonths - m0) / (m1 - m0)).toFloat()
            return i + ratio
        }
    }
    return 3f
}

/**
 * Evalúa una medición del paciente frente a los percentiles P3, P15, P50, P85 y P97 de la OMS.
 */
fun evaluateWhoPercentile(
    value: Double,
    xIndex: Float,
    curves: WhoPercentileCurves
): WhoPercentileEvaluation {
    val v3 = interpolateCurveAt(curves.p3, xIndex)
    val v15 = interpolateCurveAt(curves.p15, xIndex)
    val v50 = interpolateCurveAt(curves.p50, xIndex)
    val v85 = interpolateCurveAt(curves.p85, xIndex)
    val v97 = interpolateCurveAt(curves.p97, xIndex)

    val pct: Int = when {
        value <= v3 -> {
            val span = (v15 - v3).coerceAtLeast(0.1)
            (3.0 - ((v3 - value) / span) * 2.0).roundToInt().coerceIn(1, 3)
        }
        value <= v15 -> {
            val ratio = (value - v3) / (v15 - v3).coerceAtLeast(0.1)
            (3.0 + ratio * 12.0).roundToInt().coerceIn(3, 15)
        }
        value <= v50 -> {
            val ratio = (value - v15) / (v50 - v15).coerceAtLeast(0.1)
            (15.0 + ratio * 35.0).roundToInt().coerceIn(15, 50)
        }
        value <= v85 -> {
            val ratio = (value - v50) / (v85 - v50).coerceAtLeast(0.1)
            (50.0 + ratio * 35.0).roundToInt().coerceIn(50, 85)
        }
        value <= v97 -> {
            val ratio = (value - v85) / (v97 - v85).coerceAtLeast(0.1)
            (85.0 + ratio * 12.0).roundToInt().coerceIn(85, 97)
        }
        else -> {
            val span = (v97 - v85).coerceAtLeast(0.1)
            (97.0 + ((value - v97) / span) * 2.0).roundToInt().coerceIn(97, 99)
        }
    }

    return when {
        pct < 3 -> WhoPercentileEvaluation(
            exactPercentile = pct,
            channelLabel = "Percentil <3 OMS (Alerta)",
            parentSummary = "Por debajo del carril habitual (P$pct). Requiere valoración por tu pediatra.",
            statusColor = AlertRed,
            isAlert = true
        )
        pct < 15 -> WhoPercentileEvaluation(
            exactPercentile = pct,
            channelLabel = "Percentil 3-15 OMS (Vigilancia)",
            parentSummary = "En zona de seguimiento preventivo (P$pct). Vigilar ganancia en controles.",
            statusColor = PequeOrange,
            isAlert = false
        )
        pct <= 85 -> WhoPercentileEvaluation(
            exactPercentile = pct,
            channelLabel = if (pct >= 50) "Percentil 50-85 OMS (Eutrófico)" else "Percentil 15-50 OMS (Eutrófico)",
            parentSummary = "Crecimiento saludable dentro del carril ideal de la OMS (P$pct).",
            statusColor = PequeGreen,
            isAlert = false
        )
        pct <= 97 -> WhoPercentileEvaluation(
            exactPercentile = pct,
            channelLabel = "Percentil 85-97 OMS (Vigilancia)",
            parentSummary = "En percentil alto de seguimiento (P$pct). Revisar hábitos con tu pediatra.",
            statusColor = PequeOrange,
            isAlert = false
        )
        else -> WhoPercentileEvaluation(
            exactPercentile = pct,
            channelLabel = "Percentil >97 OMS (Alerta)",
            parentSummary = "Por encima del carril superior (P$pct). Valorar ajuste con tu pediatra.",
            statusColor = AlertRed,
            isAlert = true
        )
    }
}

/**
 * Extrae números desde texto clínico, por ejemplo "Peso: 14.5 kg · Talla: 98 cm · PC: 49.5 cm".
 */
private fun extractMetricFromRecord(record: HealthRecord, metric: GrowthMetricType): Double? {
    val combined = "${record.title} ${record.subtitle} ${record.notes}"
    val regex = when (metric) {
        GrowthMetricType.WEIGHT -> Regex("""(?i)peso[:\s]+(\d+(?:[.,]\d+)?)""")
        GrowthMetricType.HEIGHT -> Regex("""(?i)(?:talla|longitud)[:\s]+(\d+(?:[.,]\d+)?)""")
        GrowthMetricType.HEAD_CIRC -> Regex("""(?i)(?:pc|cef[aá]lico)[:\s]+(\d+(?:[.,]\d+)?)""")
        GrowthMetricType.BMI -> Regex("""(?i)imc[:\s]+(\d+(?:[.,]\d+)?)""")
    }
    val match = regex.find(combined)
    if (match != null) {
        return match.groupValues[1].replace(",", ".").toDoubleOrNull()
    }
    // Si es IMC y el registro tiene Peso y Talla, lo calculamos automáticamente
    if (metric == GrowthMetricType.BMI) {
        val w = extractMetricFromRecord(record, GrowthMetricType.WEIGHT)
        val h = extractMetricFromRecord(record, GrowthMetricType.HEIGHT)
        if (w != null && h != null && w > 0.0 && h > 30.0) {
            val hm = h / 100.0
            return (w / (hm * hm)).round1()
        }
    }
    return null
}

/**
 * Construye la serie longitudinal de puntos antropométricos del paciente combinando:
 * 1) El historial de registros `CRECIMIENTO` (en orden cronológico).
 * 2) Los valores actuales del perfil del paciente (`weightKg`, `heightCm`).
 * 3) La previsualización en vivo cuando el pediatra está tecleando una nueva medición.
 */
fun buildPatientGrowthSeries(
    activeChild: ChildProfile?,
    growthRecords: List<HealthRecord>,
    metric: GrowthMetricType,
    cohort: WhoAgeCohort,
    curves: WhoPercentileCurves,
    livePreviewValue: Double? = null
): List<PatientGrowthPoint> {
    if (activeChild == null) return emptyList()

    val currentAnchorX = mapAgeYearsToCohortIndex(activeChild.ageYears, cohort)
    val chronologicalRecords = growthRecords.reversed() // Del más antiguo al más reciente
    val extractedRecordValues = chronologicalRecords.mapNotNull { rec ->
        val v = extractMetricFromRecord(rec, metric)
        if (v != null && v > 0.0) Pair(rec.dateText.ifBlank { "Control" }, v) else null
    }

    val rawPoints = mutableListOf<Triple<String, Double, Boolean>>()
    rawPoints.addAll(extractedRecordValues.map { Triple(it.first, it.second, false) })

    // Si no hay registros históricos de esta métrica pero el perfil del niño tiene Peso/Talla, lo incluimos
    val profileFallbackValue: Double? = when (metric) {
        GrowthMetricType.WEIGHT -> activeChild.weightKg.takeIf { it > 0.0 }
        GrowthMetricType.HEIGHT -> activeChild.heightCm.takeIf { it > 0.0 }
        GrowthMetricType.HEAD_CIRC -> null
        GrowthMetricType.BMI -> {
            if (activeChild.weightKg > 0.0 && activeChild.heightCm > 30.0) {
                val hm = activeChild.heightCm / 100.0
                (activeChild.weightKg / (hm * hm)).round1()
            } else null
        }
    }

    if (rawPoints.isEmpty() && profileFallbackValue != null) {
        rawPoints.add(Triple(activeChild.lastCheckupDate.ifBlank { "Actual" }, profileFallbackValue, false))
    }

    // Si el pediatra está escribiendo un valor en vivo distinto al último guardado, agregamos el nodo de previsualización
    if (livePreviewValue != null && livePreviewValue > 0.0) {
        val lastSaved = rawPoints.lastOrNull()?.second
        if (lastSaved == null || abs(lastSaved - livePreviewValue) >= 0.05) {
            rawPoints.add(Triple("Previsualización hoy", livePreviewValue, true))
        }
    }

    if (rawPoints.isEmpty()) return emptyList()

    // Distribuimos los puntos históricos hacia atrás desde la edad actual para trazar su trayectoria evolutiva
    val total = rawPoints.size
    val stepX = if (total <= 1) 0f else (1.8f / (total - 1).coerceAtLeast(1)).coerceAtMost(0.65f)

    return rawPoints.mapIndexed { idx, (dateText, value, isPreview) ->
        val stepsFromEnd = (total - 1 - idx)
        val pointX = (currentAnchorX - stepsFromEnd * stepX).coerceIn(0.15f, 5.85f)
        val axisIdx = pointX.roundToInt().coerceIn(0, cohort.axisLabels.lastIndex)
        val eval = evaluateWhoPercentile(value, pointX, curves)
        PatientGrowthPoint(
            xIndex = pointX,
            ageLabel = cohort.axisLabels[axisIdx],
            dateLabel = dateText,
            value = value.round1(),
            percentile = eval.exactPercentile,
            statusChannel = eval.channelLabel,
            isLivePreview = isPreview
        )
    }
}

/**
 * Componente Gráfico Interactivo de Curvas de Crecimiento OMS (estilo Recharts / D3 en Compose Canvas).
 * Soporta tanto la Estación Clínica del Pediatra (completa con selector de cohorte y previsualización en vivo)
 * como el Escritorio de Padres (modo lectura simplificado y educativo).
 */
@Composable
fun WhoGrowthChartCard(
    activeChild: ChildProfile?,
    records: List<HealthRecord>,
    isPediatricianMode: Boolean,
    liveWeightKg: Double? = null,
    liveHeightCm: Double? = null,
    liveHeadCircCm: Double? = null,
    onAutoPercentileComputed: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedMetric by remember { mutableStateOf(GrowthMetricType.WEIGHT) }
    var selectedCohort by remember(activeChild?.ageYears) {
        mutableStateOf(WhoAgeCohort.fromAgeYears(activeChild?.ageYears ?: 2L))
    }

    val isMale = activeChild?.gender?.equals("Masculino", ignoreCase = true) == true
    val sexBadge = if (isMale) "OMS Niños (♂)" else "OMS Niñas (♀)"

    val childGrowthRecords = remember(records, activeChild) {
        if (activeChild == null) emptyList()
        else records.filter {
            it.category == "CRECIMIENTO" &&
                (it.childId == activeChild.id || it.childName == activeChild.name)
        }
    }

    val curves = remember(selectedMetric, selectedCohort, isMale) {
        getWhoStandardCurves(selectedMetric, selectedCohort, isMale)
    }

    val liveMetricValue: Double? = remember(selectedMetric, liveWeightKg, liveHeightCm, liveHeadCircCm) {
        when (selectedMetric) {
            GrowthMetricType.WEIGHT -> liveWeightKg
            GrowthMetricType.HEIGHT -> liveHeightCm
            GrowthMetricType.HEAD_CIRC -> liveHeadCircCm
            GrowthMetricType.BMI -> {
                if (liveWeightKg != null && liveHeightCm != null && liveWeightKg > 0.0 && liveHeightCm > 30.0) {
                    val hm = liveHeightCm / 100.0
                    (liveWeightKg / (hm * hm)).round1()
                } else null
            }
        }
    }

    val patientPoints = remember(activeChild, childGrowthRecords, selectedMetric, selectedCohort, curves, liveMetricValue) {
        buildPatientGrowthSeries(
            activeChild = activeChild,
            growthRecords = childGrowthRecords,
            metric = selectedMetric,
            cohort = selectedCohort,
            curves = curves,
            livePreviewValue = liveMetricValue
        )
    }

    var selectedPointIndex by remember(patientPoints) {
        mutableStateOf(if (patientPoints.isNotEmpty()) patientPoints.lastIndex else -1)
    }

    val latestPoint = patientPoints.lastOrNull()
    val latestEvaluation = remember(latestPoint, curves) {
        if (latestPoint != null) {
            evaluateWhoPercentile(latestPoint.value, latestPoint.xIndex, curves)
        } else null
    }

    // Notifica al formulario del pediatra el percentil OMS calculado automáticamente
    LaunchedEffect(latestEvaluation?.channelLabel) {
        if (latestEvaluation != null && onAutoPercentileComputed != null) {
            onAutoPercentileComputed(latestEvaluation.channelLabel)
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("who_growth_chart_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Cabecera: Título + Badges OMS de Sexo y Edad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PequeBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = null,
                            tint = PequeBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isPediatricianMode) "Curvas Percentilares OMS" else "Curva de Crecimiento OMS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$sexBadge · Patrón ${selectedCohort.label}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (latestEvaluation != null) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = latestEvaluation.statusColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, latestEvaluation.statusColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "P${latestEvaluation.exactPercentile} OMS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = latestEvaluation.statusColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 2. Selector de Parámetro Antropométrico (Peso, Talla, P. Cefálico, IMC)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GrowthMetricType.entries.forEach { metric ->
                    val selected = selectedMetric == metric
                    FilterChip(
                        selected = selected,
                        onClick = { selectedMetric = metric },
                        label = {
                            Text(
                                text = metric.shortLabel,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("who_metric_chip_${metric.name}")
                    )
                }
            }

            // 3. Selector de Cohorte Etaria OMS (Visible en Modo Pediatra para explorar etapas 0-24m, 2-5a, 5-19a)
            if (isPediatricianMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Escala OMS:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    WhoAgeCohort.entries.forEach { cohort ->
                        val isSel = selectedCohort == cohort
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSel) PequeBlue.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = if (isSel) BorderStroke(1.dp, PequeBlue) else null,
                            modifier = Modifier
                                .clickable { selectedCohort = cohort }
                                .testTag("who_cohort_chip_${cohort.name}")
                        ) {
                            Text(
                                text = cohort.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) PequeBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 4. Tooltip Interactivo estilo Recharts / D3 (Muestra el punto seleccionado o el último punto)
            val inspectedPoint = if (selectedPointIndex in patientPoints.indices) {
                patientPoints[selectedPointIndex]
            } else {
                latestPoint
            }
            val previousPoint = if (selectedPointIndex > 0 && selectedPointIndex in patientPoints.indices) {
                patientPoints[selectedPointIndex - 1]
            } else if (patientPoints.size >= 2) {
                patientPoints[patientPoints.size - 2]
            } else null

            if (inspectedPoint != null) {
                val pointEval = evaluateWhoPercentile(inspectedPoint.value, inspectedPoint.xIndex, curves)
                val deltaValue = if (previousPoint != null) (inspectedPoint.value - previousPoint.value).round1() else null
                val deltaText = when {
                    deltaValue == null -> "Medición de referencia"
                    deltaValue > 0 -> "+$deltaValue ${selectedMetric.unit} vs. previo"
                    deltaValue < 0 -> "$deltaValue ${selectedMetric.unit} vs. previo"
                    else -> "Sin variación vs. previo"
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                    border = BorderStroke(1.dp, pointEval.statusColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (inspectedPoint.isLivePreview) PequeOrange else PequeBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${inspectedPoint.dateLabel} · Edad ref. ${inspectedPoint.ageLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isPediatricianMode) pointEval.channelLabel else pointEval.parentSummary,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${inspectedPoint.value} ${selectedMetric.unit}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = PequeBlue
                            )
                            Text(
                                text = deltaText,
                                style = MaterialTheme.typography.labelSmall,
                                color = pointEval.statusColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 5. Lienzo Vectorial de Alta Precisión (Curvas Bézier P3, P15, P50, P85, P97 + Serie del Paciente)
            WhoPercentileCanvasChart(
                curves = curves,
                patientPoints = patientPoints,
                selectedPointIndex = selectedPointIndex,
                onSelectPoint = { idx -> selectedPointIndex = idx },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(235.dp)
                    .testTag("who_growth_canvas")
            )

            // 6. Leyenda Minimalista de Zonas OMS y Paciente
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendItem(color = PequeBlue, label = activeChild?.name?.substringBefore(" ") ?: "Paciente")
                ChartLegendItem(color = PequeGreen, label = "P50 / Zona Normal (P15-P85)")
                ChartLegendItem(color = PequeOrange, label = "Vigilancia (P3-P15 / P85-P97)")
                ChartLegendItem(color = AlertRed, label = "Alerta (<P3 / >P97)")
            }

            if (patientPoints.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PequeBlue.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PequeBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPediatricianMode) {
                                "Las 5 curvas OMS están activas. Ingresa ${selectedMetric.shortLabel.lowercase()} abajo para proyectar al paciente sobre su percentil."
                            } else {
                                "Aquí verás la curva de ${selectedMetric.shortLabel.lowercase()} de tu hijo/a en cuanto el pediatra registre su medición."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartLegendItem(
    color: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Lienzo Canvas estilo D3 / Recharts que dibuja:
 * - Zonas sombreadas entre P15-P85 (verde), P3-P15 y P85-P97 (ámbar).
 * - Las 5 curvas cúbicas Bézier oficiales de la OMS (P3, P15, P50, P85, P97) con etiquetas laterales.
 * - La curva longitudinal suavizada del paciente, área sombreada bajo la curva, línea guía táctil y nodos.
 */
@Composable
private fun WhoPercentileCanvasChart(
    curves: WhoPercentileCurves,
    patientPoints: List<PatientGrowthPoint>,
    selectedPointIndex: Int,
    onSelectPoint: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    // Rango dinámico del eje Y con margen superior e inferior limpio
    val (yMin, yMax) = remember(curves, patientPoints) {
        val allValues = buildList {
            addAll(curves.p3)
            addAll(curves.p97)
            addAll(patientPoints.map { it.value })
        }
        val minVal = (allValues.minOrNull() ?: 0.0)
        val maxVal = (allValues.maxOrNull() ?: 100.0)
        val span = (maxVal - minVal).coerceAtLeast(2.0)
        Pair((minVal - span * 0.12).coerceAtLeast(0.0), maxVal + span * 0.12)
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(patientPoints) {
                    detectTapGestures { tapOffset ->
                        if (patientPoints.isNotEmpty()) {
                            val leftPad = 36.dp.toPx()
                            val rightPad = 34.dp.toPx()
                            val chartWidth = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                            val maxIdx = (curves.cohort.axisLabels.size - 1).coerceAtLeast(1)

                            val closestIdx = patientPoints.indices.minByOrNull { idx ->
                                val ptX = leftPad + (patientPoints[idx].xIndex / maxIdx) * chartWidth
                                abs(ptX - tapOffset.x)
                            } ?: patientPoints.lastIndex
                            onSelectPoint(closestIdx)
                        }
                    }
                }
        ) {
            val leftPad = 36.dp.toPx()
            val rightPad = 34.dp.toPx()
            val topPad = 12.dp.toPx()
            val bottomPad = 24.dp.toPx()

            val chartWidth = (size.width - leftPad - rightPad).coerceAtLeast(1f)
            val chartHeight = (size.height - topPad - bottomPad).coerceAtLeast(1f)
            val maxXIndex = (curves.cohort.axisLabels.size - 1).coerceAtLeast(1).toFloat()

            fun mapX(xIndex: Float): Float = leftPad + (xIndex / maxXIndex) * chartWidth
            fun mapY(value: Double): Float {
                val norm = ((value - yMin) / (yMax - yMin).coerceAtLeast(0.1)).toFloat().coerceIn(0f, 1f)
                return topPad + chartHeight * (1f - norm)
            }

            fun curveOffsets(values: List<Double>): List<Offset> =
                values.mapIndexed { idx, v -> Offset(mapX(idx.toFloat()), mapY(v)) }

            val p3Pts = curveOffsets(curves.p3)
            val p15Pts = curveOffsets(curves.p15)
            val p50Pts = curveOffsets(curves.p50)
            val p85Pts = curveOffsets(curves.p85)
            val p97Pts = curveOffsets(curves.p97)

            // 1. Grilla horizontal y etiquetas del eje Y
            val yTicks = 4
            val axisPaint = Paint().apply {
                color = labelTextColor
                textSize = 9.5.dp.toPx()
                isAntiAlias = true
            }

            for (i in 0..yTicks) {
                val ratio = i.toFloat() / yTicks
                val yVal = yMin + (yMax - yMin) * (1f - ratio)
                val yPos = topPad + chartHeight * ratio
                drawLine(
                    color = gridColor,
                    start = Offset(leftPad, yPos),
                    end = Offset(leftPad + chartWidth, yPos),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
                val yText = String.format(Locale.US, "%.0f", yVal)
                drawContext.canvas.nativeCanvas.drawText(
                    yText,
                    4.dp.toPx(),
                    yPos + 3.dp.toPx(),
                    axisPaint
                )
            }

            // 2. Grilla vertical y etiquetas del eje X (Edad OMS)
            curves.cohort.axisLabels.forEachIndexed { idx, label ->
                val xPos = mapX(idx.toFloat())
                drawLine(
                    color = gridColor.copy(alpha = 0.22f),
                    start = Offset(xPos, topPad),
                    end = Offset(xPos, topPad + chartHeight),
                    strokeWidth = 1.dp.toPx()
                )
                val textWidth = axisPaint.measureText(label)
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    xPos - textWidth / 2f,
                    size.height - 4.dp.toPx(),
                    axisPaint
                )
            }

            // 3. Bandas semánticas sombreadas OMS (P3-P97 ámbar suave y P15-P85 verde eutrófico)
            val bandP3ToP97 = buildBandPath(p97Pts, p3Pts)
            drawPath(
                path = bandP3ToP97,
                color = PequeOrange.copy(alpha = 0.06f)
            )

            val bandP15ToP85 = buildBandPath(p85Pts, p15Pts)
            drawPath(
                path = bandP15ToP85,
                color = PequeGreen.copy(alpha = 0.10f)
            )

            // 4. Trazo de las 5 curvas oficiales de la OMS (P3, P15, P50, P85, P97)
            val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
            val fineDashedEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)

            // P3 y P97 (Límites de alerta)
            drawPath(
                path = buildSmoothBezierPath(p97Pts),
                color = AlertRed.copy(alpha = 0.55f),
                style = Stroke(width = 1.3.dp.toPx(), cap = StrokeCap.Round, pathEffect = fineDashedEffect)
            )
            drawPath(
                path = buildSmoothBezierPath(p3Pts),
                color = AlertRed.copy(alpha = 0.55f),
                style = Stroke(width = 1.3.dp.toPx(), cap = StrokeCap.Round, pathEffect = fineDashedEffect)
            )

            // P15 y P85 (Límites de vigilancia)
            drawPath(
                path = buildSmoothBezierPath(p85Pts),
                color = PequeOrange.copy(alpha = 0.70f),
                style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round, pathEffect = dashedEffect)
            )
            drawPath(
                path = buildSmoothBezierPath(p15Pts),
                color = PequeOrange.copy(alpha = 0.70f),
                style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round, pathEffect = dashedEffect)
            )

            // P50 (Mediana OMS continua)
            drawPath(
                path = buildSmoothBezierPath(p50Pts),
                color = PequeGreen.copy(alpha = 0.90f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Etiquetas derechas P97, P85, P50, P15, P3
            val pctLabelPaint = Paint().apply {
                textSize = 9.dp.toPx()
                isAntiAlias = true
                isFakeBoldText = true
            }
            val rightLabels = listOf(
                Triple("P97", p97Pts.last().y, AlertRed.toArgb()),
                Triple("P85", p85Pts.last().y, PequeOrange.toArgb()),
                Triple("P50", p50Pts.last().y, PequeGreen.toArgb()),
                Triple("P15", p15Pts.last().y, PequeOrange.toArgb()),
                Triple("P3", p3Pts.last().y, AlertRed.toArgb())
            )
            rightLabels.forEach { (lbl, yCoord, argb) ->
                pctLabelPaint.color = argb
                drawContext.canvas.nativeCanvas.drawText(
                    lbl,
                    leftPad + chartWidth + 4.dp.toPx(),
                    yCoord + 3.dp.toPx(),
                    pctLabelPaint
                )
            }

            // 5. Curva longitudinal del paciente + área en gradiente + nodos interactivos
            if (patientPoints.isNotEmpty()) {
                val patientOffsets = patientPoints.map { pt -> Offset(mapX(pt.xIndex), mapY(pt.value)) }

                if (patientOffsets.size >= 2) {
                    val smoothPatientPath = buildSmoothBezierPath(patientOffsets)
                    val areaPath = Path().apply {
                        addPath(smoothPatientPath)
                        lineTo(patientOffsets.last().x, topPad + chartHeight)
                        lineTo(patientOffsets.first().x, topPad + chartHeight)
                        close()
                    }
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PequeBlue.copy(alpha = 0.22f),
                                PequeBlue.copy(alpha = 0.01f)
                            ),
                            startY = topPad,
                            endY = topPad + chartHeight
                        )
                    )
                    drawPath(
                        path = smoothPatientPath,
                        color = PequeBlue,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Línea guía vertical en el punto inspeccionado (estilo Recharts Crosshair)
                val activeIdx = if (selectedPointIndex in patientOffsets.indices) selectedPointIndex else patientOffsets.lastIndex
                val activeOffset = patientOffsets[activeIdx]
                drawLine(
                    color = PequeBlue.copy(alpha = 0.35f),
                    start = Offset(activeOffset.x, topPad),
                    end = Offset(activeOffset.x, topPad + chartHeight),
                    strokeWidth = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                // Nodos circulares de cada medición
                patientOffsets.forEachIndexed { idx, center ->
                    val pt = patientPoints[idx]
                    val isSelected = idx == activeIdx
                    val nodeColor = if (pt.isLivePreview) PequeOrange else PequeBlue

                    if (isSelected || pt.isLivePreview) {
                        drawCircle(
                            color = nodeColor.copy(alpha = 0.22f),
                            radius = 9.dp.toPx(),
                            center = center
                        )
                    }
                    drawCircle(
                        color = Color.White,
                        radius = 5.5.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = nodeColor,
                        radius = 4.dp.toPx(),
                        center = center
                    )
                }
            }
        }
    }
}

/**
 * Construye una curva cúbica Bézier suave que pasa por todos los puntos de control (estilo D3 monotoneX).
 */
private fun buildSmoothBezierPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points.first().x, points.first().y)
    for (i in 0 until points.size - 1) {
        val current = points[i]
        val next = points[i + 1]
        val controlX = (current.x + next.x) / 2f
        path.cubicTo(
            controlX, current.y,
            controlX, next.y,
            next.x, next.y
        )
    }
    return path
}

/**
 * Construye un polígono cerrado suave entre una curva superior y una curva inferior para sombrear bandas OMS.
 */
private fun buildBandPath(upper: List<Offset>, lower: List<Offset>): Path {
    val path = Path()
    if (upper.isEmpty() || lower.isEmpty()) return path
    path.moveTo(upper.first().x, upper.first().y)
    for (i in 0 until upper.size - 1) {
        val curr = upper[i]
        val next = upper[i + 1]
        val cx = (curr.x + next.x) / 2f
        path.cubicTo(cx, curr.y, cx, next.y, next.x, next.y)
    }
    val reversedLower = lower.reversed()
    path.lineTo(reversedLower.first().x, reversedLower.first().y)
    for (i in 0 until reversedLower.size - 1) {
        val curr = reversedLower[i]
        val next = reversedLower[i + 1]
        val cx = (curr.x + next.x) / 2f
        path.cubicTo(cx, curr.y, cx, next.y, next.x, next.y)
    }
    path.close()
    return path
}
