package com.realsalmon.studentfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.realsalmon.studentfit.ui.navigation.StudentFitNavHost
import com.realsalmon.studentfit.ui.theme.StudentFitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudentFitTheme {
                StudentFitNavHost()
            }
        }
    }
}
