package com.tiktokclone.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VeoAiService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val baseUrl = "https://veoaifree.com"
    private val userAgent = "Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Mobile Safari/537.36"

    private fun buildHeaders(): Map<String, String> = mapOf(
        "accept" to "*/*",
        "accept-language" to "en-US",
        "origin" to baseUrl,
        "referer" to "$baseUrl/veo-video-generator/",
        "sec-ch-ua" to "\"Chromium\";v=\"127\", \"Not)A;Brand\";v=\"99\"",
        "sec-ch-ua-mobile" to "?1",
        "sec-ch-ua-platform" to "\"Android\"",
        "sec-fetch-dest" to "empty",
        "sec-fetch-mode" to "cors",
        "sec-fetch-site" to "same-origin",
        "user-agent" to userAgent,
        "x-requested-with" to "XMLHttpRequest",
    )

    suspend fun getNonce(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/veo-video-generator/")
                .header("user-agent", userAgent)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))

            val nonceRegex = Regex("\"nonce\":\"([^\"]+)\"")
            val match = nonceRegex.find(body)
            if (match != null) {
                Result.success(match.groupValues[1])
            } else {
                Result.failure(Exception("Nonce not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateVideo(nonce: String, prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val formBody = FormBody.Builder()
                .add("action", "veo_video_generator")
                .add("nonce", nonce)
                .add("prompt", prompt)
                .add("totalVariations", "1")
                .add("aspectRatio", "VIDEO_ASPECT_RATIO_PORTRAIT")
                .add("actionType", "full-video-generate")
                .build()

            val requestBuilder = Request.Builder()
                .url("$baseUrl/wp-admin/admin-ajax.php")
                .post(formBody)

            buildHeaders().forEach { (key, value) ->
                requestBuilder.header(key, value)
            }
            requestBuilder.header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")

            val response = client.newCall(requestBuilder.build()).execute()
            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))

            val idRegex = Regex("\\b(\\d+)\\b")
            val match = idRegex.find(body)
            if (match != null) {
                Result.success(match.groupValues[1])
            } else {
                Result.failure(Exception("Scene ID not found: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVideoResult(nonce: String, sceneData: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val formBody = FormBody.Builder()
                .add("action", "veo_video_generator")
                .add("nonce", nonce)
                .add("sceneData", sceneData)
                .add("actionType", "final-video-results")
                .build()

            val requestBuilder = Request.Builder()
                .url("$baseUrl/wp-admin/admin-ajax.php")
                .post(formBody)

            buildHeaders().forEach { (key, value) ->
                requestBuilder.header(key, value)
            }
            requestBuilder.header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")

            val response = client.newCall(requestBuilder.build()).execute()
            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))

            // Extract video URL from response
            val urlRegex = Regex("https?://[^\"\\s,}]+\\.mp4[^\"\\s,}]*")
            val urlMatch = urlRegex.find(body)
            if (urlMatch != null) {
                Result.success(urlMatch.value.replace("\\/", "/"))
            } else {
                // Maybe it's still processing, return raw body
                Result.failure(Exception(body))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateFullVideo(
        prompt: String,
        onProgress: (String) -> Unit,
    ): Result<String> {
        onProgress("Getting session token...")
        val nonceResult = getNonce()
        val nonce = nonceResult.getOrElse { return Result.failure(it) }

        onProgress("Sending prompt to AI...")
        val sceneResult = generateVideo(nonce, prompt)
        val sceneData = sceneResult.getOrElse { return Result.failure(it) }

        onProgress("AI is generating your video... (this may take 1-2 minutes)")

        // Poll for results with retries
        var attempts = 0
        val maxAttempts = 8
        while (attempts < maxAttempts) {
            delay(if (attempts == 0) 45000L else 20000L)
            attempts++

            onProgress("Checking video status... (attempt $attempts/$maxAttempts)")
            val videoResult = getVideoResult(nonce, sceneData)
            if (videoResult.isSuccess) {
                return videoResult
            }

            val error = videoResult.exceptionOrNull()?.message ?: ""
            if (error.contains("mp4", ignoreCase = true) ||
                error.contains("video", ignoreCase = true) ||
                error.contains("url", ignoreCase = true)
            ) {
                // Try to extract URL from error message
                val urlRegex = Regex("https?://[^\"\\s,}]+\\.mp4[^\"\\s,}]*")
                val urlMatch = urlRegex.find(error)
                if (urlMatch != null) {
                    return Result.success(urlMatch.value.replace("\\/", "/"))
                }
            }
        }

        return Result.failure(Exception("Video generation timed out. Please try again."))
    }
}
