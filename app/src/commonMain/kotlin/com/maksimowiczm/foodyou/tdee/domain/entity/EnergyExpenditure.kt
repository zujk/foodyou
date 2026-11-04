package com.maksimowiczm.foodyou.tdee.domain.entity

/**
 * Estimated daily energy expenditure.
 *
 * @property basalMetabolicRateKcal Energy burnt at complete rest, in kcal per day.
 * @property totalDailyEnergyExpenditureKcal Basal rate scaled by the activity level, in kcal per
 *   day. This is the intake at which body mass is expected to stay constant.
 */
data class EnergyExpenditure(
    val basalMetabolicRateKcal: Double,
    val totalDailyEnergyExpenditureKcal: Double,
)
