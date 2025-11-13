package org.lineageos.xiaomiparts.ui.dim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import org.lineageos.xiaomiparts.data.DcDimmingUtils
import org.lineageos.xiaomiparts.theme.XiaomiPartsTheme

class DcDimmingActivity : ComponentActivity() {

    private lateinit var dcDimmingUtils: DcDimmingUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        dcDimmingUtils = DcDimmingUtils.getInstance(this)

        setContent {
            XiaomiPartsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    val viewModel: DcDimmingViewModel = viewModel(
                        factory = DcDimmingViewModelFactory(
                            app = application,
                            dcDimmingUtils = dcDimmingUtils
                        )
                    )
                    DcDimmingScreen(
                        viewModel = viewModel,
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }
}