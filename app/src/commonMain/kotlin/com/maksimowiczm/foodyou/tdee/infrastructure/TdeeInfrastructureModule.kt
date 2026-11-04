package com.maksimowiczm.foodyou.tdee.infrastructure

import com.maksimowiczm.foodyou.tdee.domain.repository.TdeeRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind

internal fun Module.tdeeInfrastructureModule() {
    factoryOf(::DataStoreTdeeRepository).bind<TdeeRepository>()
}
