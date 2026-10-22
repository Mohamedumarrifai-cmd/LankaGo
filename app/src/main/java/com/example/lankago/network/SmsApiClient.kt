package com.example.lankago.network

import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit Client configured with OkHttp and HTTP Logging for SMS Gateway integration.
 */
object SmsApiClient {

    // Base URL for Twilio REST API
    private const val BASE_URL = "https://api.twilio.com/"

    // Default Twilio configuration credentials for LankaGo Admin
    const val TWILIO_ACCOUNT_SID = "ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    const val TWILIO_AUTH_TOKEN = "your_auth_token_here"
    const val TWILIO_SENDER_NUMBER = "+12025550199" // Registered Twilio sender

    /**
     * Generates HTTP Basic Auth header value for Twilio API requests:
     * Basic base64(AccountSid:AuthToken)
     */
    fun getBasicAuthHeader(
        accountSid: String = TWILIO_ACCOUNT_SID,
        authToken: String = TWILIO_AUTH_TOKEN
    ): String {
        val credentials = "$accountSid:$authToken"
        val encoded = Base64.encodeToString(credentials.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "Basic $encoded"
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: SmsApiService by lazy {
        retrofit.create(SmsApiService::class.java)
    }
}
