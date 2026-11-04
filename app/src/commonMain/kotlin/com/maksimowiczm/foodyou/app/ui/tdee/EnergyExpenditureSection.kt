package com.maksimowiczm.foodyou.app.ui.tdee

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.tdee.domain.entity.TdeeSummary
import foodyou.app.generated.resources.*
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

/**
 * Shows the basal rate and the total expenditure, and offers to copy the expenditure straight into
 * the daily energy goal, which is what most people want right after working it out.
 */
@Composable
internal fun EnergyExpenditureSection(
    summary: TdeeSummary?,
    onApplyEnergyToDailyGoals: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val energyFormatter = LocalEnergyFormatter.current

    TdeeSection(
        title = stringResource(Res.string.headline_energy_expenditure),
        modifier = modifier,
    ) {
        if (summary == null) {
            Text(
                text = stringResource(Res.string.description_tdee_incomplete_form),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@TdeeSection
        }

        val tdeeKcal = summary.expenditure.totalDailyEnergyExpenditureKcal

        TdeeStatRow(
            label = stringResource(Res.string.headline_basal_metabolic_rate),
            value =
                energyFormatter.formatEnergy(
                    summary.expenditure.basalMetabolicRateKcal.roundToInt()
                ),
            description = stringResource(Res.string.description_basal_metabolic_rate),
        )

        HorizontalDivider()

        TdeeStatRow(
            label = stringResource(Res.string.headline_total_daily_energy_expenditure),
            value = energyFormatter.formatEnergy(tdeeKcal.roundToInt()),
            description = stringResource(Res.string.description_total_daily_energy_expenditure),
            emphasize = true,
        )

        Button(
            onClick = { onApplyEnergyToDailyGoals(tdeeKcal) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.action_use_as_daily_energy_goal))
        }
    }
}
