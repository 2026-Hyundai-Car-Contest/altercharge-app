package com.altercharge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.altercharge.app.data.home.HomeCardsLoader
import com.altercharge.app.data.local.OfflineSnapshotStore
import com.altercharge.app.data.local.SharedPreferencesStore
import com.altercharge.app.data.remote.AlterChargeApi
import com.altercharge.app.ui.home.HomeScreen
import com.altercharge.app.ui.home.HomeViewModel
import com.altercharge.app.ui.home.HomeViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loader = HomeCardsLoader(
            api = AlterChargeApi(),
            store = OfflineSnapshotStore(
                store = SharedPreferencesStore(
                    context = applicationContext
                )
            )
        )

        setContent {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(loader)
            )
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
