package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ChildProfile
import com.example.data.HealthRecord
import com.example.ui.components.GrowthMetricType
import com.example.ui.components.WhoAgeCohort
import com.example.ui.components.buildPatientGrowthSeries
import com.example.ui.components.evaluateWhoPercentile
import com.example.ui.components.getWhoStandardCurves
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PequeSalud", appName)
    }

    @Test
    fun `who growth curves and percentile calculation work accurately`() {
        val curves = getWhoStandardCurves(
            metric = GrowthMetricType.WEIGHT,
            cohort = WhoAgeCohort.PRESCHOOL_2_5Y,
            isMale = false
        )
        assertEquals(7, curves.p50.size)
        assertTrue(curves.p3.first() < curves.p15.first())
        assertTrue(curves.p15.first() < curves.p50.first())
        assertTrue(curves.p50.first() < curves.p85.first())
        assertTrue(curves.p85.first() < curves.p97.first())

        val medianAt3Y = curves.p50[2]
        val eval = evaluateWhoPercentile(medianAt3Y, 2f, curves)
        assertEquals(50, eval.exactPercentile)
        assertFalse(eval.isAlert)
    }

    @Test
    fun `patient growth series extracts history and live preview`() {
        val child = ChildProfile(
            id = "c1",
            name = "Sofía López",
            ageYears = 3L,
            gender = "Femenino",
            weightKg = 14.2,
            heightCm = 95.0,
            allergies = "Penicilina"
        )
        assertTrue(child.hasDrugAllergyWarning("Amoxicilina Suspensión"))
        assertFalse(child.hasDrugAllergyWarning("Paracetamol"))

        val curves = getWhoStandardCurves(
            metric = GrowthMetricType.WEIGHT,
            cohort = WhoAgeCohort.PRESCHOOL_2_5Y,
            isMale = false
        )
        val records = listOf(
            HealthRecord(
                id = "r1",
                childId = "c1",
                childName = "Sofía López",
                category = "CRECIMIENTO",
                title = "Peso: 14.2 kg · Talla: 95 cm",
                dateText = "Hoy"
            )
        )
        val series = buildPatientGrowthSeries(
            activeChild = child,
            growthRecords = records,
            metric = GrowthMetricType.WEIGHT,
            cohort = WhoAgeCohort.PRESCHOOL_2_5Y,
            curves = curves,
            livePreviewValue = 14.8
        )
        assertEquals(2, series.size)
        assertFalse(series.first().isLivePreview)
        assertTrue(series.last().isLivePreview)
    }
}
