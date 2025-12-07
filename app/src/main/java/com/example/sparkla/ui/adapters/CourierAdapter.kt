package com.example.sparkla.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sparkla.R
import com.example.sparkla.api.OrderHistoryItem

class CourierAdapter(
    private var orders: List<OrderHistoryItem>,
    private val onStatusUpdate: (Int, String) -> Unit
) : RecyclerView.Adapter<CourierAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.courier_item, parent, false)
        return OrderViewHolder(view, onStatusUpdate)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<OrderHistoryItem>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    class OrderViewHolder(
        itemView: View,
        private val onStatusUpdate: (Int, String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val orderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val status: TextView = itemView.findViewById(R.id.tv_status)
        private val totalAmount: TextView = itemView.findViewById(R.id.tv_total_amount)
        private val address: TextView = itemView.findViewById(R.id.tv_address)
        private val btnAccept: ImageButton = itemView.findViewById(R.id.btn_accept_order)
        private val btnCancel: ImageButton = itemView.findViewById(R.id.btn_cancel_order)

        fun bind(order: OrderHistoryItem) {
            orderId.text = order.id.toString()
            status.text = translateStatus(order.status)
            totalAmount.text = "${order.totalPrice} руб."
            address.text = order.address

            if (order.status == "pending") {
                btnAccept.visibility = View.VISIBLE
                btnCancel.visibility = View.VISIBLE
                btnAccept.setOnClickListener {
                    onStatusUpdate(order.id, "delivered")
                }
                btnCancel.setOnClickListener {
                    onStatusUpdate(order.id, "cancelled")
                }
            } else {
                btnAccept.visibility = View.GONE
                btnCancel.visibility = View.GONE
            }
        }

        private fun translateStatus(status: String): String {
            return when (status) {
                "pending" -> "Ожидание"
                "delivered" -> "Доставлен"
                "cancelled" -> "Отменён"
                else -> status
            }
        }
    }
}