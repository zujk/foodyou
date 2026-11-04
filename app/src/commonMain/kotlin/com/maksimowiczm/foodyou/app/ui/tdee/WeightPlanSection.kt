package com.maksimowiczm.foodyou.app.ui.tdee

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import foodyou.app.generated.resources.*
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

/** The inputs that drive the projection: what the user eats, where they want to get to, and how
 * much energy a kilogram is assumed to be worth. */
@Composable
internal fun WeightPlanSection(
    state: TdeeFormState,
    goalEnergyKcal: Double,
    modifier: Modifier = Modifier,
) {
    val energyFormatter = LocalEnergyFormatter.current

    TdeeSection(title = stringResource(Res.string.headline_weight_projection), modifier = modifier) {
        state.dailyIntake.NumberField(
            label = stringResource(Res.string.headline_daily_intake),
            modifier = Modifier.fillMaxWidth(),
            suffix = energyFormatter.suffix(),
            supportingText = stringResource(Res.string.description_daily_intake_from_goal),
        )

        if (state.dailyIntake.value == null) {
            Text(
                text =
                    stringResource(
                        Res.string.x_using_daily_energy_goal,
                        energyFormatter.formatEnergy(goalEnergyKcal.roundToInt()),
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        state.targetWeight.NumberField(
            label = stringResource(Res.string.headline_target_weight),
            modifier = Modifier.fillMaxWidth(),
            suffix = stringResource(Res.string.unit_kilogram_short),
            supportingText = stringResource(Res.string.description_target_weight_optional),
        )

        state.energyDensity.NumberField(
            label = stringResource(Res.string.headline_energy_per_kilogram),
            modifier = Modifier.fillMaxWidth(),
            suffix = stringResource(Res.string.unit_kcal),
            supportingText = stringResource(Res.string.description_energy_per_kilogram),
            imeAction = ImeAction.Done,
        )
    }
}
