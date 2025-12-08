package com.example.sparkla.api

import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface ApiService {
    @POST("users/register/")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("users/login/")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("users/check-tokens/")
    suspend fun checkTokens(@Body request: TokenCheckRequest): Response<TokenCheckResponse>

    @POST("users/token/refresh/")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<RefreshResponse>

    @GET("users/profile/")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserData>

    @PATCH("users/profile/")
    suspend fun updateProfile(@Header("Authorization") token: String, @Body request: UpdateProfileRequest): Response<Void>

    @GET("products/")
    suspend fun getProducts(@Header("Authorization") token: String): Response<List<ProductData>>

    @GET("orders/")
    suspend fun getOrderHistory(@Header("Authorization") token: String): Response<List<OrderHistoryItem>>

    @POST("orders/")
    suspend fun createOrder(@Header("Authorization") token: String, @Body order: Order): Response<Void>

    @PATCH("orders/{id}/status/")
    suspend fun updateOrderStatus(@Header("Authorization") token: String, @Path("id") id: Int, @Body request: UpdateOrderStatusRequest): Response<Void>

    @GET("users/addresses/")
    suspend fun getAddress(@Header("Authorization") token: String): Response<List<Address>>

    @POST("users/addresses/")
    suspend fun createAddress(@Header("Authorization") token: String, @Body request: UpdateAddressRequest): Response<Address>

    @PATCH("users/addresses/{id}/")
    suspend fun updateAddress(@Header("Authorization") token: String, @Path("id") id: Int, @Body request: UpdateAddressRequest): Response<Void>

    @PATCH("users/change-password/")
    suspend fun changePassword(@Header("Authorization") token: String, @Body request: ChangePasswordRequest): Response<Void>

    @POST("ratings/")
    suspend fun rateProduct(@Header("Authorization") token: String, @Body request: RatingRequest): Response<Void>
}

data class RatingRequest(
    val product: Int,
    val score: Int
)

object ApiClient {
    private const val BASE_URL = "http://100.66.233.102:8000/api/"

    val instance: ApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(1500, TimeUnit.MILLISECONDS)
            .readTimeout(1500, TimeUnit.MILLISECONDS)
            .writeTimeout(1500, TimeUnit.MILLISECONDS)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}