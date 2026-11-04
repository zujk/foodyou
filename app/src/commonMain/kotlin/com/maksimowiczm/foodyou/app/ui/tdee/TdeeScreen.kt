package com.maksimowiczm.foodyou.app.ui.tdee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import com.maksimowiczm.foodyou.common.compose.extension.add
import com.maksimowiczm.foodyou.tdee.domain.entity.GoalOutcome
import com.maksimowiczm.foodyou.tdee.domain.entity.TdeePlan
import com.maksimowiczm.foodyou.tdee.domain.entity.TdeeSummary
import com.maksimowiczm.foodyou.tdee.domain.usecase.SummarizeTdeePlan
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TdeeScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: TdeeViewModel = koinViewModel()
    val state = viewModel.uiState.collectAsStateWithLifecycle().value ?: return

    TdeeScreen(
        state = state,
        events = viewModel.events,
        onBack = onBack,
        onSave = viewModel::savePlan,
        onApplyEnergyToDailyGoals = viewModel::applyEnergyToDailyGoals,
        modifier = modifier,
    )
}

@Composable
private fun TdeeScreen(
    state: TdeeUiState,
    events: Flow<TdeeEvent>,
    onBack: () -> Unit,
    onSave: (TdeePlan) -> Unit,
    onApplyEnergyToDailyGoals: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formState = rememberTdeeFormState(state.plan)
    val snackbarHostState = remember { SnackbarHostState() }

    val planSavedMessage = stringResource(Res.string.positive_tdee_plan_saved)
    val goalsUpdatedMessage = stringResource(Res.string.positive_daily_energy_goal_updated)

    LaunchedCollectWithLifecycle(events) { event ->
        val message =
            when (event) {
                TdeeEvent.PlanSaved -> planSavedMessage
                TdeeEvent.DailyGoalsUpdated -> goalsUpdatedMessage
            }

        snackbarHostState.showSnackbar(message)
    }

    // Recomputed on every keystroke so the figures track what the user is typing, saved or not.
    val summary: TdeeSummary? by
        remember(formState, state.goalEnergyKcal) {
            derivedStateOf {
                formState.toPlan()?.let { SummarizeTdeePlan(it, state.goalEnergyKcal) }
            }
        }

    val currentSummary = summary
    val targetDate: LocalDate? =
        remember(currentSummary, state.today) {
            (currentSummary?.projection?.goal as? GoalOutcome.Reachable)?.let {
                state.today.plus(it.days, DateTimeUnit.DAY)
            }
        }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_tdee)) },
                navigationIcon = { ArrowBackIconButton(onBack) },
                actions = {
                    FilledIconButton(
                        onClick = { formState.toPlan()?.let(onSave) },
                        shapes = IconButtonDefaults.shapes(),
                        enabled = formState.isValid,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = stringResource(Res.string.action_save),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .imePadding()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues.add(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!state.hasSavedPlan) {
                // Nothing stored yet, so the form is showing placeholder numbers. Say so, or the
                // figures below look like they are about the person reading them.
                item {
                    Text(
                        text = stringResource(Res.string.neutral_estimate_energy_needs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }

            item { BodyProfileSection(state = formState, modifier = Modifier.fillMaxWidth()) }

            item {
                EnergyExpenditureSection(
                    summary = currentSummary,
                    onApplyEnergyToDailyGoals = onApplyEnergyToDailyGoals,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                WeightPlanSection(
                    state = formState,
                    goalEnergyKcal = state.goalEnergyKcal,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                WeightProjectionSection(
                    summary = currentSummary,
                    targetDate = targetDate,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Text(
                    text = stringResource(Res.string.description_estimate_accuracy),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}
