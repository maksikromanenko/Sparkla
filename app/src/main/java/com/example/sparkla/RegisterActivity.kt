package com.example.sparkla

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sparkla.api.ApiClient
import com.example.sparkla.api.RegisterRequest
import com.example.sparkla.util.TokenManager
import com.example.sparkla.util.UserManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val username = findViewById<EditText>(R.id.username_register)
        val password = findViewById<EditText>(R.id.password_register)
        val confirmPassword = findViewById<EditText>(R.id.confirm_password_register)
        val registerButton = findViewById<Button>(R.id.register_button)

        registerButton.setOnClickListener {
            val userText = username.text.toString().trim()
            val passText = password.text.toString().trim()
            val confirmPassText = confirmPassword.text.toString().trim()

            if (userText.isEmpty() || passText.isEmpty() || confirmPassText.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passText != confirmPassText) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val registerRequest = RegisterRequest(userText, passText, confirmPassText)
                    val response = ApiClient.instance.register(registerRequest)

                    if (response.isSuccessful) {
                        val authResponse = response.body()
                        if (authResponse != null) {
                            TokenManager.saveTokens(this@RegisterActivity, authResponse.access, authResponse.refresh)
                            UserManager.currentUser = authResponse.user

                            Toast.makeText(this@RegisterActivity, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@RegisterActivity, "Ошибка: получен пустой ответ", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@RegisterActivity, "Ошибка регистрации: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
