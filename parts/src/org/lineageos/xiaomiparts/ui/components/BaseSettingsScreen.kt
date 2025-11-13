package org.lineageos.xiaomiparts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.lineageos.xiaomiparts.R
import org.lineageos.xiaomiparts.theme.AppFonts
import org.lineageos.xiaomiparts.theme.CustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseSettingsScreen(
    title: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = title,
                        fontFamily = AppFonts.robotoFlexTopBar,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier.padding(start = 10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(35.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.abc_action_bar_up_description),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                actions = actions,
                colors = CustomColors.topBarColors,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}