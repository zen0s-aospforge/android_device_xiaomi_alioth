package org.lineageos.xiaomiparts.ui.charge

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
import androidx.lifecycle.viewmodel.compose.viewModel
import org.lineageos.xiaomiparts.R
import org.lineageos.xiaomiparts.ui.components.SettingsSwitchItem
import org.lineageos.xiaomiparts.ui.components.BaseSettingsScreen
import org.lineageos.xiaomiparts.theme.GroupedListDefaults
import androidx.compose.ui.res.stringResource
import org.lineageos.xiaomiparts.ui.components.ExpandableInfoButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargeScreen(
    onBackPressed: () -> Unit,
    viewModel: ChargeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showBypassWarningDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissBypassDialog,
            title = { Text(stringResource(R.string.charge_bypass_title)) },
            text = { Text(stringResource(R.string.charge_bypass_warning)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmBypassCharge) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissBypassDialog) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
    BaseSettingsScreen(
        title = stringResource(R.string.charge_bypass_title),
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
                    title = R.string.charge_bypass_title,
                    summary = if (uiState.isBypassSupported)
                                  R.string.charge_bypass_summary
                              else
                                  R.string.charge_bypass_unavailable,
                    checked = uiState.bypassChargeEnabled,
                    onCheckedChange = viewModel::setBypassCharge,
                    modifier = Modifier.clip(shape),
                    icon = null,
                    enabled = uiState.isBypassSupported
                )
            }
            item {
                ExpandableInfoButton(
                    infoContent = stringResource(R.string.charge_bypass_info),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}