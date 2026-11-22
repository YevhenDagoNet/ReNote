package com.example.renote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.renote.navigation.ReNoteNavGraph
import com.example.renote.ui.theme.ReNoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReNoteTheme {
                ReNoteNavGraph(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
