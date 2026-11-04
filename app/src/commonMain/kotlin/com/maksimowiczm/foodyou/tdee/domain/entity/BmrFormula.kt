package com.maksimowiczm.foodyou.tdee.domain.entity

/** Equation used to estimate the basal metabolic rate. */
enum class BmrFormula {
    /**
     * Mifflin-St Jeor (1990). The most accurate of the three for the general population and the
     * default here.
     */
    MifflinStJeor,

    /** Harris-Benedict as revised by Roza and Shizgal (1984). */
    HarrisBenedict,

    /**
     * Katch-McArdle, which is based on lean body mass instead of total mass and therefore requires
     * a body fat percentage. It is the most accurate option for lean or very muscular people.
     */
    KatchMcArdle;

    /** Whether this formula needs [BodyProfile.bodyFatPercentage] to be set. */
    val requiresBodyFat: Boolean
        get() = this == KatchMcArdle

    companion object {
        val default: BmrFormula = MifflinStJeor
    }
}
