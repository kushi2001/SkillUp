package com.example.skillup

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.skillup.ui.theme.SkillUPTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomeActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkillUPTheme {

                val userEmail = auth.currentUser?.email ?: "learner@email.com"
                val userName = auth.currentUser?.displayName ?: "Learner"

                MainScreen(
                    userName = userName,
                    userEmail = userEmail,
                    onLogout = {
                        auth.signOut()
                        startActivity(Intent(this, SignInActivity::class.java))
                        finish()
                    },
                    onOpenCourse = { course ->
                        val i = Intent(this, CourseDetailActivity::class.java).apply {
                            putExtra("title", course.title)
                            putExtra("category", course.category)
                            putExtra("level", course.level)
                            putExtra("duration", course.duration)
                            putExtra("description", "This is a detailed overview of ${course.title}.")
                        }
                        startActivity(i)
                    }
                )
            }
        }
    }
}

// ------------ Data models ------------

data class Course(
    val title: String,
    val level: String,
    val duration: String,
    val category: String = "General"
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
fun MainScreen(
    userName: String,
    userEmail: String,
    onLogout: () -> Unit,
    onOpenCourse: (Course) -> Unit
) {
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
                0 -> HomeScreen(userName = userName, onOpenCourse = onOpenCourse)
                1 -> LeaderboardScreen()
                2 -> ProfileScreen(
                    userName = userName,
                    userEmail = userEmail,
                    onLogout = onLogout
                )
            }
        }
    }
}

// ------------ Screen 1: Dashboard ------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String = "Learner",
    onOpenCourse: (Course) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val quickStats = listOf(
        Triple("Points", "2,150", Icons.Filled.Bolt),
        Triple("Streak", "5 days", Icons.Filled.Whatshot),
        Triple("Courses", "6", Icons.Filled.School)
    )

    val continueCourses = remember {
        listOf(
            Course("Kotlin Basics", "Beginner", "12 mins left", "Programming"),
            Course("UI Design Fundamentals", "Intermediate", "25 mins left", "Design")
        )
    }

    val categories = listOf("All", "Programming", "Design", "Marketing", "Business", "AI & Data", "Languages")

    val popularCourses = remember {
        listOf(
            Course("Android Jetpack Compose", "Intermediate", "4h 20m", "Programming"),
            Course("Intro to Machine Learning", "Beginner", "3h 05m", "AI & Data"),
            Course("UX for Mobile Apps", "Beginner", "2h 15m", "Design"),
        )
    }

    // ✅ My Plan: functional list
    val myPlan = remember { mutableStateListOf<Course>() }

    val allCourses = remember(continueCourses, popularCourses) { continueCourses + popularCourses }

    fun matches(course: Course): Boolean {
        val s = searchText.trim()
        val catOk = selectedCategory == "All" || course.category == selectedCategory
        val searchOk = s.isEmpty() ||
                course.title.contains(s, ignoreCase = true) ||
                course.category.contains(s, ignoreCase = true)
        return catOk && searchOk
    }

    val filteredContinue by remember(searchText, selectedCategory) {
        derivedStateOf { continueCourses.filter { matches(it) } }
    }

    val filteredPopular by remember(searchText, selectedCategory) {
        derivedStateOf { popularCourses.filter { matches(it) } }
    }

    // ✅ Suggestions under search bar (this makes “typing” feel functional)
    val suggestions by remember(searchText) {
        derivedStateOf {
            val s = searchText.trim()
            if (s.isEmpty()) emptyList()
            else allCourses
                .distinctBy { it.title }
                .filter { it.title.contains(s, ignoreCase = true) || it.category.contains(s, ignoreCase = true) }
                .take(5)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Title + notification icon
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SkillUp", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF616161),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Notifications clicked (demo)")
                                }
                            }
                    )
                }
            }

            // Gradient header
            item {
                val headerGradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFD54F), Color(0xFFFFB300))
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
                            text = "Let’s hit today’s learning goal!",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Daily Goal: 20 mins", color = Color.White, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { 0.65f },
                            modifier = Modifier.fillMaxWidth(),
                            trackColor = Color.White.copy(alpha = 0.25f),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Continue Learning clicked")
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Continue",
                                    tint = Color(0xFFFFA000),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Continue Learning",
                                    color = Color(0xFFFFA000),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Quick stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    quickStats.forEach { (title, value, icon) ->
                        DashboardStatCard(
                            title = title,
                            value = value,
                            icon = icon,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    scope.launch { snackbarHostState.showSnackbar("$title clicked") }
                                }
                        )
                    }
                }
            }

            // Search bar (✅ now has suggestions below)
            item {
                Column {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Search courses") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(50.dp),
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { searchText = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        }
                    )

                    AnimatedVisibility(visible = suggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text("Suggestions", fontSize = 12.sp, color = Color.Gray)
                                Spacer(Modifier.height(6.dp))

                                suggestions.forEach { course ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Opening: ${course.title}")
                                                }
                                                onOpenCourse(course)
                                            }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF616161))
                                        Spacer(Modifier.width(10.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(course.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            Text(course.category, fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Categories row (✅ snackbar confirms filter applied)
            item {
                SectionHeader("Categories", "Explore by topic")
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        CategoryChip(
                            name = cat,
                            selected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = cat
                                scope.launch { snackbarHostState.showSnackbar("Filtered: $cat") }
                            }
                        )
                    }
                }
            }

            // ✅ If nothing matches, show a “No results” card (so it doesn’t look broken)
            item {
                val noResults = filteredContinue.isEmpty() && filteredPopular.isEmpty() && searchText.isNotBlank()
                if (noResults) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFFEF6C00))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("No results found", fontWeight = FontWeight.SemiBold)
                                Text("Try a different keyword or category.", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // My Plan (✅ add/remove shows snackbar)
            if (myPlan.isNotEmpty()) {
                item { SectionHeader("My Plan", "Courses you saved") }

                items(myPlan, key = { it.title }) { course ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Bookmark, contentDescription = "Saved", tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(course.title, fontWeight = FontWeight.SemiBold)
                                Text("${course.category} • ${course.level}", fontSize = 12.sp, color = Color.Gray)
                            }
                            IconButton(onClick = {
                                myPlan.remove(course)
                                scope.launch { snackbarHostState.showSnackbar("Removed from plan") }
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = Color(0xFF616161))
                            }
                        }
                    }
                }
            }

            // Continue learning section
            item { SectionHeader("Continue Learning", "Pick up where you left off") }

            items(filteredContinue, key = { it.title }) { course ->
                ContinueCourseCard(
                    course = course,
                    onPlay = {
                        scope.launch { snackbarHostState.showSnackbar("Playing: ${course.title}") }
                        onOpenCourse(course)
                    },
                    onCardClick = {
                        scope.launch { snackbarHostState.showSnackbar("Opened: ${course.title}") }
                        onOpenCourse(course)
                    }
                )
            }

            // Popular courses
            item { SectionHeader("Popular Courses", "Most loved by learners") }

            items(filteredPopular, key = { it.title }) { course ->
                PopularCourseCardEnhanced(
                    course = course,
                    onAddToPlan = {
                        if (myPlan.none { it.title == course.title }) {
                            myPlan.add(course)
                            scope.launch { snackbarHostState.showSnackbar("Added to plan") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Already in plan") }
                        }
                    },
                    onStart = {
                        scope.launch { snackbarHostState.showSnackbar("Starting: ${course.title}") }
                        onOpenCourse(course)
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(subtitle, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun DashboardStatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        modifier = modifier.height(90.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = title, tint = Color(0xFF616161), modifier = Modifier.size(20.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(title, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ContinueCourseCard(
    course: Course,
    onPlay: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFFFA000), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.MenuBook, contentDescription = "Course", tint = Color.White)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(course.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF6D4C41))
                Spacer(modifier = Modifier.height(2.dp))
                Text("${course.level} • ${course.duration}", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                Text(course.category, fontSize = 12.sp, color = Color(0xFFEF6C00))
            }

            IconButton(onClick = onPlay) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color(0xFFFFA000))
            }
        }
    }
}

@Composable
fun PopularCourseCardEnhanced(
    course: Course,
    onAddToPlan: () -> Unit,
    onStart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = "Popular", tint = Color(0xFFFFA000))
                Spacer(modifier = Modifier.width(6.dp))
                Text(course.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("${course.level} • ${course.duration}", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Category: ${course.category}", fontSize = 12.sp, color = Color(0xFF616161))

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                AssistChip(
                    onClick = onAddToPlan,
                    label = { Text("Add to Plan") },
                    leadingIcon = { Icon(Icons.Filled.Add, contentDescription = "Add") }
                )
                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(50)
                ) { Text("Start") }
            }
        }
    }
}

// ------------ Screen 2: Leaderboard ------------

@Composable
fun LeaderboardScreen() {
    var selectedFilter by remember { mutableStateOf("Weekly") }
    val filters = listOf("Weekly", "Monthly", "All-time")

    val topLearners = listOf(
        LeaderboardEntry(1, "Aarav", 4520, 15, "Advanced"),
        LeaderboardEntry(2, "Saanvi", 4310, 12, "Advanced"),
        LeaderboardEntry(3, "Rahul", 3980, 10, "Intermediate"),
        LeaderboardEntry(4, "Meera", 3550, 8, "Intermediate"),
        LeaderboardEntry(5, "Dev", 3200, 7, "Intermediate")
    )

    val yourStats = LeaderboardEntry(12, "You", 2150, 5, "Beginner")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text("Leaderboard", fontSize = 24.sp, fontWeight = FontWeight.Bold) }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { f ->
                    FilterChip(
                        selected = selectedFilter == f,
                        onClick = { selectedFilter = f },
                        label = { Text(f) }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PodiumCard(entry = topLearners[0], modifier = Modifier.weight(1f))
                PodiumCard(entry = topLearners[1], modifier = Modifier.weight(1f))
                PodiumCard(entry = topLearners[2], modifier = Modifier.weight(1f))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.PersonPin, contentDescription = "You", tint = Color(0xFF1565C0))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Your Rank", fontWeight = FontWeight.SemiBold)
                        Text("#${yourStats.rank} • ${yourStats.points} pts • 🔥 ${yourStats.streak}d", fontSize = 12.sp)
                    }
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Go", tint = Color(0xFF1565C0))
                }
            }
        }

        items(topLearners) { entry -> LeaderboardRow(entry) }
        item { Spacer(modifier = Modifier.height(10.dp)) }
    }
}

@Composable
fun PodiumCard(entry: LeaderboardEntry, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        modifier = modifier.height(110.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("#${entry.rank}", fontWeight = FontWeight.Bold, color = Color(0xFFEF6C00))
            Text(entry.name, fontWeight = FontWeight.SemiBold)
            Text("${entry.points} pts", fontSize = 12.sp, color = Color.Gray)
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
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(rankColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("#${entry.rank}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(entry.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(entry.level, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { entry.points.coerceAtMost(5000) / 5000f },
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = Color(0xFFF5F5F5)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text("${entry.points} pts", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("🔥 ${entry.streak}d", fontSize = 12.sp, color = Color(0xFFD32F2F))
            }
        }
    }
}

// ------------ Screen 3: Profile ------------

@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    onLogout: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }

    val achievements = listOf(
        Achievement("First Course Completed", "You finished your first SkillUp course"),
        Achievement("3-Day Streak", "You’ve learned 3 days in a row"),
        Achievement("Goal Setter", "You set your first weekly goal")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text("Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold) }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFFFA000), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            userName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(userName, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Text(userEmail, fontSize = 12.sp, color = Color.Gray)
                        Text("Intermediate Learner", fontSize = 12.sp, color = Color(0xFFEF6C00))
                    }

                    FilledTonalIconButton(onClick = { }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardStatCard("Points", "2,150", Icons.Filled.Bolt, Modifier.weight(1f))
                DashboardStatCard("Courses", "6", Icons.Filled.School, Modifier.weight(1f))
                DashboardStatCard("Streak", "5 days", Icons.Filled.Whatshot, Modifier.weight(1f))
            }
        }

        item {
            Text("Achievements", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        items(achievements) { ach -> AchievementRow(ach) }

        item {
            Text("Preferences", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        item {
            ToggleRow(
                icon = Icons.Filled.Notifications,
                title = "Notifications",
                subtitle = "Daily reminders and streak alerts",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }

        item {
            ToggleRow(
                icon = Icons.Filled.DarkMode,
                title = "Dark Mode",
                subtitle = "Switch app theme",
                checked = darkMode,
                onCheckedChange = { darkMode = it }
            )
        }

        item {
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }
    }
}

@Composable
fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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
            Icon(icon, contentDescription = title, tint = Color(0xFF616161))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

// ------------ Common UI ------------

@Composable
fun CategoryChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFFFFA000) else Color(0xFFFFECB3)
    val textColor = if (selected) Color.White else Color(0xFFBF360C)

    Surface(
        shape = CircleShape,
        color = bg,
        tonalElevation = 2.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
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
                Text(achievement.description, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    SkillUPTheme {
        MainScreen(
            userName = "Learner",
            userEmail = "learner@email.com",
            onLogout = {},
            onOpenCourse = {}
        )
    }
}
