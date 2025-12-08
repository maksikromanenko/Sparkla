package com.example.sparkla.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AuthResponse(
    val user: UserData,
    val refresh: String,
    val access: String
)

data class UserData(
    val id: Int,
    val username: String,
    val email: String?,
    val role: String?,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    @SerializedName("full_name") val fullName: String?
)

data class TokenCheckResponse(
    @SerializedName("access_valid") val isAccessValid: Boolean,
    @SerializedName("refresh_valid") val isRefreshValid: Boolean
)

data class RefreshResponse(
    val access: String
)

data class ProductData(
    val pk: Int,
    val name: String,
    val description: String,
    @SerializedName("image_url")
    val imageUrl: String?,
    val price: String,
    @SerializedName("is_available")
    val isAvailable: Boolean,
    @SerializedName("product_type")
    val productType: String,
    @SerializedName("average_rating")
    val averageRating: String?
): Serializable

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    @SerializedName("password_confirm") val passwordConfirm: String
)

data class TokenCheckRequest(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)

data class RefreshRequest(
    val refresh: String
)

data class OrderItem(
    @SerializedName("product_item") val productItem: Int,
    val quantity: Int,
    @SerializedName("price_at_time") val priceAtTime: String
): Serializable

data class Order(
    val address: Int,
    val items: List<OrderItem>
)

data class OrderHistoryItem(
    val id: Int,
    val status: String,
    @SerializedName("total_price") val totalPrice: String,
    val address: String,
    val items: List<OrderItem>
): Serializable

data class UpdateProfileRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String
)

data class Address(
    val id: Int,
    @SerializedName("address_line") val addressLine: String?,
    val city: String?,
    @SerializedName("postal_code") val postalCode: String?,
    val location: String?
)

data class UpdateAddressRequest(
    @SerializedName("address_line") val addressLine: String,
    val city: String,
    @SerializedName("postal_code") val postalCode: String
)

data class ChangePasswordRequest(
    @SerializedName("old_password") val oldPassword: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("new_password_confirm") val newPasswordConfirm: String
)

data class UpdateOrderStatusRequest(
    val status: String
)