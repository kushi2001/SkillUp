package com.example.skillup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class CourseDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra("title") ?: ""
        val category = intent.getStringExtra("category") ?: ""
        val level = intent.getStringExtra("level") ?: ""
        val duration = intent.getStringExtra("duration") ?: ""
        val description = intent.getStringExtra("description") ?: ""

        setContent {
            MaterialTheme {
                CourseDetailScreen(title, category, level, duration, description)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    title: String,
    category: String,
    level: String,
    duration: String,
    description: String
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Course Details") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text("$category • $level • $duration", style = MaterialTheme.typography.bodyMedium)
            Divider()
            Text(description, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
