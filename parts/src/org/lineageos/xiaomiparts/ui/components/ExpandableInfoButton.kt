@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package org.lineageos.xiaomiparts.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ExpandableInfoButton(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Info,
    infoContent: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        // The shape-changing button
        FilledTonalIconToggleButton(
            checked = expanded,
            onCheckedChange = { expanded = it },
            shapes = IconButtonDefaults.toggleableShapes(),
            modifier = Modifier.width(52.dp).align(Alignment.End)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Info"
            )
        }

        // The animated content
        AnimatedVisibility(expanded) {
            Text(
                text = infoContent,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}