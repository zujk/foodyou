package com.maksimowiczm.foodyou.app.ui.tdee

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import com.maksimowiczm.foodyou.app.ui.common.form.FormField
import com.maksimowiczm.foodyou.app.ui.common.form.doubleParser
import com.maksimowiczm.foodyou.app.ui.common.form.intParser
import com.maksimowiczm.foodyou.app.ui.common.form.nullableDoubleParser
import com.maksimowiczm.foodyou.app.ui.common.form.rememberFormField
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.common.compose.utility.formatClipZeros
import com.maksimowiczm.foodyou.tdee.domain.entity.ActivityLevel
import com.maksimowiczm.foodyou.tdee.domain.entity.BiologicalSex
import com.maksimowiczm.foodyou.tdee.domain.entity.BmrFormula
import com.maksimowiczm.foodyou.tdee.domain.entity.BodyProfile
import com.maksimowiczm.foodyou.tdee.domain.entity.TdeePlan
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

internal enum class TdeeFormError {
    Required,
    InvalidNumber,
    OutOfRange;

    @Composable
    fun stringResource(): String =
        when (this) {
            Required -> stringResource(Res.string.neutral_required)
            InvalidNumber -> stringResource(Res.string.error_invalid_number)
            OutOfRange -> stringResource(Res.string.error_value_out_of_range)
        }
}

/**
 * Holds the TDEE screen inputs.
 *
 * Energy fields are kept in the unit the user has chosen for the app, so they are converted back to
 * kilocalories in [TdeeFormState.toPlan]. The energy density is the exception: it is a figure from
 * the literature expressed in kcal per kilogram and is labelled as such, so it is never converted.
 */
@Composable
internal fun rememberTdeeFormState(plan: TdeePlan): TdeeFormState {
    val energyFormatter = LocalEnergyFormatter.current
    val profile = plan.profile

    val sexState = remember(plan) { mutableStateOf(profile.sex) }
    val activityLevelState = remember(plan) { mutableStateOf(profile.activityLevel) }
    val formulaState = remember(plan) { mutableStateOf(profile.formula) }

    val age =
        rememberFormField(
            initialValue = profile.ageYears,
            parser =
                intParser(
                    onNotANumber = { TdeeFormError.InvalidNumber },
                    onBlank = { TdeeFormError.Required },
                ),
            validator = {
                if (it in BodyProfile.MIN_AGE..BodyProfile.MAX_AGE) {
                    null
                } else {
                    TdeeFormError.OutOfRange
                }
            },
            textFieldState = rememberTextFieldState(profile.ageYears.toString()),
        )

    val height =
        rememberFormField(
            initialValue = profile.heightCm,
            parser =
                doubleParser(
                    onNotANumber = { TdeeFormError.InvalidNumber },
                    onBlank = { TdeeFormError.Required },
                ),
            validator = {
                if (it in BodyProfile.MIN_HEIGHT_CM..BodyProfile.MAX_HEIGHT_CM) {
                    null
                } else {
                    TdeeFormError.OutOfRange
                }
            },
            textFieldState = rememberTextFieldState(profile.heightCm.formatClipZeros()),
        )

    val weight =
        rememberFormField(
            initialValue = profile.weightKg,
            parser =
                doubleParser(
                    onNotANumber = { TdeeFormError.InvalidNumber },
                    onBlank = { TdeeFormError.Required },
                ),
            validator = {
                if (it in BodyProfile.MIN_WEIGHT_KG..BodyProfile.MAX_WEIGHT_KG) {
                    null
                } else {
                    TdeeFormError.OutOfRange
                }
            },
            textFieldState = rememberTextFieldState(profile.weightKg.formatClipZeros()),
        )

    val bodyFat =
        rememberFormField(
            initialValue = profile.bodyFatPercentage,
            parser = nullableDoubleParser(onNotANumber = { TdeeFormError.InvalidNumber }),
            validator = {
                when {
                    it == null -> null
                    it in BodyProfile.MIN_BODY_FAT..BodyProfile.MAX_BODY_FAT -> null
                    else -> TdeeFormError.OutOfRange
                }
            },
            textFieldState =
                rememberTextFieldState(profile.bodyFatPercentage?.formatClipZeros() ?: ""),
        )

    val intakeInUserUnit = plan.dailyIntakeKcal?.let(energyFormatter::fromKcal)
    val dailyIntake =
        rememberFormField(
            initialValue = intakeInUserUnit,
            parser = nullableDoubleParser(onNotANumber = { TdeeFormError.InvalidNumber }),
            validator = {
                when {
                    it == null -> null
                    it < 0.0 -> TdeeFormError.OutOfRange
                    else -> null
                }
            },
            textFieldState = rememberTextFieldState(intakeInUserUnit?.formatClipZeros() ?: ""),
        )

    val targetWeight =
        rememberFormField(
            initialValue = plan.targetWeightKg,
            parser = nullableDoubleParser(onNotANumber = { TdeeFormError.InvalidNumber }),
            validator = {
                when {
                    it == null -> null
                    it in BodyProfile.MIN_WEIGHT_KG..BodyProfile.MAX_WEIGHT_KG -> null
                    else -> TdeeFormError.OutOfRange
                }
            },
            textFieldState = rememberTextFieldState(plan.targetWeightKg?.formatClipZeros() ?: ""),
        )

    val energyDensity =
        rememberFormField(
            initialValue = plan.energyDensityKcalPerKg,
            parser =
                doubleParser(
                    onNotANumber = { TdeeFormError.InvalidNumber },
                    onBlank = { TdeeFormError.Required },
                ),
            validator = {
                if (it in MIN_ENERGY_DENSITY..MAX_ENERGY_DENSITY) {
                    null
                } else {
                    TdeeFormError.OutOfRange
                }
            },
            textFieldState =
                rememberTextFieldState(plan.energyDensityKcalPerKg.formatClipZeros("%.0f")),
        )

    return remember(
        plan,
        sexState,
        activityLevelState,
        formulaState,
        age,
        height,
        weight,
        bodyFat,
        dailyIntake,
        targetWeight,
        energyDensity,
        energyFormatter,
    ) {
        TdeeFormState(
            sexState = sexState,
            activityLevelState = activityLevelState,
            formulaState = formulaState,
            age = age,
            height = height,
            weight = weight,
            bodyFat = bodyFat,
            dailyIntake = dailyIntake,
            targetWeight = targetWeight,
            energyDensity = energyDensity,
            toKcal = energyFormatter::toKcal,
        )
    }
}

@Stable
internal class TdeeFormState(
    sexState: MutableState<BiologicalSex>,
    activityLevelState: MutableState<ActivityLevel>,
    formulaState: MutableState<BmrFormula>,
    val age: FormField<Int, TdeeFormError>,
    val height: FormField<Double, TdeeFormError>,
    val weight: FormField<Double, TdeeFormError>,
    val bodyFat: FormField<Double?, TdeeFormError>,
    val dailyIntake: FormField<Double?, TdeeFormError>,
    val targetWeight: FormField<Double?, TdeeFormError>,
    val energyDensity: FormField<Double, TdeeFormError>,
    private val toKcal: (Double) -> Double,
) {
    var sex by sexState
    var activityLevel by activityLevelState
    var formula by formulaState

    /** Katch-McArdle cannot be evaluated without a body fat percentage. */
    val missingBodyFat by derivedStateOf { formula.requiresBodyFat && bodyFat.value == null }

    val isValid by derivedStateOf {
        !missingBodyFat &&
            age.error == null &&
            height.error == null &&
            weight.error == null &&
            bodyFat.error == null &&
            dailyIntake.error == null &&
            targetWeight.error == null &&
            energyDensity.error == null
    }

    /**
     * Builds a plan from the current inputs, or null when they do not make one. The validators
     * already cover the entity requirements, so null here means the form is simply incomplete.
     */
    fun toPlan(): TdeePlan? =
        runCatching {
                TdeePlan(
                    profile =
                        BodyProfile(
                            sex = sex,
                            ageYears = age.value,
                            heightCm = height.value,
                            weightKg = weight.value,
                            bodyFatPercentage = bodyFat.value,
                            activityLevel = activityLevel,
                            formula = formula,
                        ),
                    dailyIntakeKcal = dailyIntake.value?.let(toKcal),
                    targetWeightKg = targetWeight.value,
                    energyDensityKcalPerKg = energyDensity.value,
                )
            }
            .getOrNull()
}

private const val MIN_ENERGY_DENSITY = 1000.0
private const val MAX_ENERGY_DENSITY = 20000.0
