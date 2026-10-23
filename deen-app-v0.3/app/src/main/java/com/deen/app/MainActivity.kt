package com.deen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.deen.app.data.AppState
import com.deen.app.prayer.Reminders
import com.deen.app.ui.DeenApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Reminders.scheduleNext(this)
        setContent {
            val state = remember { AppState(applicationContext) }
            DeenApp(state)
        }
    }
}
