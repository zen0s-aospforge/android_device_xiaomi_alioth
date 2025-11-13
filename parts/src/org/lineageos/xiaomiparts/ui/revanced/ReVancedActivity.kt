package org.lineageos.xiaomiparts.ui.revanced

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import org.lineageos.xiaomiparts.data.ReVancedManager
import org.lineageos.xiaomiparts.theme.XiaomiPartsTheme

class ReVancedActivity : ComponentActivity() {

    private lateinit var reVancedManager: ReVancedManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        reVancedManager = ReVancedManager.getInstance(this)

        setContent {
            XiaomiPartsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    val viewModel: ReVancedViewModel = viewModel(
                        factory = ReVancedViewModelFactory(reVancedManager)
                    )
                    ReVancedScreen(
                        viewModel = viewModel,
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }
}