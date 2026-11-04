package com.maksimowiczm.foodyou.tdee.domain.entity

/**
 * The anthropometric data needed to estimate energy expenditure.
 *
 * @property ageYears Age in whole years.
 * @property heightCm Height in centimetres.
 * @property weightKg Current body mass in kilograms.
 * @property bodyFatPercentage Body fat as a percentage of total mass, in `0..100`. Required when
 *   [formula] is [BmrFormula.KatchMcArdle], optional otherwise.
 */
data class BodyProfile(
    val sex: BiologicalSex,
    val ageYears: Int,
    val heightCm: Double,
    val weightKg: Double,
    val bodyFatPercentage: Double?,
    val activityLevel: ActivityLevel,
    val formula: BmrFormula,
) {
    init {
        require(ageYears in MIN_AGE..MAX_AGE) { "Age must be between $MIN_AGE and $MAX_AGE years" }
        require(heightCm in MIN_HEIGHT_CM..MAX_HEIGHT_CM) {
            "Height must be between $MIN_HEIGHT_CM and $MAX_HEIGHT_CM cm"
        }
        require(weightKg in MIN_WEIGHT_KG..MAX_WEIGHT_KG) {
            "Weight must be between $MIN_WEIGHT_KG and $MAX_WEIGHT_KG kg"
        }
        require(bodyFatPercentage == null || bodyFatPercentage in MIN_BODY_FAT..MAX_BODY_FAT) {
            "Body fat percentage must be between $MIN_BODY_FAT and $MAX_BODY_FAT"
        }
        require(!formula.requiresBodyFat || bodyFatPercentage != null) {
            "${formula.name} requires a body fat percentage"
        }
    }

    /** Lean body mass in kilograms, or null when [bodyFatPercentage] is unknown. */
    val leanBodyMassKg: Double?
        get() = bodyFatPercentage?.let { weightKg * (1.0 - it / 100.0) }

    /** Body mass index in kg/m². */
    val bodyMassIndex: Double
        get() = weightKg / (heightCm / 100.0).let { it * it }

    companion object {
        const val MIN_AGE: Int = 1
        const val MAX_AGE: Int = 130
        const val MIN_HEIGHT_CM: Double = 50.0
        const val MAX_HEIGHT_CM: Double = 272.0
        const val MIN_WEIGHT_KG: Double = 2.0
        const val MAX_WEIGHT_KG: Double = 700.0
        const val MIN_BODY_FAT: Double = 1.0
        const val MAX_BODY_FAT: Double = 75.0

        /**
         * A neutral starting point for the form. It is deliberately a plain adult average rather
         * than anything derived from the user's data, which the app does not have until they fill
         * the form in.
         */
        val default: BodyProfile =
            BodyProfile(
                sex = BiologicalSex.Male,
                ageYears = 30,
                heightCm = 175.0,
                weightKg = 75.0,
                bodyFatPercentage = null,
                activityLevel = ActivityLevel.default,
                formula = BmrFormula.default,
            )
    }
}
