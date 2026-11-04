package com.maksimowiczm.foodyou.tdee.domain.entity

/**
 * Sex used by the basal metabolic rate equations.
 *
 * The BMR formulas implemented in [com.maksimowiczm.foodyou.tdee.domain.usecase.BmrCalculator] were
 * derived on male and female cohorts, so only those two coefficients sets exist. Users who do not
 * identify with either can pick whichever produces the estimate that matches their own measured
 * expenditure, or use [BmrFormula.KatchMcArdle], which is sex independent.
 */
enum class BiologicalSex {
    Male,
    Female,
}
