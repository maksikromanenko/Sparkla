package com.example.sparkla.ui.fragments

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.sparkla.R
import com.example.sparkla.api.OrderItem
import com.example.sparkla.api.ProductData
import com.example.sparkla.util.CartManager

class ProductDetailsDialogFragment(private val product: ProductData) : DialogFragment() {

    private var quantity = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.dialog_product_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productImage: ImageView = view.findViewById(R.id.product_image_dialog)
        val productName: TextView = view.findViewById(R.id.product_name_dialog)
        val productDescription: TextView = view.findViewById(R.id.product_description_dialog)
        val productPrice: TextView = view.findViewById(R.id.product_price_dialog)
        val quantityValue: TextView = view.findViewById(R.id.quantity_value_dialog)
        val addToCartButton: Button = view.findViewById(R.id.add_to_cart_button_dialog)
        val addButton: ImageButton = view.findViewById(R.id.add_button_dialog)
        val removeButton: ImageButton = view.findViewById(R.id.remove_button_dialog)

        Glide.with(this).load(product.imageUrl).centerCrop().into(productImage)
        productName.text = product.name
        productDescription.text = product.description
        productPrice.text = "${product.price} руб."
        quantityValue.text = quantity.toString()

        addButton.setOnClickListener {
            quantity++
            quantityValue.text = quantity.toString()
        }

        removeButton.setOnClickListener {
            if (quantity > 1) {
                quantity--
                quantityValue.text = quantity.toString()
            }
        }

        addToCartButton.setOnClickListener {
            val orderItem = OrderItem(
                productItem = product.pk,
                quantity = quantity,
                priceAtTime = product.price
            )
            CartManager.addItem(orderItem)
            Toast.makeText(requireContext(), "Добавлено в корзину", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            val width = (Resources.getSystem().displayMetrics.widthPixels * 0.90).toInt()
            it.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}
