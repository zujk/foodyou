package com.maksimowiczm.foodyou.app.ui.tdee

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.maksimowiczm.foodyou.common.compose.utility.formatClipZeros
import com.maksimowiczm.foodyou.tdee.domain.entity.ActivityLevel
import com.maksimowiczm.foodyou.tdee.domain.entity.BiologicalSex
import com.maksimowiczm.foodyou.tdee.domain.entity.BmrFormula
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BodyProfileSection(state: TdeeFormState, modifier: Modifier = Modifier) {
    TdeeSection(title = stringResource(Res.string.headline_body_profile), modifier = modifier) {
        Text(
            text = stringResource(Res.string.headline_sex),
            style = MaterialTheme.typography.labelLarge,
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            BiologicalSex.entries.forEachIndexed { index, sex ->
                SegmentedButton(
                    selected = state.sex == sex,
                    onClick = { state.sex = sex },
                    shape =
                        SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = BiologicalSex.entries.size,
                        ),
                ) {
                    Text(sex.label())
                }
            }
        }

        state.age.NumberField(
            label = stringResource(Res.string.headline_age),
            modifier = Modifier.fillMaxWidth(),
            suffix = stringResource(Res.string.unit_years_short),
            keyboardType = KeyboardType.Number,
        )

        state.height.NumberField(
            label = stringResource(Res.string.headline_height),
            modifier = Modifier.fillMaxWidth(),
            suffix = stringResource(Res.string.unit_centimeter_short),
        )

        state.weight.NumberField(
            label = stringResource(Res.string.headline_body_weight),
            modifier = Modifier.fillMaxWidth(),
            suffix = stringResource(Res.string.unit_kilogram_short),
        )

        state.bodyFat.NumberField(
            label = stringResource(Res.string.headline_body_fat),
            modifier = Modifier.fillMaxWidth(),
            suffix = "%",
            supportingText =
                if (state.formula.requiresBodyFat) {
                    stringResource(Res.string.description_body_fat_required)
                } else {
                    stringResource(Res.string.description_body_fat_optional)
                },
            imeAction = ImeAction.Done,
        )

        if (state.missingBodyFat) {
            Text(
                text = stringResource(Res.string.description_body_fat_required),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        TdeeStatRow(
            label = stringResource(Res.string.headline_body_mass_index),
            value = state.bodyMassIndexText(),
        )

        HorizontalDivider()

        Text(
            text = stringResource(Res.string.headline_activity_level),
            style = MaterialTheme.typography.labelLarge,
        )

        ActivityLevel.entries.forEach { level ->
            TdeeOptionRow(
                selected = state.activityLevel == level,
                onClick = { state.activityLevel = level },
                label = level.label(),
                description = level.description(),
            )
        }

        HorizontalDivider()

        Text(
            text = stringResource(Res.string.headline_bmr_formula),
            style = MaterialTheme.typography.labelLarge,
        )

        BmrFormula.entries.forEach { formula ->
            TdeeOptionRow(
                selected = state.formula == formula,
                onClick = { state.formula = formula },
                label = formula.label(),
                description = formula.description(),
            )
        }
    }
}

@Composable
private fun TdeeFormState.bodyMassIndexText(): String {
    val heightMetres = height.value / 100.0

    if (heightMetres <= 0.0) {
        return stringResource(Res.string.em_dash)
    }

    return (weight.value / (heightMetres * heightMetres)).formatClipZeros("%.1f")
}

@Composable
internal fun BiologicalSex.label(): String =
    when (this) {
        BiologicalSex.Male -> stringResource(Res.string.neutral_male)
        BiologicalSex.Female -> stringResource(Res.string.neutral_female)
    }

@Composable
internal fun ActivityLevel.label(): String =
    when (this) {
        ActivityLevel.Sedentary -> stringResource(Res.string.neutral_activity_sedentary)
        ActivityLevel.LightlyActive -> stringResource(Res.string.neutral_activity_lightly_active)
        ActivityLevel.ModeratelyActive ->
            stringResource(Res.string.neutral_activity_moderately_active)

        ActivityLevel.VeryActive -> stringResource(Res.string.neutral_activity_very_active)
        ActivityLevel.ExtraActive -> stringResource(Res.string.neutral_activity_extra_active)
    }

@Composable
internal fun ActivityLevel.description(): String =
    when (this) {
        ActivityLevel.Sedentary -> stringResource(Res.string.description_activity_sedentary)
        ActivityLevel.LightlyActive ->
            stringResource(Res.string.description_activity_lightly_active)

        ActivityLevel.ModeratelyActive ->
            stringResource(Res.string.description_activity_moderately_active)

        ActivityLevel.VeryActive -> stringResource(Res.string.description_activity_very_active)
        ActivityLevel.ExtraActive -> stringResource(Res.string.description_activity_extra_active)
    }

@Composable
internal fun BmrFormula.label(): String =
    when (this) {
        BmrFormula.MifflinStJeor -> stringResource(Res.string.neutral_formula_mifflin_st_jeor)
        BmrFormula.HarrisBenedict -> stringResource(Res.string.neutral_formula_harris_benedict)
        BmrFormula.KatchMcArdle -> stringResource(Res.string.neutral_formula_katch_mcardle)
    }

@Composable
internal fun BmrFormula.description(): String =
    when (this) {
        BmrFormula.MifflinStJeor -> stringResource(Res.string.description_formula_mifflin_st_jeor)
        BmrFormula.HarrisBenedict ->
            stringResource(Res.string.description_formula_harris_benedict)

        BmrFormula.KatchMcArdle -> stringResource(Res.string.description_formula_katch_mcardle)
    }
