package com.example.renote

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.renote.navigation.ReNoteNavGraph
import com.example.renote.ui.theme.ReNoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // The app is light-only (spec §7), so the system bars keep dark icons even when the
        // device is in dark mode; the default auto style would turn them light on cream.
        val lightBars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)
        super.onCreate(savedInstanceState)
        setContent {
            ReNoteTheme {
                ReNoteNavGraph(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
