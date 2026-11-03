package za.co.sprinta.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import za.co.sprinta.app.util.GeoUtils

class GeoUtilsTest {
    @Test fun paceCalculationIsCorrect() {
        assertEquals(300.0, GeoUtils.paceSecondsPerKm(5000.0, 1500L), 0.001)
    }

    @Test fun haversineDistanceIsPositive() {
        val distance = GeoUtils.haversineMeters(-33.9249, 18.4241, -33.9250, 18.4251)
        assertTrue(distance > 0)
    }

    @Test fun durationFormattingIsCorrect() {
        assertEquals("05:32", GeoUtils.formatDuration(332))
    }
}
