package com.altercharge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.altercharge.app.data.home.DEMO_LOCATION
import com.altercharge.app.data.home.HomeCardsLoader
import com.altercharge.app.data.local.OfflineSnapshotStore
import com.altercharge.app.data.local.SharedPreferencesStore
import com.altercharge.app.data.local.StationCacheStore
import com.altercharge.app.data.local.VisitCountStore
import com.altercharge.app.data.remote.AlterChargeApi
import com.altercharge.app.data.station.StationRepository
import com.altercharge.app.data.upload.ChargingEventRecorder
import com.altercharge.app.data.vehicle.MockVehicleTelemetrySource
import com.altercharge.app.ui.home.HomeScreen
import com.altercharge.app.ui.home.HomeViewModel
import com.altercharge.app.ui.home.HomeViewModelFactory
import com.altercharge.app.ui.station.StationListViewModel
import com.altercharge.app.ui.station.StationListViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 의존성은 한 곳에서 만들어 넘긴다 (Gradle에 DI 라이브러리를 넣지 않는다)
        val store = SharedPreferencesStore(context = applicationContext)
        val api = AlterChargeApi()
        val visits = VisitCountStore(store)
        val stations = StationRepository(
            api = api,
            cache = StationCacheStore(store),
            visits = visits,
        )
        val loader = HomeCardsLoader(
            api = api,
            store = OfflineSnapshotStore(store = store),
        )
        val recorder = ChargingEventRecorder(
            api = api,
            store = store,
            visits = visits,
        )
        // 충전 상태 · SOC는 아직 차량 SDK 대신 목업이다 (REQ_VS_002 · REQ_VS_003)
        val telemetry = MockVehicleTelemetrySource()
        val driverName = getString(R.string.common_driver_name)

        setContent {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(
                    loader = loader,
                    telemetry = telemetry,
                    recorder = recorder,
                    visits = visits,
                    stations = stations,
                    location = DEMO_LOCATION,
                    driverName = driverName,
                )
            )
            val stationViewModel: StationListViewModel = viewModel(
                factory = StationListViewModelFactory(
                    repository = stations,
                    lat = DEMO_LOCATION.lat,
                    lng = DEMO_LOCATION.lng,
                )
            )

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        stationViewModel = stationViewModel,
                    )
                }
            }
        }
    }
}
