package com.example.data.network

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import android.util.Log

enum class AgentRole {
    AGENT_1_EXTRACTOR,
    AGENT_2_EXPLAINER_STANDARDIZER,
    AGENT_3_SOCRATIC_CHATBOT
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Executes content generation calling the Gemini REST API.
     */
    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null,
        responseMimeType: String? = null,
        temperature: Float? = null,
        agentRole: AgentRole = AgentRole.AGENT_1_EXTRACTOR
    ): String {
        val key = when (agentRole) {
            AgentRole.AGENT_1_EXTRACTOR -> BuildConfig.GEMINI_API_KEY
            AgentRole.AGENT_2_EXPLAINER_STANDARDIZER -> BuildConfig.Agent2
            AgentRole.AGENT_3_SOCRATIC_CHATBOT -> BuildConfig.Agent3
        }

        if (key.isEmpty() || key == "MY_GEMINI_API_KEY" || key == "MY_AGENT2_API_KEY" || key == "MY_AGENT3_API_KEY") {
            return "API Key is empty. Please configure keys in the Secrets Panel in AI Studio."
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            systemInstruction = systemInstruction?.let {
                GeminiContent(parts = listOf(GeminiPart(text = it)))
            },
            generationConfig = if (responseMimeType != null || temperature != null) {
                GenerationConfig(
                    temperature = temperature,
                    responseMimeType = responseMimeType
                )
            } else null
        )

        val url = "v1beta/models/gemini-1.5-pro:generateContent?key=$key" // Upgrading to 1.5-pro for better reasoning

        return try {
            val response = service.generateContent(url, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Không nhận được phản hồi từ AI."
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Failed for request: ${e.message}")
            "Lỗi gọi AI: ${e.message}"
        }
    }
}
