package com.jd.fitness

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMigrationValidationTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun testLegacyWebsiteJsonDetection() {
        val legacyJson = """
        {
            "userName": "JD",
            "startDate": "2026-11-03",
            "history": {
                "2026-11-03": {
                    "habit_1": true,
                    "habit_2": true,
                    "habit_3": true,
                    "habit_4": true,
                    "habit_5": true
                }
            },
            "weights": [
                { "date": "2026-11-03", "weightKg": 74.5, "notes": "Fasted baseline" }
            ]
        }
        """.trimIndent()

        val root = json.parseToJsonElement(legacyJson).jsonObject
        val userName = root["userName"]?.jsonPrimitive?.content
        val startDate = root["startDate"]?.jsonPrimitive?.content
        val historyObj = root["history"]?.jsonObject

        assertEquals("JD", userName)
        assertEquals("2026-11-03", startDate)
        assertNotNull(historyObj)
        assertTrue(historyObj!!.containsKey("2026-11-03"))

        val dayObj = historyObj["2026-11-03"]?.jsonObject
        assertNotNull(dayObj)
        assertEquals(true, dayObj!!["habit_4"]?.jsonPrimitive?.booleanOrNull)
    }

    @Test
    fun testWeightAveragesCalculation() {
        val sampleWeights = listOf(75.0, 74.5, 74.0, 73.5, 74.0)
        val average = sampleWeights.average()
        assertEquals(74.2, average, 0.01)
    }
}

