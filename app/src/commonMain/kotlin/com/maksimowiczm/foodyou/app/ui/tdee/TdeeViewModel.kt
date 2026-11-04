package com.maksimowiczm.foodyou.app.ui.tdee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.NutritionFactsField
import com.maksimowiczm.foodyou.goals.domain.entity.DailyGoal
import com.maksimowiczm.foodyou.goals.domain.entity.MacronutrientGoal
import com.maksimowiczm.foodyou.goals.domain.entity.WeeklyGoals
import com.maksimowiczm.foodyou.goals.domain.repository.GoalsRepository
import com.maksimowiczm.foodyou.tdee.domain.entity.TdeePlan
import com.maksimowiczm.foodyou.tdee.domain.repository.TdeeRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

internal class TdeeViewModel(
    private val tdeeRepository: TdeeRepository,
    private val goalsRepository: GoalsRepository,
    dateProvider: DateProvider,
) : ViewModel() {

    private val _events = Channel<TdeeEvent>()
    val events = _events.receiveAsFlow()

    val uiState =
        combine(
                tdeeRepository.observePlan(),
                tdeeRepository.observeHasPlan(),
                dateProvider.observeDate(),
            ) { plan, hasPlan, today ->
                Triple(plan, hasPlan, today)
            }
            .flatMapLatest { (plan, hasPlan, today) ->
                // The projection follows what the user is actually aiming to eat, so an unset
                // intake falls back to the daily energy goal configured for today.
                goalsRepository.observeDailyGoals(today).map { dailyGoal ->
                    TdeeUiState(
                        plan = plan,
                        hasSavedPlan = hasPlan,
                        goalEnergyKcal = dailyGoal[NutritionFactsField.Energy],
                        today = today,
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    fun savePlan(plan: TdeePlan) {
        viewModelScope.launch {
            tdeeRepository.updatePlan(plan)
            _events.send(TdeeEvent.PlanSaved)
        }
    }

    /**
     * Writes [energyKcal] into every day of the weekly goals.
     *
     * Goals defined as a distribution recompute their macronutrient grams from the new energy on
     * their own. Goals defined in grams do not, so the grams are scaled by the same factor to keep
     * them consistent with the new energy figure.
     */
    fun applyEnergyToDailyGoals(energyKcal: Double) {
        viewModelScope.launch {
            val current = goalsRepository.observeWeeklyGoals().first()

            goalsRepository.updateWeeklyGoals(current.withEnergy(energyKcal))
            _events.send(TdeeEvent.DailyGoalsUpdated)
        }
    }
}

internal data class TdeeUiState(
    /** The stored plan, which seeds the form. */
    val plan: TdeePlan,
    /** False until the user saves for the first time, which drives the explanatory empty state. */
    val hasSavedPlan: Boolean,
    /** The daily energy goal configured in the app, in kcal. */
    val goalEnergyKcal: Double,
    val today: LocalDate,
)

internal sealed interface TdeeEvent {
    data object PlanSaved : TdeeEvent

    data object DailyGoalsUpdated : TdeeEvent
}

private fun WeeklyGoals.withEnergy(energyKcal: Double): WeeklyGoals =
    copy(
        monday = monday.withEnergy(energyKcal),
        tuesday = tuesday.withEnergy(energyKcal),
        wednesday = wednesday.withEnergy(energyKcal),
        thursday = thursday.withEnergy(energyKcal),
        friday = friday.withEnergy(energyKcal),
        saturday = saturday.withEnergy(energyKcal),
        sunday = sunday.withEnergy(energyKcal),
    )

private fun DailyGoal.withEnergy(energyKcal: Double): DailyGoal {
    val updated =
        when (val goal = macronutrientGoal) {
            is MacronutrientGoal.Distribution -> goal.copy(energyKcal = energyKcal)

            is MacronutrientGoal.Manual -> {
                val factor = if (goal.energyKcal > 0.0) energyKcal / goal.energyKcal else 1.0

                goal.copy(
                    energyKcal = energyKcal,
                    proteinsGrams = goal.proteinsGrams * factor,
                    fatsGrams = goal.fatsGrams * factor,
                    carbohydratesGrams = goal.carbohydratesGrams * factor,
                )
            }
        }

    return copy(macronutrientGoal = updated, map = map + (NutritionFactsField.Energy to energyKcal))
}
