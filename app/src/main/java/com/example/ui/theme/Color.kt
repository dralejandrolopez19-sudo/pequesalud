package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// Paleta oficial de PequeSalud basada en la imagen de referencia
val PequeBlue = Color(0xFF1D7BE8)         // Azul: Confianza, Seguridad, Salud
val PequeNavy = Color(0xFF123E73)         // Azul marino para títulos y marca ("Peque")
val PequeGreen = Color(0xFF0FBA81)        // Verde: Bienestar, Crecimiento, Vida ("Salud")
val PequePurple = Color(0xFF8B6CEB)       // Morado: Cuidado, Tecnología, Innovación (Vacunas / IA)
val PequeYellow = Color(0xFFF5B027)       // Amarillo: Alegría, Optimismo, Infancia (Medicamentos)
val PequeOrange = Color(0xFFF59E0B)       // Ámbar/Naranja para estados pendientes o avisos
val PequeLightBlue = Color(0xFFE2ECFA)    // Celeste claro: Equilibrio, Calma, Frescura
val PequeSkyBg = Color(0xFFF4F8FD)        // Fondo principal suave de las pantallas

// Colores para Modo Oscuro
val PequeDarkBg = Color(0xFF0B1524)
val PequeDarkSurface = Color(0xFF152238)
val PequeDarkSurfaceVariant = Color(0xFF1E304C)
val PequeDarkOnSurface = Color(0xFFEAF2FF)
val PequeDarkOnSurfaceVariant = Color(0xFFA3B8D7)

// Colores pastel para las tarjetas del Grid 3x3 de Accesos Rápidos (Inicio)
val CardHistorialBg = Color(0xFFE3F7EC)
val CardHistorialIcon = Color(0xFF10A36C)

val CardVacunasBg = Color(0xFFEDE8FD)
val CardVacunasIcon = Color(0xFF7C5CE6)

val CardCrecimientoBg = Color(0xFFE0F6EE)
val CardCrecimientoIcon = Color(0xFF0E9F6E)

val CardMedicamentosBg = Color(0xFFFFF4DC)
val CardMedicamentosIcon = Color(0xFFE59500)

val CardLaboratoriosBg = Color(0xFFE3F0FE)
val CardLaboratoriosIcon = Color(0xFF1D7BE8)

val CardConsultasBg = Color(0xFFE7EFFD)
val CardConsultasIcon = Color(0xFF2563EB)

val CardNutricionBg = Color(0xFFE6F8E8)
val CardNutricionIcon = Color(0xFF16A34A)

val CardDocumentosBg = Color(0xFFE6F1FC)
val CardDocumentosIcon = Color(0xFF1E78E5)

val CardCalendarioBg = Color(0xFFFDE8EF)
val CardCalendarioIcon = Color(0xFFE11D48)

val CardHospitalBg = Color(0xFFFFECE6)
val CardHospitalIcon = Color(0xFFEA580C)

val CardAiBg = Color(0xFFEEE9FE)
val CardAiIcon = Color(0xFF6D28D9)

val CardAjustesBg = Color(0xFFEAEFF7)
val CardAjustesIcon = Color(0xFF475569)

val AlertRed = Color(0xFFE53935)
val AlertRedSoft = Color(0xFFFEECEB)
val AppliedGreenBadge = Color(0xFF1FC77E)
val PendingAmberBadge = Color(0xFFF59E0B)

@Composable
fun adaptiveCardTint(lightBg: Color, accentColor: Color): Color {
    return if (LocalIsDarkTheme.current) {
        lerp(PequeDarkSurface, accentColor, 0.22f)
    } else {
        lightBg
    }
}
