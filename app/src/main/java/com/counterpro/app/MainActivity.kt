package com.counterpro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.*
import androidx.room.Room
import com.counterpro.app.data.db.CountDatabase
import com.counterpro.app.data.repository.CountRepository
import com.counterpro.app.ui.counter.CounterScreen
import com.counterpro.app.ui.counter.CounterViewModel
import com.counterpro.app.ui.history.HistoryScreen
import com.counterpro.app.ui.history.HistoryViewModel

class MainActivity : ComponentActivity() {
    private val db by lazy {
        Room.databaseBuilder(applicationContext, CountDatabase::class.java, "counter_db").build()
    }
    private val repo by lazy { CountRepository(db) }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val currentRoute by navController.currentBackStackEntryAsState()
                val route = currentRoute?.destination?.route ?: "counter"

                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text(if (route == "counter") "Counter Pro" else "Histórico") })
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = route == "counter",
                                onClick = { navController.navigate("counter") { launchSingleTop = true } },
                                icon = { Icon(Icons.Default.Add, null) },
                                label = { Text("Contador") }
                            )
                            NavigationBarItem(
                                selected = route == "history",
                                onClick = { navController.navigate("history") { launchSingleTop = true } },
                                icon = { Icon(Icons.Default.History, null) },
                                label = { Text("Histórico") }
                            )
                        }
                    }
                ) { padding ->
                    NavHost(navController, startDestination = "counter", modifier = Modifier.padding(padding)) {
                        composable("counter") {
                            CounterScreen(viewModel(factory = object : ViewModelProvider.Factory {
                                override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T {
                                    @Suppress("UNCHECKED_CAST")
                                    return CounterViewModel(repo) as T
                                }
                            }))
                        }
                        composable("history") {
                            HistoryScreen(viewModel(factory = object : ViewModelProvider.Factory {
                                override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T {
                                    @Suppress("UNCHECKED_CAST")
                                    return HistoryViewModel(repo) as T
                                }
                            }))
                        }
                    }
                }
            }
        }
    }
}
