package com.maksimowiczm.foodyou.tdee.domain.entity

/**
 * Everything the user entered on the TDEE screen, persisted between sessions.
 *
 * @property dailyIntakeKcal Intake the projection assumes. Null means "use the daily energy goal
 *   configured in the app", which keeps the projection in step with what the user is actually
 *   aiming to eat.
 * @property targetWeightKg Weight the user is working towards, or null if they only want the trend.
 * @property energyDensityKcalPerKg Energy assumed per kilogram of body mass gained or lost.
 */
data class TdeePlan(
    val profile: BodyProfile,
    val dailyIntakeKcal: Double?,
    val targetWeightKg: Double?,
    val energyDensityKcalPerKg: Double,
) {
    init {
        require(dailyIntakeKcal == null || dailyIntakeKcal >= 0.0) {
            "Daily intake must be non-negative"
        }
        require(
            targetWeightKg == null ||
                targetWeightKg in BodyProfile.MIN_WEIGHT_KG..BodyProfile.MAX_WEIGHT_KG
        ) {
            "Target weight must be between ${BodyProfile.MIN_WEIGHT_KG} and " +
                "${BodyProfile.MAX_WEIGHT_KG} kg"
        }
        require(energyDensityKcalPerKg > 0.0) { "Energy density must be positive" }
    }

    companion object {
        val default: TdeePlan =
            TdeePlan(
                profile = BodyProfile.default,
                dailyIntakeKcal = null,
                targetWeightKg = null,
                energyDensityKcalPerKg = DEFAULT_ENERGY_DENSITY_KCAL_PER_KG,
            )
    }
}

/**
 * Energy assumed per kilogram of body mass, used to turn an energy balance into a weight change.
 *
 * 7000 kcal/kg is the practical figure: it is a little below the ~7700 kcal/kg of pure adipose
 * tissue because real weight change also moves lean mass and water. Users can override it.
 */
const val DEFAULT_ENERGY_DENSITY_KCAL_PER_KG: Double = 7000.0

/** The theoretical energy content of a kilogram of body fat, offered as an alternative preset. */
const val ADIPOSE_ENERGY_DENSITY_KCAL_PER_KG: Double = 7700.0
