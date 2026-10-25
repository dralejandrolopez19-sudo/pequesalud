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
            .size(252.dp)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE8F4FE), Color(0xFFD4ECFA), Color(0xFFC7E8F3))
                )
            )
            .border(4.dp, Color.White.copy(alpha = 0.85f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft warm sunlight & foliage bokeh in background
            drawCircle(
                color = Color(0xFFFFF9C4).copy(alpha = 0.45f),
                radius = w * 0.26f,
                center = Offset(w * 0.22f, h * 0.20f)
            )
            drawCircle(
                color = Color(0xFFB2DFDB).copy(alpha = 0.45f),
                radius = w * 0.24f,
                center = Offset(w * 0.82f, h * 0.68f)
            )
            drawCircle(
                color = Color(0xFFBBDEFB).copy(alpha = 0.50f),
                radius = w * 0.22f,
                center = Offset(w * 0.18f, h * 0.72f)
            )

            // =====================================================================
            // 1. FATHER (Back Right-Center) — Warm tan skin, short styled hair, polo/sweater
            // =====================================================================
            val dadX = w * 0.62f
            val dadY = h * 0.34f
            val dadSkin = Color(0xFFE8B490)
            val dadSkinShadow = Color(0xFFD39972)
            val dadHair = Color(0xFF1E293B)

            // Dad torso & shoulders
            val dadTorso = Path().apply {
                moveTo(dadX - w * 0.23f, h * 0.92f)
                cubicTo(
                    dadX - w * 0.22f, dadY + h * 0.20f,
                    dadX - w * 0.14f, dadY + h * 0.15f,
                    dadX, dadY + h * 0.15f
                )
                cubicTo(
                    dadX + w * 0.14f, dadY + h * 0.15f,
                    dadX + w * 0.23f, dadY + h * 0.20f,
                    dadX + w * 0.24f, h * 0.92f
                )
                close()
            }
            drawPath(
                path = dadTorso,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF14967F), Color(0xFF0D7664)),
                    startY = dadY + h * 0.14f,
                    endY = h * 0.92f
                )
            )

            // Dad neck & collar shadow
            drawRoundRect(
                color = dadSkinShadow,
                topLeft = Offset(dadX - w * 0.042f, dadY + h * 0.07f),
                size = Size(w * 0.084f, h * 0.11f),
                cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
            )
            // White crew undershirt collar
            drawArc(
                color = Color.White.copy(alpha = 0.9f),
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(dadX - w * 0.055f, dadY + h * 0.11f),
                size = Size(w * 0.11f, h * 0.07f),
                style = Stroke(width = w * 0.016f, cap = StrokeCap.Round)
            )

            // Dad back hair crown (only upper half of head so chin/neck are natural)
            drawOval(
                color = dadHair,
                topLeft = Offset(dadX - w * 0.125f, dadY - h * 0.145f),
                size = Size(w * 0.25f, h * 0.21f)
            )

            // Dad ears
            drawCircle(color = dadSkinShadow, radius = w * 0.026f, center = Offset(dadX - w * 0.112f, dadY + h * 0.005f))
            drawCircle(color = dadSkinShadow, radius = w * 0.026f, center = Offset(dadX + w * 0.112f, dadY + h * 0.005f))

            // Dad oval face
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFF3C6A5), dadSkin),
                    center = Offset(dadX - w * 0.02f, dadY - h * 0.02f),
                    radius = w * 0.15f
                ),
                topLeft = Offset(dadX - w * 0.11f, dadY - h * 0.11f),
                size = Size(w * 0.22f, h * 0.245f)
            )

            // Dad styled top hair & sideburns
            val dadHairFront = Path().apply {
                moveTo(dadX - w * 0.11f, dadY - h * 0.01f)
                cubicTo(
                    dadX - w * 0.12f, dadY - h * 0.11f,
                    dadX - w * 0.04f, dadY - h * 0.145f,
                    dadX + w * 0.03f, dadY - h * 0.135f
                )
                cubicTo(
                    dadX + w * 0.10f, dadY - h * 0.125f,
                    dadX + w * 0.12f, dadY - h * 0.07f,
                    dadX + w * 0.11f, dadY - h * 0.01f
                )
                cubicTo(
                    dadX + w * 0.07f, dadY - h * 0.075f,
                    dadX - w * 0.04f, dadY - h * 0.085f,
                    dadX - w * 0.11f, dadY - h * 0.01f
                )
                close()
            }
            drawPath(path = dadHairFront, color = dadHair)

            // Dad facial features (eyebrows, eyes with catchlight, nose, warm smile)
            drawHumanFaceFeatures(
                cx = dadX,
                cy = dadY + h * 0.01f,
                faceW = w * 0.22f,
                faceH = h * 0.24f,
                browColor = dadHair,
                cheekColor = Color(0xFFE57373).copy(alpha = 0.22f),
                 noseShadow = dadSkinShadow
            )

            // =====================================================================
            // 2. MOTHER (Back Left) — Long flowing chestnut hair, warm smile, coral top
            // =====================================================================
            val momX = w * 0.35f
            val momY = h * 0.38f
            val momSkin = Color(0xFFF7CBB2)
            val momSkinShadow = Color(0xFFE3AE92)
            val momHair = Color(0xFF3E2217)

            // Mom flowing long back hair (drapes past shoulders)
            val momBackHair = Path().apply {
                moveTo(momX - w * 0.145f, momY - h * 0.04f)
                cubicTo(
                    momX - w * 0.17f, momY + h * 0.10f,
                    momX - w * 0.16f, momY + h * 0.24f,
                    momX - w * 0.11f, momY + h * 0.30f
                )
                lineTo(momX + w * 0.11f, momY + h * 0.30f)
                cubicTo(
                    momX + w * 0.16f, momY + h * 0.24f,
                    momX + w * 0.17f, momY + h * 0.10f,
                    momX + w * 0.145f, momY - h * 0.04f
                )
                cubicTo(
                    momX + w * 0.13f, momY - h * 0.15f,
                    momX - w * 0.13f, momY - h * 0.15f,
                    momX - w * 0.145f, momY - h * 0.04f
                )
                close()
            }
            drawPath(path = momBackHair, color = momHair)

            // Mom torso & shoulders
            val momTorso = Path().apply {
                moveTo(momX - w * 0.22f, h * 0.92f)
                cubicTo(
                    momX - w * 0.21f, momY + h * 0.19f,
                    momX - w * 0.13f, momY + h * 0.14f,
                    momX, momY + h * 0.14f
                )
                cubicTo(
                    momX + w * 0.13f, momY + h * 0.14f,
                    momX + w * 0.20f, momY + h * 0.19f,
                    momX + w * 0.21f, h * 0.92f
                )
                close()
            }
            drawPath(
                path = momTorso,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFF47B98), Color(0xFFE05779)),
                    startY = momY + h * 0.14f,
                    endY = h * 0.92f
                )
            )

            // Mom neck & V-neckline
            drawRoundRect(
                color = momSkinShadow,
                topLeft = Offset(momX - w * 0.036f, momY + h * 0.07f),
                size = Size(w * 0.072f, h * 0.10f),
                cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
            )
            drawCircle(
                color = momSkin,
                radius = w * 0.048f,
                center = Offset(momX, momY + h * 0.155f)
            )

            // Mom ears & pearl earrings
            drawCircle(color = momSkinShadow, radius = w * 0.024f, center = Offset(momX - w * 0.105f, momY + h * 0.01f))
            drawCircle(color = momSkinShadow, radius = w * 0.024f, center = Offset(momX + w * 0.105f, momY + h * 0.01f))
            drawCircle(color = Color.White, radius = w * 0.009f, center = Offset(momX - w * 0.105f, momY + h * 0.03f))
            drawCircle(color = Color.White, radius = w * 0.009f, center = Offset(momX + w * 0.105f, momY + h * 0.03f))

            // Mom oval face
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFDFCA), momSkin),
                    center = Offset(momX - w * 0.02f, momY - h * 0.02f),
                    radius = w * 0.14f
                ),
                topLeft = Offset(momX - w * 0.105f, momY - h * 0.105f),
                size = Size(w * 0.21f, h * 0.23f)
            )

            // Mom side-parted wavy bangs (frames top of forehead naturally)
            val momHairBangs = Path().apply {
                moveTo(momX - w * 0.108f, momY + h * 0.005f)
                cubicTo(
                    momX - w * 0.115f, momY - h * 0.095f,
                    momX - w * 0.05f, momY - h * 0.13f,
                    momX + w * 0.02f, momY - h * 0.12f
                )
                cubicTo(
                    momX + w * 0.09f, momY - h * 0.11f,
                    momX + w * 0.115f, momY - h * 0.06f,
                    momX + w * 0.108f, momY + h * 0.01f
                )
                cubicTo(
                    momX + w * 0.065f, momY - h * 0.055f,
                    momX + w * 0.01f, momY - h * 0.085f,
                    momX - w * 0.035f, momY - h * 0.075f
                )
                cubicTo(
                    momX - w * 0.075f, momY - h * 0.065f,
                    momX - w * 0.095f, momY - h * 0.03f,
                    momX - w * 0.108f, momY + h * 0.005f
                )
                close()
            }
            drawPath(path = momHairBangs, color = momHair)

            // Mom facial features
            drawHumanFaceFeatures(
                cx = momX,
                cy = momY + h * 0.012f,
                faceW = w * 0.21f,
                faceH = h * 0.23f,
                browColor = momHair,
                cheekColor = Color(0xFFF06292).copy(alpha = 0.28f),
                noseShadow = momSkinShadow,
                lipTint = Color(0xFFD85C72)
            )

            // =====================================================================
            // 3. DAUGHTER (Front Right) — Playful pigtails/bun, warm yellow sweater
            // =====================================================================
            val girlX = w * 0.64f
            val girlY = h * 0.64f
            val girlSkin = Color(0xFFF5C6A8)
            val girlSkinShadow = Color(0xFFE0AC8B)
            val girlHair = Color(0xFF4A2C1D)

            // Cute side puffs / pigtails behind head
            drawCircle(color = girlHair, radius = w * 0.052f, center = Offset(girlX - w * 0.10f, girlY - h * 0.05f))
            drawCircle(color = girlHair, radius = w * 0.052f, center = Offset(girlX + w * 0.10f, girlY - h * 0.05f))
            // Hair ties
            drawCircle(color = Color(0xFFF48FB1), radius = w * 0.016f, center = Offset(girlX - w * 0.082f, girlY - h * 0.045f))
            drawCircle(color = Color(0xFFF48FB1), radius = w * 0.016f, center = Offset(girlX + w * 0.082f, girlY - h * 0.045f))

            // Daughter torso
            val girlTorso = Path().apply {
                moveTo(girlX - w * 0.18f, h * 1.02f)
                cubicTo(
                    girlX - w * 0.17f, girlY + h * 0.15f,
                    girlX - w * 0.10f, girlY + h * 0.11f,
                    girlX, girlY + h * 0.11f
                )
                cubicTo(
                    girlX + w * 0.10f, girlY + h * 0.11f,
                    girlX + w * 0.17f, girlY + h * 0.15f,
                    girlX + w * 0.18f, h * 1.02f
                )
                close()
            }
            drawPath(
                path = girlTorso,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFFB74D), Color(0xFFF59E0B)),
                    startY = girlY + h * 0.11f,
                    endY = h
                )
            )

            // Daughter neck
            drawRoundRect(
                color = girlSkinShadow,
                topLeft = Offset(girlX - w * 0.03f, girlY + h * 0.06f),
                size = Size(w * 0.06f, h * 0.075f),
                cornerRadius = CornerRadius(w * 0.025f, w * 0.025f)
            )

            // Daughter ears
            drawCircle(color = girlSkinShadow, radius = w * 0.021f, center = Offset(girlX - w * 0.09f, girlY + h * 0.008f))
            drawCircle(color = girlSkinShadow, radius = w * 0.021f, center = Offset(girlX + w * 0.09f, girlY + h * 0.008f))

            // Daughter face oval
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFDEC7), girlSkin),
                    center = Offset(girlX - w * 0.015f, girlY - h * 0.015f),
                    radius = w * 0.12f
                ),
                topLeft = Offset(girlX - w * 0.09f, girlY - h * 0.09f),
                size = Size(w * 0.18f, h * 0.19f)
            )

            // Daughter bangs framing forehead
            val girlBangs = Path().apply {
                moveTo(girlX - w * 0.092f, girlY - h * 0.002f)
                cubicTo(
                    girlX - w * 0.095f, girlY - h * 0.085f,
                    girlX - w * 0.04f, girlY - h * 0.105f,
                    girlX, girlY - h * 0.102f
                )
                cubicTo(
                    girlX + w * 0.04f, girlY - h * 0.105f,
                    girlX + w * 0.095f, girlY - h * 0.085f,
                    girlX + w * 0.092f, girlY - h * 0.002f
                )
                cubicTo(
                    girlX + w * 0.05f, girlY - h * 0.062f,
                    girlX - w * 0.05f, girlY - h * 0.062f,
                    girlX - w * 0.092f, girlY - h * 0.002f
                )
                close()
            }
            drawPath(path = girlBangs, color = girlHair)

            // Daughter facial features
            drawHumanFaceFeatures(
                cx = girlX,
                cy = girlY + h * 0.01f,
                faceW = w * 0.18f,
                faceH = h * 0.19f,
                browColor = girlHair,
                cheekColor = Color(0xFFF48FB1).copy(alpha = 0.34f),
                noseShadow = girlSkinShadow
            )

            // =====================================================================
            // 4. SON (Front Left) — Tousled hair, bright blue hoodie/sweater, happy grin
            // =====================================================================
            val boyX = w * 0.39f
            val boyY = h * 0.65f
            val boySkin = Color(0xFFF4C3A3)
            val boySkinShadow = Color(0xFFDDA784)
            val boyHair = Color(0xFF233044)

            // Boy torso
            val boyTorso = Path().apply {
                moveTo(boyX - w * 0.18f, h * 1.02f)
                cubicTo(
                    boyX - w * 0.17f, boyY + h * 0.15f,
                    boyX - w * 0.10f, boyY + h * 0.11f,
                    boyX, boyY + h * 0.11f
                )
                cubicTo(
                    boyX + w * 0.10f, boyY + h * 0.11f,
                    boyX + w * 0.17f, boyY + h * 0.15f,
                    boyX + w * 0.18f, h * 1.02f
                )
                close()
            }
            drawPath(
                path = boyTorso,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF2994F2), Color(0xFF1976D2)),
                    startY = boyY + h * 0.11f,
                    endY = h
                )
            )

            // Boy neck & collar
            drawRoundRect(
                color = boySkinShadow,
                topLeft = Offset(boyX - w * 0.03f, boyY + h * 0.06f),
                size = Size(w * 0.06f, h * 0.075f),
                cornerRadius = CornerRadius(w * 0.025f, w * 0.025f)
            )
            drawArc(
                color = Color.White.copy(alpha = 0.85f),
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(boyX - w * 0.042f, boyY + h * 0.095f),
                size = Size(w * 0.084f, h * 0.05f),
                style = Stroke(width = w * 0.013f, cap = StrokeCap.Round)
            )

            // Boy crown hair
            drawOval(
                color = boyHair,
                topLeft = Offset(boyX - w * 0.098f, boyY - h * 0.115f),
                size = Size(w * 0.196f, h * 0.15f)
            )

            // Boy ears
            drawCircle(color = boySkinShadow, radius = w * 0.021f, center = Offset(boyX - w * 0.09f, boyY + h * 0.008f))
            drawCircle(color = boySkinShadow, radius = w * 0.021f, center = Offset(boyX + w * 0.09f, boyY + h * 0.008f))

            // Boy face oval
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFDCC4), boySkin),
                    center = Offset(boyX - w * 0.015f, boyY - h * 0.015f),
                    radius = w * 0.12f
                ),
                topLeft = Offset(boyX - w * 0.09f, boyY - h * 0.09f),
                size = Size(w * 0.18f, h * 0.19f)
            )

            // Boy tousled bangs
            val boyBangs = Path().apply {
                moveTo(boyX - w * 0.092f, boyY - h * 0.005f)
                cubicTo(
                    boyX - w * 0.098f, boyY - h * 0.085f,
                    boyX - w * 0.03f, boyY - h * 0.112f,
                    boyX + w * 0.025f, boyY - h * 0.105f
                )
                cubicTo(
                    boyX + w * 0.08f, boyY - h * 0.098f,
                    boyX + w * 0.098f, boyY - h * 0.06f,
                    boyX + w * 0.092f, boyY - h * 0.005f
                )
                cubicTo(
                    boyX + w * 0.045f, boyY - h * 0.062f,
                    boyX - w * 0.035f, boyY - h * 0.07f,
                    boyX - w * 0.092f, boyY - h * 0.005f
                )
                close()
            }
            drawPath(path = boyBangs, color = boyHair)

            // Boy facial features
            drawHumanFaceFeatures(
                cx = boyX,
                cy = boyY + h * 0.01f,
                faceW = w * 0.18f,
                faceH = h * 0.19f,
                browColor = boyHair,
                cheekColor = Color(0xFFF48FB1).copy(alpha = 0.30f),
                noseShadow = boySkinShadow
            )

            // =====================================================================
            // 5. EMBRACING HANDS ON SHOULDERS (Warm human connection)
            // =====================================================================
            // Mom's warm hand resting gently on son's left shoulder
            drawOval(
                color = momSkin,
                topLeft = Offset(boyX - w * 0.155f, boyY + h * 0.125f),
                size = Size(w * 0.065f, h * 0.048f)
            )
            // Dad's warm hand resting gently on daughter's right shoulder
            drawOval(
                color = dadSkin,
                topLeft = Offset(girlX + w * 0.095f, girlY + h * 0.125f),
                size = Size(w * 0.068f, h * 0.050f)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHumanFaceFeatures(
    cx: Float,
    cy: Float,
    faceW: Float,
    faceH: Float,
    browColor: Color,
    cheekColor: Color,
    noseShadow: Color,
    lipTint: Color = Color(0xFFC95B5B)
) {
    val eyeOffsetX = faceW * 0.21f
    val eyeY = cy - faceH * 0.06f
    val eyeRadius = faceW * 0.062f

    // Soft rosy cheeks
    drawOval(
        color = cheekColor,
        topLeft = Offset(cx - eyeOffsetX * 1.45f, cy + faceH * 0.01f),
        size = Size(faceW * 0.18f, faceH * 0.11f)
    )
    drawOval(
        color = cheekColor,
        topLeft = Offset(cx + eyeOffsetX * 0.60f, cy + faceH * 0.01f),
        size = Size(faceW * 0.18f, faceH * 0.11f)
    )

    // Natural curved eyebrows
    drawArc(
        color = browColor.copy(alpha = 0.85f),
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(cx - eyeOffsetX - eyeRadius * 1.3f, eyeY - faceH * 0.13f),
        size = Size(eyeRadius * 2.6f, faceH * 0.10f),
        style = Stroke(width = faceW * 0.028f, cap = StrokeCap.Round)
    )
    drawArc(
        color = browColor.copy(alpha = 0.85f),
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(cx + eyeOffsetX - eyeRadius * 1.3f, eyeY - faceH * 0.13f),
        size = Size(eyeRadius * 2.6f, faceH * 0.10f),
        style = Stroke(width = faceW * 0.028f, cap = StrokeCap.Round)
    )

    // Left & Right Eyes (warm dark iris + bright white catchlight)
    drawCircle(
        color = Color(0xFF1E293B),
        radius = eyeRadius,
        center = Offset(cx - eyeOffsetX, eyeY)
    )
    drawCircle(
        color = Color.White,
        radius = eyeRadius * 0.38f,
        center = Offset(cx - eyeOffsetX - eyeRadius * 0.25f, eyeY - eyeRadius * 0.28f)
    )

    drawCircle(
        color = Color(0xFF1E293B),
        radius = eyeRadius,
        center = Offset(cx + eyeOffsetX, eyeY)
    )
    drawCircle(
        color = Color.White,
        radius = eyeRadius * 0.38f,
        center = Offset(cx + eyeOffsetX - eyeRadius * 0.25f, eyeY - eyeRadius * 0.28f)
    )

    // Delicate nose tip
    drawArc(
        color = noseShadow,
        startAngle = 15f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(cx - faceW * 0.055f, cy + faceH * 0.01f),
        size = Size(faceW * 0.11f, faceH * 0.065f),
        style = Stroke(width = faceW * 0.025f, cap = StrokeCap.Round)
    )

    // Warm expressive smile
    val smilePath = Path().apply {
        moveTo(cx - faceW * 0.16f, cy + faceH * 0.15f)
        cubicTo(
            cx - faceW * 0.08f, cy + faceH * 0.26f,
            cx + faceW * 0.08f, cy + faceH * 0.26f,
            cx + faceW * 0.16f, cy + faceH * 0.15f
        )
    }
    drawPath(
        path = smilePath,
        color = lipTint,
        style = Stroke(width = faceW * 0.036f, cap = StrokeCap.Round)
    )
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
        name.startsWith("Val", ignoreCase = true) ||
        name.startsWith("Luc", ignoreCase = true) ||
        name.startsWith("Cam", ignoreCase = true)

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

            val hairColor = if (isFemale) Color(0xFF4A2C1D) else Color(0xFF233044)
            val skinColor = Color(0xFFF6C7AA)
            val skinShadow = Color(0xFFE0AC8B)
            val shirtColor = if (isFemale) Color(0xFFEC407A) else Color(0xFF1E88E5)

            // Back hair for girls (soft bob/waves) or crown for boys
            if (isFemale) {
                drawRoundRect(
                    color = hairColor,
                    topLeft = Offset(w * 0.19f, h * 0.22f),
                    size = Size(w * 0.62f, h * 0.54f),
                    cornerRadius = CornerRadius(w * 0.24f, w * 0.24f)
                )
            } else {
                drawOval(
                    color = hairColor,
                    topLeft = Offset(w * 0.22f, h * 0.18f),
                    size = Size(w * 0.56f, h * 0.42f)
                )
            }

            // Torso / shirt
            drawRoundRect(
                color = shirtColor,
                topLeft = Offset(w * 0.18f, h * 0.72f),
                size = Size(w * 0.64f, h * 0.36f),
                cornerRadius = CornerRadius(w * 0.22f, w * 0.22f)
            )

            // Neck
            drawRoundRect(
                color = skinShadow,
                topLeft = Offset(w * 0.42f, h * 0.62f),
                size = Size(w * 0.16f, h * 0.16f),
                cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
            )

            // Ears
            drawCircle(color = skinShadow, radius = w * 0.055f, center = Offset(w * 0.24f, h * 0.48f))
            drawCircle(color = skinShadow, radius = w * 0.055f, center = Offset(w * 0.76f, h * 0.48f))

            // Face oval
            drawOval(
                color = skinColor,
                topLeft = Offset(w * 0.25f, h * 0.24f),
                size = Size(w * 0.50f, h * 0.46f)
            )

            // Forehead bangs
            val bangs = Path().apply {
                moveTo(w * 0.24f, h * 0.43f)
                cubicTo(w * 0.23f, h * 0.22f, w * 0.40f, h * 0.18f, w * 0.52f, h * 0.19f)
                cubicTo(w * 0.66f, h * 0.20f, w * 0.77f, h * 0.26f, w * 0.76f, h * 0.43f)
                cubicTo(w * 0.62f, h * 0.30f, w * 0.38f, h * 0.30f, w * 0.24f, h * 0.43f)
                close()
            }
            drawPath(path = bangs, color = hairColor)

            // Features
            drawHumanFaceFeatures(
                cx = w * 0.50f,
                cy = h * 0.49f,
                faceW = w * 0.50f,
                faceH = h * 0.46f,
                browColor = hairColor,
                cheekColor = Color(0xFFF48FB1).copy(alpha = 0.32f),
                noseShadow = skinShadow
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
