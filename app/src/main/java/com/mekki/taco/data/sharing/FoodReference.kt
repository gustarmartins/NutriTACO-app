package com.mekki.taco.data.sharing

import com.google.gson.annotations.SerializedName

/**
 * A universal reference to a food item that works across devices.
 * tacoID for official TACO foods and UUID for custom foods.
 *
 */
data class FoodReference(
    @SerializedName("taco_id")
    val tacoId: String? = null,

    @SerializedName("uuid")
    val uuid: String? = null
) {
    val isOfficial: Boolean get() = tacoId != null && !tacoId.startsWith("CUSTOM-")
    val isCustom: Boolean get() = uuid != null || tacoId?.startsWith("CUSTOM-") == true

    companion object {
        /**
         * Create a reference from a Food entity.
         */
        fun fromFood(food: com.mekki.taco.data.db.entity.Food): FoodReference {
            return if (food.isCustom) {
                FoodReference(
                    tacoId = null,
                    uuid = food.uuid ?: food.tacoID  // legacy custom foods
                )
            } else {
                FoodReference(tacoId = food.tacoID, uuid = null)
            }
        }
    }
}