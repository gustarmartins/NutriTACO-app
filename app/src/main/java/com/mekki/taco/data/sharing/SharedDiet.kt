package com.mekki.taco.data.sharing

import com.google.gson.annotations.SerializedName
import com.mekki.taco.data.db.entity.Aminoacidos
import com.mekki.taco.data.db.entity.Lipidios

/**
 * Root object for diet sharing.
 * Contains all data needed to reconstruct a diet on another device.
 */
data class SharedDiet(
    /** Schema version for future compatibility */
    @SerializedName("format_version")
    val formatVersion: Int = CURRENT_FORMAT_VERSION,

    /** When this export was created */
    @SerializedName("exported_at")
    val exportedAt: Long = System.currentTimeMillis(),

    /** Diet metadata */
    @SerializedName("diet")
    val diet: SharedDietInfo,

    /** All meal entries in this diet */
    @SerializedName("entries")
    val entries: List<SharedDietEntry>,

    /**
     * Only includes foods with isCustom=true that are referenced by entries.
     * Official TACO foods Should Not be included in these
     */
    @SerializedName("custom_foods")
    val customFoods: List<SharedFood>
) {
    companion object {
        const val CURRENT_FORMAT_VERSION = 1
        const val MIME_TYPE = "application/json"
        const val FILE_EXTENSION = "dieta"
    }
}

/**
 * Diet metadata
 */
data class SharedDietInfo(
    @SerializedName("name")
    val name: String,

    @SerializedName("calorie_goal")
    val calorieGoal: Double?,

    @SerializedName("creation_date")
    val creationDate: Long
)

/**
 * A single item in the diet
 */
data class SharedDietEntry(
    /** Reference (tacoID or UUID) */
    @SerializedName("food_ref")
    val foodRef: FoodReference,

    /** Quantity in grams */
    @SerializedName("quantity_grams")
    val quantityGrams: Double,

    /** Meal type */
    @SerializedName("meal_type")
    val mealType: String?,

    /** consumption time */
    @SerializedName("consumption_time")
    val consumptionTime: String?
)

/**
 * Complete data for a custom food.
 * embedded in the shared diet so the receiver gets full food details.
 */
data class SharedFood(
    /** Unique identifier for this food across all users */
    @SerializedName("uuid")
    val uuid: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("category")
    val category: String,

    // Macros
    @SerializedName("energia_kcal")
    val energiaKcal: Double?,

    @SerializedName("energia_kj")
    val energiaKj: Double?,

    @SerializedName("proteina")
    val proteina: Double?,

    @SerializedName("carboidratos")
    val carboidratos: Double?,

    @SerializedName("colesterol")
    val colesterol: Double?,

    @SerializedName("fibra_alimentar")
    val fibraAlimentar: Double?,

    // Lipidios
    @SerializedName("lipidios")
    val lipidios: SharedLipidios?,

    // Mineral
    @SerializedName("calcio")
    val calcio: Double?,

    @SerializedName("magnesio")
    val magnesio: Double?,

    @SerializedName("manganes")
    val manganes: Double?,

    @SerializedName("fosforo")
    val fosforo: Double?,

    @SerializedName("ferro")
    val ferro: Double?,

    @SerializedName("sodio")
    val sodio: Double?,

    @SerializedName("potassio")
    val potassio: Double?,

    @SerializedName("cobre")
    val cobre: Double?,

    @SerializedName("zinco")
    val zinco: Double?,

    // Vitaminas
    @SerializedName("retinol")
    val retinol: Double?,

    @SerializedName("re")
    val RE: Double?,

    @SerializedName("rae")
    val RAE: Double?,

    @SerializedName("tiamina")
    val tiamina: Double?,

    @SerializedName("riboflavina")
    val riboflavina: Double?,

    @SerializedName("piridoxina")
    val piridoxina: Double?,

    @SerializedName("niacina")
    val niacina: Double?,

    @SerializedName("vitamina_c")
    val vitaminaC: Double?,

    @SerializedName("umidade")
    val umidade: Double?,

    @SerializedName("cinzas")
    val cinzas: Double?,

    @SerializedName("aminoacidos")
    val aminoacidos: SharedAminoacidos?
) {
    companion object {
        /**
         * Create from a Food entity.
         */
        fun fromFood(food: com.mekki.taco.data.db.entity.Food): SharedFood {
            require(food.uuid != null || food.tacoID.startsWith("CUSTOM-")) {
                "Cannot share official TACO food as SharedFood. Use FoodReference instead."
            }

            return SharedFood(
                uuid = food.uuid ?: food.tacoID,  // legacy
                name = food.name,
                category = food.category,
                energiaKcal = food.energiaKcal,
                energiaKj = food.energiaKj,
                proteina = food.proteina,
                carboidratos = food.carboidratos,
                colesterol = food.colesterol,
                fibraAlimentar = food.fibraAlimentar,
                lipidios = food.lipidios?.let { SharedLipidios.fromLipidios(it) },
                calcio = food.calcio,
                magnesio = food.magnesio,
                manganes = food.manganes,
                fosforo = food.fosforo,
                ferro = food.ferro,
                sodio = food.sodio,
                potassio = food.potassio,
                cobre = food.cobre,
                zinco = food.zinco,
                retinol = food.retinol,
                RE = food.RE,
                RAE = food.RAE,
                tiamina = food.tiamina,
                riboflavina = food.riboflavina,
                piridoxina = food.piridoxina,
                niacina = food.niacina,
                vitaminaC = food.vitaminaC,
                umidade = food.umidade,
                cinzas = food.cinzas,
                aminoacidos = food.aminoacidos?.let { SharedAminoacidos.fromAminoacidos(it) }
            )
        }
    }

    /**
     * Convert to a Food entity for database insertion.
     */
    fun toFood(): com.mekki.taco.data.db.entity.Food {
        return com.mekki.taco.data.db.entity.Food(
            id = 0,  // Auto-generate
            tacoID = "CUSTOM-$uuid",
            uuid = uuid,
            name = name,
            category = category,
            isCustom = true,
            usageCount = 0,
            energiaKcal = energiaKcal,
            energiaKj = energiaKj,
            proteina = proteina,
            carboidratos = carboidratos,
            colesterol = colesterol,
            fibraAlimentar = fibraAlimentar,
            lipidios = lipidios?.toLipidios(),
            calcio = calcio,
            magnesio = magnesio,
            manganes = manganes,
            fosforo = fosforo,
            ferro = ferro,
            sodio = sodio,
            potassio = potassio,
            cobre = cobre,
            zinco = zinco,
            retinol = retinol,
            RE = RE,
            RAE = RAE,
            tiamina = tiamina,
            riboflavina = riboflavina,
            piridoxina = piridoxina,
            niacina = niacina,
            vitaminaC = vitaminaC,
            umidade = umidade,
            cinzas = cinzas,
            aminoacidos = aminoacidos?.toAminoacidos()
        )
    }
}

data class SharedLipidios(
    @SerializedName("total")
    val total: Double?,
    @SerializedName("saturados")
    val saturados: Double?,
    @SerializedName("monoinsaturados")
    val monoinsaturados: Double?,
    @SerializedName("poliinsaturados")
    val poliinsaturados: Double?
) {
    companion object {
        fun fromLipidios(l: Lipidios) =
            SharedLipidios(l.total, l.saturados, l.monoinsaturados, l.poliinsaturados)
    }

    fun toLipidios() = Lipidios(total, saturados, monoinsaturados, poliinsaturados)
}

data class SharedAminoacidos(
    @SerializedName("triptofano") val triptofano: Double?,
    @SerializedName("treonina") val treonina: Double?,
    @SerializedName("isoleucina") val isoleucina: Double?,
    @SerializedName("leucina") val leucina: Double?,
    @SerializedName("lisina") val lisina: Double?,
    @SerializedName("metionina") val metionina: Double?,
    @SerializedName("cistina") val cistina: Double?,
    @SerializedName("fenilalanina") val fenilalanina: Double?,
    @SerializedName("tirosina") val tirosina: Double?,
    @SerializedName("valina") val valina: Double?,
    @SerializedName("arginina") val arginina: Double?,
    @SerializedName("histidina") val histidina: Double?,
    @SerializedName("alanina") val alanina: Double?,
    @SerializedName("acido_aspartico") val acidoAspartico: Double?,
    @SerializedName("acido_glutamico") val acidoGlutamico: Double?,
    @SerializedName("glicina") val glicina: Double?,
    @SerializedName("prolina") val prolina: Double?,
    @SerializedName("serina") val serina: Double?
) {
    companion object {
        fun fromAminoacidos(a: Aminoacidos) = SharedAminoacidos(
            a.triptofano, a.treonina, a.isoleucina, a.leucina, a.lisina,
            a.metionina, a.cistina, a.fenilalanina, a.tirosina, a.valina,
            a.arginina, a.histidina, a.alanina, a.acidoAspartico, a.acidoGlutamico,
            a.glicina, a.prolina, a.serina
        )
    }

    fun toAminoacidos() = Aminoacidos(
        triptofano, treonina, isoleucina, leucina, lisina,
        metionina, cistina, fenilalanina, tirosina, valina,
        arginina, histidina, alanina, acidoAspartico, acidoGlutamico,
        glicina, prolina, serina
    )
}