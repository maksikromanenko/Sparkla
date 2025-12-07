package com.example.sparkla

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sparkla.api.ApiClient
import com.example.sparkla.api.LoginRequest
import com.example.sparkla.util.TokenManager
import com.example.sparkla.util.UserManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login)
        val switchToRegister = findViewById<TextView>(R.id.switch_to_register)

        loginButton.setOnClickListener {
            val userText = username.text.toString().trim()
            val passText = password.text.toString().trim()

            if (userText.isEmpty() || passText.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите логин и пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val loginRequest = LoginRequest(userText, passText)
                    val response = ApiClient.instance.login(loginRequest)

                    if (response.isSuccessful) {
                        val authResponse = response.body()
                        if (authResponse != null) {
                            TokenManager.saveTokens(this@LoginActivity, authResponse.access, authResponse.refresh)
                            UserManager.currentUser = authResponse.user
                            Toast.makeText(this@LoginActivity, "Вход выполнен успешно", Toast.LENGTH_SHORT).show()

                            when (authResponse.user.role) {
                                "customer" -> navigateToMain()
                                "courier" -> navigateToCourier()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Ошибка: получен пустой ответ", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
                    }
                } catch (t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        switchToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
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

}
