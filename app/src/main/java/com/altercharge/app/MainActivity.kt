package com.altercharge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.altercharge.app.data.home.HomeCardsLoader
import com.altercharge.app.data.home.VehicleReading
import com.altercharge.app.data.local.KeyValueStore
import com.altercharge.app.data.local.OfflineSnapshotStore
import com.altercharge.app.data.local.SharedPreferencesStore
import com.altercharge.app.data.remote.AlterChargeApi
import com.altercharge.app.ui.common.ChargePowerSlider
import com.altercharge.app.ui.common.dropdown.SortDropDown
import com.altercharge.app.ui.common.dropdown.StationSortType
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

//@Preview(showBackground = true)
//@Composable
//private fun HomeScreenPreview() {
//    MaterialTheme { HomeScreen() }
//}
