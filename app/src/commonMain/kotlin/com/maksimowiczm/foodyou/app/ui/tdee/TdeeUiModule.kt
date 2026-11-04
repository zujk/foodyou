package com.maksimowiczm.foodyou.app.ui.tdee

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf

fun Module.tdee() {
    viewModelOf(::TdeeViewModel)
}
