package com.mekki.taco

import com.mekki.taco.utils.unaccent
import org.junit.Assert.assertEquals
import org.junit.Test

class FoodSearchTest {

    private fun normalizeForFts(text: String): String {
        return text.unaccent().lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun buildFtsQuery(userInput: String): String {
        val normalized = normalizeForFts(userInput)
        return normalized.split(" ")
            .filter { it.isNotEmpty() }
            .joinToString(" ") { "$it*" }
    }

    @Test
    fun testNormalization_RemovesCommas() {
        val rawName = "Pão, de queijo, cru"
        val expected = "pao de queijo cru"
        assertEquals(expected, normalizeForFts(rawName))
    }

    @Test
    fun testNormalization_MacaPtVsMaca() {
        val apple = "Maçã"
        val stretcher = "Maca"

        assertEquals("maca", normalizeForFts(apple))
        assertEquals("maca", normalizeForFts(stretcher))
    }

    @Test
    fun testQueryConstruction_PaoDeQueijo() {
        val userInput = "Pão de queijo"
        assertEquals("pao* de* queijo*", buildFtsQuery(userInput))
    }

    @Test
    fun testQueryConstruction_FilesDeFrango() {
        val userInput = "Filés de frango"
        val expected = "files* de* frango*"
        assertEquals(expected, buildFtsQuery(userInput))
    }

    // Logic Expectation:
    /*
     Scenario: User types "Maçã"
     Candidates:
     1. "Maçã" (Apple) -> Norm: "maca"
     2. "Maca" (Stretcher) -> Norm: "maca"
     3. "Macarrão" -> Norm: "macarrao"
     
     Query: "maca*"
     Matches: 1, 2, 3 (maca matches start of macarrao)
     
     Ranking (Proposed):
     1. Exact Match on Original Name (Case Insensitive):
        - "Maçã" == "Maçã" -> YES (Priority 1)
        - "Maca" != "Maçã" -> NO
        - "Macarrão" != "Maçã" -> NO
     
     2. Length ASC:
        - "Maçã" (4)
        - "Maca" (4)
        - "Macarrão" (8)
     
     Result Order: Maçã, Maca, Macarrão.
    */
}