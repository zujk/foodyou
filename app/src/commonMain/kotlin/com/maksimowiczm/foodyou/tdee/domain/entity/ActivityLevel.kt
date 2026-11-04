package com.maksimowiczm.foodyou.tdee.domain.entity

/**
 * Physical activity level, expressed as the multiplier applied to the basal metabolic rate in order
 * to obtain the total daily energy expenditure.
 *
 * The multipliers are the ones commonly published alongside the Harris-Benedict and Mifflin-St
 * Jeor equations.
 */
enum class ActivityLevel(val multiplier: Double) {
    /** Little or no exercise, desk job. */
    Sedentary(1.2),

    /** Light exercise one to three days per week. */
    LightlyActive(1.375),

    /** Moderate exercise three to five days per week. */
    ModeratelyActive(1.55),

    /** Hard exercise six to seven days per week. */
    VeryActive(1.725),

    /** Very hard exercise, physical job or training twice a day. */
    ExtraActive(1.9);

    companion object {
        val default: ActivityLevel = ModeratelyActive
    }
}
