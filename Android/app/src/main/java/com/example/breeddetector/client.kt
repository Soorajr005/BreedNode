package com.example.breeddetector

import retrofit2.Retrofit
import retrofit2.http.*
import okhttp3.MultipartBody
import retrofit2.converter.gson.GsonConverterFactory

data class PredictionResponse(
    val predictions: List<BreedResult>
)

data class BreedInfo(
    val origin: String = "",
    val utility: String = "",
    val milk_yield: String = "",
    val traits: String = "",
    val fact: String = ""
)

interface CattleApiService {
    @Multipart
    @POST("predict")
    suspend fun predictBreed(
        @Part file: MultipartBody.Part
    ): PredictionResponse
}

object RetrofitClient {
    private const val BASE_URL = "http://ip address:8000/"

    val instance: CattleApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CattleApiService::class.java)
    }
}