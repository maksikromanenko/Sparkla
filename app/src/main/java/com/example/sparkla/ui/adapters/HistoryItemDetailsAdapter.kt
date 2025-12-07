package com.example.sparkla.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sparkla.R
import com.example.sparkla.api.OrderItem
import com.example.sparkla.api.ProductData

class HistoryItemDetailsAdapter(
    private val items: List<OrderItem>,
    private val productsMap: Map<Int, ProductData>,
    private val orderStatus: String,
    private val ratedProductIds: Set<Int>,
    private val onRateClick: (orderItem: OrderItem) -> Unit
) : RecyclerView.Adapter<HistoryItemDetailsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_order_item_details, parent, false)
        return ViewHolder(view, onRateClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val product = productsMap[item.productItem]
        holder.bind(item, product, orderStatus, ratedProductIds)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        itemView: View,
        private val onRateClick: (orderItem: OrderItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productPrice: TextView = itemView.findViewById(R.id.product_price)
        private val quantityValue: TextView = itemView.findViewById(R.id.quantity_value)
        private val productImage: ImageView = itemView.findViewById(R.id.product_image)
        private val rateButton: Button = itemView.findViewById(R.id.rate_button)

        fun bind(item: OrderItem, product: ProductData?, orderStatus: String, ratedProductIds: Set<Int>) {
            quantityValue.text = "x${item.quantity}"
            productPrice.text = "${item.priceAtTime} BYN"

            if (product != null) {
                productName.text = product.name
                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(productImage)
            } else {
                productName.text = "Товар не найден (ID: ${item.productItem})"
                productImage.setImageResource(R.drawable.ic_launcher_background)
            }

            if (orderStatus == "delivered" && !ratedProductIds.contains(item.productItem)) {
                rateButton.visibility = View.VISIBLE
                rateButton.setOnClickListener { onRateClick(item) }
            } else {
                rateButton.visibility = View.GONE
            }
        }
    }
}
