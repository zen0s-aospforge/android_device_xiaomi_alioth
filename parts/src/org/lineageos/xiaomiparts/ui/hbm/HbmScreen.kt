package org.lineageos.xiaomiparts.ui.hbm

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.viewmodel.compose.viewModel
import org.lineageos.xiaomiparts.R
import org.lineageos.xiaomiparts.ui.components.SettingsSliderItem
import org.lineageos.xiaomiparts.ui.components.SettingsSwitchItem
import org.lineageos.xiaomiparts.data.HBMManager
import org.lineageos.xiaomiparts.ui.components.BaseSettingsScreen
import org.lineageos.xiaomiparts.theme.GroupedListDefaults
import androidx.compose.ui.res.stringResource
import org.lineageos.xiaomiparts.ui.components.ExpandableInfoButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HbmScreen(
    onBackPressed: () -> Unit,
    viewModel: HbmViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val thresholdValueRange = HBMManager.MIN_THRESHOLD.toFloat()..HBMManager.MAX_THRESHOLD.toFloat()
    val thresholdSteps = (HBMManager.MAX_THRESHOLD - HBMManager.MIN_THRESHOLD) / HBMManager.STEP_THRESHOLD - 1
    
    val timeValueRange = HBMManager.MIN_TIME_DELAY.toFloat()..HBMManager.MAX_TIME_DELAY.toFloat()
    val timeSteps = (HBMManager.MAX_TIME_DELAY - HBMManager.MIN_TIME_DELAY) / HBMManager.STEP_TIME_DELAY - 1

    BaseSettingsScreen(
        title = stringResource(R.string.hbm_title),
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
                SettingsSwitchItem(
                    title = R.string.hbm_mode_title,
                    summary = R.string.hbm_mode_summary,
                    checked = uiState.isHbmEnabled,
                    onCheckedChange = { viewModel.setHbm(it) },
                    modifier = Modifier.clip(GroupedListDefaults.getShape(0, 4)),
                    icon = null,
                    enabled = !uiState.isDcDimmingEnabled
                )
            }

            item {
                SettingsSwitchItem(
                    title = R.string.auto_hbm_title,
                    summary = R.string.auto_hbm_summary,
                    checked = uiState.isAutoHbmEnabled,
                    onCheckedChange = { viewModel.setAutoHbm(it) },
                    modifier = Modifier.clip(GroupedListDefaults.getShape(1, 4)),
                    icon = null,
                    enabled = !uiState.isDcDimmingEnabled
                )
            }
            
            item {
                SettingsSliderItem(
                    title = R.string.auto_hbm_threshold_title,
                    // Pass the formatted text here (e.g., "2000 lux")
                    valueText = "${uiState.thresholdValue.toInt()} lux", 
                    value = uiState.thresholdValue,
                    valueRange = thresholdValueRange,
                    steps = thresholdSteps,
                    onValueChange = { viewModel.setThreshold(it) },
                    modifier = Modifier.clip(GroupedListDefaults.getShape(2, 4)),
                    icon = null,
                    enabled = uiState.isAutoHbmSliderEnabled && !uiState.isDcDimmingEnabled
                )
            }

            item {
                SettingsSliderItem(
                    title = R.string.auto_hbm_time_delay_title,
                    // Pass the formatted text here (e.g., "3 s")
                    valueText = "${uiState.hbmDisableTime} s",
                    value = uiState.hbmDisableTime.toFloat(),
                    valueRange = timeValueRange,
                    steps = timeSteps,
                    onValueChange = { viewModel.setDisableTime(it) },
                    modifier = Modifier.clip(GroupedListDefaults.getShape(3, 4)),
                    icon = null,
                    enabled = uiState.isAutoHbmSliderEnabled && !uiState.isDcDimmingEnabled
                )
            }

            item {
                ExpandableInfoButton(
                    infoContent = stringResource(R.string.hbm_info),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}