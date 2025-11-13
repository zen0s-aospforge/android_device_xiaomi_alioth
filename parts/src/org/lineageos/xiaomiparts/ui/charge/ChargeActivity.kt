package org.lineageos.xiaomiparts.ui.charge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import org.lineageos.xiaomiparts.data.ChargeUtils
import org.lineageos.xiaomiparts.theme.XiaomiPartsTheme
import org.lineageos.xiaomiparts.ui.charge.ChargeScreen

class ChargeActivity : ComponentActivity() {

    private lateinit var chargeUtils: ChargeUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        chargeUtils = ChargeUtils.getInstance(this)

        setContent {
            XiaomiPartsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    val viewModel: ChargeViewModel = viewModel(
                        factory = ChargeViewModelFactory(chargeUtils)
                    )
                    ChargeScreen(
                        viewModel = viewModel,
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }
}