package com.example.sparkla.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sparkla.R
import com.example.sparkla.api.OrderItem
import com.example.sparkla.util.ProductRepository

class CartAdapter(private val cartItems: List<OrderItem>) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.cart_item_name)
        private val itemQuantity: TextView = itemView.findViewById(R.id.cart_item_quantity)
        private val itemPrice: TextView = itemView.findViewById(R.id.cart_item_price)

        fun bind(orderItem: OrderItem) {
            val product = ProductRepository.getProductById(orderItem.productItem)
            itemName.text = product?.name ?: "Product ID: ${orderItem.productItem}"
            itemQuantity.text = "x${orderItem.quantity}"
            itemPrice.text = "${orderItem.priceAtTime} руб."
        }
    }
}
