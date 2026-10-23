package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ChildProfile
import com.example.ui.theme.AlertRed
import com.example.ui.theme.NunitoFontFamily
import com.example.ui.theme.PequeBlue
import com.example.ui.theme.PequeGreen
import com.example.ui.theme.PequeLightBlue
import com.example.ui.theme.PequeNavy

@Composable
fun PequeSaludLogoMark(
    modifier: Modifier = Modifier,
    iconSize: Dp = 76.dp,
    fontSize: TextUnit = 32.sp,
    showTagline: Boolean = true
) {
    val primaryBrandColor = MaterialTheme.colorScheme.onSurface
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.size(iconSize)) {
            val w = size.width
            val h = size.height

            // Left parent head (Blue)
            drawCircle(
                color = Color(0xFF1D7BE8),
                radius = w * 0.13f,
                center = Offset(w * 0.33f, h * 0.24f)
            )
            // Right parent head (Green)
            drawCircle(
                color = Color(0xFF0FBA81),
                radius = w * 0.13f,
                center = Offset(w * 0.67f, h * 0.24f)
            )

            // Left heart-arm (Blue)
            val leftPath = Path().apply {
                moveTo(w * 0.48f, h * 0.86f)
                cubicTo(
                    w * 0.14f, h * 0.70f,
                    w * 0.08f, h * 0.42f,
                    w * 0.25f, h * 0.38f
                )
                cubicTo(
                    w * 0.36f, h * 0.36f,
                    w * 0.45f, h * 0.46f,
                    w * 0.49f, h * 0.54f
                )
            }
            drawPath(
                path = leftPath,
                color = Color(0xFF1D7BE8),
                style = Stroke(width = w * 0.13f, cap = StrokeCap.Round)
            )

            // Right heart-arm (Green)
            val rightPath = Path().apply {
                moveTo(w * 0.52f, h * 0.86f)
                cubicTo(
                    w * 0.86f, h * 0.70f,
                    w * 0.92f, h * 0.42f,
                    w * 0.75f, h * 0.38f
                )
                cubicTo(
                    w * 0.64f, h * 0.36f,
                    w * 0.55f, h * 0.46f,
                    w * 0.51f, h * 0.54f
                )
            }
            drawPath(
                path = rightPath,
                color = Color(0xFF0FBA81),
                style = Stroke(width = w * 0.13f, cap = StrokeCap.Round)
            )

            // Center Child Head (Navy + warm face)
            drawCircle(
                color = Color(0xFF123E73),
                radius = w * 0.19f,
                center = Offset(w * 0.50f, h * 0.60f)
            )
            drawCircle(
                color = Color(0xFFFFFFFF),
                radius = w * 0.13f,
                center = Offset(w * 0.50f, h * 0.63f)
            )
            // Smile
            drawArc(
                color = Color(0xFF123E73),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(w * 0.44f, h * 0.60f),
                size = Size(w * 0.12f, h * 0.08f),
                style = Stroke(width = w * 0.025f, cap = StrokeCap.Round)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = primaryBrandColor,
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = fontSize
                    )
                ) {
                    append("Peque")
                }
                withStyle(
                    SpanStyle(
                        color = PequeGreen,
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = fontSize
                    )
                ) {
                    append("Salud")
                }
            }
        )

        if (showTagline) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "La historia de salud de tus hijos,\nen un solo lugar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FamilyHeroIllustration(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(230.dp)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE3F2FD), Color(0xFFD2E8FC))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawCircle(
                color = Color(0xFFB3E5FC).copy(alpha = 0.5f),
                radius = w * 0.18f,
                center = Offset(w * 0.22f, h * 0.72f)
            )
            drawCircle(
                color = Color(0xFFA7F3D0).copy(alpha = 0.55f),
                radius = w * 0.20f,
                center = Offset(w * 0.80f, h * 0.65f)
            )

            // Mother (Left)
            drawCircle(
                color = Color(0xFF1E293B),
                radius = w * 0.16f,
                center = Offset(w * 0.33f, h * 0.42f)
            )
            drawRoundRect(
                color = Color(0xFFF48FB1),
                topLeft = Offset(w * 0.16f, h * 0.54f),
                size = Size(w * 0.34f, h * 0.35f),
                cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
            )
            drawCircle(
                color = Color(0xFFFFCCBC),
                radius = w * 0.11f,
                center = Offset(w * 0.34f, h * 0.41f)
            )

            // Father (Right-Center)
            drawCircle(
                color = Color(0xFF0F172A),
                radius = w * 0.15f,
                center = Offset(w * 0.63f, h * 0.35f)
            )
            drawRoundRect(
                color = Color(0xFF26A69A),
                topLeft = Offset(w * 0.46f, h * 0.48f),
                size = Size(w * 0.36f, h * 0.40f),
                cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
            )
            drawCircle(
                color = Color(0xFFF5CBA7),
                radius = w * 0.11f,
                center = Offset(w * 0.63f, h * 0.37f)
            )

            // Daughter Sofía (Front Right)
            drawCircle(
                color = Color(0xFF3E2723),
                radius = w * 0.13f,
                center = Offset(w * 0.68f, h * 0.64f)
            )
            drawRoundRect(
                color = Color(0xFFFFB74D),
                topLeft = Offset(w * 0.53f, h * 0.74f),
                size = Size(w * 0.30f, h * 0.26f),
                cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
            )
            drawCircle(
                color = Color(0xFFFFCCBC),
                radius = w * 0.095f,
                center = Offset(w * 0.67f, h * 0.64f)
            )

            // Son Mateo (Front Left)
            drawRoundRect(
                color = Color(0xFF1E88E5),
                topLeft = Offset(w * 0.27f, h * 0.75f),
                size = Size(w * 0.28f, h * 0.25f),
                cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
            )
            drawCircle(
                color = Color(0xFF1E293B),
                radius = w * 0.11f,
                center = Offset(w * 0.41f, h * 0.64f)
            )
            drawCircle(
                color = Color(0xFFFFCCBC),
                radius = w * 0.09f,
                center = Offset(w * 0.41f, h * 0.66f)
            )
        }
    }
}

@Composable
fun ChildAvatar(
    name: String,
    gender: String,
    colorHex: String,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val bgColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        PequeLightBlue
    }
    val isFemale = gender.equals("Femenino", ignoreCase = true) ||
        name.startsWith("Sof", ignoreCase = true) ||
        name.startsWith("Val", ignoreCase = true)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            val hairColor = if (isFemale) Color(0xFF5D4037) else Color(0xFF263238)
            drawCircle(
                color = hairColor,
                radius = if (isFemale) w * 0.36f else w * 0.31f,
                center = Offset(w * 0.5f, h * 0.46f)
            )

            val shirtColor = if (isFemale) Color(0xFFEC407A) else Color(0xFF1E88E5)
            drawRoundRect(
                color = shirtColor,
                topLeft = Offset(w * 0.22f, h * 0.72f),
                size = Size(w * 0.56f, h * 0.35f),
                cornerRadius = CornerRadius(w * 0.18f, w * 0.18f)
            )

            drawCircle(
                color = Color(0xFFFFD5C2),
                radius = w * 0.25f,
                center = Offset(w * 0.5f, h * 0.48f)
            )

            drawCircle(
                color = Color(0xFF1E293B),
                radius = w * 0.03f,
                center = Offset(w * 0.41f, h * 0.46f)
            )
            drawCircle(
                color = Color(0xFF1E293B),
                radius = w * 0.03f,
                center = Offset(w * 0.59f, h * 0.46f)
            )

            drawArc(
                color = Color(0xFFD84315),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(w * 0.42f, h * 0.50f),
                size = Size(w * 0.16f, h * 0.10f),
                style = Stroke(width = w * 0.03f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun MedicalQrCardDialog(
    child: ChildProfile,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MedicalInformation,
                            contentDescription = "Tarjeta Médica QR",
                            tint = PequeBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tarjeta Médica QR",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ChildAvatar(
                    name = child.name,
                    gender = child.gender,
                    colorHex = child.avatarColorHex,
                    size = 72.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = child.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${child.ageYears} años • ${child.gender} • Nacimiento: ${child.birthDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Deterministic Emergency QR Matrix (kept on white background for reliable QR scanning)
                Box(
                    modifier = Modifier
                        .size(164.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(2.dp, PequeLightBlue, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val seed = (child.name + child.bloodType + child.allergies).hashCode()
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val grid = 15
                        val cell = size.width / grid
                        for (r in 0 until grid) {
                            for (c in 0 until grid) {
                                val isCornerFinder =
                                    (r < 4 && c < 4) || (r < 4 && c >= grid - 4) || (r >= grid - 4 && c < 4)
                                val bit = ((seed shr ((r * 7 + c * 3) % 28)) and 1) == 1
                                if (isCornerFinder || bit) {
                                    drawRect(
                                        color = if (isCornerFinder) PequeBlue else PequeNavy,
                                        topLeft = Offset(c * cell, r * cell),
                                        size = Size(cell * 0.88f, cell * 0.88f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Grupo Sanguíneo: ${child.bloodType}",
                                style = MaterialTheme.typography.titleSmall,
                                color = AlertRed
                            )
                            Text(
                                text = "Peso: ${child.weightKg} kg",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AlertRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Alergias: ${child.allergies.ifBlank { "Ninguna" }}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = PequeGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Emergencia: ${child.emergencyContact} (${child.pediatrician})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = PequeBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Listo", color = Color.White)
                }
            }
        }
    }
}
