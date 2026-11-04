package com.maksimowiczm.foodyou.app.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TdeeSettingsListItem(
    onClick: () -> Unit,
    shape: Shape,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    SettingsListItem(
        icon = { Icon(imageVector = Icons.Outlined.MonitorWeight, contentDescription = null) },
        label = { Text(stringResource(Res.string.headline_tdee)) },
        supportingContent = { Text(stringResource(Res.string.neutral_estimate_energy_needs)) },
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
    )
}
