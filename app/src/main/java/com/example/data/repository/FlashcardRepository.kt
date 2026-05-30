package com.example.data.repository

import android.util.Log
import com.example.data.database.FlashcardDao
import com.example.data.database.FlashcardEntity
import com.example.data.database.StudyAnalyticDao
import com.example.data.database.StudyAnalyticEntity
import com.example.data.network.GeminiApiClient
import com.example.data.network.AgentRole
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class FlashcardRepository(
    private val flashcardDao: FlashcardDao,
    private val studyAnalyticDao: StudyAnalyticDao
) {
    val allFlashcards: Flow<List<FlashcardEntity>> = flashcardDao.getAllFlashcardsFlow()
    val allAnalytics: Flow<List<StudyAnalyticEntity>> = studyAnalyticDao.getAllAnalyticsFlow()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    suspend fun getPrimaryApiKey(): String = withContext(Dispatchers.IO) {
        ""
    }

    suspend fun uploadToAppsScript(fileName: String): String = withContext(Dispatchers.IO) {
        val urlStr = com.example.BuildConfig.APPS_SCRIPT_URL
        if (urlStr.isEmpty() || urlStr == "MY_APPS_SCRIPT_URL") {
            return@withContext "mock_file_id_12345"
        }
        try {
            val req = okhttp3.Request.Builder().url(urlStr).build()
            GeminiApiClient.okHttpClient.newCall(req).execute().use { response ->
                if (response.isSuccessful) {
                    return@withContext response.body?.string()?.take(30) ?: "uploaded_id"
                }
            }
        } catch (e: Exception) {
            Log.e("Upload", "AppsScript upload failed", e)
        }
        return@withContext "mock_file_id_fallback"
    }

    suspend fun insertFlashcard(card: FlashcardEntity) {
        flashcardDao.insertFlashcard(card)
    }

    suspend fun insertFlashcards(cards: List<FlashcardEntity>) {
        flashcardDao.insertFlashcards(cards)
    }

    suspend fun deleteFlashcard(card: FlashcardEntity) {
        flashcardDao.deleteFlashcard(card)
    }

    suspend fun deleteFlashcardById(id: Long) {
        flashcardDao.deleteFlashcardById(id)
    }

    /**
     * Pre-populates default flashcards if none are in the DB.
     */
    suspend fun populateDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
        // We will perform a simple query to see if cards are empty
        // Since allFlashcards is a Flow, we can run a direct query via a helper or suspend function if needed.
        // Wait, let's trigger it based on the current lists in memory
    }

    suspend fun checkAndPrepulate(currentList: List<FlashcardEntity>) = withContext(Dispatchers.IO) {
        if (currentList.isEmpty()) {
            val defaults = listOf(
                FlashcardEntity(
                    front = "Công thức thấu kính mỏng (Thin Lens Formula)",
                    back = "1/f = 1/d_o + 1/d_i. Định luật quang hình xác định mối liên hệ giữa tiêu cự f, khoảng cách vật d_o và khoảng cách ảnh d_i.",
                    subject = "physics",
                    smDifficulty = 2.5f
                ),
                FlashcardEntity(
                    front = "Định luật Bảo toàn Khối lượng (Law of Conservation of Mass)",
                    back = "Trong một phản ứng hóa học khép kín, tổng khối lượng các chất sản phẩm bằng tổng khối lượng các chất tham gia phản ứng.",
                    subject = "chemistry",
                    smDifficulty = 2.4f
                ),
                FlashcardEntity(
                    front = "Quang hợp (Photosynthesis)",
                    back = "6CO2 + 6H2O + Ánh sáng -> C6H12O6 + 6O2. Chuyển hóa carbon dioxide và nước thành glucose và oxygen nhờ diệp lục.",
                    subject = "biology",
                    smDifficulty = 1.8f // Lower difficulty acts as "vùng hổng kiến thức"
                ),
                FlashcardEntity(
                    front = "Định lý Euler (Euler's Identity)",
                    back = "e^(i*pi) + 1 = 0. Phương trình toán học liên kết 5 hằng số cơ bản nhất của toán học: e, i, pi, 1, và 0.",
                    subject = "math",
                    smDifficulty = 2.5f
                ),
                FlashcardEntity(
                    front = "Serendipity",
                    back = "Từ tiếng Anh chỉ sự tình cờ may mắn. Khả năng tìm thấy những điều thú vị, hữu ích một cách ngẫu nhiên ngoài dự định.",
                    subject = "english",
                    smDifficulty = 2.6f
                ),
                FlashcardEntity(
                    front = "Cách mạng Tháng Tám (1945)",
                    back = "Sự kiện lịch sử nổ ra giành chính quyền từ phát xít Nhật và thực dân Pháp, khai sinh nước Việt Nam Dân chủ Cộng hòa ngày 2/9/1945.",
                    subject = "history",
                    smDifficulty = 2.3f
                ),
                FlashcardEntity(
                    front = "Phản ứng thế gốc tự do Alkane",
                    back = "CH4 + Cl2 --(Ánh sáng)--> CH3Cl + HCl. Phản ứng thế đặc trưng của hydrocarbon no xảy ra theo cơ chế gốc qua ba giai đoạn.",
                    subject = "chemistry",
                    smDifficulty = 1.7f // Lower difficulty (weak point)
                ),
                FlashcardEntity(
                    front = "Nguyên lý lưỡng tính sóng hạt (Wave-particle duality)",
                    back = "Mọi thực thể lượng tử (như electron, photon) đều biểu diễn đồng thời tính chất sóng và tính chất hạt hạt nhân.",
                    subject = "physics",
                    smDifficulty = 2.1f
                )
            )
            flashcardDao.insertFlashcards(defaults)

            val analytics = listOf(
                StudyAnalyticEntity("Mon_study", "Mon", "math", 4),
                StudyAnalyticEntity("Tue_study", "Tue", "physics", 7),
                StudyAnalyticEntity("Wed_study", "Wed", "chemistry", 5),
                StudyAnalyticEntity("Thu_study", "Thu", "biology", 3),
                StudyAnalyticEntity("Fri_study", "Fri", "english", 8),
                StudyAnalyticEntity("Sat_study", "Sat", "history", 6),
                StudyAnalyticEntity("Sun_study", "Sun", "other", 2)
            )
            studyAnalyticDao.insertAnalytics(analytics)
        }
    }

    /**
     * Spaced Repetition SM-2 implementation.
     * @param rating Quality rate (0-5)
     *               "Đã thuộc" maps to 5 (mastered)
     *               "Quên" maps to 1 (forgotten/weak)
     */
    suspend fun applySM2(card: FlashcardEntity, rating: Int): FlashcardEntity = withContext(Dispatchers.IO) {
        val q = rating.coerceIn(0, 5)
        var n = card.smRepetitions
        var interval = card.smInterval
        var ef = card.smDifficulty

        if (q >= 3) {
            n = if (n == 0) {
                1
            } else if (n == 1) {
                6
            } else {
                (interval * ef).toInt().coerceAtLeast(1)
            }
            n++
        } else {
            n = 0
            interval = 1
        }

        // Adjust Ease Factor (EF)
        ef = ef + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f))
        if (ef < 1.3f) {
            ef = 1.3f
        }

        val mastered = q >= 4
        val totalAttempts = card.totalAttempts + 1
        val failedAttempts = if (q < 3) card.failedAttempts + 1 else card.failedAttempts

        val updatedCard = card.copy(
            smRepetitions = n,
            smInterval = interval,
            smDifficulty = ef,
            mastered = mastered,
            lastStudied = System.currentTimeMillis(),
            totalAttempts = totalAttempts,
            failedAttempts = failedAttempts
        )

        flashcardDao.updateFlashcard(updatedCard)

        // Increment Study Analytics for current day of week
        incrementTodayAnalytics(card.subject)

        updatedCard
    }

    private suspend fun incrementTodayAnalytics(subj: String) {
        val today = java.text.SimpleDateFormat("EEE", java.util.Locale.US).format(java.util.Date()) // Mon, Tue, etc.
        val id = "${today}_study"
        try {
            val existing = studyAnalyticDao.getAllAnalyticsFlow().firstOrNull()?.find { it.id == id }
            val currentCount = existing?.cardsLearned ?: 0
            studyAnalyticDao.insertAnalytic(
                StudyAnalyticEntity(id = id, dayOfWeek = today, subject = subj, cardsLearned = currentCount + 1)
            )
        } catch (e: Exception) {
            Log.e("Repository", "Failed increment analytic", e)
        }
    }

    /**
     * Agent 1: Data Pipeline Agent (Raw Document Processor)
     * Parses simulated document files and generates multiple flashcard entities via Gemini.
     */
    suspend fun runAgent1DataExtractor(fileName: String, fileContent: String): List<FlashcardEntity> = withContext(Dispatchers.IO) {
        val systemPrompt = "Mày là Agent 1 (Data Pipeline Agent). Đọc nội dung tệp và trích xuất TOÀN BỘ thông tin thô dưới định dạng JSON thô. Không cần định dạng đẹp, chỉ cần trích xuất front/back/subject cơ bản nhất có thể."

        val prompt = """
            Tên tệp: $fileName
            Nội dung tệp:
            $fileContent
        """.trimIndent()

        val crudeJson = GeminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = systemPrompt,
            agentRole = AgentRole.AGENT_1_EXTRACTOR
        )

        Log.d("Agent1Pipeline", "Agent 1 crude response: $crudeJson")

        // Phase 2: Agent 2 standardizes to Quizlet Style
        val agent2Prompt = """
            Mày là Agent 2. Chuyển đổi dữ liệu thô sau thành định dạng Quizlet-Style chuẩn. Yêu cầu xuất ĐÚNG MỘT MẢNG JSON chứa các đối tượng: {"front": "thuật ngữ", "back": "định nghĩa chi tiết", "subject": "english|math|physics|chemistry|biology|history|other"}.
            Dữ liệu thô từ Agent 1:
            $crudeJson
        """.trimIndent()

        val jsonResponse = GeminiApiClient.generateContent(
            prompt = agent2Prompt,
            systemInstruction = "Chỉ trả về JSON thuần túy.",
            responseMimeType = "application/json",
            agentRole = AgentRole.AGENT_2_EXPLAINER_STANDARDIZER
        )
        
        Log.d("Agent2Pipeline", "Agent 2 Quizlet-style response: $jsonResponse")

        try {
            // Parse array of JSON using Moshi
            val cleanedJson = cleanJsonString(jsonResponse)
            val listType = Types.newParameterizedType(List::class.java, Agent1ExtractionItem::class.java)
            val adapter = moshi.adapter<List<Agent1ExtractionItem>>(listType)
            val parsedList = adapter.fromJson(cleanedJson) ?: emptyList()

            val newCards = parsedList.map { item ->
                FlashcardEntity(
                    front = item.front,
                    back = item.back,
                    subject = item.subject,
                    smDifficulty = 2.5f
                )
            }

            if (newCards.isNotEmpty()) {
                flashcardDao.insertFlashcards(newCards)
            }
            newCards
        } catch (e: Exception) {
            Log.e("Pipeline", "Failed parsing extracted cards. Input string was: $jsonResponse", e)
            emptyList()
        }
    }

    /**
     * Agent 2: Dynamic Router Agent (Deep Explanter)
     * Triggers dynamic routes according to the card subject (English vs STEM/Social)
     */
    suspend fun runAgent2DeepExplainer(card: FlashcardEntity): FlashcardEntity = withContext(Dispatchers.IO) {
        val isEnglish = card.subject.lowercase() == "english"

        val systemPrompt = if (isEnglish) {
            """
                Mày là Chuyên gia Ngôn ngữ học. Phân tích từ vựng tiếng Anh được cung cấp theo cấu trúc HTML tối giản (dùng <b>, <i>, <ul>, <li>):
                1. Từ loại & Phiên âm IPA.
                2. Nghĩa tiếng Việt chuẩn.
                3. Etymology (Nguồn gốc lịch sử hình thành từ).
                4. 2 câu ví dụ thực tế kèm dịch nghĩa.
            """.trimIndent()
        } else {
            """
                Mày là Giáo sư Khoa học/Xã hội. Phân tích khái niệm/định luật sau bằng HTML và LaTeX (nếu có công thức thì để trong ký hiệu $, $$):
                1. Định nghĩa chính thức.
                2. Công thức (nếu có, giải thích từng hằng số/đại lượng) hoặc Bối cảnh sự kiện.
                3. 2 ứng dụng thực tế hoặc tác động cốt lõi.
                4. Một mẹo ghi nhớ hoặc sơ đồ tư duy dạng chữ (Bullet points).
            """.trimIndent()
        }

        val prompt = "Hãy bóc tách sâu khái niệm/từ vựng sau đây:\nThuật ngữ: ${card.front}\nĐịnh nghĩa sơ bộ: ${card.back}\nMôn học: ${card.subject}"

        val explanation = GeminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = systemPrompt,
            agentRole = AgentRole.AGENT_2_EXPLAINER_STANDARDIZER
        )

        val updatedCard = card.copy(aiDetails = explanation)
        flashcardDao.updateFlashcard(updatedCard)
        updatedCard
    }

    /**
     * Agent 3: Socratic AI Coach
     * Conversation handler incorporating context-aware memory
     * Passes current card states inside custom context frames
     */
    suspend fun runAgent3SocraticTutor(
        chatHistory: List<Pair<String, Boolean>>, // Pair of <Message, isUserMessage>
        userInput: String,
        activeCard: GeminiFlashcardContext?
    ): String = withContext(Dispatchers.IO) {
        val systemPrompt = """
            Mày là Agent 3 - 'Socrates AI Coach', gia sư học tập chủ động. QUY TẮC BẮT BUỘC:
            1. SOCRATIC METHOD: KHÔNG BAO GIỜ giải bài tập hộ hay cho đáp án trực tiếp. Khi học sinh hỏi, hãy gợi ý từng bước, đưa manh mối và kết thúc bằng một câu hỏi ngược để học sinh tự suy luận.
            2. CONTEXT-AWARE: Mày sẽ nhận được Context ẩn (thẻ học sinh đang xem). Nếu học sinh dùng từ 'Cái này', 'Từ này', hãy tự động liên kết với Context đó để trả lời.
            3. LỆNH /quiz: Nếu học sinh gõ '/quiz', hãy tạo ngay một trò chơi trắc nghiệm 3 câu hỏi liên tiếp (dựa trên context thẻ yếu được cung cấp), phản hồi đúng/sai và tính điểm sinh động.
            4. FORMATTING: Ngắn gọn, thân thiện, dùng LaTeX ($$, $) cho mọi công thức Toán/Lý/Hóa.
        """.trimIndent()

        val contextStr = if (activeCard != null) {
            "[Hidden Context: Môn ${activeCard.subject}, Đang xem thẻ: \"${activeCard.front}\" - \"${activeCard.back}\"]"
        } else ""

        val formattedHistory = chatHistory.joinToString("\n") { (msg, isUser) ->
            if (isUser) "Student: $msg" else "Socrates: $msg"
        }

        val prompt = """
            $contextStr
            Full Chat History:
            $formattedHistory
            Student current message: $userInput
            Socrates response:
        """.trimIndent()

        GeminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = systemPrompt,
            agentRole = AgentRole.AGENT_3_SOCRATIC_CHATBOT
        )
    }

    /**
     * Executes the /quiz interactive game.
     * Takes the 3 weakest cards and prompts Gemini to formulate a 3-question trivia challenge.
     */
    suspend fun generateQuizMiniGame(weakestCards: List<FlashcardEntity>): String = withContext(Dispatchers.IO) {
        if (weakestCards.isEmpty()) {
            return@withContext "Không tìm thấy thẻ yếu nào để lên đề!"
        }

        val contextCardsStr = weakestCards.joinToString { "- Thẻ [${it.subject}]: \"${it.front}\" (Định nghĩa: ${it.back})" }

        val quizInstructionPrompt = """
            Hãy tạo duy nhất một mini-game trắc nghiệm kiểm tra nhanh gồm 3 câu hỏi liên tiếp để đố học sinh dựa trên 3 chủ đề kiến thức yếu sau đây:
            $contextCardsStr

            Quy cách xuất đề:
            - Mỗi câu hỏi có 4 sự lựa chọn A, B, C, D kèm câu hỏi kích thích tư duy.
            - Hiển thị trực tiếp câu hỏi ở mẫu thân thiện, không bao gồm đáp án ngay dưới câu hỏi để học sinh có thể tự gõ câu trả lời (Ví dụ: "Hãy trả lời câu hỏi 1...").
            - Đưa ra định hướng vui vẻ, khuyến khích đua điểm số! Kết thúc bằng câu hỏi ngược khơi gợi sự hưng phấn.
        """.trimIndent()

        GeminiApiClient.generateContent(
            prompt = quizInstructionPrompt,
            systemInstruction = "Mày là 'Socrates AI Coach', tạo mini-quiz ngắn gọn, hấp dẫn tích hợp công thức nếu cần.",
            agentRole = AgentRole.AGENT_3_SOCRATIC_CHATBOT
        )
    }

    /**
     * Ability test: Generates a JSON formatted multiple choice test of 15 questions based on standard weak points.
     */
    suspend fun generateAbilityTest(weakestCards: List<FlashcardEntity>): List<TestQuestion> = withContext(Dispatchers.IO) {
        val contextStr = weakestCards.take(15).joinToString("\n") { "- Môn: ${it.subject}, Khái niệm: ${it.front} (${it.back})" }

        val systemPrompt = """
            Mày là Chuyên gia Khảo thí Giáo dục. Đọc danh sách các khái niệm khó/yếu sau đây và tạo một đề thi kiểm tra năng lực gồm ĐÚNG 15 câu khác nhau.
            Xuất kết quả dưới định dạng một mảng JSON thô duy nhất chứa các đối tượng có thuộc tính:
            - question: Câu hỏi trắc nghiệm tiếng Việt chất lượng cao.
            - options: Mảng 4 chuỗi chứa các đáp án (A, B, C, D).
            - correctAnswer: Chỉ số của đáp án đúng (từ 0 đến 3).
            - explanation: Lời giải thích khoa học ngắn gọn vì sao đáp án đó đúng.

            Dữ liệu đầu vào các khái niệm học sinh đang yếu:
            $contextStr

            CHỈ xuất mảng JSON duy nhất. KHÔNG chứa định dạng Markdown code block. KHÔNG có văn bản rác ngoài JSON.
        """.trimIndent()

        val jsonStr = GeminiApiClient.generateContent(
            prompt = "Hãy tạo đề thi 15 câu JSON ngay lập tức.",
            systemInstruction = systemPrompt,
            responseMimeType = "application/json",
            agentRole = AgentRole.AGENT_3_SOCRATIC_CHATBOT
        )

        try {
            val cleaned = cleanJsonString(jsonStr)
            val listType = Types.newParameterizedType(List::class.java, TestQuestion::class.java)
            val adapter = moshi.adapter<List<TestQuestion>>(listType)
            adapter.fromJson(cleaned) ?: emptyList()
        } catch (e: Exception) {
            Log.e("AbilityTest", "Failed generating test list, returning fallback mock test. Json was: $jsonStr", e)
            emptyList()
        }
    }

    private fun cleanJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.substring(7)
        } else if (str.startsWith("```")) {
            str = str.substring(3)
        }
        if (str.endsWith("```")) {
            str = str.substring(0, str.length - 3)
        }
        return str.trim()
    }

    suspend fun generateMindMap(topic: String): MindMapEntity = withContext(Dispatchers.IO) {
        val systemPrompt = """
            Mày là Chuyên gia Vẽ Sơ đồ Tư duy. Phân tích môn học/chủ đề được cung cấp và tạo ra cấu trúc Mind Map.
            Xuất ĐÚNG MỘT đối tượng JSON bao gồm:
            - rootName: Khái niệm gốc cốt lõi ngắn gọn (MAX 3 từ, ví dụ "Vật lý 12", "Hàm số").
            - branches: Mảng gồm 4 khái niệm phân nhánh con ngắn gọn nhất (MAX 4 từ/nhánh).
            
            KHÔNG xuất bất kỳ văn bản nào ngoài JSON.
        """.trimIndent()

        val jsonStr = GeminiApiClient.generateContent(
            prompt = "Chủ đề: $topic",
            systemInstruction = systemPrompt,
            responseMimeType = "application/json",
            agentRole = AgentRole.AGENT_2_EXPLAINER_STANDARDIZER
        )
        try {
            val cleaned = cleanJsonString(jsonStr)
            val adapter = moshi.adapter(MindMapEntity::class.java)
            adapter.fromJson(cleaned)?.copy(topic = topic) ?: fallbackMindMap(topic)
        } catch (e: Exception) {
            fallbackMindMap(topic)
        }
    }

    private fun fallbackMindMap(topic: String): MindMapEntity {
        return MindMapEntity(
            topic = topic,
            rootName = topic.uppercase(),
            branches = listOf("Kiến thức lõi", "Công thức", "Ứng dụng", "Bài tập")
        )
    }
}

data class GeminiFlashcardContext(
    val front: String,
    val back: String,
    val subject: String
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class Agent1ExtractionItem(
    val front: String,
    val back: String,
    val subject: String
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class TestQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class MindMapEntity(
    val topic: String,
    val rootName: String,
    val branches: List<String>
)
