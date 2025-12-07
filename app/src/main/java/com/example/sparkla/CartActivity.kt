package com.example.sparkla

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sparkla.api.ApiClient
import com.example.sparkla.api.Order
import com.example.sparkla.ui.adapters.CartAdapter
import com.example.sparkla.util.CartManager
import com.example.sparkla.util.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        recyclerView = findViewById(R.id.cart_recycler_view)
        setupRecyclerView()

        val placeOrderButton = findViewById<Button>(R.id.place_order_button)
        placeOrderButton.setOnClickListener {
            if (CartManager.getCartItems().isEmpty()) {
                Toast.makeText(this, "Корзина пуста", Toast.LENGTH_SHORT).show()
            } else {
                getAddressAndPlaceOrder()
            }
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(CartManager.getCartItems())
        recyclerView.adapter = cartAdapter
    }

    private fun getAddressAndPlaceOrder() {
        lifecycleScope.launch {
            val token = TokenManager.getAccessToken(this@CartActivity).first()
            if (token.isNullOrBlank()) {
                Toast.makeText(this@CartActivity, "Требуется авторизация", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                val addressResponse = ApiClient.instance.getAddress("Bearer $token")
                if (addressResponse.isSuccessful && addressResponse.body()?.isNotEmpty() == true) {
                    val addressId = addressResponse.body()!![0].id
                    placeOrder(addressId)
                } else {
                    Toast.makeText(this@CartActivity, "Не удалось получить адрес. Добавьте адрес в профиле.", Toast.LENGTH_LONG).show()
                }
            } catch (t: Throwable) {
                Toast.makeText(this@CartActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun placeOrder(addressId: Int) {
        lifecycleScope.launch {
            val token = TokenManager.getAccessToken(this@CartActivity).first()
            if (token.isNullOrBlank()) {
                Toast.makeText(this@CartActivity, "Требуется авторизация", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val order = Order(address = addressId, items = CartManager.getCartItems())

            try {
                val response = ApiClient.instance.createOrder("Bearer $token", order)
                if (response.isSuccessful) {
                    Toast.makeText(this@CartActivity, "Заказ успешно создан", Toast.LENGTH_SHORT).show()
                    CartManager.clearCart()
                    finish()
                } else {
                    Toast.makeText(this@CartActivity, "Не удалось создать заказ", Toast.LENGTH_SHORT).show()
                }
            } catch (t: Throwable) {
                Toast.makeText(this@CartActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}