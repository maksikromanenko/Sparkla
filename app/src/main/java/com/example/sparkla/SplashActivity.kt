package com.example.sparkla

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sparkla.api.*
import com.example.sparkla.util.TokenManager
import com.example.sparkla.util.UserManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            val accessToken = TokenManager.getAccessToken(this@SplashActivity).first()
            val refreshToken = TokenManager.getRefreshToken(this@SplashActivity).first()

            if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
                navigateToLogin(clearTokens = true) // Clear any partial tokens if one is missing
                return@launch
            }

            checkTokens(accessToken, refreshToken)
        }
    }

    private fun checkTokens(accessToken: String, refreshToken: String) {
        lifecycleScope.launch {
            try {
                val checkRequest = TokenCheckRequest(accessToken, refreshToken)
                val response = ApiClient.instance.checkTokens(checkRequest)

                if (response.isSuccessful) {
                    val checkResponse = response.body()
                    if (checkResponse == null) {
                        handleApiError("Пустой ответ от сервера", clearTokens = false)
                        return@launch
                    }

                    when {
                        checkResponse.isAccessValid && checkResponse.isRefreshValid -> {
                            fetchProfileAndNavigate(accessToken)
                        }
                        !checkResponse.isAccessValid && checkResponse.isRefreshValid -> {
                            refreshAccessToken(refreshToken)
                        }
                        else -> {
                            handleApiError("Сессия истекла, войдите снова", clearTokens = true)
                        }
                    }
                } else {
                    handleApiError("Ошибка проверки токенов: ${response.code()}", clearTokens = false)
                }
            } catch (t: Throwable) {
                handleNetworkError(t)
            }
        }
    }

    private fun refreshAccessToken(refreshToken: String) {
        lifecycleScope.launch {
            val refreshRequest = RefreshRequest(refreshToken)
            try {
                val response = ApiClient.instance.refreshToken(refreshRequest)
                if (response.isSuccessful) {
                    val newAccessToken = response.body()?.access
                    if (newAccessToken != null) {
                        TokenManager.saveTokens(this@SplashActivity, newAccessToken, refreshToken)
                        fetchProfileAndNavigate(newAccessToken)
                    } else {
                        handleApiError("Не удалось получить новый токен доступа", clearTokens = true)
                    }
                } else {
                    handleApiError("Сессия истекла. Пожалуйста, войдите снова.", clearTokens = true)
                }
            } catch (t: Throwable) {
                handleNetworkError(t)
            }
        }
    }

    private fun fetchProfileAndNavigate(accessToken: String) {
        lifecycleScope.launch {
            val authHeader = "Bearer $accessToken"
            try {
                val response = ApiClient.instance.getProfile(authHeader)
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    UserManager.currentUser = user
                    Toast.makeText(this@SplashActivity, "Добро пожаловать, ${user.username}!", Toast.LENGTH_SHORT).show()

                    when (user.role) {
                        "customer" -> navigateToMain()
                        "courier" -> navigateToCourier()
                        else -> navigateToLogin(true) // Or a default screen
                    }
                } else {
                    handleApiError("Ошибка при загрузке профиля: ${response.code()}", clearTokens = false)
                }
            } catch (t: Throwable) {
                handleNetworkError(t)
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToCourier() {
        val intent = Intent(this, CourierActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin(clearTokens: Boolean) {
        lifecycleScope.launch {
            if (clearTokens) {
                UserManager.clear()
                TokenManager.clearTokens(this@SplashActivity)
            }
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun handleApiError(message: String, clearTokens: Boolean) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        navigateToLogin(clearTokens)
    }

    private fun handleNetworkError(t: Throwable) {
        Toast.makeText(this, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show()
        navigateToLogin(clearTokens = false) // Don't clear tokens on network error
    }
}