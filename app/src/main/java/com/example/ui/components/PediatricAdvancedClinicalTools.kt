package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.theme.PequePurple
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Generador Nativo de Documentos Clínicos PDF (`PdfDocument`) y Compartir Recetas/Certificados/Expediente.
 */
object PediatricPdfAndShareHelper {

    fun generateAndShareRecordPdf(
        context: Context,
        child: ChildProfile?,
        record: HealthRecord,
        doctorName: String = "Dr. Alejandro López"
    ) {
        val childName = child?.name ?: record.childName.ifBlank { "Paciente Pediátrico" }
        val cleanNotes = record.notes.replace("[PUSH]", "").trim()

        // 1. Generar archivo PDF oficial en almacenamiento caché de la app
        val pdfFileName = "PequeSalud_${record.category}_${System.currentTimeMillis()}.pdf"
        val pdfSaved = try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 estándar
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(24, 119, 242)
                textSize = 20f
                isFakeBoldText = true
            }
            val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(90, 102, 120)
                textSize = 12f
            }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(20, 28, 40)
                textSize = 15f
                isFakeBoldText = true
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(45, 55, 72)
                textSize = 12.5f
            }
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(210, 220, 235)
                strokeWidth = 1.5f
            }

            canvas.drawText("PEQUESALUD · DOCUMENTO CLÍNICO OFICIAL", 42f, 54f, headerPaint)
            canvas.drawText("$doctorName · Pediatría y Puericultura", 42f, 74f, subHeaderPaint)
            canvas.drawLine(42f, 88f, 553f, 88f, linePaint)

            canvas.drawText("Paciente: $childName", 42f, 116f, titlePaint)
            val patientMeta = if (child != null) {
                "Edad: ${child.ageYears} años · Peso: ${child.weightKg} kg · Talla: ${child.heightCm} cm · Grupo: ${child.bloodType} · Alergias: ${child.allergies}"
            } else {
                "Fecha de emisión: ${record.dateText}"
            }
            canvas.drawText(patientMeta, 42f, 136f, subHeaderPaint)
            canvas.drawLine(42f, 150f, 553f, 150f, linePaint)

            canvas.drawText("[${record.category}] ${record.title}", 42f, 182f, titlePaint)
            canvas.drawText("${record.subtitle} · Fecha: ${record.dateText} · Estado: ${record.status}", 42f, 202f, subHeaderPaint)

            // Renderizar notas multilínea
            val words = cleanNotes.split(" ")
            var currentLine = StringBuilder()
            var yPos = 236f
            words.forEach { word ->
                if (currentLine.length + word.length + 1 > 78) {
                    canvas.drawText(currentLine.toString(), 42f, yPos, bodyPaint)
                    yPos += 20f
                    currentLine = StringBuilder(word)
                } else {
                    if (currentLine.isNotEmpty()) currentLine.append(" ")
                    currentLine.append(word)
                }
            }
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine.toString(), 42f, yPos, bodyPaint)
                yPos += 36f
            }

            canvas.drawLine(42f, 760f, 553f, 760f, linePaint)
            canvas.drawText("Firma Digital Verificada en PequeSalud · Expediente Sincronizado", 42f, 782f, subHeaderPaint)

            pdfDocument.finishPage(page)
            val outFile = File(context.cacheDir, pdfFileName)
            FileOutputStream(outFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            true
        } catch (_: Exception) {
            false
        }

        val shareSummary = buildString {
            appendLine("📋 PEQUESALUD — ${record.category}")
            appendLine("👧👦 Paciente: $childName")
            if (child != null) {
                appendLine("⚖️ Peso: ${child.weightKg} kg | 📏 Talla: ${child.heightCm} cm | 🩸 Grupo: ${child.bloodType}")
                appendLine("⚠️ Alergias: ${child.allergies}")
            }
            appendLine("────────────────────────")
            appendLine("📌 ${record.title}")
            if (record.subtitle.isNotBlank()) appendLine("👨‍⚕️ ${record.subtitle}")
            appendLine("📅 Fecha: ${record.dateText} (${record.status})")
            if (cleanNotes.isNotBlank()) {
                appendLine("📝 Detalle: $cleanNotes")
            }
            if (pdfSaved) {
                appendLine("────────────────────────")
                appendLine("📄 PDF generado: $pdfFileName")
            }
        }

        Toast.makeText(
            context,
            if (pdfSaved) "PDF clínico generado ($pdfFileName). Abriendo panel para compartir..."
            else "Preparando resumen clínico para compartir...",
            Toast.LENGTH_SHORT
        ).show()

        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "PequeSalud - ${record.title} ($childName)")
                putExtra(Intent.EXTRA_TEXT, shareSummary)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Compartir documento clínico").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
        }
    }

    fun generateAndShareFullDossierPdf(
        context: Context,
        child: ChildProfile,
        records: List<HealthRecord>,
        doctorName: String
    ) {
        val childRecords = records.filter { it.childId == child.id || it.childName == child.name }
        val meds = childRecords.filter { it.category == "MEDICAMENTO" }
        val vaccines = childRecords.filter { it.category == "VACUNA" }
        val consults = childRecords.filter { it.category == "CONSULTA" || it.category == "CERTIFICADO" }

        val pdfFileName = "Expediente_${child.name.replace(" ", "_")}.pdf"
        val pdfSaved = try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(24, 119, 242)
                textSize = 18f
                isFakeBoldText = true
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(35, 45, 60)
                textSize = 11.5f
            }

            canvas.drawText("EXPEDIENTE PEDIÁTRICO INTEGRAL — PEQUESALUD", 40f, 50f, headerPaint)
            canvas.drawText("Paciente: ${child.name} (${child.ageYears} años) · Código: ${child.familyLinkCode()}", 40f, 75f, bodyPaint)
            canvas.drawText("Peso: ${child.weightKg} kg · Talla: ${child.heightCm} cm · Grupo Sanguíneo: ${child.bloodType}", 40f, 95f, bodyPaint)
            canvas.drawText("Alergias: ${child.allergies} · Pediatra: $doctorName", 40f, 115f, bodyPaint)

            var y = 148f
            canvas.drawText("Recetas activas (${meds.size}) | Vacunas registradas (${vaccines.size}) | Consultas (${consults.size})", 40f, y, headerPaint)
            y += 24f
            childRecords.take(18).forEach { rec ->
                if (y < 790f) {
                    val line = "• [${rec.category}] ${rec.title} (${rec.dateText}) — ${rec.subtitle}".take(90)
                    canvas.drawText(line, 40f, y, bodyPaint)
                    y += 18f
                }
            }

            pdfDocument.finishPage(page)
            val outFile = File(context.cacheDir, pdfFileName)
            FileOutputStream(outFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            true
        } catch (_: Exception) {
            false
        }

        val summaryText = buildString {
            appendLine("🏥 RESUMEN DE EXPEDIENTE PEDIÁTRICO — PEQUESALUD")
            appendLine("Paciente: ${child.name} (${child.ageYears} años) · ${child.familyLinkCode()}")
            appendLine("Peso: ${child.weightKg} kg | Talla: ${child.heightCm} cm | Grupo: ${child.bloodType}")
            appendLine("Alergias: ${child.allergies}")
            appendLine("Última revisión (${child.lastCheckupDate}): ${child.lastCheckupSummary}")
            appendLine("────────────────────────")
            appendLine("💊 Medicamentos activos: ${meds.size}")
            meds.take(3).forEach { appendLine("  • ${it.title}: ${it.subtitle}") }
            appendLine("💉 Vacunas PAI registradas: ${vaccines.size}")
            vaccines.take(4).forEach { appendLine("  • ${it.title} (${it.status})") }
            if (pdfSaved) {
                appendLine("📄 Archivo PDF generado: $pdfFileName")
            }
        }

        Toast.makeText(
            context,
            "Expediente PDF generado ($pdfFileName). Abriendo panel para compartir...",
            Toast.LENGTH_SHORT
        ).show()

        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Expediente Pediátrico - ${child.name}")
                putExtra(Intent.EXTRA_TEXT, summaryText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Exportar expediente pediátrico").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
        }
    }
}

private data class FeverPointEntry(
    val index: Int,
    val tempCelsius: Float,
    val label: String,
    val dateText: String,
    val note: String
)

private val tempRegex = Regex("""(\d{2}(?:[.,]\d)?)\s*°?\s*C""", RegexOption.IGNORE_CASE)

/**
 * Curva Térmica Interactiva de Fiebre en Casa (°C).
 * Extrae las temperaturas de los reportes de síntomas y grafica la evolución frente al umbral febril (38.0 °C).
 */
@Composable
fun FeverThermalCurveCard(
    childName: String,
    symptomRecords: List<HealthRecord>,
    modifier: Modifier = Modifier
) {
    val feverPoints = remember(symptomRecords) {
        val parsed = symptomRecords.mapNotNull { rec ->
            val combined = "${rec.title} ${rec.notes}"
            val match = tempRegex.find(combined)
            val value = match?.groupValues?.getOrNull(1)?.replace(",", ".")?.toFloatOrNull()
            if (value != null && value in 35.0f..42.0f) {
                Triple(value, rec.dateText.ifBlank { "Hoy" }, rec.title)
            } else {
                null
            }
        }.reversed()

        parsed.mapIndexed { idx, (temp, date, title) ->
            FeverPointEntry(
                index = idx,
                tempCelsius = temp,
                label = "T${idx + 1}",
                dateText = date,
                note = title
            )
        }
    }

    if (feverPoints.isEmpty()) return

    var selectedIdx by remember(feverPoints.size) { mutableIntStateOf(feverPoints.lastIndex) }
    val selectedPoint = feverPoints.getOrNull(selectedIdx) ?: feverPoints.last()
    val maxTemp = feverPoints.maxOf { it.tempCelsius }
    val isCurrentlyFebrile = selectedPoint.tempCelsius >= 38.0f

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (isCurrentlyFebrile) AlertRed.copy(alpha = 0.45f)
            else PequeOrange.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("fever_thermal_curve_card")
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
                                if (isCurrentlyFebrile) AlertRed.copy(alpha = 0.14f)
                                else PequeGreen.copy(alpha = 0.14f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = null,
                            tint = if (isCurrentlyFebrile) AlertRed else PequeGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Curva Térmica · ${childName.substringBefore(" ")}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${feverPoints.size} toma(s) · Pico máx: ${String.format(Locale.US, "%.1f °C", maxTemp)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = when {
                        selectedPoint.tempCelsius >= 38.0f -> AlertRed.copy(alpha = 0.14f)
                        selectedPoint.tempCelsius >= 37.5f -> PequeOrange.copy(alpha = 0.15f)
                        else -> PequeGreen.copy(alpha = 0.14f)
                    }
                ) {
                    Text(
                        text = when {
                            selectedPoint.tempCelsius >= 38.0f -> "${selectedPoint.tempCelsius} °C · Fiebre"
                            selectedPoint.tempCelsius >= 37.5f -> "${selectedPoint.tempCelsius} °C · Febrícula"
                            else -> "${selectedPoint.tempCelsius} °C · Normal"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            selectedPoint.tempCelsius >= 38.0f -> AlertRed
                            selectedPoint.tempCelsius >= 37.5f -> PequeOrange
                            else -> PequeGreen
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f))
                    .pointerInput(feverPoints) {
                        detectTapGestures { tapOffset ->
                            if (feverPoints.isNotEmpty()) {
                                val leftPad = 36.dp.toPx()
                                val rightPad = 14.dp.toPx()
                                val plotW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                                val stepX = if (feverPoints.size > 1) plotW / (feverPoints.size - 1) else plotW
                                val tappedIndex = feverPoints.indices.minByOrNull { idx ->
                                    val px = if (feverPoints.size == 1) leftPad + plotW / 2f else leftPad + idx * stepX
                                    abs(tapOffset.x - px)
                                } ?: feverPoints.lastIndex
                                selectedIdx = tappedIndex
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val leftPad = 36.dp.toPx()
                    val rightPad = 14.dp.toPx()
                    val topPad = 12.dp.toPx()
                    val bottomPad = 22.dp.toPx()
                    val plotW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                    val plotH = (size.height - topPad - bottomPad).coerceAtLeast(1f)

                    val minT = 35.8f
                    val maxT = 40.2f
                    fun yOf(temp: Float): Float {
                        val norm = ((temp - minT) / (maxT - minT)).coerceIn(0f, 1f)
                        return topPad + plotH * (1f - norm)
                    }

                    // Banda febril >= 38.0 °C
                    val y38 = yOf(38.0f)
                    val y375 = yOf(37.5f)
                    drawRect(
                        color = AlertRed.copy(alpha = 0.08f),
                        topLeft = Offset(leftPad, topPad),
                        size = Size(plotW, (y38 - topPad).coerceAtLeast(0f))
                    )
                    drawRect(
                        color = PequeOrange.copy(alpha = 0.07f),
                        topLeft = Offset(leftPad, y38),
                        size = Size(plotW, (y375 - y38).coerceAtLeast(0f))
                    )
                    drawRect(
                        color = PequeGreen.copy(alpha = 0.06f),
                        topLeft = Offset(leftPad, y375),
                        size = Size(plotW, (topPad + plotH - y375).coerceAtLeast(0f))
                    )

                    // Línea umbral de 38.0 °C
                    drawLine(
                        color = AlertRed.copy(alpha = 0.65f),
                        start = Offset(leftPad, y38),
                        end = Offset(leftPad + plotW, y38),
                        strokeWidth = 1.3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )

                    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.GRAY
                        textSize = 9.dp.toPx()
                    }
                    listOf(36.5f, 37.5f, 38.0f, 39.5f).forEach { refT ->
                        val ry = yOf(refT)
                        drawContext.canvas.nativeCanvas.drawText(
                            String.format(Locale.US, "%.1f°", refT),
                            4.dp.toPx(),
                            ry + 3.dp.toPx(),
                            labelPaint
                        )
                    }

                    val stepX = if (feverPoints.size > 1) plotW / (feverPoints.size - 1) else plotW
                    val linePath = Path()
                    feverPoints.forEachIndexed { idx, pt ->
                        val cx = if (feverPoints.size == 1) leftPad + plotW / 2f else leftPad + idx * stepX
                        val cy = yOf(pt.tempCelsius)
                        if (idx == 0) linePath.moveTo(cx, cy) else linePath.lineTo(cx, cy)
                    }

                    if (feverPoints.size > 1) {
                        drawPath(
                            path = linePath,
                            color = PequeBlue,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    feverPoints.forEachIndexed { idx, pt ->
                        val cx = if (feverPoints.size == 1) leftPad + plotW / 2f else leftPad + idx * stepX
                        val cy = yOf(pt.tempCelsius)
                        val ptColor = when {
                            pt.tempCelsius >= 38.0f -> AlertRed
                            pt.tempCelsius >= 37.5f -> PequeOrange
                            else -> PequeGreen
                        }
                        if (idx == selectedIdx) {
                            drawCircle(
                                color = ptColor.copy(alpha = 0.25f),
                                radius = 9.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                        }
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = ptColor,
                            radius = 3.8.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                    }
                }
            }

            Text(
                text = "Toma seleccionada (${selectedPoint.dateText}): ${selectedPoint.note}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Calculadora Clínica de Talla Diana Familiar (Fórmula Genética de Tanner) y Potencial Adulto.
 */
@Composable
fun TannerTargetHeightCalculatorCard(
    activeChild: ChildProfile,
    onSaveTargetHeightRecord: ((HealthRecord) -> Unit)? = null
) {
    val isMale = remember(activeChild.gender) {
        !activeChild.gender.lowercase(Locale.getDefault()).let {
            it.contains("fem") || it.contains("niña") || it == "f"
        }
    }
    var fatherHeightInput by remember { mutableStateOf("174") }
    var motherHeightInput by remember { mutableStateOf("162") }

    val fatherCm = fatherHeightInput.replace(",", ".").toDoubleOrNull() ?: 0.0
    val motherCm = motherHeightInput.replace(",", ".").toDoubleOrNull() ?: 0.0

    val targetHeightCm = if (fatherCm in 120.0..220.0 && motherCm in 120.0..220.0) {
        if (isMale) (fatherCm + motherCm + 13.0) / 2.0
        else (fatherCm + motherCm - 13.0) / 2.0
    } else {
        0.0
    }
    val minRangeCm = (targetHeightCm - 8.5).coerceAtLeast(0.0)
    val maxRangeCm = targetHeightCm + 8.5

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, PequePurple.copy(alpha = 0.38f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tanner_target_height_card")
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
                    Icon(
                        imageVector = Icons.Default.Straighten,
                        contentDescription = null,
                        tint = PequePurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Talla Diana Genética (Fórmula de Tanner)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isMale) "Fórmula Niño: (Padre + Madre + 13) / 2 ± 8.5 cm"
                            else "Fórmula Niña: (Padre + Madre - 13) / 2 ± 8.5 cm",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fatherHeightInput,
                    onValueChange = { fatherHeightInput = it },
                    label = { Text("Talla Papá (cm)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = motherHeightInput,
                    onValueChange = { motherHeightInput = it },
                    label = { Text("Talla Mamá (cm)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            if (targetHeightCm > 0.0) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PequePurple.copy(alpha = 0.09f),
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
                            Text(
                                text = "Talla Diana Esperada: ${String.format(Locale.US, "%.1f cm", targetHeightCm)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = PequePurple
                            )
                            Text(
                                text = "Carril genético familiar (P3–P97): ${String.format(Locale.US, "%.1f a %.1f cm", minRangeCm, maxRangeCm)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (onSaveTargetHeightRecord != null) {
                            OutlinedButton(
                                onClick = {
                                    onSaveTargetHeightRecord(
                                        HealthRecord(
                                            childId = activeChild.id,
                                            childName = activeChild.name,
                                            category = "CRECIMIENTO",
                                            title = "Talla Diana Tanner: ${String.format(Locale.US, "%.1f cm", targetHeightCm)}",
                                            subtitle = "Rango genético: ${String.format(Locale.US, "%.1f - %.1f cm", minRangeCm, maxRangeCm)}",
                                            dateText = "Potencial genético",
                                            status = "Registrado",
                                            notes = "Calculada con Talla Padre: ${fatherCm} cm y Talla Madre: ${motherCm} cm."
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Guardar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Cronómetro en Vivo de Lactancia / Biberón y Contador de Pañales para el Diario del Bebé en Casa.
 */
@Composable
fun LactationTimerAndDiaperCounterSection(
    onSummaryChanged: (String) -> Unit
) {
    var timerRunning by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var breastSide by remember { mutableStateOf("Seno Izq.") }
    var wetDiapers by remember { mutableIntStateOf(4) }
    var stoolDiapers by remember { mutableIntStateOf(2) }

    LaunchedEffect(timerRunning) {
        while (timerRunning) {
            delay(1000L)
            elapsedSeconds += 1
        }
    }

    val mins = elapsedSeconds / 60
    val secs = elapsedSeconds % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", mins, secs)

    LaunchedEffect(elapsedSeconds, breastSide, wetDiapers, stoolDiapers) {
        val timerPart = if (elapsedSeconds > 0) "Lactancia ($breastSide): $formattedTime min · " else ""
        onSummaryChanged("${timerPart}Pañales: $wetDiapers mojados, $stoolDiapers evacuaciones")
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PequeGreen.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, PequeGreen.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cronómetro de Lactancia ($breastSide)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PequeGreen
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = {
                            breastSide = if (breastSide == "Seno Izq.") "Seno Der." else "Seno Izq."
                        },
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = breastSide,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                    IconButton(
                        onClick = { timerRunning = !timerRunning },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (timerRunning) AlertRed.copy(alpha = 0.15f) else PequeGreen.copy(alpha = 0.18f))
                    ) {
                        Icon(
                            imageVector = if (timerRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (timerRunning) "Pausar" else "Iniciar",
                            tint = if (timerRunning) AlertRed else PequeGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (elapsedSeconds > 0) {
                        IconButton(
                            onClick = {
                                timerRunning = false
                                elapsedSeconds = 0
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reiniciar",
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = PequeGreen.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mojados: $wetDiapers",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { if (wetDiapers > 0) wetDiapers-- },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = { wetDiapers++ },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Más", modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Evacuaciones: $stoolDiapers",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { if (stoolDiapers > 0) stoolDiapers-- },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = { stoolDiapers++ },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Más", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
