package com.sanad.anklerehab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanad.anklerehab.ui.MainViewModel
import com.sanad.anklerehab.ui.MainViewModelFactory
import com.sanad.anklerehab.ui.RehabApp
import com.sanad.anklerehab.ui.theme.AnkleRehabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as RehabApplication).container
        setContent {
            AnkleRehabTheme {
                val vm: MainViewModel = viewModel(factory = MainViewModelFactory(container))
                RehabApp(vm)
            }
        }
    }
}
