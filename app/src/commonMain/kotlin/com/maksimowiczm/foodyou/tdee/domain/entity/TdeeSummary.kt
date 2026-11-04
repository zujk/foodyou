package com.maksimowiczm.foodyou.tdee.domain.entity

/**
 * Everything derived from a [TdeePlan]: what the user burns, and where their weight is heading.
 *
 * @property dailyIntakeKcal The intake the projection used.
 * @property usesFallbackIntake True when [TdeePlan.dailyIntakeKcal] was unset and the caller's
 *   fallback (normally the daily energy goal) was used instead.
 */
data class TdeeSummary(
    val plan: TdeePlan,
    val expenditure: EnergyExpenditure,
    val projection: WeightProjection,
    val dailyIntakeKcal: Double,
    val usesFallbackIntake: Boolean,
)
