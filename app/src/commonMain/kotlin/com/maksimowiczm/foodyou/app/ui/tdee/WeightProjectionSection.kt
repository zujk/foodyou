package com.maksimowiczm.foodyou.app.ui.tdee

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.common.compose.utility.LocalDateFormatter
import com.maksimowiczm.foodyou.common.compose.utility.formatClipZeros
import com.maksimowiczm.foodyou.tdee.domain.entity.GoalOutcome
import com.maksimowiczm.foodyou.tdee.domain.entity.TdeeSummary
import com.maksimowiczm.foodyou.tdee.domain.entity.WeightTrend
import foodyou.app.generated.resources.*
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The projection itself: where the current energy balance leads, and when the target is met.
 *
 * @param targetDate The calendar day the target is expected to be reached, when it is reachable.
 */
@Composable
internal fun WeightProjectionSection(
    summary: TdeeSummary?,
    targetDate: LocalDate?,
    modifier: Modifier = Modifier,
) {
    val energyFormatter = LocalEnergyFormatter.current
    val dateFormatter = LocalDateFormatter.current

    TdeeSection(title = stringResource(Res.string.headline_projected_weight), modifier = modifier) {
        if (summary == null) {
            Text(
                text = stringResource(Res.string.description_tdee_incomplete_form),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@TdeeSection
        }

        val projection = summary.projection

        TdeeStatRow(
            label = stringResource(Res.string.headline_daily_energy_balance),
            value =
                when (projection.trend) {
                    WeightTrend.Maintenance -> stringResource(Res.string.neutral_weight_maintenance)

                    else ->
                        buildString {
                            append(
                                if (projection.dailyBalanceKcal < 0.0) {
                                    stringResource(Res.string.neutral_deficit)
                                } else {
                                    stringResource(Res.string.neutral_surplus)
                                }
                            )
                            append(" ")
                            append(
                                energyFormatter.formatEnergy(
                                    abs(projection.dailyBalanceKcal).roundToInt()
                                )
                            )
                        }
                },
            emphasize = true,
        )

        if (projection.trend == WeightTrend.Maintenance) {
            Text(
                text = stringResource(Res.string.description_weight_maintenance),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            TdeeStatRow(
                label = stringResource(Res.string.headline_rate_of_change),
                value =
                    stringResource(
                        Res.string.x_per_week,
                        signedKilograms(projection.weightChangeKgPerWeek),
                    ),
                description =
                    stringResource(
                        Res.string.x_per_month,
                        signedKilograms(projection.weightChangeKgPerMonth),
                    ),
            )

            HorizontalDivider()

            projection.milestones.forEach { milestone ->
                TdeeStatRow(
                    label = horizonLabel(milestone.days),
                    value = kilograms(milestone.projectedWeightKg),
                    description = signedKilograms(milestone.changeKg),
                )
            }
        }

        val goal = projection.goal
        if (goal != null) {
            HorizontalDivider()

            TdeeStatRow(
                label = stringResource(Res.string.headline_target),
                value = kilograms(goal.targetWeightKg),
            )

            when (goal) {
                is GoalOutcome.Reachable ->
                    TdeeStatRow(
                        label =
                            stringResource(
                                Res.string.x_target_reached_in,
                                pluralStringResource(
                                    Res.plurals.neutral_x_days,
                                    goal.days,
                                    goal.days,
                                ),
                            ),
                        value =
                            targetDate?.let {
                                stringResource(
                                    Res.string.x_target_reached_on,
                                    dateFormatter.formatDateShort(it),
                                )
                            } ?: "",
                        emphasize = true,
                    )

                is GoalOutcome.AlreadyReached ->
                    Text(
                        text = stringResource(Res.string.positive_target_weight_reached),
                        style = MaterialTheme.typography.bodyMedium,
                    )

                is GoalOutcome.Unreachable ->
                    Text(
                        text =
                            when (goal.reason) {
                                GoalOutcome.Unreachable.Reason.WrongDirection ->
                                    stringResource(Res.string.negative_target_wrong_direction)

                                GoalOutcome.Unreachable.Reason.Maintenance ->
                                    stringResource(Res.string.negative_target_at_maintenance)

                                GoalOutcome.Unreachable.Reason.BeyondHorizon ->
                                    stringResource(Res.string.negative_target_beyond_horizon)
                            },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
            }
        }

        HorizontalDivider()

        Text(
            text =
                stringResource(
                    Res.string.description_projection_is_linear,
                    projection.energyDensityKcalPerKg.formatClipZeros("%.0f"),
                ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** "1 week", "3 months" and so on, derived from a milestone in days. */
@Composable
private fun horizonLabel(days: Int): String =
    if (days < 30) {
        pluralStringResource(Res.plurals.neutral_x_weeks, days / 7, days / 7)
    } else {
        pluralStringResource(Res.plurals.neutral_x_months, days / 30, days / 30)
    }

@Composable
private fun kilograms(value: Double): String = buildString {
    append(value.formatClipZeros(ONE_DECIMAL))
    append(" ")
    append(stringResource(Res.string.unit_kilogram_short))
}

/** Always carries a sign, so a projection reads as a change rather than as a weight. */
@Composable
private fun signedKilograms(value: Double): String = buildString {
    if (value > 0.0) {
        append("+")
    }

    append(value.formatClipZeros(ONE_DECIMAL))
    append(" ")
    append(stringResource(Res.string.unit_kilogram_short))
}

private const val ONE_DECIMAL = "%.1f"
