package org.lineageos.xiaomiparts.ui.revanced

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import org.lineageos.xiaomiparts.theme.XiaomiPartsShapeDefaults
import androidx.compose.ui.unit.dp
import org.lineageos.xiaomiparts.R
import org.lineageos.xiaomiparts.ui.components.BaseSettingsScreen
import org.lineageos.xiaomiparts.ui.components.ExpandableInfoButton
import org.lineageos.xiaomiparts.ui.components.SettingsSwitchItem
import org.lineageos.xiaomiparts.theme.GroupedListDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReVancedScreen(
    onBackPressed: () -> Unit,
    viewModel: ReVancedViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current

    BaseSettingsScreen(
        title = stringResource(R.string.revanced_title),
        onBackPressed = onBackPressed
    ) { paddingValues ->
        toastMessage?.let { (msg, restart) ->
            LaunchedEffect(msg, restart) {
                val toastMsg = if (restart)
                    context.getString(R.string.revanced_restart_required)
                else
                    context.getString(R.string.revanced_toggle_failed)
                Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
                
                viewModel.toastMessageShown()
            }
        }

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
                    title = R.string.revanced_enable_title,
                    checked = uiState.isEnabled,
                    onCheckedChange = viewModel::setEnabled,
                    modifier = Modifier.clip(shape),
                    icon = R.drawable.youtube_activity_24px,
                    enabled = true
                )
            }
            item {
                ExpandableInfoButton(
                    infoContent = stringResource(R.string.revanced_info),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } 
}