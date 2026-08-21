package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.BrieflyApp
import com.example.ui.theme.BrieflyTheme
import com.example.ui.viewmodel.BrieflyViewModel
import com.example.ui.viewmodel.BrieflyViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: BrieflyViewModel by viewModels {
        BrieflyViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val themePalette by viewModel.themePalette.collectAsStateWithLifecycle()
            val isDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            BrieflyTheme(
                darkTheme = isDarkTheme,
                paletteId = themePalette
            ) {
                BrieflyApp(viewModel = viewModel)
            }
        }
    }
}

