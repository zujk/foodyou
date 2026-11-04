package com.maksimowiczm.foodyou.tdee.domain.usecase

import com.maksimowiczm.foodyou.tdee.domain.entity.TdeePlan
import com.maksimowiczm.foodyou.tdee.domain.entity.TdeeSummary

/**
 * Combines [BmrCalculator] and [WeightProjector] into the single figure set the TDEE screen shows.
 *
 * Kept as a pure function so the screen can recompute it on every keystroke for a live preview
 * while the view model computes the same thing from the stored plan.
 */
object SummarizeTdeePlan {

    /**
     * @param fallbackIntakeKcal Intake used when the plan does not pin one down, normally the daily
     *   energy goal configured in the app.
     */
    operator fun invoke(plan: TdeePlan, fallbackIntakeKcal: Double): TdeeSummary {
        val expenditure = BmrCalculator.calculate(plan.profile)
        val intakeKcal = plan.dailyIntakeKcal ?: fallbackIntakeKcal

        val projection =
            WeightProjector.project(
                currentWeightKg = plan.profile.weightKg,
                maintenanceKcal = expenditure.totalDailyEnergyExpenditureKcal,
                dailyIntakeKcal = intakeKcal,
                targetWeightKg = plan.targetWeightKg,
                energyDensityKcalPerKg = plan.energyDensityKcalPerKg,
            )

        return TdeeSummary(
            plan = plan,
            expenditure = expenditure,
            projection = projection,
            dailyIntakeKcal = intakeKcal,
            usesFallbackIntake = plan.dailyIntakeKcal == null,
        )
    }
}
