package org.lineageos.xiaomiparts.ui.hbm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import org.lineageos.xiaomiparts.data.DcDimmingUtils
import org.lineageos.xiaomiparts.data.HBMManager
import org.lineageos.xiaomiparts.theme.XiaomiPartsTheme

class HBMActivity : ComponentActivity() {
    private lateinit var hbmManager: HBMManager
    private lateinit var dcDimmingUtils: DcDimmingUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hbmManager = HBMManager.getInstance(this)
        dcDimmingUtils = DcDimmingUtils.getInstance(this)

        setContent {
            XiaomiPartsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    val viewModel: HbmViewModel = viewModel(
                        factory = HbmViewModelFactory(
                            app = application, 
                            hbmManager = hbmManager,
                            dcDimmingUtils = dcDimmingUtils
                        )
                    )
                    HbmScreen(onBackPressed = { finish() }, viewModel = viewModel)
                }
            }
        }
    }
}