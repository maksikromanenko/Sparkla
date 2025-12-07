package com.example.sparkla

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sparkla.api.ApiClient
import com.example.sparkla.api.UpdateOrderStatusRequest
import com.example.sparkla.ui.adapters.CourierAdapter
import com.example.sparkla.util.TokenManager
import com.example.sparkla.util.UserManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CourierActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var courierAdapter: CourierAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courier)

        recyclerView = findViewById(R.id.courier_orders_recycler_view)
        setupRecyclerView()
        fetchOrders()

        val btnLogout = findViewById<Button>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun setupRecyclerView() {
        courierAdapter = CourierAdapter(emptyList()) { orderId, newStatus ->
            updateOrderStatus(orderId, newStatus)
        }
        recyclerView.adapter = courierAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchOrders() {
        lifecycleScope.launch {
            val token = TokenManager.getAccessToken(this@CourierActivity).first()
            if (token.isNullOrBlank()) {
                Toast.makeText(this@CourierActivity, "Требуется авторизация", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                val response = ApiClient.instance.getOrderHistory("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        val sortedOrders = it.sortedBy { order -> if (order.status == "pending") 0 else 1 }
                        courierAdapter.updateOrders(sortedOrders)
                    }
                } else {
                    Toast.makeText(this@CourierActivity, "Не удалось загрузить заказы", Toast.LENGTH_SHORT).show()
                }
            } catch (t: Throwable) {
                Toast.makeText(this@CourierActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateOrderStatus(orderId: Int, newStatus: String) {
        lifecycleScope.launch {
            val token = TokenManager.getAccessToken(this@CourierActivity).first()
            if (token.isNullOrBlank()) {
                Toast.makeText(this@CourierActivity, "Требуется авторизация", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val request = UpdateOrderStatusRequest(status = newStatus)
            try {
                val response = ApiClient.instance.updateOrderStatus("Bearer $token", orderId, request)
                if (response.isSuccessful) {
                    Toast.makeText(this@CourierActivity, "Статус заказа #$orderId обновлен", Toast.LENGTH_SHORT).show()
                    fetchOrders()
                } else {
                    Toast.makeText(this@CourierActivity, "Не удалось обновить статус заказа", Toast.LENGTH_SHORT).show()
                }
            } catch (t: Throwable) {
                Toast.makeText(this@CourierActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this, R.style.PurpleDialog)
            .setTitle("Выход")
            .setMessage("Вы действительно хотите выйти?")
            .setPositiveButton("Да") { _, _ ->
                logout()
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun logout() {
        lifecycleScope.launch {
            TokenManager.clearTokens(this@CourierActivity)
            UserManager.clear()
        }

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}