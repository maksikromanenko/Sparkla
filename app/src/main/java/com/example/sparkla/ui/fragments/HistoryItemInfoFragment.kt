package com.example.sparkla.ui.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sparkla.R
import com.example.sparkla.api.ApiClient
import com.example.sparkla.api.OrderHistoryItem
import com.example.sparkla.api.OrderItem
import com.example.sparkla.api.ProductData
import com.example.sparkla.api.RatingRequest
import com.example.sparkla.ui.adapters.HistoryItemDetailsAdapter
import com.example.sparkla.util.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HistoryItemInfoFragment : Fragment() {

    private var historyItem: OrderHistoryItem? = null
    private var products: List<ProductData>? = null
    private lateinit var orderItemsRecyclerView: RecyclerView
    private lateinit var adapter: HistoryItemDetailsAdapter

    private val ratedProductIds = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            historyItem = it.getSerializable("historyItem") as? OrderHistoryItem
            @Suppress("DEPRECATION")
            products = it.getSerializable("products") as? List<ProductData>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.history_item_info, container, false)

        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)?.visibility = View.GONE

        orderItemsRecyclerView = view.findViewById(R.id.order_items_recycler_view)
        val orderNumber: TextView = view.findViewById(R.id.order_number)
        val statusValue: TextView = view.findViewById(R.id.status_value)
        val sumValue: TextView = view.findViewById(R.id.sum_value)

        orderItemsRecyclerView.layoutManager = LinearLayoutManager(context)

        if (historyItem != null && products != null) {
            val productsMap = products!!.associateBy { it.pk }
            adapter = HistoryItemDetailsAdapter(
                items = historyItem!!.items,
                productsMap = productsMap,
                orderStatus = historyItem!!.status,
                ratedProductIds = ratedProductIds
            ) { orderItem ->
                showRatingDialog(orderItem)
            }
            orderItemsRecyclerView.adapter = adapter

            orderNumber.text = "Заказ №${historyItem!!.id}"
            statusValue.text = translateStatus(historyItem!!.status)
            sumValue.text = "${historyItem!!.totalPrice} BYN"
        }

        return view
    }

    private fun showRatingDialog(orderItem: OrderItem) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rating, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.dialog_rating_bar)

        val titleTextView = TextView(requireContext()).apply {
            text = "Оцените товар"
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 40)
            setTextColor(resources.getColor(android.R.color.black, null))
        }

        AlertDialog.Builder(requireContext(), R.style.App_AlertDialogStyle)
            .setCustomTitle(titleTextView)
            .setView(dialogView)
            .setPositiveButton("ОК") { dialog, _ ->
                val score = ratingBar.rating.toInt()
                if (score > 0) {
                    rateProduct(orderItem.productItem, score)
                } else {
                    Toast.makeText(requireContext(), "Пожалуйста, выберите оценку", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun rateProduct(productId: Int, score: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext()).first()
            if (token.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Требуется авторизация", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val ratingRequest = RatingRequest(product = productId, score = score)

            try {
                val response = ApiClient.instance.rateProduct("Bearer $token", ratingRequest)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Рейтинг сохранен!", Toast.LENGTH_SHORT).show()
                    ratedProductIds.add(productId)
                    val position = historyItem?.items?.indexOfFirst { it.productItem == productId }
                    if (position != null && position != -1) {
                        adapter.notifyItemChanged(position)
                    }
                } else {
                    Toast.makeText(requireContext(), "Не удалось сохранить рейтинг. Возможно, вы уже оценили этот товар.", Toast.LENGTH_LONG).show()
                }
            } catch (t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)?.visibility = View.VISIBLE
    }
}
