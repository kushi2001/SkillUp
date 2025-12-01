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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.text.font.FontWeight
import com.example.skillup.ui.theme.SkillUPTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkillUPTheme {
                MainScreen()
            }
        }
    }
}

// ------------ Data models ------------

data class Course(
    val title: String,
    val level: String,
    val duration: String
)

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val points: Int,
    val streak: Int,
    val level: String
)

data class Achievement(
    val title: String,
    val description: String
)

// ------------ Bottom nav ------------

sealed class BottomNavItem(val title: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem("Dashboard", Icons.Filled.Home)
    object Leaderboard : BottomNavItem("Leaderboard", Icons.Filled.Star)
    object Profile : BottomNavItem("Profile", Icons.Filled.Person)
}

@Composable
fun MainScreen() {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Leaderboard,
        BottomNavItem.Profile
    )

    var selectedIndex by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (selectedIndex) {
                0 -> HomeScreen()
                1 -> LeaderboardScreen()
                2 -> ProfileScreen()
            }
        }
    }
}

// ------------ Screen 1: Dashboard ------------

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
                        text = "Hi, $userName 👋",
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

// ------------ Screen 2: Leaderboard ------------

@Composable
fun LeaderboardScreen() {
    val topLearners = listOf(
        LeaderboardEntry(1, "Aarav", 4520, 15, "Advanced"),
        LeaderboardEntry(2, "Saanvi", 4310, 12, "Advanced"),
        LeaderboardEntry(3, "Rahul", 3980, 10, "Intermediate"),
        LeaderboardEntry(4, "Meera", 3550, 8, "Intermediate"),
        LeaderboardEntry(5, "Dev", 3200, 7, "Intermediate")
    )

    val yourStats = LeaderboardEntry(12, "You", 2150, 5, "Beginner")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Leaderboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // Your position card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Your Rank", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "#${yourStats.rank} • ${yourStats.points} pts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🔥 Streak: ${yourStats.streak} days", fontSize = 12.sp)
                }
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = Color(0xFFFFA000),
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Text(
            text = "Top Learners",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(topLearners) { entry ->
                LeaderboardRow(entry)
            }
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {
    val rankColor = when (entry.rank) {
        1 -> Color(0xFFFFD54F)
        2 -> Color(0xFFB0BEC5)
        3 -> Color(0xFFFFCC80)
        else -> Color(0xFFF5F5F5)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(rankColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${entry.rank}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = entry.level,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = {
                        entry.points.coerceAtMost(5000) / 5000f
                    },
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = Color(0xFFF5F5F5),
                    color = Color(0xFF42A5F5)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${entry.points} pts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🔥 ${entry.streak}d",
                    fontSize = 12.sp,
                    color = Color(0xFFD32F2F)
                )
            }
        }
    }
}

// ------------ Screen 3: Profile ------------

@Composable
fun ProfileScreen() {
    val achievements = listOf(
        Achievement("First Course Completed", "You finished your first SkillUp course"),
        Achievement("3-Day Streak", "You’ve learned 3 days in a row"),
        Achievement("Goal Setter", "You set your first weekly goal")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Profile header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFFFA000), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "L",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Learner Name", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("learner@email.com", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Intermediate Learner",
                            fontSize = 12.sp,
                            color = Color(0xFFEF6C00)
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Profile",
                        tint = Color(0xFF5D4037),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileStatCard(
                    title = "Points",
                    value = "2,150",
                    icon = Icons.Filled.Bolt,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    title = "Courses",
                    value = "6",
                    icon = Icons.Filled.School,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    title = "Streak",
                    value = "5 days",
                    icon = Icons.Filled.Whatshot,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Achievements
        item {
            Text(
                text = "Recent Achievements",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(achievements) { ach ->
            AchievementRow(ach)
        }

        // Settings section
        item {
            Text(
                text = "Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            SettingsRow(
                icon = Icons.Filled.Notifications,
                title = "Notifications",
                subtitle = "Reminders, streak alerts"
            )
        }
        item {
            SettingsRow(
                icon = Icons.Filled.Security,
                title = "Privacy & Security",
                subtitle = "Account, data usage"
            )
        }
        item {
            SettingsRow(
                icon = Icons.Filled.Settings,
                title = "App Settings",
                subtitle = "Theme, language"
            )
        }
        item {
            SettingsRow(
                icon = Icons.Filled.Help,
                title = "Help & Support",
                subtitle = "FAQ, contact us"
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun ProfileStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        modifier = modifier.height(90.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(20.dp),
                tint = Color(0xFF616161)
            )
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(title, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AchievementRow(achievement: Achievement) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.WorkspacePremium,
                contentDescription = "Achievement",
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(achievement.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(achievement.description, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun SettingsRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: open settings item */ }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = Color(0xFF616161)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
        Icon(
            imageVector = Icons.Filled.KeyboardArrowRight,
            contentDescription = "Go",
            tint = Color(0xFFBDBDBD)
        )
    }
}

// ------------ Reusable UI ------------

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
fun MainPreview() {
    SkillUPTheme {
        MainScreen()
    }
}
