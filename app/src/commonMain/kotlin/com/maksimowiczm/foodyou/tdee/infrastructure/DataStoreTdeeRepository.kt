package com.maksimowiczm.foodyou.tdee.infrastructure

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.maksimowiczm.foodyou.tdee.domain.entity.ActivityLevel
import com.maksimowiczm.foodyou.tdee.domain.entity.BiologicalSex
import com.maksimowiczm.foodyou.tdee.domain.entity.BmrFormula
import com.maksimowiczm.foodyou.tdee.domain.entity.BodyProfile
import com.maksimowiczm.foodyou.tdee.domain.entity.DEFAULT_ENERGY_DENSITY_KCAL_PER_KG
import com.maksimowiczm.foodyou.tdee.domain.entity.TdeePlan
import com.maksimowiczm.foodyou.tdee.domain.repository.TdeeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class DataStoreTdeeRepository(private val dataStore: DataStore<Preferences>) :
    TdeeRepository {

    override fun observePlan(): Flow<TdeePlan> = dataStore.data.map { it.readPlan() }

    override fun observeHasPlan(): Flow<Boolean> =
        dataStore.data.map { it[TdeeDataStoreKeys.plan] != null }

    override suspend fun updatePlan(plan: TdeePlan) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                set(TdeeDataStoreKeys.plan, Json.encodeToString(DataStoreTdeePlan(plan)))
            }
        }
    }

    private fun Preferences.readPlan(): TdeePlan {
        val serialized = this[TdeeDataStoreKeys.plan] ?: return TdeePlan.default

        // A plan written by a newer version, or hand-edited preferences, must not take the screen
        // down. Fall back to the default and let the user save over it.
        return runCatching { Json.decodeFromString<DataStoreTdeePlan>(serialized).toTdeePlan() }
            .getOrDefault(TdeePlan.default)
    }
}

private object TdeeDataStoreKeys {
    val plan = stringPreferencesKey("tdee:plan")
}

@Serializable
private class DataStoreTdeePlan(
    val sex: BiologicalSex,
    val ageYears: Int,
    val heightCm: Double,
    val weightKg: Double,
    val bodyFatPercentage: Double? = null,
    val activityLevel: ActivityLevel,
    val formula: BmrFormula,
    val dailyIntakeKcal: Double? = null,
    val targetWeightKg: Double? = null,
    val energyDensityKcalPerKg: Double = DEFAULT_ENERGY_DENSITY_KCAL_PER_KG,
) {
    constructor(
        plan: TdeePlan
    ) : this(
        sex = plan.profile.sex,
        ageYears = plan.profile.ageYears,
        heightCm = plan.profile.heightCm,
        weightKg = plan.profile.weightKg,
        bodyFatPercentage = plan.profile.bodyFatPercentage,
        activityLevel = plan.profile.activityLevel,
        formula = plan.profile.formula,
        dailyIntakeKcal = plan.dailyIntakeKcal,
        targetWeightKg = plan.targetWeightKg,
        energyDensityKcalPerKg = plan.energyDensityKcalPerKg,
    )

    fun toTdeePlan(): TdeePlan =
        TdeePlan(
            profile =
                BodyProfile(
                    sex = sex,
                    ageYears = ageYears,
                    heightCm = heightCm,
                    weightKg = weightKg,
                    bodyFatPercentage = bodyFatPercentage,
                    activityLevel = activityLevel,
                    formula = formula,
                ),
            dailyIntakeKcal = dailyIntakeKcal,
            targetWeightKg = targetWeightKg,
            energyDensityKcalPerKg = energyDensityKcalPerKg,
        )
}
