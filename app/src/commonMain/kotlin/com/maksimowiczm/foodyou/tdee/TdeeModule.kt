package com.maksimowiczm.foodyou.tdee

import com.maksimowiczm.foodyou.tdee.infrastructure.tdeeInfrastructureModule
import org.koin.dsl.module

val tdeeModule = module { tdeeInfrastructureModule() }
