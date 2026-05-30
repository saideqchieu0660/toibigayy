package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.FlashcardEntity
import com.example.data.database.StudyAnalyticEntity
import com.example.data.repository.MindMapEntity
import com.example.ui.MainViewModel
import com.example.ui.StudyGroupScreen
import com.example.ui.formatScienceText
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            MyApplicationTheme(darkTheme = mainViewModel.isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppMainLayout(viewModel = mainViewModel)
                }
            }
        }
    }
}

@Composable
fun AppMainLayout(viewModel: MainViewModel) {
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (viewModel.isDarkTheme) {
                        listOf(Color(0xFF1A1510), Color(0xFF0F0C08)) // Deep rich golden dark
                    } else {
                        listOf(Color(0xFFFBF8F1), Color(0xFFEBE3D5)) // Light creamy golden marble
                    }
                )
            )
    ) {
        when (viewModel.authScreen) {
            "login" -> LoginScreen(viewModel = viewModel)
            "signup" -> SignUpScreen(viewModel = viewModel)
            "main" -> {
                if (isWideScreen) {
                    LaptopLayout(viewModel = viewModel)
                } else {
                    PhoneLayout(viewModel = viewModel)
                }
            }
        }
    }
}

// ==========================================
// 1. TRANG ĐĂNG NHẬP & CHỌN ROLE
// ==========================================
@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var email by remember { mutableStateOf("educator@stem.vn") }
    var password by remember { mutableStateOf("123456") }
    var errorMsg by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (viewModel.isDarkTheme) {
                        listOf(Color(0xFF1A1510), Color(0xFF0F0C08)) // Deep rich golden dark
                    } else {
                        listOf(Color(0xFFFBF8F1), Color(0xFFEBE3D5)) // Light creamy golden marble
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Theme Toggle at top right
        IconButton(
            onClick = { viewModel.isDarkTheme = !viewModel.isDarkTheme },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(
                imageVector = if (viewModel.isDarkTheme) Icons.Default.Add else Icons.Default.Check, // Placeholder, usually Lightbulb or DarkMode
                contentDescription = "Toggle Theme",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Card(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .padding(24.dp)
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f) // Glass Effect
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.marcus_logo),
                    contentDescription = "Marcus Aurelius Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "HENOSIS",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 4.sp
                )

                Text(
                    text = "EXCLUSIVE AI LEARNING PLATFORM",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Input Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMsg = "" },
                    label = { Text("Email hoặc Tên đăng nhập") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMsg = "" },
                    label = { Text("Mật khẩu") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                var adminKey by remember { mutableStateOf("") }
                
                // Input Admin Key
                OutlinedTextField(
                    value = adminKey,
                    onValueChange = { adminKey = it; errorMsg = "" },
                    label = { Text("Key Admin (Nếu có)") },
                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = "Admin Key") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Button(
                    onClick = {
                        if (email.trim().isEmpty() || password.trim().isEmpty()) {
                            errorMsg = "Vui lòng nhập đầy đủ thông tin đăng nhập."
                        } else if (adminKey.isNotEmpty() && adminKey != "230923anhduY@") {
                            errorMsg = "Key Admin không hợp lệ."
                        } else {
                            val role = if (adminKey == "230923anhduY@") "teacher_admin" else "student"
                            viewModel.performLogin(email, password, role)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Đăng nhập")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ĐĂNG NHẬP", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        viewModel.authScreen = "signup"
                    }
                ) {
                    Text("Chưa có tài khoản? Đăng ký ngay")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.isDarkTheme = !viewModel.isDarkTheme }
                ) {
                    Icon(
                        imageVector = if (viewModel.isDarkTheme) Icons.Default.Star else Icons.Default.Star,
                        contentDescription = "Theme",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (viewModel.isDarkTheme) "Giao diện: Tối" else "Giao diện: Sáng",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var adminKey by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (viewModel.isDarkTheme) {
                        listOf(Color(0xFF1A1510), Color(0xFF0F0C08)) // Deep rich golden dark
                    } else {
                        listOf(Color(0xFFFBF8F1), Color(0xFFEBE3D5)) // Light creamy golden marble
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Theme Toggle at top right
        IconButton(
            onClick = { viewModel.isDarkTheme = !viewModel.isDarkTheme },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(
                imageVector = if (viewModel.isDarkTheme) Icons.Default.Add else Icons.Default.Check, // Placeholder
                contentDescription = "Toggle Theme",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Card(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .padding(24.dp)
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f) // Glass Effect
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Sign Up",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "HENOSIS",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "CREATE EXCLUSIVE ACCOUNT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMsg = "" },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMsg = "" },
                    label = { Text("Mật khẩu") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMsg = "" },
                    label = { Text("Nhập lại mật khẩu") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = adminKey,
                    onValueChange = { adminKey = it; errorMsg = "" },
                    label = { Text("Mã Admin (Tùy chọn)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.trim().isEmpty() || password.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
                            errorMsg = "Vui lòng nhập đầy đủ thông tin."
                        } else if (password != confirmPassword) {
                            errorMsg = "Mật khẩu không khớp."
                        } else if (adminKey.isNotEmpty() && adminKey != "230923anhduY@") {
                            errorMsg = "Key Admin không hợp lệ."
                        } else {
                            val role = if (adminKey == "230923anhduY@") "teacher_admin" else "student"
                            viewModel.performLogin(email, password, role)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ĐĂNG KÝ", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { viewModel.authScreen = "login" }) {
                    Text("Đã có tài khoản? Quay lại đăng nhập")
                }
            }
        }
    }
}

// ==========================================
// 2. KHU VỰC ĐIỀU HƯỚNG & RESPONSIVE
// ==========================================

// Laptop layout showing Left Sidebar + Middle Content
@Composable
fun LaptopLayout(viewModel: MainViewModel) {
    var activeSubTab by remember { mutableStateOf("cards") } // "cards", "student_dash", "teacher_dash", "settings"

    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar on the left
        SidebarLayout(
            viewModel = viewModel,
            activeTab = activeSubTab,
            onTabSelected = { activeSubTab = it },
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)) // Glassy sidebar
        )

        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        // Main content in the middle
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            when (activeSubTab) {
                "cards" -> MainCardsScreen(viewModel = viewModel)
                "study_groups" -> StudyGroupScreen(viewModel = viewModel)
                "leaderboard" -> LeaderboardScreen(viewModel = viewModel)
                "teacher_dash" -> TeacherDashboardScreen(viewModel = viewModel)
                else -> MainCardsScreen(viewModel = viewModel)
            }

            // Chatbot Gia Sư bubble layout
            TutorChatBotBubble(viewModel = viewModel)
        }
    }
}

// Phone Mobile Layout showing Bottom Navigation
@Composable
fun PhoneLayout(viewModel: MainViewModel) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableStateOf("cards") } // "cards", "study_groups", "leaderboard"

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f) // Glass effect
            ) {
                NavigationBarItem(
                    selected = activeSubTab == "cards",
                    onClick = { activeSubTab = "cards" },
                    icon = { Icon(Icons.Default.Menu, contentDescription = "Học tập") },
                    label = { Text("Học tập", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = activeSubTab == "leaderboard",
                    onClick = { activeSubTab = "leaderboard" },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Xếp hạng") },
                    label = { Text("Xếp hạng", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = activeSubTab == "study_groups",
                    onClick = { activeSubTab = "study_groups" },
                    icon = { Icon(Icons.Default.Share, contentDescription = "Nhóm học") },
                    label = { Text("Nhóm học", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { 
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/henosis_community"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    icon = { Icon(Icons.Default.Send, contentDescription = "Telegram") },
                    label = { Text("Telegram", fontSize = 11.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeSubTab) {
                "cards" -> MainCardsScreen(viewModel = viewModel)
                "study_groups" -> StudyGroupScreen(viewModel = viewModel)
                "leaderboard" -> LeaderboardScreen(viewModel = viewModel)
            }

            // Chatbot bubble
            TutorChatBotBubble(viewModel = viewModel)
        }
    }
}

@Composable
fun SidebarLayout(
    viewModel: MainViewModel,
    activeTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val subjects by viewModel.customSubjects.collectAsState()
    val rawCards by viewModel.allFlashcards.collectAsState()

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Profile Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.currentRole.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (viewModel.currentRole == "teacher_admin") "Giáo viên / Admin" else "Học sinh STEM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = viewModel.loggedInUserEmail.take(18) + "...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Main Menu Options
            Text(
                text = "DANH MỤC CHÍNH",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            NavigationButton(
                title = "Flashcard Học tập",
                icon = Icons.Default.Menu,
                isSelected = activeTab == "cards",
                onClick = { onTabSelected("cards") }
            )

            NavigationButton(
                title = "Bảng Xếp Hạng",
                icon = Icons.Default.Star,
                isSelected = activeTab == "leaderboard",
                onClick = { onTabSelected("leaderboard") }
            )

            NavigationButton(
                title = "Nhóm Học Tập",
                icon = Icons.Default.Share,
                isSelected = activeTab == "study_groups",
                onClick = { onTabSelected("study_groups") }
            )

            if (viewModel.currentRole == "teacher_admin") {
                NavigationButton(
                    title = "Báo Cáo Giáo Viên",
                    icon = Icons.Default.Person,
                    isSelected = activeTab == "teacher_dash",
                    onClick = { onTabSelected("teacher_dash") }
                )
            }

            NavigationButton(
                title = "Cộng đồng (Telegram)",
                icon = Icons.Default.Send,
                isSelected = false,
                onClick = { 
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/henosis_community"))
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Subject Filter Section
            Text(
                text = "DANH SÁCH MÔN HỌC",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // "All" filter button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (viewModel.selectedSubject == "All") MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable {
                        viewModel.selectedSubject = "All"
                        viewModel.currentCardIndex = 0
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = null,
                    tint = if (viewModel.selectedSubject == "All") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Tất cả môn học (${rawCards.size})",
                    fontSize = 13.sp,
                    fontWeight = if (viewModel.selectedSubject == "All") FontWeight.Bold else FontWeight.Normal
                )
            }

            // Loop subjects lists
            subjects.forEach { subject ->
                val cardsCount = rawCards.filter { it.subject.lowercase() == subject.lowercase() }.size
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (viewModel.selectedSubject.lowercase() == subject.lowercase()) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .clickable {
                            viewModel.selectedSubject = subject
                            viewModel.currentCardIndex = 0
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (viewModel.selectedSubject.lowercase() == subject.lowercase()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${subject.replaceFirstChar { it.uppercase() }} ($cardsCount)",
                        fontSize = 13.sp,
                        fontWeight = if (viewModel.selectedSubject.lowercase() == subject.lowercase()) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    // If teacher_admin, show delete option for custom subjects to allow editing classifications
                    if (viewModel.currentRole == "teacher_admin") {
                        IconButton(
                            onClick = { viewModel.removeCustomSubject(subject) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xóa môn",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // "Thêm phân loại" (Admin only button)
            if (viewModel.currentRole == "teacher_admin") {
                OutlinedButton(
                    onClick = { viewModel.showAddSubjectDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thêm phân loại", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Upload document action button
            Button(
                onClick = { viewModel.showUploadDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Upload")
                Spacer(modifier = Modifier.width(8.dp))
                Text("UPLOAD TÀI LIỆU", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Global theme toggle
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.isDarkTheme = !viewModel.isDarkTheme }
            ) {
                Icon(
                    imageVector = if (viewModel.isDarkTheme) Icons.Default.Star else Icons.Default.Star,
                    contentDescription = "dark mode",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Giao diện: " + if (viewModel.isDarkTheme) "Tối" else "Sáng", fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Log out")
                Spacer(modifier = Modifier.width(8.dp))
                Text("ĐĂNG XUẤT", fontSize = 12.sp)
            }
        }
    }

    // Modal adding subject dialog
    if (viewModel.showAddSubjectDialog) {
        var newSubName by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { viewModel.showAddSubjectDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Tự thêm phân loại môn học mới", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newSubName,
                        onValueChange = { newSubName = it },
                        label = { Text("Tên môn học (Ví dụ: sinh, sử...)") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { viewModel.showAddSubjectDialog = false }) {
                            Text("Hủy")
                        }
                        Button(onClick = { viewModel.addCustomSubject(newSubName) }) {
                            Text("Lưu môn")
                        }
                    }
                }
            }
        }
    }

    // Modal upload documents simulation
    if (viewModel.showUploadDialog) {
        UploadDocumentDialog(viewModel = viewModel)
    }
}

@Composable
fun NavigationButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

// ==========================================
// 3. TRANG HỌC TẬP CHÍNH (FLASHCARD)
// ==========================================
@Composable
fun MainCardsScreen(viewModel: MainViewModel) {
    val allCards by viewModel.allFlashcards.collectAsState()
    val cards = viewModel.getFilteredCards() // will now recompose because allCards triggers it
    val activeCard = viewModel.getActiveCard()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            // Upper subject filter bar header (for compact devices showing the category)
            Text(
                text = "CHỦ ĐỀ LỌC: ${viewModel.selectedSubject.uppercase()}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
            IconButton(
                onClick = { viewModel.isDarkTheme = !viewModel.isDarkTheme }
            ) {
                Icon(
                    imageVector = if (viewModel.isDarkTheme) Icons.Default.Add else Icons.Default.Check, // Placeholder
                    contentDescription = "Theme Toggle",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Category Badge Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val subjects = listOf("All", "math", "physics", "chemistry", "biology", "english", "history", "weak_cards", "other")
            subjects.forEach { subject ->
                val label = when(subject) {
                    "All" -> "📚 Tất cả"
                    "math" -> "📐 Toán học"
                    "physics" -> "⚡ Vật lý"
                    "chemistry" -> "🧪 Hóa học"
                    "biology" -> "🧬 Sinh học"
                    "english" -> "🇬🇧 Tiếng Anh"
                    "history" -> "📜 Lịch sử"
                    "weak_cards" -> "⚠️ Yếu/Chưa thuộc"
                    else -> "🌟 Khác"
                }
                val isSelected = viewModel.selectedSubject.lowercase() == subject.lowercase()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel.selectedSubject = subject
                            viewModel.currentCardIndex = 0
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detailed Study Units List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📖 CÁC HỌC PHẦN HIỆN CÓ (${viewModel.studyUnits.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Hiển thị theo bộ lọc: ${if (viewModel.selectedSubject == "All") "Toàn bộ lớp học" else "Môn " + viewModel.selectedSubject.uppercase()}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                viewModel.studyUnits.forEach { unit ->
                    val isCurrentSubjectUnit = unit.subject.lowercase() == viewModel.selectedSubject.lowercase() || viewModel.selectedSubject == "All"
                    if (isCurrentSubjectUnit) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (unit.status == "empty") MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val (subjectEmoji, subjectLabel) = when(unit.subject) {
                                            "math" -> Pair("📐", "Toán")
                                            "physics" -> Pair("⚡", "Lý")
                                            "chemistry" -> Pair("🧪", "Hóa")
                                            "biology" -> Pair("🧬", "Sinh")
                                            "english" -> Pair("🇬🇧", "Anh")
                                            else -> Pair("🌟", "Khác")
                                        }
                                        Text(
                                            "$subjectEmoji $subjectLabel",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (unit.status == "empty") {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFEAB308).copy(alpha = 0.15f))
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    "MUTIL-CHUNK",
                                                    fontSize = 7.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFCA8A04)
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF14B8A6).copy(alpha = 0.15f))
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    "NẠP SẴN",
                                                    fontSize = 7.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0D9488)
                                                )
                                            }
                                        }
                                    }
                                    
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = unit.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { viewModel.startAbilityTest() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("📝 Test MCQ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = { viewModel.triggerStudyUnit(unit) },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (unit.status == "empty") Color(0xFFEAB308) else MaterialTheme.colorScheme.primary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        if (unit.status == "empty") {
                                            Text("Lazy Load", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("Học Ngay", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (cards.isEmpty()) {
            // Empty Screen Placeholder UI
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Empty",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Không tìm thấy thẻ nào cả!",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Hãy nhấn 'Upload Tài liệu' ở góc hoặc chọn lại bộ lọc",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (!viewModel.studyModeActive) {
            // "Bắt đầu học" state
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Sẵn sàng học tập!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tổng số thẻ trong bộ lọc này: ${cards.size}", fontSize = 14.sp)
                     
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { viewModel.startStudySession() },
                        modifier = Modifier.height(54.dp).fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(27.dp)
                    ) {
                        Text("▶️ Bắt đầu học", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (viewModel.showStudySummary) {
            // Study Summary matching the uploaded purple UI screenshot
            val knowCount = viewModel.knownCount
            val reviewCount = viewModel.studyCardsReviewQueue.size
            val total = viewModel.studyCardsQueue.size
            // Use requested logic: "mức độ phần trăm thông hiểu bằng thẻ đánh dấu x (reviewCount) chia tổng số thẻ"
            val pct = if (total > 0) (reviewCount.toFloat() / total) * 100 else 0f
            val progressPct = if (total > 0) reviewCount.toFloat() / total else 0f
            
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bạn đang làm rất tuyệt! Hãy tiếp tục tập trung vào các thuật ngữ khó.", 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Tiến độ của bạn", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                        CircularProgressIndicator(
                            progress = progressPct,
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFFEF4444), // Show Red/Orange for 'Chưa hiểu/Đánh X'
                            strokeWidth = 10.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text("${"%.1f".format(pct)}%", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(24.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF6EE7B7)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Biết", color = Color(0xFF064E3B), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("$knowCount", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFFDBA74)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Đang học", color = Color(0xFF7C2D12), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("$reviewCount", color = Color(0xFF7C2D12), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFE2E8F0)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Còn lại", color = Color(0xFF334155), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("${total - knowCount - reviewCount}", color = Color(0xFF334155), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                if (reviewCount > 0) {
                    Button(
                        onClick = { viewModel.continueReviewingTerms() },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A6CFF)),
                        shape = RoundedCornerShape(27.dp)
                    ) {
                        Text("Tiếp tục ôn thuật ngữ ($reviewCount)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Button(
                    onClick = { viewModel.restartStudySession() },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(27.dp)
                ) {
                    Text("Ôn luyện lại", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = { viewModel.quitStudySession() }) {
                    Text("Đặt lại Thẻ ghi nhớ về danh sách", fontWeight = FontWeight.Bold)
                }
            }

        } else if (viewModel.studyCardsQueue.isNotEmpty() && viewModel.currentStudyIndex < viewModel.studyCardsQueue.size) {
            val studyCard = viewModel.studyCardsQueue[viewModel.currentStudyIndex]
            
            Text(
                text = "Thẻ số ${viewModel.currentStudyIndex + 1} trên ${viewModel.studyCardsQueue.size}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3D Lật Thẻ Animation Component
            var degreesCardFlipped by remember { mutableStateOf(0f) }
            val animatedRotationY by animateFloatAsState(
                targetValue = if (viewModel.isCardFlipped) 180f else 0f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .graphicsLayer {
                        rotationY = animatedRotationY
                        cameraDistance = 14f * density
                    }
                    .clickable {
                        viewModel.isCardFlipped = !viewModel.isCardFlipped
                    }
                    .shadow(16.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isCardFlipped) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Agent 2 Bóc tách sâu
                    Button(
                        onClick = { 
                            viewModel.triggerAgent2DeepExplain() 
                            viewModel.isCardFlipped = true // Show back to see output
                        },
                        enabled = viewModel.aiCooldownRemaining == 0 && !viewModel.isAgent2Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)),
                        modifier = Modifier.align(Alignment.TopEnd).height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        if (viewModel.isAgent2Loading) {
                            Text("⏳ Đang xử lý...", fontSize = 10.sp, color = Color.White)
                        } else {
                            Text("✨ Giải thích sâu (AI)", fontSize = 10.sp, color = Color.White)
                        }
                    }

                    // Content layout
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                if (animatedRotationY > 90f) {
                                    rotationY = 180f
                                }
                            },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (animatedRotationY <= 90f) {
                            // Front display (Question)
                            Text(
                                text = formatScienceText(studyCard.front),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            // Back display (Answer)
                            Text(
                                text = formatScienceText(studyCard.back),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = if (viewModel.isDarkTheme) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // X and Check buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // X Button
                IconButton(
                    onClick = { viewModel.submitStudyAnswer(studyCard, knewIt = false) },
                    modifier = Modifier.size(64.dp).background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Chưa thuộc", tint = Color(0xFFEF4444), modifier = Modifier.size(32.dp))
                }

                // Check Button
                IconButton(
                    onClick = { viewModel.submitStudyAnswer(studyCard, knewIt = true) },
                    modifier = Modifier.size(64.dp).background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Đã thuộc", tint = Color(0xFF10B981), modifier = Modifier.size(32.dp))
                }
            }

            // Expanded deep explainer Agent 2 output panel
            if (studyCard.aiDetails != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "BÁC TÁCH KIẾN THỨC SÂU (Agent 2)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))

                        Text(
                            text = formatScienceText(studyCard.aiDetails ?: ""),
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // Active inline modal to edit flashcard (Admin Pen)
        val targetCard = viewModel.editTargetCard
        if (viewModel.showEditCardDialog && targetCard != null) {
            var frontText by remember(targetCard) { mutableStateOf(targetCard.front) }
            var backText by remember(targetCard) { mutableStateOf(targetCard.back) }
            var cardSubj by remember(targetCard) { mutableStateOf(targetCard.subject) }

            Dialog(onDismissRequest = { viewModel.showEditCardDialog = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Sửa đổi Flashcard", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = frontText,
                            onValueChange = { frontText = it },
                            label = { Text("Mặt trước (Keyword / Đề bài)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = backText,
                            onValueChange = { backText = it },
                            label = { Text("Mặt sau (Giải nghĩa / Đáp án)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = cardSubj,
                            onValueChange = { cardSubj = it },
                            label = { Text("Môn học (math, physics, biology...)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = {
                                    viewModel.deleteCard(targetCard)
                                    viewModel.showEditCardDialog = false
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                            ) {
                                Text("Xóa thẻ")
                            }

                            Row {
                                TextButton(onClick = { viewModel.showEditCardDialog = false }) {
                                    Text("Quay lại")
                                }
                                Button(onClick = { viewModel.saveEditedCard(frontText, backText, cardSubj) }) {
                                    Text("Lưu")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. CHATBOT GIA SƯ (AGENT 3) - FLOATING WIDGET
// ==========================================
@Composable
fun TutorChatBotBubble(viewModel: MainViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(horizontalAlignment = Alignment.End) {
            // Expanded Chatbox Widget overlay
            if (viewModel.isChatOpen) {
                ElevatedCard(
                    modifier = Modifier
                        .sizeIn(
                            minWidth = 320.dp,
                            maxWidth = 420.dp,
                            minHeight = 420.dp,
                            maxHeight = 540.dp
                        )
                        .padding(bottom = 12.dp)
                        .shadow(24.dp, RoundedCornerShape(24.dp))
                        .border(1.dp, if (viewModel.isDarkTheme) Color(0xFF2E3D60) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkTheme) Color(0xFF0F1527) else MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header Chat panel
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF5A6CFF), Color(0xFF8E62FF))
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Agent 3: Trợ Lý Đa Năng", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Sẵn sàng dập tắt câu hỏi", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Normal)
                                }
                            }

                            IconButton(
                                onClick = { viewModel.isChatOpen = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        // Message Feed Stream scrolling
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(12.dp)
                                .verticalScroll(scrollState)
                        ) {
                            viewModel.chatMessages.forEach { (text, isUser) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isUser) 16.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 16.dp
                                        ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isUser) Color(0xFF4C5BF4) else (if (viewModel.isDarkTheme) Color(0xFF1C2744) else MaterialTheme.colorScheme.surfaceVariant)
                                        ),
                                        border = if (!isUser) BorderStroke(1.dp, if (viewModel.isDarkTheme) Color(0xFF2E3D60) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else null,
                                        modifier = Modifier.widthIn(max = 280.dp)
                                    ) {
                                        Text(
                                            text = text,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(12.dp),
                                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Dynamic loading state indicator
                            if (viewModel.isTutorResponding) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2744)),
                                        border = BorderStroke(1.dp, Color(0xFF2E3D60)),
                                        modifier = Modifier.widthIn(max = 200.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                strokeWidth = 1.5.dp,
                                                color = Color(0xFF5A6CFF)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Socrates đang gõ...", fontSize = 12.sp, fontStyle = FontStyle.Italic, color = Color(0xFF94A3B8))
                                        }
                                    }
                                }
                            }

                            // Auto Scroll to last chat message
                            LaunchedEffect(viewModel.chatMessages.size, viewModel.isTutorResponding) {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }

                        // Custom Capsule Input Panel
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F1527))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(Color(0xFF131B2F))
                                    .border(1.dp, Color(0xFF233256), RoundedCornerShape(28.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Add Button
                                IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF94A3B8))
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Text Box
                                Box(modifier = Modifier.weight(1f)) {
                                    if (viewModel.chatInputText.isEmpty()) {
                                        Text(
                                            "Hỏi công thức, từ vựng...",
                                            color = Color(0xFF94A3B8).copy(alpha = 0.6f),
                                            fontSize = 13.sp
                                        )
                                    }
                                    androidx.compose.foundation.text.BasicTextField(
                                        value = viewModel.chatInputText,
                                        onValueChange = { viewModel.chatInputText = it },
                                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF5A6CFF)),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // List mic placeholder icon
                                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.List, contentDescription = "Mic", tint = Color(0xFF94A3B8))
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Send blue float icon button
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (viewModel.chatInputText.trim().isNotEmpty() && !viewModel.isTutorResponding && viewModel.aiCooldownRemaining == 0) Color(0xFF5A6CFF) else Color(0xFF1C2744))
                                        .clickable(enabled = viewModel.chatInputText.trim().isNotEmpty() && !viewModel.isTutorResponding && viewModel.aiCooldownRemaining == 0) {
                                            viewModel.sendChatMessage()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (viewModel.aiCooldownRemaining > 0) {
                                        Text(
                                            text = "${viewModel.aiCooldownRemaining}s",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Gửi",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bubble trigger FAB button (Fixed post-scroll state)
            FloatingActionButton(
                onClick = { viewModel.isChatOpen = !viewModel.isChatOpen },
                containerColor = Color(0xFF5A6CFF),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (viewModel.isChatOpen) Icons.Default.Clear else Icons.Default.Star,
                    contentDescription = "Socrates AI"
                )
            }
        }
    }
}

// ==========================================
// 5. DASHBOARD HỌC SINH (STUDENT)
// ==========================================
@Composable
fun StudentDashboardScreen(viewModel: MainViewModel) {
    val cards by viewModel.allFlashcards.collectAsState()
    val analytics by viewModel.studyAnalytics.collectAsState()

    // Calculations of Mastery Rate
    val masteredCount = cards.filter { it.mastered }.size
    val totalCount = cards.size
    val masteryRate = if (totalCount > 0) (masteredCount.toFloat() / totalCount.toFloat() * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("DASHBOARD HỌC SINH", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        Text("Kết quả rèn luyện và đua top tinh thông", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        // Vertical metric stats stack matching the exact screenshot design
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card 1: Tổng Thể Bài
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (viewModel.isDarkTheme) Color(0xFF2E3D60) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkTheme) Color(0xFF131B2F) else MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TỔNG THỂ BÀI",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$totalCount",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF5A6CFF)
                    )
                }
            }

            // Card 2: Đã Thông Thạo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (viewModel.isDarkTheme) Color(0xFF2E3D60) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkTheme) Color(0xFF131B2F) else MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ĐÃ THÔNG THẠO",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$masteredCount",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF14B8A6)
                    )
                }
            }

            // Card 3: Tỉ Lệ Mastery - Glowing deep gradient
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (viewModel.isDarkTheme) Color(0xFF4C1D95) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkTheme) Color(0xFF2E2260) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TỶ LỆ (MASTERY)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE2E8F0),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$masteryRate%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Weakness Detection Class Alert matching the first screenshot
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkTheme) Color(0xFF1C1A14) else Color(0xFFFEF3C7))
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Cảnh báo",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AI Weakness Detection (Cảnh báo Lớp học)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFF59E0B)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "⚠️ Chú ý: 85% học sinh đang liên tục chọn \"Cực khó\" ở thẻ \"Chu kỳ dao động (T)\".",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (viewModel.isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF78350F)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Khuyến nghị từ AI: Ôn tập lại phần công thức con lắc đơn trong 10 phút đầu giờ học tới. Hệ thống đã tự động tạo bài Assignment phụ trợ cho nhóm học sinh yếu.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = if (viewModel.isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF92400E)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Curved Canvas-drawn Progress Chart showing progress by week
        Text("TIẾN ĐỘ HỌC TẬP TRONG TUẦN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Drawing Canvas Chart
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    // Sample analytics mapping
                    val values = days.map { day ->
                        analytics.filter { it.dayOfWeek.lowercase() == day.lowercase() }.sumOf { it.cardsLearned }.coerceAtLeast(1)
                    }

                    val maxVal = (values.maxOrNull() ?: 10).toFloat().coerceAtLeast(10f)
                    val widthScale = size.width / 7
                    val heightScale = size.height / maxVal

                    // Draw baseline
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 2f
                    )

                    // Draw vertical gradient bar chart columns
                    for (i in 0 until 7) {
                        val barHeight = values[i] * heightScale * 0.85f
                        val x = i * widthScale + (widthScale * 0.25f)
                        val y = size.height - barHeight

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF3B82F6))
                            ),
                            topLeft = Offset(x, y),
                            size = Size(widthScale * 0.5f, barHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }
                }

                // Days Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { day ->
                        Text(
                            text = day,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(42.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Leaderboard đua top tinh thông
        Text("BẢNG XẾP HẠNG THI ĐUA (LEADERBOARD)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                LeaderboardRow(rank = 1, name = "Nguyễn Minh Đức (11A1)", points = 980, percentage = 98)
                LeaderboardRow(rank = 2, name = "Trần Khánh Vy (11A2)", points = 890, percentage = 89)
                LeaderboardRow(rank = 3, name = "Bạn (STEM Profile)", points = masteryRate * 10, percentage = masteryRate, isCurrentUser = true)
                LeaderboardRow(rank = 4, name = "Lê Hoàng Long (11A1)", points = 650, percentage = 65)
                LeaderboardRow(rank = 5, name = "Phạm Uyên Thy (11A3)", points = 450, percentage = 45)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Big CTA Practice ability Test button "Kiểm tra năng lực" (15 Questions timer)
        Button(
            onClick = { viewModel.startAbilityTest() },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (viewModel.isAgent2Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Đang tạo đề thi năng lực...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = "Test", modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text("KIỂM TRA NĂNG LỰC (15 CÂU)", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    Text("Đề sinh tự động từ các chủ đề bạn đang còn yếu bởi AI", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        val context = androidx.compose.ui.platform.LocalContext.current
        Button(
            onClick = {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://web.telegram.org/k/#-5129735011"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Không thể mở Telegram", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(Icons.Default.Send, contentDescription = "Telegram", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("Góp ý / Hỗ trợ qua Telegram", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(72.dp))
    }

    // Active full overlay test screen when CTA is triggered
    if (false && viewModel.isTestActive) {
        // Removed AbilityTestScreen
    }

    // Lazy load SCAN progress indicator
    if (viewModel.isCheckingChunk) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Đang gọi Agent 1 (Google Drive Reader)...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Đang đọc tệp '${viewModel.currentLazyLoadingFileName}' từ Google Drive và tiến hành bóc tách phân mảnh vi học (On-Demand Chunking)...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // ZenQuotes dialog popover
    if (viewModel.showQuoteDialog) {
        Dialog(onDismissRequest = { viewModel.showQuoteDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, Color(0xFFD97706)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1527))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFD97706).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌸", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "HOÀN THÀNH HỌC PHẦN XUẤT SẮC!",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color(0xFFFBBF24),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = viewModel.motivatingQuote,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.showQuoteDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Text("Nhận động lực học tiếp 🌟", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Connected Mind Map Overlay Canvas
    if (viewModel.showMindMapSheet) {
        Dialog(onDismissRequest = { viewModel.showMindMapSheet = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, Color(0xFFD4AF37)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F19)),
                modifier = Modifier.fillMaxWidth().height(420.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SƠ ĐỒ LIÊN QUAN KIẾN THỨC (MIND MAP)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = Color(0xFFD4AF37),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sơ đồ tự động kiến tạo bằng AI cho Chủ đề: ${viewModel.mindMapData?.topic ?: viewModel.selectedSubject}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (viewModel.isMindMapLoading) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFFD4AF37))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Đang kiến tạo Mind Map...", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerX = size.width / 2
                                val centerY = size.height / 2
                                
                                drawCircle(
                                    color = Color(0xFFD4AF37),
                                    radius = 26f,
                                    center = Offset(centerX, centerY)
                                )
                                
                                val branches = viewModel.mindMapData?.branches ?: listOf("-", "-", "-", "-")
                                
                                val angles = listOf(0.0, 90.0, 180.0, 270.0)
                                val distance = size.width.coerceAtMost(size.height) * 0.35f
                                
                                branches.forEachIndexed { idx, _ ->
                                    val angleRad = Math.toRadians(angles.getOrElse(idx) { 0.0 })
                                    val satX = (centerX + distance * Math.cos(angleRad)).toFloat()
                                    val satY = (centerY + distance * Math.sin(angleRad)).toFloat()
                                    
                                    drawLine(
                                        color = Color(0xFFD4AF37).copy(alpha = 0.5f),
                                        start = Offset(centerX, centerY),
                                        end = Offset(satX, satY),
                                        strokeWidth = 3f,
                                        cap = StrokeCap.Round
                                    )
                                    
                                    drawCircle(
                                        color = Color(0xFF14B8A6),
                                        radius = 18f,
                                        center = Offset(satX, satY)
                                    )
                                }
                            }
                            
                            val branches = viewModel.mindMapData?.branches ?: emptyList()
                            
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    viewModel.mindMapData?.rootName ?: "",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.Center).padding(top = 52.dp)
                                )
                                
                                if (branches.isNotEmpty()) {
                                    Text(branches.getOrElse(0) { "" }, fontSize = 10.sp, color = Color.White, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp))
                                    Text(branches.getOrElse(1) { "" }, fontSize = 10.sp, color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp))
                                    Text(branches.getOrElse(2) { "" }, fontSize = 10.sp, color = Color.White, modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp))
                                    Text(branches.getOrElse(3) { "" }, fontSize = 10.sp, color = Color.White, modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp))
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.showMindMapSheet = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Đóng bản đồ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(rank: Int, name: String, points: Int, percentage: Int, isCurrentUser: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = when (rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> " $rank"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.width(36.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$points đ",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$percentage % tinh thông",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==========================================
// 6. DASHBOARD GIÁO VIÊN/ADMIN
// ==========================================
@Composable
fun TeacherDashboardScreen(viewModel: MainViewModel) {
    val cards by viewModel.allFlashcards.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("BÁO CÁO GIÁO VIÊN", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(16.dp))

        // Document Uploader Container for Admin
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Upload doc",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "KHO TÀI LIỆU SÁCH MỀM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.showUploadDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Tải tài liệu & Trích xuất Flashcards", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("THỐNG KÊ 3 PHÂN LOẠI MÔN HỌC (SET CARDS) DƯỚI 50% THÔNG HIỂU", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Group cards by subject, calculate weak average
        val weakSubjects = cards
            .groupBy { it.subject }
            .map { entry -> 
                val subject = entry.key
                val subjectCards = entry.value
                val weakCount = subjectCards.count { it.smDifficulty < 2.5f } // Proxy for < 50% comprehension
                val pct = if(subjectCards.isNotEmpty()) (weakCount.toFloat() / subjectCards.size)*100 else 0f
                Pair(subject, pct)
            }
            .filter { it.second > 0 } // Or >= 50, but we just simulate it with the highest weakly performed ones
            .sortedByDescending { it.second }
            .take(3)

        if (weakSubjects.isEmpty()) {
            Text("Không có set card nào dưới mức 50% thông hiểu. Học sinh đang học tập tốt!", fontSize = 13.sp, fontStyle = FontStyle.Italic)
        } else {
            weakSubjects.forEach { stat ->
                val subjectName = stat.first.uppercase()
                val weakPct = stat.second.toInt()
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(subjectName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Yếu: $weakPct%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("QUẢN LÝ KHO THẺ FLASHCARD (${cards.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(10.dp))

        // Keeping flashcards manager
        cards.forEach { card ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(card.subject.uppercase(), fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        Text(card.front, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(card.back, fontSize = 11.sp, maxLines = 1)
                    }
                    IconButton(onClick = { viewModel.startEditingCard(card) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { viewModel.deleteCard(card) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}

// Remove AtRiskStudentRow

@Composable
fun LeaderboardScreen(viewModel: MainViewModel) {
    // Collect stats to use the current user's knowCount for ranking
    val cards by viewModel.allFlashcards.collectAsState()
    val userPoints = cards.filter{ it.smDifficulty >= 2.5f || it.smRepetitions > 1 }.size // A proxy for green checks
    // We could also track green checks globally in ViewModel, but computing from cards is a robust fallback
    
    val leaderboard = listOf(
        Pair("Le Tuan Anh", userPoints + 15),
        Pair("Nguyen Van A", userPoints + 5),
        Pair("Bạn", userPoints),
        Pair("Tran Thi B", (userPoints - 3).coerceAtLeast(0)),
        Pair("Pham Hoang", (userPoints - 10).coerceAtLeast(0))
    ).sortedByDescending { it.second }.take(10)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Bảng Xếp Hạng Hàng Tuần", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Đua top dựa trên số thẻ đạt tích xanh (Đã thuộc) - 1 thẻ = 1 điểm", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        
        leaderboard.forEachIndexed { index, pair ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pair.first == "Bạn") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("#${index + 1}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(pair.first, fontSize = 16.sp, fontWeight = if (pair.first == "Bạn") FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("${pair.second} đ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}



// ==========================================
// 7. UTILS & DIALOGS & SPECIAL OVERLAYS
// ==========================================

// Upload files simulated parser
@Composable
fun UploadDocumentDialog(viewModel: MainViewModel) {
    var rawTextContext by remember { mutableStateOf("") }
    var pickedPresetTitle by remember { mutableStateOf("") }

    val presets = listOf(
        Pair("GIÁO TRÌNH ANH VĂN - CHUYÊN ĐỀ TỪ VỰNG (.pdf)", "vui mừng: cheerful, hạnh phúc: happy, tình cờ: serendipity, hụt hẫng: disappointed"),
        Pair("SÁCH GIÁO KHOA VẬT LÝ 12 - CHƯƠNG LƯỢNG TỬ SÁNG (.pdf)", "Thuyết lượng tử ánh sáng phát biểu ánh sáng được tạo thành bởi các hạt photon bay dọc theo các tia sáng. Năng lượng photon E = h * f. Hằng số Planck h = 6.625 x 10^-34."),
        Pair("TỔNG HỢP TOÁN GIẢI TÍCH - ĐẠI SỐ SƠ CẤP (.txt)", "Đạo hàm của hàm số lượng giác sinx là cosx, đạo hàm của cosx là -sinx. Tích phân xác định biểu diễn diện tích hình phẳng.")
    )

    Dialog(onDismissRequest = { viewModel.showUploadDialog = false }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Upload Tài liệu & Bóc tách (Data Pipeline)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Chọn tài liệu có sẵn hoặc chèn dữ liệu mộc tự soạn",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Select mock preset documents representing Google Drive integration
                presets.forEach { (title, text) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                rawTextContext = text
                                pickedPresetTitle = title
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (pickedPresetTitle == title) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = rawTextContext,
                    onValueChange = { rawTextContext = it },
                    label = { Text("Dữ liệu thô tài liệu trích xuất") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { viewModel.showUploadDialog = false }) {
                        Text("Đóng")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val fName = if (pickedPresetTitle.isNotEmpty()) pickedPresetTitle else "TuBienSoan.txt"
                            viewModel.simulateFileUpload(fName, rawTextContext)
                        },
                        enabled = rawTextContext.trim().isNotEmpty()
                    ) {
                        Text("BÓC TÁCH NGAY (Agent 1)")
                    }
                }
            }
        }
    }
}

// Full screen overlay test (Ability Test 15 Questions)
@Composable
fun AbilityTestScreen(viewModel: MainViewModel) {
    val durationMinutes = viewModel.testTimeRemaining / 60
    val durationSeconds = viewModel.testTimeRemaining % 60
    val formattedTime = String.format("%02d:%02d", durationMinutes, durationSeconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) {}, // Intercept taps
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize(0.95f)
                .shadow(16.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header of Test
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Bài kiểm tra năng lực (15 câu)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Đánh giá toàn diện vùng kiến thức yếu", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }

                    // Countdown timer widget (timer countdown active state)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Timer", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedTime,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }

                if (!viewModel.isTestSubmitted) {
                    // Running test screen
                    val currentQuestion = viewModel.testQuestions.getOrNull(viewModel.currentQuestionIndex)
                    if (currentQuestion != null) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Câu hỏi ${viewModel.currentQuestionIndex + 1} / ${viewModel.testQuestions.size}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = currentQuestion.question,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Options display
                            currentQuestion.options.forEachIndexed { optIndex, text ->
                                val alphabetIndex = when (optIndex) {
                                    0 -> "A"
                                    1 -> "B"
                                    2 -> "C"
                                    else -> "D"
                                }

                                val isSelected = viewModel.testAnswers[viewModel.currentQuestionIndex] == optIndex

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clickable { viewModel.pickTestOption(optIndex) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = alphabetIndex,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        // Test navigation controllers footer panel
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { viewModel.prevTestQuestion() },
                                enabled = viewModel.currentQuestionIndex > 0
                            ) {
                                Text("Quay lại")
                            }

                            Button(
                                onClick = {
                                    if (viewModel.currentQuestionIndex == viewModel.testQuestions.size - 1) {
                                        viewModel.submitTest()
                                    } else {
                                        viewModel.nextTestQuestion()
                                    }
                                }
                            ) {
                                Text(
                                    if (viewModel.currentQuestionIndex == viewModel.testQuestions.size - 1) "NỘP BÀI THI" else "Câu tiếp theo"
                                )
                            }
                        }
                    }
                } else {
                    // Test results summary screen showing scores and detailed explanations
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("KẾT QUẢ KIỂM TRA NĂNG LỰC", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${viewModel.testScore}%", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                Text("Điểm số", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        if (viewModel.motivatingQuote.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Star, contentDescription = "Zen Quote", tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = viewModel.motivatingQuote,
                                        fontSize = 14.sp,
                                        fontStyle = FontStyle.Italic,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Chúc mừng bạn đã hoàn thành bài Test! Dưới đây là bóc tách lời giải chi tiết kiến thức:", fontSize = 13.sp, textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Loop detailed results list
                        viewModel.testQuestions.forEachIndexed { index, q ->
                            val selectedText = if (viewModel.testAnswers[index] != -1) q.options[viewModel.testAnswers[index]] else "Không trả lời"
                            val correct = viewModel.testAnswers[index] == q.correctAnswer

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (correct) Icons.Default.Check else Icons.Default.Clear,
                                            contentDescription = null,
                                            tint = if (correct) Color(0xFF22C55E) else Color(0xFFEF4444)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Câu số ${index + 1}: ${if (correct) "Chính xác" else "Sai rồi"}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(q.question, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Đáp án đã chọn: $selectedText", fontSize = 11.sp, fontStyle = FontStyle.Italic)
                                    Text("Đáp án đúng: ${q.options[q.correctAnswer]}", fontSize = 11.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Giải thích AI: ${q.explanation}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.closeTest() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("QUAY TRỞ LẠI HỌC FLASHCARD")
                        }
                    }
                }
            }
        }
    }
}
