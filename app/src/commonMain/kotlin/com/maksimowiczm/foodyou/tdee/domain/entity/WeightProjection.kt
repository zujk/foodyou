package com.maksimowiczm.foodyou.tdee.domain.entity

/**
 * A linear extrapolation of body mass from the gap between energy intake and energy expenditure.
 *
 * The model is deliberately simple: a fixed amount of energy per kilogram of body mass
 * ([energyDensityKcalPerKg]) and a constant expenditure. Real expenditure falls as mass falls, so
 * the further out a projection goes the more it overstates the change. Treat the far milestones as
 * an upper bound, not a forecast.
 *
 * @property maintenanceKcal Intake at which mass is expected to stay constant, normally the TDEE.
 * @property dailyBalanceKcal Intake minus maintenance. Negative is a deficit.
 * @property weightChangeKgPerDay Negative when losing mass.
 */
data class WeightProjection(
    val currentWeightKg: Double,
    val dailyIntakeKcal: Double,
    val maintenanceKcal: Double,
    val energyDensityKcalPerKg: Double,
    val dailyBalanceKcal: Double,
    val weightChangeKgPerDay: Double,
    val milestones: List<WeightMilestone>,
    val goal: GoalOutcome?,
) {
    val weightChangeKgPerWeek: Double
        get() = weightChangeKgPerDay * 7.0

    val weightChangeKgPerMonth: Double
        get() = weightChangeKgPerDay * 30.0

    val trend: WeightTrend
        get() =
            when {
                weightChangeKgPerDay < -NEGLIGIBLE_KG_PER_DAY -> WeightTrend.Loss
                weightChangeKgPerDay > NEGLIGIBLE_KG_PER_DAY -> WeightTrend.Gain
                else -> WeightTrend.Maintenance
            }

    companion object {
        /** Below roughly 7 g per day the trend is noise, not a direction. */
        const val NEGLIGIBLE_KG_PER_DAY: Double = 0.007
    }
}

enum class WeightTrend {
    Loss,
    Gain,
    Maintenance,
}

/**
 * Projected body mass after [days] days of the current energy balance.
 *
 * @property changeKg Signed difference from the starting mass.
 */
data class WeightMilestone(val days: Int, val projectedWeightKg: Double, val changeKg: Double)

/** What the current energy balance means for the target weight the user asked about. */
sealed interface GoalOutcome {
    val targetWeightKg: Double

    /**
     * @property days Whole days until the target is reached, rounded up.
     */
    data class Reachable(override val targetWeightKg: Double, val days: Int) : GoalOutcome {
        val weeks: Double
            get() = days / 7.0
    }

    /** The current mass is already at the target, within [WeightProjector.GOAL_TOLERANCE_KG]. */
    data class AlreadyReached(override val targetWeightKg: Double) : GoalOutcome

    data class Unreachable(override val targetWeightKg: Double, val reason: Reason) : GoalOutcome {
        enum class Reason {
            /** The balance moves mass away from the target, e.g. a surplus with a lower target. */
            WrongDirection,

            /** Intake matches expenditure, so mass is not expected to move at all. */
            Maintenance,

            /** The target is reachable in principle but further out than the model is projected. */
            BeyondHorizon,
        }
    }
}
