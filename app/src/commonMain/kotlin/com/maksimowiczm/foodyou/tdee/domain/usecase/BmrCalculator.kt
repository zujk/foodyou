package com.maksimowiczm.foodyou.tdee.domain.usecase

import com.maksimowiczm.foodyou.tdee.domain.entity.BiologicalSex
import com.maksimowiczm.foodyou.tdee.domain.entity.BmrFormula
import com.maksimowiczm.foodyou.tdee.domain.entity.BodyProfile
import com.maksimowiczm.foodyou.tdee.domain.entity.EnergyExpenditure

/**
 * Estimates basal metabolic rate and total daily energy expenditure from a [BodyProfile].
 *
 * Every equation here is a population regression. Individual expenditure varies by roughly ±10%
 * around the estimate, so the output is a starting point to be corrected against real weight
 * measurements, not a measurement in itself.
 */
object BmrCalculator {

    /** Basal metabolic rate in kcal per day. */
    fun basalMetabolicRate(profile: BodyProfile): Double =
        when (profile.formula) {
            BmrFormula.MifflinStJeor -> mifflinStJeor(profile)
            BmrFormula.HarrisBenedict -> harrisBenedict(profile)
            BmrFormula.KatchMcArdle -> katchMcArdle(profile)
        }

    /** Basal metabolic rate scaled by the profile's activity level, in kcal per day. */
    fun totalDailyEnergyExpenditure(profile: BodyProfile): Double =
        basalMetabolicRate(profile) * profile.activityLevel.multiplier

    fun calculate(profile: BodyProfile): EnergyExpenditure {
        val bmr = basalMetabolicRate(profile)

        return EnergyExpenditure(
            basalMetabolicRateKcal = bmr,
            totalDailyEnergyExpenditureKcal = bmr * profile.activityLevel.multiplier,
        )
    }

    /**
     * Mifflin-St Jeor (1990):
     * `10 * kg + 6.25 * cm - 5 * age + 5` for men, the same with `- 161` for women.
     */
    private fun mifflinStJeor(profile: BodyProfile): Double {
        val sexOffset =
            when (profile.sex) {
                BiologicalSex.Male -> 5.0
                BiologicalSex.Female -> -161.0
            }

        return 10.0 * profile.weightKg + 6.25 * profile.heightCm - 5.0 * profile.ageYears +
            sexOffset
    }

    /** Harris-Benedict, revised by Roza and Shizgal (1984). */
    private fun harrisBenedict(profile: BodyProfile): Double =
        when (profile.sex) {
            BiologicalSex.Male ->
                88.362 + 13.397 * profile.weightKg + 4.799 * profile.heightCm -
                    5.677 * profile.ageYears

            BiologicalSex.Female ->
                447.593 + 9.247 * profile.weightKg + 3.098 * profile.heightCm -
                    4.330 * profile.ageYears
        }

    /** Katch-McArdle: `370 + 21.6 * lean body mass in kg`. Sex independent. */
    private fun katchMcArdle(profile: BodyProfile): Double {
        val leanBodyMass =
            requireNotNull(profile.leanBodyMassKg) {
                "Katch-McArdle requires a body fat percentage"
            }

        return 370.0 + 21.6 * leanBodyMass
    }
}
