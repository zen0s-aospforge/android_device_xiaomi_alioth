package org.lineageos.xiaomiparts.ui.dim

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.lineageos.xiaomiparts.theme.XiaomiPartsShapeDefaults
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import org.lineageos.xiaomiparts.R
import org.lineageos.xiaomiparts.ui.components.SettingsSwitchItem
import org.lineageos.xiaomiparts.ui.components.BaseSettingsScreen
import org.lineageos.xiaomiparts.theme.GroupedListDefaults
import androidx.compose.ui.res.stringResource
import org.lineageos.xiaomiparts.ui.components.ExpandableInfoButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DcDimmingScreen(
    onBackPressed: () -> Unit,
    viewModel: DcDimmingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    BaseSettingsScreen(
        title = stringResource(R.string.dc_dimming_title),
        onBackPressed = onBackPressed
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(GroupedListDefaults.VerticalSpacing)
        ) {
            item {
                Spacer(Modifier.height(12.dp))
            }
            item {
                val shape = GroupedListDefaults.getShape(0, 1)
                
                SettingsSwitchItem(
                    title = R.string.dc_dimming_title,
                    summary = if (uiState.isDcDimmingSupported)
                                  R.string.dc_dimming_summary
                              else
                                  R.string.dc_dimming_summary_not_supported,
                    checked = uiState.isDcDimmingEnabled,
                    onCheckedChange = viewModel::setDcDimmingEnabled,
                    modifier = Modifier.clip(shape),
                    icon = null,
                    enabled = uiState.isDcDimmingSupported
                )
            }

            item {
                ExpandableInfoButton(
                    infoContent = stringResource(R.string.dc_dimming_info),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}