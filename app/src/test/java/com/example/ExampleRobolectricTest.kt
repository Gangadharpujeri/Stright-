package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.FaultType
import com.example.data.model.ReportStatus
import com.example.data.model.Severity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("StreetLight Alert", appName)
    }

    @Test
    fun `test fault types parsing and default severity`() {
        val darkFault = FaultType.fromString("TOTAL_OUTAGE")
        assertEquals(FaultType.TOTAL_OUTAGE, darkFault)
        assertEquals(Severity.HIGH, darkFault.defaultSeverity)

        val wireHazard = FaultType.fromString("EXPOSED_WIRES")
        assertEquals(Severity.CRITICAL, wireHazard.defaultSeverity)

        val defaultFault = FaultType.fromString("UNKNOWN_VALUE")
        assertEquals(FaultType.TOTAL_OUTAGE, defaultFault)
    }

    @Test
    fun `test report status progression steps`() {
        val submitted = ReportStatus.fromString("SUBMITTED")
        val resolved = ReportStatus.fromString("RESOLVED")
        assertEquals(0, submitted.stepIndex)
        assertEquals(3, resolved.stepIndex)
    }
}
