package com.example.tagenglish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tagenglish.ui.navigation.AppNavGraph
import com.example.tagenglish.ui.theme.TagEnglishTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TagEnglishTheme {
                AppNavGraph()
            }
        }
    }
}