package com.maksimowiczm.foodyou.tdee.domain.usecase

import com.maksimowiczm.foodyou.tdee.domain.entity.DEFAULT_ENERGY_DENSITY_KCAL_PER_KG
import com.maksimowiczm.foodyou.tdee.domain.entity.GoalOutcome
import com.maksimowiczm.foodyou.tdee.domain.entity.WeightMilestone
import com.maksimowiczm.foodyou.tdee.domain.entity.WeightProjection
import kotlin.math.abs
import kotlin.math.ceil

/**
 * Turns an energy balance into a weight trajectory.
 *
 * `kg per day = (intake - maintenance) / energy density`, applied linearly. See [WeightProjection]
 * for why the far end of the projection is optimistic.
 */
object WeightProjector {

    /** Days at which a projected weight is reported. */
    val MILESTONE_DAYS: List<Int> = listOf(7, 14, 30, 60, 90, 180, 365)

    /** A target within this distance of the current weight counts as already reached. */
    const val GOAL_TOLERANCE_KG: Double = 0.1

    /** Targets further out than this are reported as beyond the horizon rather than as a date. */
    const val MAX_HORIZON_DAYS: Int = 365 * 5

    /**
     * The linear model stops meaning anything near zero, so projected weights are not allowed
     * below this.
     */
    private const val MIN_PROJECTED_WEIGHT_KG: Double = 1.0

    fun project(
        currentWeightKg: Double,
        maintenanceKcal: Double,
        dailyIntakeKcal: Double,
        targetWeightKg: Double? = null,
        energyDensityKcalPerKg: Double = DEFAULT_ENERGY_DENSITY_KCAL_PER_KG,
    ): WeightProjection {
        require(currentWeightKg > 0.0) { "Current weight must be positive" }
        require(energyDensityKcalPerKg > 0.0) { "Energy density must be positive" }

        val dailyBalance = dailyIntakeKcal - maintenanceKcal
        val kgPerDay = dailyBalance / energyDensityKcalPerKg
        val goal = targetWeightKg?.let { goalOutcome(currentWeightKg, it, kgPerDay) }

        return WeightProjection(
            currentWeightKg = currentWeightKg,
            dailyIntakeKcal = dailyIntakeKcal,
            maintenanceKcal = maintenanceKcal,
            energyDensityKcalPerKg = energyDensityKcalPerKg,
            dailyBalanceKcal = dailyBalance,
            weightChangeKgPerDay = kgPerDay,
            milestones =
                milestones(
                    currentWeightKg = currentWeightKg,
                    kgPerDay = kgPerDay,
                    goal = goal,
                ),
            goal = goal,
        )
    }

    /**
     * Milestones are truncated at the day the target is reached, so the list never shows the user
     * sailing past their own goal.
     */
    private fun milestones(
        currentWeightKg: Double,
        kgPerDay: Double,
        goal: GoalOutcome?,
    ): List<WeightMilestone> {
        val lastDay = (goal as? GoalOutcome.Reachable)?.days ?: Int.MAX_VALUE

        return MILESTONE_DAYS.filter { it <= lastDay }.map { days ->
            val rawChange = kgPerDay * days
            val projected = (currentWeightKg + rawChange).coerceAtLeast(MIN_PROJECTED_WEIGHT_KG)

            WeightMilestone(
                days = days,
                projectedWeightKg = projected,
                changeKg = projected - currentWeightKg,
            )
        }
    }

    private fun goalOutcome(
        currentWeightKg: Double,
        targetWeightKg: Double,
        kgPerDay: Double,
    ): GoalOutcome {
        val remaining = targetWeightKg - currentWeightKg

        if (abs(remaining) <= GOAL_TOLERANCE_KG) {
            return GoalOutcome.AlreadyReached(targetWeightKg)
        }

        if (abs(kgPerDay) < WeightProjection.NEGLIGIBLE_KG_PER_DAY) {
            return GoalOutcome.Unreachable(
                targetWeightKg = targetWeightKg,
                reason = GoalOutcome.Unreachable.Reason.Maintenance,
            )
        }

        // Losing while aiming higher, or gaining while aiming lower.
        if (remaining > 0.0 != kgPerDay > 0.0) {
            return GoalOutcome.Unreachable(
                targetWeightKg = targetWeightKg,
                reason = GoalOutcome.Unreachable.Reason.WrongDirection,
            )
        }

        val days = ceil(remaining / kgPerDay)

        if (days > MAX_HORIZON_DAYS) {
            return GoalOutcome.Unreachable(
                targetWeightKg = targetWeightKg,
                reason = GoalOutcome.Unreachable.Reason.BeyondHorizon,
            )
        }

        return GoalOutcome.Reachable(
            targetWeightKg = targetWeightKg,
            days = days.toInt().coerceAtLeast(1),
        )
    }
}
