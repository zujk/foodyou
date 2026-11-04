package com.maksimowiczm.foodyou.tdee.domain.usecase

import com.maksimowiczm.foodyou.tdee.domain.entity.ActivityLevel
import com.maksimowiczm.foodyou.tdee.domain.entity.BiologicalSex
import com.maksimowiczm.foodyou.tdee.domain.entity.BmrFormula
import com.maksimowiczm.foodyou.tdee.domain.entity.BodyProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BmrCalculatorTest {

    private fun profile(
        sex: BiologicalSex = BiologicalSex.Male,
        ageYears: Int = 30,
        heightCm: Double = 180.0,
        weightKg: Double = 80.0,
        bodyFatPercentage: Double? = null,
        activityLevel: ActivityLevel = ActivityLevel.Sedentary,
        formula: BmrFormula = BmrFormula.MifflinStJeor,
    ) =
        BodyProfile(
            sex = sex,
            ageYears = ageYears,
            heightCm = heightCm,
            weightKg = weightKg,
            bodyFatPercentage = bodyFatPercentage,
            activityLevel = activityLevel,
            formula = formula,
        )

    @Test
    fun mifflinStJeor_male() {
        // 10*80 + 6.25*180 - 5*30 + 5 = 1780
        assertEquals(1780.0, BmrCalculator.basalMetabolicRate(profile()), TOLERANCE)
    }

    @Test
    fun mifflinStJeor_female() {
        // 10*65 + 6.25*165 - 5*30 - 161 = 1370.25
        val bmr =
            BmrCalculator.basalMetabolicRate(
                profile(sex = BiologicalSex.Female, heightCm = 165.0, weightKg = 65.0)
            )

        assertEquals(1370.25, bmr, TOLERANCE)
    }

    @Test
    fun harrisBenedict_male() {
        // 88.362 + 13.397*80 + 4.799*180 - 5.677*30 = 1858.772
        val bmr = BmrCalculator.basalMetabolicRate(profile(formula = BmrFormula.HarrisBenedict))

        assertEquals(1858.772, bmr, TOLERANCE)
    }

    @Test
    fun harrisBenedict_female() {
        // 447.593 + 9.247*65 + 3.098*165 - 4.330*30 = 1427.098
        val bmr =
            BmrCalculator.basalMetabolicRate(
                profile(
                    sex = BiologicalSex.Female,
                    heightCm = 165.0,
                    weightKg = 65.0,
                    formula = BmrFormula.HarrisBenedict,
                )
            )

        assertEquals(1427.098, bmr, TOLERANCE)
    }

    @Test
    fun katchMcArdle_usesLeanMassAndIgnoresSex() {
        // lean mass 80 * 0.8 = 64 kg; 370 + 21.6*64 = 1752.4
        val male =
            BmrCalculator.basalMetabolicRate(
                profile(bodyFatPercentage = 20.0, formula = BmrFormula.KatchMcArdle)
            )
        val female =
            BmrCalculator.basalMetabolicRate(
                profile(
                    sex = BiologicalSex.Female,
                    bodyFatPercentage = 20.0,
                    formula = BmrFormula.KatchMcArdle,
                )
            )

        assertEquals(1752.4, male, TOLERANCE)
        assertEquals(male, female, TOLERANCE)
    }

    @Test
    fun katchMcArdle_withoutBodyFatIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            profile(bodyFatPercentage = null, formula = BmrFormula.KatchMcArdle)
        }
    }

    @Test
    fun activityLevel_scalesBasalRate() {
        val sedentary = BmrCalculator.calculate(profile(activityLevel = ActivityLevel.Sedentary))
        val veryActive = BmrCalculator.calculate(profile(activityLevel = ActivityLevel.VeryActive))

        assertEquals(
            sedentary.basalMetabolicRateKcal,
            veryActive.basalMetabolicRateKcal,
            TOLERANCE,
        )
        assertEquals(
            1780.0 * ActivityLevel.VeryActive.multiplier,
            veryActive.totalDailyEnergyExpenditureKcal,
            TOLERANCE,
        )
        assertTrue(
            veryActive.totalDailyEnergyExpenditureKcal > sedentary.totalDailyEnergyExpenditureKcal
        )
    }

    @Test
    fun outOfRangeMeasurementsAreRejected() {
        assertFailsWith<IllegalArgumentException> { profile(ageYears = 0) }
        assertFailsWith<IllegalArgumentException> { profile(heightCm = 10.0) }
        assertFailsWith<IllegalArgumentException> { profile(weightKg = 0.0) }
        assertFailsWith<IllegalArgumentException> { profile(bodyFatPercentage = 90.0) }
    }

    @Test
    fun bodyMassIndex() {
        // 80 / 1.8^2 = 24.691
        assertEquals(24.691, profile().bodyMassIndex, 0.001)
    }

    private companion object {
        const val TOLERANCE = 0.001
    }
}
