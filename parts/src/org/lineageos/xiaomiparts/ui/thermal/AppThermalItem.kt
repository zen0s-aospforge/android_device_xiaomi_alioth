@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package org.lineageos.xiaomiparts.ui.thermal

import android.graphics.drawable.Drawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.style.TextOverflow
import android.widget.ImageView
import org.lineageos.xiaomiparts.ui.thermal.AppThermalState
import org.lineageos.xiaomiparts.theme.CustomColors
import org.lineageos.xiaomiparts.theme.GroupedListDefaults

@Composable
fun AppThermalItem(
    app: AppThermalState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            AppIcon(
                drawable = app.icon,
                contentDescription = app.label,
                modifier = Modifier.size(36.dp)
            )
        },
        trailingContent = {
            ToggleButton(
                checked = true,
                onCheckedChange = { onClick() },
                enabled = true,
                modifier = Modifier.defaultMinSize(minWidth = 110.dp),
                colors = ToggleButtonDefaults.toggleButtonColors(
                    checkedContainerColor = CustomColors.selectedListItemColors.containerColor,
                    checkedContentColor = CustomColors.selectedListItemColors.leadingIconColor
                )
            ) {
                Text(
                    text = stringResource(app.currentState.label),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        modifier = modifier.clickable(onClick = onClick),
        colors = GroupedListDefaults.listColors()
    )
}


@Composable
fun AppIcon(
    drawable: Drawable,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageDrawable(drawable)
                this.contentDescription = contentDescription
            }
        },
        modifier = modifier
    )
}