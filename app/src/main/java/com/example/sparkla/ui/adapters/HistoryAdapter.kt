package com.example.sparkla.ui.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sparkla.R
import com.example.sparkla.api.OrderHistoryItem
import com.example.sparkla.api.ProductData
import com.example.sparkla.ui.fragments.HistoryFragment
import com.example.sparkla.ui.fragments.HistoryItemInfoFragment
import java.io.Serializable

class HistoryAdapter(
    private val historyItems: List<OrderHistoryItem>,
    private val products: List<ProductData>,
    private val fragment: HistoryFragment
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return ViewHolder(view, fragment, products)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyItems[position]
        holder.bind(item, position)
    }

    override fun getItemCount() = historyItems.size

    class ViewHolder(
        itemView: View,
        private val fragment: HistoryFragment,
        private val products: List<ProductData>
    ) : RecyclerView.ViewHolder(itemView) {
        private val orderIdTextView: TextView = itemView.findViewById(R.id.tv_order_id)
        private val statusTextView: TextView = itemView.findViewById(R.id.tv_status)
        private val totalAmountTextView: TextView = itemView.findViewById(R.id.tv_total_amount)

        fun bind(item: OrderHistoryItem, position: Int) {
            orderIdTextView.text = (position + 1).toString()
            statusTextView.text = translateStatus(item.status)
            totalAmountTextView.text = item.totalPrice

            itemView.setOnClickListener {
                val fragmentToGo = HistoryItemInfoFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("historyItem", item as Serializable)
                        putSerializable("products", products as Serializable)
                    }
                }

                fragment.parentFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, fragmentToGo)
                    .addToBackStack(null)
                    .commit()
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
