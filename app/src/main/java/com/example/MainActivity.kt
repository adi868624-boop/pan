package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.RetailerMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PanViewModel
import com.example.ui.viewmodel.UserSession

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        PanMitraApp()
      }
    }
  }
}

@Composable
fun PanMitraApp(viewModel: PanViewModel = viewModel()) {
  val session by viewModel.userSession.collectAsStateWithLifecycle()

  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    when (val currentSession = session) {
      is UserSession.None -> {
        AuthScreen(
          viewModel = viewModel,
          modifier = Modifier.padding(innerPadding)
        )
      }
      is UserSession.RetailerSession -> {
        RetailerMainScreen(
          viewModel = viewModel,
          retailer = currentSession.retailer,
          modifier = Modifier.padding(innerPadding)
        )
      }
      is UserSession.AdminSession -> {
        AdminDashboardScreen(
          viewModel = viewModel,
          modifier = Modifier.padding(innerPadding)
        )
      }
    }
  }
}

