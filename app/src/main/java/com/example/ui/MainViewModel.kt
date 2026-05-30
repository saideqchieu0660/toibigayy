package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.FlashcardEntity
import com.example.data.database.StudyAnalyticEntity
import com.example.data.database.StudyGroupEntity
import com.example.data.database.GroupMemberEntity
import com.example.data.database.SharedFlashcardEntity
import kotlinx.coroutines.flow.Flow
import com.example.data.repository.FlashcardRepository
import com.example.data.repository.GeminiFlashcardContext
import com.example.data.repository.TestQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import android.util.Log
import com.example.data.repository.MindMapEntity

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = FlashcardRepository(
        database.flashcardDao(),
        database.studyAnalyticDao()
    )

    // User session states
    var loggedInUserEmail by mutableStateOf("")
    var loggedInUserPassword by mutableStateOf("")
    var currentRole by mutableStateOf("") // "student", "teacher_admin", or "" for login screen
    var isDarkTheme by mutableStateOf(true)

    // Flashcard learning states
    val allFlashcards: StateFlow<List<FlashcardEntity>> = repository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyAnalytics: StateFlow<List<StudyAnalyticEntity>> = repository.allAnalytics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active filters
    var selectedSubject by mutableStateOf("All") // "All", "math", "physics", etc.
    var currentCardIndex by mutableStateOf(0)
    var isCardFlipped by mutableStateOf(false)

    // Learning Mode Session states
    var studyModeActive by mutableStateOf(false)
    var studyCardsQueue by mutableStateOf<List<com.example.data.database.FlashcardEntity>>(emptyList())
    var studyCardsReviewQueue by mutableStateOf<List<com.example.data.database.FlashcardEntity>>(emptyList())
    var currentStudyIndex by mutableStateOf(0)
    var knownCount by mutableStateOf(0)
    var showStudySummary by mutableStateOf(false)

    // New flashcard / category popup states
    var showAddSubjectDialog by mutableStateOf(false)
    var showUploadDialog by mutableStateOf(false)
    var showEditCardDialog by mutableStateOf(false)
    var editTargetCard: FlashcardEntity? by mutableStateOf(null)

    // Simulated local custom subjects
    val customSubjects = MutableStateFlow<List<String>>(listOf("math", "physics", "chemistry", "biology", "english", "history", "other"))

    // Filtered card list based on selected category
    val currentCards: StateFlow<List<FlashcardEntity>> = combine(allFlashcards, MutableStateFlow("")) { cards, _ ->
        cards
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Auth screen navigation
    var authScreen by mutableStateOf("login") // "login", "signup", "main"

    // Deep exploration (Agent 2) cooldown and details
    var isAgent2Loading by mutableStateOf(false)
    var aiCooldownRemaining by mutableStateOf(0)
    private var cooldownJob: Job? = null

    // Socratic Chatbot (Agent 3) states
    var isChatOpen by mutableStateOf(false)
    val chatMessages = mutableStateListOf<Pair<String, Boolean>>() // Pair<MessageContent, IsUserText>
    var chatInputText by mutableStateOf("")
    var isTutorResponding by mutableStateOf(false)

    // Practice Ability Test (15 Questions) states
    var isTestActive by mutableStateOf(false)
    var testQuestions by mutableStateOf<List<TestQuestion>>(emptyList())
    var currentQuestionIndex by mutableStateOf(0)
    var selectedTestOption by mutableStateOf(-1)
    val testAnswers = mutableStateListOf<Int>() // indices of student answers
    var testScore by mutableStateOf(0)
    var isTestSubmitted by mutableStateOf(false)
    var testTimeRemaining by mutableStateOf(600) // 10 minutes in seconds
    private var testTimerJob: Job? = null
    var motivatingQuote by mutableStateOf("")
    var showMindMapSheet by mutableStateOf(false)
    var isMindMapLoading by mutableStateOf(false)
    var mindMapData: MindMapEntity? by mutableStateOf(null)

    fun startDynamicMindMap() {
        val topic = if (selectedSubject != "All") selectedSubject else "Khoa học"
        if (mindMapData?.topic == topic) {
            showMindMapSheet = true
            return
        }

        isMindMapLoading = true
        showMindMapSheet = true
        mindMapData = null

        viewModelScope.launch {
            val response = repository.generateMindMap(topic)
            mindMapData = response
            isMindMapLoading = false
        }
    }

    var showQuoteDialog by mutableStateOf(false)
    var isCheckingChunk by mutableStateOf(false)
    var currentLazyLoadingFileName by mutableStateOf("")

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Current list of Study Units (Học phần hiện có)
    val studyUnits = mutableStateListOf<StudyUnit>(
        StudyUnit(
            id = "unit_math_derivative",
            name = "Chương 1: Đạo hàm & Khảo sát hàm số",
            subject = "math",
            status = "cached",
            fileName = "DaoHam_Ch1.docx",
            lastStudiedTime = System.currentTimeMillis() - 1000L * 60 * 60 * 12, // 12 hours ago
            rawContentMock = ""
        ),
        StudyUnit(
            id = "unit_physics_electrostatics",
            name = "Chương 1: Điện tích - Điện trường",
            subject = "physics",
            status = "cached",
            fileName = "DienTinh_Ch1.pdf",
            lastStudiedTime = System.currentTimeMillis() - 1000L * 60 * 60 * 48, // 48 hours ago
            rawContentMock = ""
        ),
        StudyUnit(
            id = "unit_chemistry_ester",
            name = "Chương 1: Ester - Lipit - Xà phòng hóa",
            subject = "chemistry",
            status = "cached",
            fileName = "EsterLipit_Ch1.txt",
            lastStudiedTime = 0L, // Never studied
            rawContentMock = ""
        ),
        StudyUnit(
            id = "unit_english_ielts",
            name = "IELTS Vocabulary: Academic Key Terms",
            subject = "english",
            status = "cached",
            fileName = "IELTS_Vocab_L1.docx",
            lastStudiedTime = System.currentTimeMillis() - 1000L * 60 * 10, // 10 mins ago
            rawContentMock = ""
        ),
        StudyUnit(
            id = "unit_physics_induction",
            name = "Chương 4: Cảm ứng Điện từ (CHUNKING)",
            subject = "physics",
            status = "empty",
            fileName = "CamUngDienTu_Ch4.pdf",
            lastStudiedTime = 0L,
            rawContentMock = """
                [
                  {"front": "Từ thông (Magnetic Flux)", "back": "phi = B.S.cos(alpha). Đại lượng vật lý đặc trưng cho số đường sức từ đi qua một diện tích S kín.", "subject": "physics"},
                  {"front": "Hiện tượng Cảm ứng Điện từ", "back": "Hiện tượng xuất hiện suất điện động cảm ứng khi từ thông qua mạch biến thiên theo thời gian.", "subject": "physics"},
                  {"front": "Định luật Faraday về Cảm ứng từ", "back": "can_ung = - delta_phi / delta_t. Giá trị suất điện động tỉ lệ thuận với tốc độ biến thiên từ thông.", "subject": "physics"},
                  {"front": "Định luật Lenz (Lăng-xơ)", "back": "Dòng điện cảm ứng xuất hiện có chiều sao cho từ trường do nó sinh ra chống lại nguyên nhân sinh ra nó.", "subject": "physics"}
                ]
            """.trimIndent()
        ),
        StudyUnit(
            id = "unit_biology_genetics",
            name = "Học phần 2: Đột biến gen & Giảm phân (CHUNKING)",
            subject = "biology",
            status = "empty",
            fileName = "Biology_Genetics_Ch2.txt",
            lastStudiedTime = 0L,
            rawContentMock = """
                [
                  {"front": "Đột biến gen (Gene Mutation)", "back": "Biến đổi trong cấu trúc gen liên quan đến việc thay thế, thêm hoặc mất một hay một số cặp base-nitơ.", "subject": "biology"},
                  {"front": "Giảm phân (Meiosis)", "back": "Quá trình phân bào đặc biệt tạo ra giao tử có bộ nhiễm sắc thể giảm đi một nửa (từ 2n còn n).", "subject": "biology"},
                  {"front": "Thể đột biến (Mutant)", "back": "Cá thể mang đột biến đã biểu hiện trực tiếp ra kiểu hình của cơ thể hữu cơ.", "subject": "biology"}
                ]
            """.trimIndent()
        )
    )

    fun getForgettingPercentage(lastStudiedTime: Long): Int {
        if (lastStudiedTime == 0L) return 80 // Haven't studied yet (high forget rate)
        val elapsedHrs = (System.currentTimeMillis() - lastStudiedTime) / (1000 * 60 * 60)
        return when {
            elapsedHrs < 1 -> 15  // 15% forgotten in first hour (85% retention)
            elapsedHrs < 6 -> 30  // 30% forgotten
            elapsedHrs < 24 -> 50 // 50% forgotten after 24 hrs
            elapsedHrs < 48 -> 68 // 68% forgotten after 48 hrs
            else -> 78            // Maxes out around 78% forgotten
        }
    }

    fun fetchZenQuoteAndShow() {
        showQuoteDialog = true
        val quotes = listOf(
            "\"The impediment to action advances action. What stands in the way becomes the way.\" — Marcus Aurelius",
            "\"You have power over your mind - not outside events. Realize this, and you will find strength.\" — Marcus Aurelius",
            "\"Limit your 'always' and your 'nevers'.\" — Amy Poehler",
            "\"Nothing diminishes anxiety faster than action.\" — Walter Anderson",
            "\"We suffer more often in imagination than in reality.\" — Seneca",
            "\"He who fears he will suffer, already suffers because he fears.\" — Michel De Montaigne",
            "\"Amateurs sit and wait for inspiration, the rest of us just get up and go to work.\" — Stephen King",
            "\"Focus on the journey, not the destination. Joy is found not in finishing an activity but in doing it.\" — Greg Anderson",
            "\"The only way to do great work is to love what you do.\" — Steve Jobs",
            "\"The secret of getting ahead is getting started.\" — Mark Twain"
        )
        motivatingQuote = quotes.random()
    }

    private suspend fun useRandomFallbackQuote() {
        withContext(Dispatchers.Main) {
            val randomQuote = zenQuotes.random()
            motivatingQuote = "$randomQuote"
        }
    }

    fun triggerStudyUnit(unit: StudyUnit) {
        if (unit.status == "empty") {
            isCheckingChunk = true
            currentLazyLoadingFileName = unit.fileName
            
            // Simulates Agent 1 document reader chunking
            viewModelScope.launch {
                delay(2500) // Delay showing beautiful scan loader
                
                try {
                    val listType = Types.newParameterizedType(List::class.java, Map::class.java)
                    val adapter = moshi.adapter<List<Map<String, Any>>>(listType)
                    val parsedList = adapter.fromJson(unit.rawContentMock) ?: emptyList()
                    
                    val newCards = parsedList.map { map ->
                        FlashcardEntity(
                            front = map["front"]?.toString() ?: "Unnamed concept",
                            back = map["back"]?.toString() ?: "No explanation",
                            subject = map["subject"]?.toString() ?: "other",
                            smDifficulty = 2.5f
                        )
                    }
                    if (newCards.isNotEmpty()) {
                        repository.insertFlashcards(newCards)
                    }
                    
                    // Mark unit as cached!
                    val index = studyUnits.indexOfFirst { it.id == unit.id }
                    if (index >= 0) {
                        studyUnits[index] = studyUnits[index].copy(
                            status = "cached",
                            lastStudiedTime = System.currentTimeMillis()
                        )
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Failed lazy loading chunked cards", e)
                } finally {
                    isCheckingChunk = false
                    currentLazyLoadingFileName = ""
                }
            }
        } else {
            selectedSubject = unit.subject
            currentCardIndex = 0
            
            // Update last studied timestamp
            val index = studyUnits.indexOfFirst { it.id == unit.id }
            if (index >= 0) {
                studyUnits[index] = studyUnits[index].copy(
                    lastStudiedTime = System.currentTimeMillis()
                )
            }
        }
    }

    private val zenQuotes = listOf(
        "Hành trình ngàn dặm bắt đầu từ một bước chân. – Lão Tử",
        "Không có giới hạn nào cho việc học. – Krishnamurti",
        "Sự tập trung là nguồn cội của mọi suy nghĩ sâu sắc.",
        "Sự tĩnh lặng là người thầy vĩ đại nhất.",
        "Tri thức là báu vật, thực hành là chiếc chìa khóa."
    )

    // Prepopulate DB on start if empty
    init {
        viewModelScope.launch {
            // First delay slightly to fetch and ensure we pre-populate empty room DB
            delay(500)
            val list = allFlashcards.value
            repository.checkAndPrepulate(list)
            
            // Add initial friendly greeting in chat bot
            chatMessages.add(Pair("Chào bạn! Mình là Socrates AI Coach. Hãy nhấn vào bất cứ thẻ nào để học chung với mình nha. Bạn có thể sử dụng câu lệnh /quiz để trải nghiệm mini-game hỏi đáp trắc nghiệm lý thú!", false))
        }
    }

    /**
     * User Login handler
     */
    fun performLogin(email: String, psw: String, role: String) {
        if (email.isNotEmpty() && psw.isNotEmpty()) {
            loggedInUserEmail = email
            loggedInUserPassword = psw
            currentRole = role
            authScreen = "main"
        }
    }

    fun logout() {
        currentRole = ""
        loggedInUserEmail = ""
        loggedInUserPassword = ""
        authScreen = "login"
        isTestActive = false
        isChatOpen = false
    }

    /**
     * Handles card edit dialog populating
     */
    fun startEditingCard(card: FlashcardEntity) {
        editTargetCard = card
        showEditCardDialog = true
    }

    fun saveEditedCard(front: String, back: String, subject: String) {
        val target = editTargetCard ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = target.copy(front = front, back = back, subject = subject)
            database.flashcardDao().updateFlashcard(updated)
            withContext(Dispatchers.Main) {
                showEditCardDialog = false
                editTargetCard = null
            }
        }
    }

    fun deleteCard(card: FlashcardEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.flashcardDao().deleteFlashcard(card)
        }
    }

    fun addCustomSubject(subject: String) {
        viewModelScope.launch {
            val formatted = subject.trim().lowercase()
            if (formatted.isNotEmpty() && !customSubjects.value.contains(formatted)) {
                customSubjects.value = customSubjects.value + formatted
            }
            showAddSubjectDialog = false
        }
    }

    fun removeCustomSubject(subject: String) {
        viewModelScope.launch {
            val formatted = subject.trim().lowercase()
            if (customSubjects.value.contains(formatted)) {
                customSubjects.value = customSubjects.value - formatted
                if (selectedSubject.lowercase() == formatted) {
                    selectedSubject = "All"
                    currentCardIndex = 0
                }
            }
        }
    }

    /**
     * Agent 1: Raw document file uploader. Simulate files & triggers Gemini to digest!
     */
    fun simulateFileUpload(fileName: String, content: String) {
        showUploadDialog = false
        isAgent2Loading = true // Shows progress on pipeline
        viewModelScope.launch {
            chatMessages.add(Pair("Đang tải tệp '$fileName' lên hệ thống qua Google Apps Script...", false))
            val fileId = repository.uploadToAppsScript(fileName)
            
            chatMessages.add(Pair("Tải lên hoàn tất (File ID: $fileId). Agent 1 đang tiến hành bóc tách dữ liệu thô...", false))
            val extracted = repository.runAgent1DataExtractor("$fileName (ID: $fileId)", content)
            
            isAgent2Loading = false
            if (extracted.isNotEmpty()) {
                currentCardIndex = 0
                isCardFlipped = false
                chatMessages.add(Pair("Hệ thống đã hoàn tất. Agent 2 đã chuẩn hóa Quizlet-Style và nạp ${extracted.size} thẻ học vào Firestore thành công!", false))
            } else {
                chatMessages.add(Pair("Lỗi trong quá trình Pipeline. Bạn có thể kiểm tra định dạng hoặc thử lại sau nha.", false))
            }
        }
    }

    /**
     * SM-2 repetition trigger with explicit quality ratings (1-5) as requested in the feedback buttons
     */
    fun submitFeedback(card: FlashcardEntity, isMastered: Boolean) {
        viewModelScope.launch {
            val quality = if (isMastered) 5 else 1
            repository.applySM2(card, quality)
            nextCard(card)
        }
    }

    fun submitFeedbackWithQuality(card: FlashcardEntity, quality: Int) {
        viewModelScope.launch {
            repository.applySM2(card, quality)
            nextCard(card)
        }
    }

    fun startStudySession() {
        val cards = getFilteredCards()
        if (cards.isNotEmpty()) {
            studyCardsQueue = cards.shuffled()
            studyCardsReviewQueue = emptyList()
            currentStudyIndex = 0
            knownCount = 0
            showStudySummary = false
            studyModeActive = true
            isCardFlipped = false
        }
    }

    fun submitStudyAnswer(card: FlashcardEntity, knewIt: Boolean) {
        viewModelScope.launch {
            if (knewIt) {
                knownCount++
                repository.applySM2(card, 5) // Kept the db call so cardsLearned gets updated inside
            } else {
                studyCardsReviewQueue = studyCardsReviewQueue + card
                repository.applySM2(card, 1)
            }
            
            isCardFlipped = false
            if (currentStudyIndex < studyCardsQueue.size - 1) {
                currentStudyIndex++
            } else {
                showStudySummary = true
                fetchZenQuoteAndShow() // show quote dialog
            }
        }
    }

    fun continueReviewingTerms() {
        if (studyCardsReviewQueue.isNotEmpty()) {
            studyCardsQueue = studyCardsReviewQueue.shuffled()
            studyCardsReviewQueue = emptyList()
            currentStudyIndex = 0
            knownCount = 0
            showStudySummary = false
            isCardFlipped = false
        } else {
            // Nothing to review, back to normal list
            studyModeActive = false
        }
    }

    fun restartStudySession() {
        startStudySession()
    }
    
    fun quitStudySession() {
        studyModeActive = false
    }

    private fun nextCard(currentCard: FlashcardEntity) {
        isCardFlipped = false
        val cards = getFilteredCards()
        if (cards.isNotEmpty()) {
            val currentIndexInBounds = currentCardIndex.coerceIn(0, cards.size - 1)
            if (currentIndexInBounds == cards.size - 1) {
                currentCardIndex = 0
            } else {
                currentCardIndex = currentIndexInBounds + 1
            }
        } else {
            currentCardIndex = 0
        }
    }

    fun prevCard() {
        isCardFlipped = false
        val cards = getFilteredCards()
        if (cards.isNotEmpty()) {
            val currentIndexInBounds = currentCardIndex.coerceIn(0, cards.size - 1)
            currentCardIndex = if (currentIndexInBounds == 0) cards.size - 1 else currentIndexInBounds - 1
        } else {
            currentCardIndex = 0
        }
    }

    fun nextCard() {
        isCardFlipped = false
        val cards = getFilteredCards()
        if (cards.isNotEmpty()) {
            val currentIndexInBounds = currentCardIndex.coerceIn(0, cards.size - 1)
            currentCardIndex = (currentIndexInBounds + 1) % cards.size
        } else {
            currentCardIndex = 0
        }
    }

    fun getFilteredCards(): List<FlashcardEntity> {
        val list = allFlashcards.value
        return if (selectedSubject == "All") {
            list
        } else if (selectedSubject == "weak_cards") {
            list.filter { !it.mastered }
        } else {
            list.filter { it.subject.lowercase() == selectedSubject.lowercase() }
        }
    }

    fun getActiveCard(): FlashcardEntity? {
        val cards = getFilteredCards()
        if (cards.isEmpty()) return null
        return cards.getOrNull(currentCardIndex) ?: cards.firstOrNull()
    }

    /**
     * Agent 2: Cooldown triggered "Bóc Tách Sâu"
     */
    fun triggerAgent2DeepExplain() {
        val activeCard = getActiveCard() ?: return
        if (aiCooldownRemaining > 0 || isAgent2Loading) return

        isAgent2Loading = true
        startCooldownTimer()

        viewModelScope.launch {
            repository.runAgent2DeepExplainer(activeCard)
            isAgent2Loading = false
        }
    }

    private fun startCooldownTimer() {
        cooldownJob?.cancel()
        aiCooldownRemaining = 20
        cooldownJob = viewModelScope.launch {
            while (aiCooldownRemaining > 0) {
                delay(1000)
                aiCooldownRemaining--
            }
        }
    }

    /**
     * Agent 3: Chat with Socratic Tutor
     */
    fun sendChatMessage() {
        val text = chatInputText.trim()
        if (text.isEmpty() || isTutorResponding || aiCooldownRemaining > 0) return

        chatMessages.add(Pair(text, true))
        chatInputText = ""
        isTutorResponding = true

        val activeCard = getActiveCard()
        val cardContext = activeCard?.let {
            GeminiFlashcardContext(front = it.front, back = it.back, subject = it.subject)
        }

        startCooldownTimer()

        viewModelScope.launch {
            if (text.equals("/quiz", ignoreCase = true)) {
                // Fetch up to 3 weak scoring cards for Trivia game
                val weakCards = database.flashcardDao().getWeakestFlashcards().take(3)
                val quizResponse = repository.generateQuizMiniGame(weakCards)
                chatMessages.add(Pair(quizResponse, false))
            } else {
                // Socratic answering incorporating card context frames
                val reply = repository.runAgent3SocraticTutor(
                    chatHistory = chatMessages.toList(),
                    userInput = text,
                    activeCard = cardContext
                )
                chatMessages.add(Pair(reply, false))
            }
            isTutorResponding = false
        }
    }

    /**
     * Ability test CTA: 15 questions
     */
    fun startAbilityTest() {
        if (isAgent2Loading) return
        isAgent2Loading = true
        viewModelScope.launch {
            val weakCards = database.flashcardDao().getWeakestFlashcards()
            val questions = repository.generateAbilityTest(weakCards)
            isAgent2Loading = false

            if (questions.isNotEmpty()) {
                testQuestions = questions
                currentQuestionIndex = 0
                testAnswers.clear()
                testAnswers.addAll(List(questions.size) { -1 })
                selectedTestOption = -1
                isTestActive = true
                isTestSubmitted = false
                testScore = 0
                testTimeRemaining = 600 // 10 minutes countdown
                startTestTimer()
            } else {
                // Fallback structured questions if AI endpoint hits quota or is offline
                testQuestions = generateFallbackQuizQuestions()
                currentQuestionIndex = 0
                testAnswers.clear()
                testAnswers.addAll(List(testQuestions.size) { -1 })
                selectedTestOption = -1
                isTestActive = true
                isTestSubmitted = false
                testScore = 0
                testTimeRemaining = 600
                startTestTimer()
            }
        }
    }

    private fun startTestTimer() {
        testTimerJob?.cancel()
        testTimerJob = viewModelScope.launch {
            while (testTimeRemaining > 0 && isTestActive && !isTestSubmitted) {
                delay(1000)
                testTimeRemaining--
            }
            if (testTimeRemaining == 0 && isTestActive && !isTestSubmitted) {
                submitTest()
            }
        }
    }

    fun pickTestOption(optionIndex: Int) {
        if (isTestSubmitted) return
        selectedTestOption = optionIndex
        testAnswers[currentQuestionIndex] = optionIndex
    }

    fun nextTestQuestion() {
        if (currentQuestionIndex < testQuestions.size - 1) {
            currentQuestionIndex++
            selectedTestOption = testAnswers[currentQuestionIndex]
        }
    }

    fun prevTestQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--
            selectedTestOption = testAnswers[currentQuestionIndex]
        }
    }

    fun submitTest() {
        testTimerJob?.cancel()
        var correctCount = 0
        testQuestions.forEachIndexed { idx, q ->
            val userAns = testAnswers.getOrNull(idx) ?: -1
            if (userAns == q.correctAnswer) {
                correctCount++
            }
        }
        testScore = if (testQuestions.isNotEmpty()) {
            ((correctCount.toFloat() / testQuestions.size.toFloat()) * 100).toInt()
        } else {
            0
        }
        isTestSubmitted = true
        motivatingQuote = zenQuotes.random()

        // Push study analytic item for learning test complete
        viewModelScope.launch {
            repository.applySM2(
                FlashcardEntity(front = "Làm bài kiểm tra năng lực", back = "", subject = "other"),
                if (correctCount >= 10) 5 else 2
            )
        }
    }

    fun closeTest() {
        isTestActive = false
        testQuestions = emptyList()
    }

    private fun generateFallbackQuizQuestions(): List<TestQuestion> {
        return listOf(
            TestQuestion(
                question = "Định luật I Newton (Định luật Quán tính) phát biểu gì?",
                options = listOf(
                    "Mọi vật đều hút nhau bằng lực tỉ lệ thuận với khối lượng.",
                    "Một vật cô lập sẽ giữ nguyên trạng thái đứng yên hoặc chuyển động thẳng đều nếu không bị tác dụng lực.",
                    "Gia tốc tỉ lệ thuận với lực tác dụng và tỉ lệ nghịch với khối lượng.",
                    "Lực tác dụng luôn bằng phản lực."
                ),
                correctAnswer = 1,
                explanation = "Theo định luật I Newton, một vật không chịu tác dụng của lực nào hoặc chịu tác dụng của các lực cân bằng sẽ đứng yên hoặc chuyển động thẳng đều mãi mãi."
            ),
            TestQuestion(
                question = "Trong phản ứng quang hợp, phân tử nào hấp thụ ánh sáng để cung cấp năng lượng?",
                options = listOf("Mitochoria", "Glucose", "Chlorophyll (Diệp lục)", "Carbon Dioxide"),
                correctAnswer = 2,
                explanation = "Chlorophyll (chất diệp lục) nằm trong lục lạp là sắc tố chính hấp thụ năng lượng mặt trời để khởi động chuỗi phản ứng hóa học."
            ),
            TestQuestion(
                question = "Hằng số toán học liên kết trực tiếp giữa chu vi và đường kính đường tròn là gì?",
                options = listOf("e", "Phi (tỷ lệ vàng)", "i", "Pi (3.14159...)"),
                correctAnswer = 3,
                explanation = "Mối quan hệ tỷ lệ giữa chu vi và đường kính của bất kỳ hình tròn nào luôn bằng hằng số Pi."
            ),
            TestQuestion(
                question = "Nguyên tố hóa học nào dồi dào nhất trong vũ trụ?",
                options = listOf("Hydrogen", "Oxygen", "Helium", "Carbon"),
                correctAnswer = 0,
                explanation = "Hydrogen chiếm khoảng 75% lượng vật chất nhẹ trong toàn vũ trụ, sau đó đến Helium."
            ),
            TestQuestion(
                question = "Công thức liên hệ bước sóng, tần số và vận tốc truyền sóng là gì?",
                options = listOf("λ = v * f", "λ = v / f", "λ = f / v", "λ = 1 / (v * f)"),
                correctAnswer = 1,
                explanation = "Bước sóng λ tỷ lệ thuận với vận tốc truyền v và tỉ lệ nghịch với tần số f."
            )
        )
    }

    /**
     * Admin quick trigger to build a fresh sample card!
     */
    fun quickCreateFlashcard(front: String, back: String, subject: String) {
        viewModelScope.launch {
            val card = FlashcardEntity(front = front, back = back, subject = subject)
            repository.insertFlashcard(card)
        }
    }

    // ==========================================
    // STUDY GROUP OPERATIONS & PERSISTENCE
    // ==========================================
    var selectedGroupCode by mutableStateOf("")
    var groupErrorMsg by mutableStateOf("")

    val studyGroups: StateFlow<List<StudyGroupEntity>> = database.studyGroupDao().getAllGroupsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getGroupMembersFlow(code: String): Flow<List<GroupMemberEntity>> {
        return database.studyGroupDao().getMembersByGroupFlow(code)
    }

    fun getSharedFlashcardsFlow(code: String): Flow<List<SharedFlashcardEntity>> {
        return database.studyGroupDao().getSharedFlashcardsFlow(code)
    }

    fun createStudyGroup(name: String, description: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            groupErrorMsg = "Tên nhóm không được để trống"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val randomSuffix = (100..999).random().toString()
            val code = trimmedName.take(3).uppercase() + randomSuffix
            val newGroup = StudyGroupEntity(
                name = trimmedName,
                description = description.trim(),
                code = code,
                ownerEmail = loggedInUserEmail
            )
            database.studyGroupDao().insertGroup(newGroup)
            
            // Auto add the creator as member with initial points
            val currentUsername = loggedInUserEmail.substringBefore("@")
            val creatorMember = GroupMemberEntity(
                groupCode = code,
                userEmail = loggedInUserEmail,
                userName = currentUsername,
                points = 120
            )
            database.studyGroupDao().insertMember(creatorMember)

            // Seed 2 simulated members to foster friendly competition right away!
            val simulatedFriends = listOf(
                Pair("Thùy Linh", "linh.stemed@gmail.com"),
                Pair("Đức Anh", "anhduc.stem@gmail.com")
            )
            for (friend in simulatedFriends) {
                database.studyGroupDao().insertMember(
                    GroupMemberEntity(
                        groupCode = code,
                        userEmail = friend.second,
                        userName = friend.first,
                        points = (40..100).random()
                    )
                )
            }

            withContext(Dispatchers.Main) {
                selectedGroupCode = code
                groupErrorMsg = ""
            }
        }
    }

    fun joinStudyGroup(code: String) {
        val trimmedCode = code.trim().uppercase()
        if (trimmedCode.isEmpty()) {
            groupErrorMsg = "Vui lòng nhập mã nhóm"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val group = database.studyGroupDao().getGroupByCode(trimmedCode)
            if (group == null) {
                withContext(Dispatchers.Main) {
                    groupErrorMsg = "Mã nhóm không tồn tại"
                }
            } else {
                val existing = database.studyGroupDao().getMemberByGroupAndEmail(trimmedCode, loggedInUserEmail)
                if (existing == null) {
                    val currentUsername = loggedInUserEmail.substringBefore("@")
                    database.studyGroupDao().insertMember(
                        GroupMemberEntity(
                            groupCode = trimmedCode,
                            userEmail = loggedInUserEmail,
                            userName = currentUsername,
                            points = 0
                        )
                    )
                }
                withContext(Dispatchers.Main) {
                    selectedGroupCode = trimmedCode
                    groupErrorMsg = ""
                }
            }
        }
    }

    fun inviteFriend(groupCode: String, name: String, email: String) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim().lowercase()
        if (trimmedName.isEmpty() || trimmedEmail.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            database.studyGroupDao().insertMember(
                GroupMemberEntity(
                    groupCode = groupCode,
                    userEmail = trimmedEmail,
                    userName = trimmedName,
                    points = (50..110).random()
                )
            )
        }
    }

    fun shareCardToGroup(groupCode: String, card: FlashcardEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val sharedCard = SharedFlashcardEntity(
                groupCode = groupCode,
                front = card.front,
                back = card.back,
                subject = card.subject,
                sharedBy = loggedInUserEmail
            )
            database.studyGroupDao().insertSharedFlashcard(sharedCard)

            // Reward sharing user +15 points!
            val member = database.studyGroupDao().getMemberByGroupAndEmail(groupCode, loggedInUserEmail)
            if (member != null) {
                database.studyGroupDao().insertMember(member.copy(points = member.points + 15))
            }
        }
    }

    fun studyGroupCardCompleted(groupCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val member = database.studyGroupDao().getMemberByGroupAndEmail(groupCode, loggedInUserEmail)
            if (member != null) {
                database.studyGroupDao().insertMember(member.copy(points = member.points + 20))
            }
        }
    }
}

data class StudyUnit(
    val id: String,
    val name: String,
    val subject: String,
    val status: String, // "cached" or "empty"
    val fileName: String,
    val lastStudiedTime: Long,
    val rawContentMock: String
)
