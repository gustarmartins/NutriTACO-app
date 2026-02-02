package com.mekki.taco

import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.db.entity.Lipidios
import com.mekki.taco.utils.NutrientWarning
import com.mekki.taco.utils.NutrientWarnings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NutrientWarningsTest {

    private fun createTestFood(
        sodio: Double? = null,
        saturados: Double? = null
    ) = Food(
        id = 1,
        tacoID = "TEST-1",
        name = "Test Food",
        category = "Test",
        isCustom = false,
        proteina = null,
        carboidratos = null,
        energiaKcal = null,
        energiaKj = null,
        colesterol = null,
        cinzas = null,
        calcio = null,
        magnesio = null,
        manganes = null,
        fosforo = null,
        ferro = null,
        sodio = sodio,
        potassio = null,
        cobre = null,
        zinco = null,
        retinol = null,
        RE = null,
        RAE = null,
        tiamina = null,
        riboflavina = null,
        piridoxina = null,
        niacina = null,
        vitaminaC = null,
        umidade = null,
        fibraAlimentar = null,
        lipidios = if (saturados != null) Lipidios(
            total = saturados,
            saturados = saturados,
            monoinsaturados = null,
            poliinsaturados = null
        ) else null,
        aminoacidos = null
    )

    // --- Sodium Tests (threshold: 600mg/100g) ---

    @Test
    fun sodium_BelowThreshold_NoWarning() {
        val food = createTestFood(sodio = 599.0)
        val warnings = NutrientWarnings.getWarningsForFood(food)
        assertTrue(warnings.isEmpty())
    }

    @Test
    fun sodium_AtThreshold_HasWarning() {
        val food = createTestFood(sodio = 600.0)
        val warnings = NutrientWarnings.getWarningsForFood(food)
        assertTrue(NutrientWarning.HIGH_SODIUM in warnings)
    }

    @Test
    fun sodium_AboveThreshold_HasWarning() {
        val food = createTestFood(sodio = 1200.0)
        val warnings = NutrientWarnings.getWarningsForFood(food)
        assertTrue(NutrientWarning.HIGH_SODIUM in warnings)
    }

    @Test
    fun sodium_Null_NoWarning() {
        val food = createTestFood(sodio = null)
        val warnings = NutrientWarnings.getWarningsForFood(food)
        assertTrue(NutrientWarning.HIGH_SODIUM !in warnings)
    }

    // --- Saturated Fat Tests (threshold: 6g/100g) ---

    @Test
    fun saturatedFat_BelowThreshold_NoWarning() {
        val food = createTestFood(saturados = 5.9)
        val warnings = NutrientWarnings.getWarningsForFood(food)
        assertTrue(NutrientWarning.HIGH_SATURATED_FAT !in warnings)
    }

    @Test
    fun saturatedFat_AtThreshold_HasWarning() {
        val food = createTestFood(saturados = 6.0)
        val warnings = NutrientWarnings.getWarningsForFood(food)
        assertTrue(NutrientWarning.HIGH_SATURATED_FAT in warnings)
    }

    @Test
    fun saturatedFat_AboveThreshold_HasWarning() {
        val food = createTestFood(saturados = 12.0)
        val warnings = NutrientWarnings.getWarningsForFood(food)
        assertTrue(NutrientWarning.HIGH_SATURATED_FAT in warnings)
    }

    @Test
    fun saturatedFat_Null_NoWarning() {
        val food = createTestFood(saturados = null)
        val warnings = NutrientWarnings.getWarningsForFood(food)
        assertTrue(NutrientWarning.HIGH_SATURATED_FAT !in warnings)
    }

    // --- Combined Tests ---

    @Test
    fun bothHighNutrients_BothWarnings() {
        val food = createTestFood(sodio = 800.0, saturados = 10.0)
        val warnings = NutrientWarnings.getWarningsForFood(food)
        assertEquals(2, warnings.size)
        assertTrue(NutrientWarning.HIGH_SODIUM in warnings)
        assertTrue(NutrientWarning.HIGH_SATURATED_FAT in warnings)
    }

    @Test
    fun noHighNutrients_NoWarnings() {
        val food = createTestFood(sodio = 100.0, saturados = 2.0)
        val warnings = NutrientWarnings.getWarningsForFood(food)
        assertTrue(warnings.isEmpty())
    }

    // --- Real-World Food Examples (TACO Database Values per 100g) ---

    @Test
    fun realFood_Bacon_HighInSodiumAndFat() {
        // Bacon, grilled - TACO values
        // Sodium: 1684mg/100g, Saturated fat: 10.12g/100g
        val bacon = createTestFood(sodio = 1684.0, saturados = 10.12)
        val warnings = NutrientWarnings.getWarningsForFood(bacon)
        assertEquals(2, warnings.size)
        assertTrue(NutrientWarning.HIGH_SODIUM in warnings)
        assertTrue(NutrientWarning.HIGH_SATURATED_FAT in warnings)
    }

    @Test
    fun realFood_Manteiga_HighInFatOnly() {
        // Butter, with salt - TACO values
        // Sodium: 579mg/100g (below 600), Saturated fat: 48.68g/100g
        val manteiga = createTestFood(sodio = 579.0, saturados = 48.68)
        val warnings = NutrientWarnings.getWarningsForFood(manteiga)
        assertEquals(1, warnings.size)
        assertTrue(NutrientWarning.HIGH_SATURATED_FAT in warnings)
        assertTrue(NutrientWarning.HIGH_SODIUM !in warnings)
    }

    @Test
    fun realFood_SalRefinado_HighInSodiumOnly() {
        // Refined salt - approximate
        // Sodium: 38758mg/100g, no fat
        val sal = createTestFood(sodio = 38758.0, saturados = 0.0)
        val warnings = NutrientWarnings.getWarningsForFood(sal)
        assertEquals(1, warnings.size)
        assertTrue(NutrientWarning.HIGH_SODIUM in warnings)
    }

    @Test
    fun realFood_ArrozBranco_NoWarnings() {
        // White rice, cooked - TACO values
        // Sodium: 1mg/100g, Saturated fat: 0.06g/100g
        val arroz = createTestFood(sodio = 1.0, saturados = 0.06)
        val warnings = NutrientWarnings.getWarningsForFood(arroz)
        assertTrue(warnings.isEmpty())
    }

    @Test
    fun realFood_QueijoMussarela_HighInBoth() {
        // Mozzarella cheese - TACO values
        // Sodium: 604mg/100g, Saturated fat: 11.94g/100g
        val queijo = createTestFood(sodio = 604.0, saturados = 11.94)
        val warnings = NutrientWarnings.getWarningsForFood(queijo)
        assertEquals(2, warnings.size)
        assertTrue(NutrientWarning.HIGH_SODIUM in warnings)
        assertTrue(NutrientWarning.HIGH_SATURATED_FAT in warnings)
    }

    @Test
    fun realFood_BatataFrita_HighInFat() {
        // French fries - TACO values
        // Sodium: 163mg/100g, Saturated fat: 2.24g/100g
        // Note: Many commercial versions would have higher values due to added salt
        val batataFrita = createTestFood(sodio = 163.0, saturados = 2.24)
        val warnings = NutrientWarnings.getWarningsForFood(batataFrita)
        assertTrue(warnings.isEmpty())
    }

    // --- Brazilian Processed Foods (Industry Products) ---

    @Test
    fun processedFood_NissinMiojoGalinha_HighInBoth() {
        // Nissin Miojo Galinha Caipira (dry) - from official website
        // Sodium: 1814mg/100g (VERY HIGH), Saturated fat: 8.6g/100g
        val miojo = createTestFood(sodio = 1814.0, saturados = 8.6)
        val warnings = NutrientWarnings.getWarningsForFood(miojo)
        assertEquals(2, warnings.size)
        assertTrue(NutrientWarning.HIGH_SODIUM in warnings)
        assertTrue(NutrientWarning.HIGH_SATURATED_FAT in warnings)
    }

    @Test
    fun processedFood_DoritosQueijoNacho_BelowThresholds() {
        // Doritos Queijo Nacho - from FatSecret
        // Sodium: 575mg/100g (just below 600), Saturated fat: 3.6g/100g
        val doritos = createTestFood(sodio = 575.0, saturados = 3.6)
        val warnings = NutrientWarnings.getWarningsForFood(doritos)
        assertTrue(warnings.isEmpty())
    }

    @Test
    fun processedFood_OreoOriginal_CloseToBothThresholds() {
        // Biscoito Oreo Original - from Standout/Mondelez
        // Sodium: 277mg/100g, Saturated fat: 5.6g/100g (just below 6)
        val oreo = createTestFood(sodio = 277.0, saturados = 5.6)
        val warnings = NutrientWarnings.getWarningsForFood(oreo)
        assertTrue(warnings.isEmpty())
    }

    @Test
    fun processedFood_PassatempoChocolate_HighInFat() {
        // Biscoito Passatempo Recheado - from Nestlé
        // Sodium: 280mg/100g, Saturated fat: 6.2g/100g
        val passatempo = createTestFood(sodio = 280.0, saturados = 6.2)
        val warnings = NutrientWarnings.getWarningsForFood(passatempo)
        assertEquals(1, warnings.size)
        assertTrue(NutrientWarning.HIGH_SATURATED_FAT in warnings)
    }

    @Test
    fun processedFood_FiniDentaduras_LowInBoth() {
        // Bala de Gelatina Fini Dentaduras - from FatSecret
        // Sodium: 9.2mg/100g, Saturated fat: 0g/100g
        // Note: High in sugar (~83g), but we don't track that yet
        val fini = createTestFood(sodio = 9.2, saturados = 0.0)
        val warnings = NutrientWarnings.getWarningsForFood(fini)
        assertTrue(warnings.isEmpty())
    }
}
