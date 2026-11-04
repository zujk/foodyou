package com.maksimowiczm.foodyou.tdee.domain.repository

import com.maksimowiczm.foodyou.tdee.domain.entity.TdeePlan
import kotlinx.coroutines.flow.Flow

interface TdeeRepository {

    /** Emits the stored plan, or [TdeePlan.default] when the user has not saved one yet. */
    fun observePlan(): Flow<TdeePlan>

    /** Whether the user has ever saved a plan. Drives the empty state on the screen. */
    fun observeHasPlan(): Flow<Boolean>

    suspend fun updatePlan(plan: TdeePlan)
}
