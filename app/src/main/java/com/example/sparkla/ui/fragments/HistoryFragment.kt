package com.example.sparkla.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.sparkla.R
import com.example.sparkla.api.ApiClient
import com.example.sparkla.ui.adapters.HistoryAdapter
import com.example.sparkla.util.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        historyRecyclerView = view.findViewById(R.id.history_recycler_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchOrderHistoryAndProducts()
    }

    private fun fetchOrderHistoryAndProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext()).first()

            if (token.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Требуется авторизация", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                val historyResponse = ApiClient.instance.getOrderHistory("Bearer $token")
                if (!historyResponse.isSuccessful) {
                    Toast.makeText(requireContext(), "Не удалось загрузить историю заказов", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val orderHistory = historyResponse.body() ?: emptyList()

                val productsResponse = ApiClient.instance.getProducts("Bearer $token")
                if(!productsResponse.isSuccessful) {
                    Toast.makeText(requireContext(), "Не удалось загрузить продукты", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val products = productsResponse.body() ?: emptyList()

                historyAdapter = HistoryAdapter(orderHistory, products, this@HistoryFragment)
                historyRecyclerView.adapter = historyAdapter
            } catch (t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
