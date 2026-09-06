package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LeaderboardEntry(
    val team: String,
    val round: String?,
    val total: Int,
    val timeSec: String?
)

interface LeaderboardApi {
    @GET("leaderboard")
    suspend fun getLeaderboard(): List<LeaderboardEntry>

    @POST("leaderboard")
    suspend fun postScore(@Body entry: LeaderboardEntry): Map<String, Any>?
}

object ApiClient {
    private const val BASE_URL = "https://6a4394cf1353da40-27-130-224-176.serveousercontent.com/"

    val service: LeaderboardApi by lazy {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(LeaderboardApi::class.java)
    }
}
