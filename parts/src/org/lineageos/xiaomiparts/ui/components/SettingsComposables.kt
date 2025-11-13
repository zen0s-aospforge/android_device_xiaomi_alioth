package org.lineageos.xiaomiparts.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.lineageos.xiaomiparts.theme.CustomColors
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Slider
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SwitchDefaults
import org.lineageos.xiaomiparts.theme.GroupedListDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSwitchItem(
    @StringRes title: Int,
    @StringRes summary: Int? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true
) {
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = if (summary != null) {
            {
                Text(
                    text = stringResource(summary),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else null,
        leadingContent = {
            if (icon != null) {
                Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = stringResource(title)
                    )
                }
            }
        },
        trailingContent = {
            Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                Switch(
                    checked = checked,
                    enabled = enabled,
                    onCheckedChange = onCheckedChange,
                    colors = CustomColors.switchColors,
                    thumbContent = if (checked) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        }
                    } else null
                )
            }
        },
        modifier = modifier.clickable(
            enabled = enabled,
            onClick = { onCheckedChange(!checked) }
        ),
        colors = GroupedListDefaults.listColors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSliderItem(
    @StringRes title: Int,
    @StringRes summary: Int? = null,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
    valueText: String? = null
) {
    ListItem(
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(title),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (valueText != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Text(
                            text = valueText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        supportingContent = {
            Column(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                if (summary != null) {
                    Text(
                        text = stringResource(summary),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = valueRange,
                    steps = steps,
                    enabled = enabled,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        },
        leadingContent = {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = stringResource(title)
                )
            }
        },
        modifier = modifier,
        colors = GroupedListDefaults.listColors()
    )
}