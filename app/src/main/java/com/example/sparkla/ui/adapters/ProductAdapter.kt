package com.example.sparkla.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sparkla.R
import com.example.sparkla.api.ProductData
import com.example.sparkla.ui.fragments.ProductDetailsDialogFragment

class ProductAdapter(private var products: List<ProductData>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            val activity = it.context as AppCompatActivity
            ProductDetailsDialogFragment(product).show(activity.supportFragmentManager, "product_details")
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<ProductData>) {
        products = newProducts
        notifyDataSetChanged()
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productPrice: TextView = itemView.findViewById(R.id.product_price)
        private val productImage: ImageView = itemView.findViewById(R.id.product_image)
        private val productRating: TextView = itemView.findViewById(R.id.product_rating)

        fun bind(product: ProductData) {
            productName.text = product.name
            productPrice.text = "${product.price} руб."
            if (product.averageRating != null) {
                productRating.text = product.averageRating
                productRating.visibility = View.VISIBLE
            } else {
                productRating.visibility = View.GONE
            }

            Glide.with(itemView.context)
                .load(product.imageUrl)
                .centerCrop()
                .into(productImage)
        }
    }
}
