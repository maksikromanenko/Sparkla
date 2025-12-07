package com.example.sparkla.util

import com.example.sparkla.api.OrderItem

object CartManager {
    private val cartItems = mutableListOf<OrderItem>()

    fun addItem(item: OrderItem) {
        val existingItem = cartItems.find { it.productItem == item.productItem }
        if (existingItem != null) {
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + item.quantity)
            cartItems.remove(existingItem)
            cartItems.add(updatedItem)
        } else {
            cartItems.add(item)
        }
    }

    fun getCartItems(): List<OrderItem> = cartItems.toList()

    fun clearCart() {
        cartItems.clear()
    }
}