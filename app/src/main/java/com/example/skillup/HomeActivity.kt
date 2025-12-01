package com.example.skillup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding   // <-- add this import
import com.example.skillup.ui.theme.SkillUPTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkillUPTheme {
                // Add status bar padding here so everything is pushed below system bar
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    HomeScreen()
                }
            }
        }
    }
}

data class Course(
    val title: String,
    val level: String,
    val duration: String
)

@Composable
fun HomeScreen(userName: String = "Learner") {

    val searchText = remember { mutableStateOf("") }

    val continueCourses = listOf(
        Course("Kotlin Basics", "Beginner", "12 mins left"),
        Course("UI Design Fundamentals", "Intermediate", "25 mins left")
    )

    val categories = listOf("Programming", "Design", "Marketing", "Business", "AI & Data", "Languages")

    val popularCourses = listOf(
        Course("Android Jetpack Compose", "Intermediate", "4h 20m"),
        Course("Intro to Machine Learning", "Beginner", "3h 05m"),
        Course("UX for Mobile Apps", "Beginner", "2h 15m"),
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // App title
        item {
            Text(
                text = "SkillUp",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Gradient header
        item {
            val headerGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFD54F),
                    Color(0xFFFFB300)
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerGradient, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Hi, $userName ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ready to continue your learning journey?",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White,
                        modifier = Modifier.clickable {
                            // TODO: navigate to "Continue" course
                        }
                    ) {
                        Text(
                            text = "Continue Learning",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color(0xFFFFA000),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Search bar
        item {
            OutlinedTextField(
                value = searchText.value,
                onValueChange = { searchText.value = it },
                label = { Text("Search courses") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(50.dp)
            )
        }

        // Continue learning section
        item {
            Text(
                text = "Continue Learning",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(continueCourses) { course ->
            CourseProgressCard(course)
        }

        // Categories row
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Categories",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    CategoryChip(cat)
                }
            }
        }

        // Popular courses
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Popular Courses",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(popularCourses) { course ->
            PopularCourseCard(course)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CourseProgressCard(course: Course) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = course.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFA000)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = course.level,
                fontSize = 13.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• ${course.duration}",
                fontSize = 13.sp,
                color = Color(0xFF8D6E63)
            )
        }
    }
}

@Composable
fun CategoryChip(name: String) {
    Surface(
        shape = CircleShape,
        color = Color(0xFFFFECB3),
        tonalElevation = 2.dp,
        modifier = Modifier.clickable {
            // TODO: filter by category
        }
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFBF360C)
        )
    }
}

@Composable
fun PopularCourseCard(course: Course) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = course.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = course.level,
                fontSize = 13.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = course.duration,
                fontSize = 13.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    SkillUPTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            HomeScreen()
        }
    }
}
