package com.example.sparkla.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sparkla.R
import com.example.sparkla.api.ApiClient
import com.example.sparkla.ui.adapters.ProductAdapter
import com.example.sparkla.util.ProductRepository
import com.example.sparkla.util.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var searchView: SearchView
    private lateinit var filterButton: Button

    private var currentSortType = SortType.DEFAULT

    private enum class SortType {
        DEFAULT,
        PRICE_DESC,
        PRICE_ASC,
        NAME_AZ
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.products_recycler_view)
        searchView = view.findViewById(R.id.search_view)
        filterButton = view.findViewById(R.id.filters_button)

        setupRecyclerView()
        setupSearchView()
        setupFilterButton()
        fetchProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(emptyList())
        recyclerView.adapter = productAdapter
        recyclerView.layoutManager = GridLayoutManager(context, 3)
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                applyFiltersAndSorting()
                return true
            }
        })

        val editText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        editText.setTextColor(Color.BLACK)
        editText.setHintTextColor(Color.parseColor("#808080"))
    }

    private fun setupFilterButton() {
        filterButton.setOnClickListener {
            showSortDialog()
        }
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf("По умолчанию", "Сначала дорогие", "Сначала дешевые", "По алфавиту")
        val currentChoice = currentSortType.ordinal

        AlertDialog.Builder(requireContext())
            .setTitle("Сортировать")
            .setSingleChoiceItems(sortOptions, currentChoice) { dialog, which ->
                currentSortType = SortType.values()[which]
                applyFiltersAndSorting()
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun fetchProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext()).first()
            if (token.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Требуется авторизация", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                val response = ApiClient.instance.getProducts("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        ProductRepository.setProducts(it)
                        applyFiltersAndSorting()
                    }
                } else {
                    Toast.makeText(requireContext(), "Не удалось загрузить продукты", Toast.LENGTH_SHORT).show()
                }
            } catch (t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyFiltersAndSorting() {
        val allProducts = ProductRepository.getAllProducts()
        val query = searchView.query.toString()

        val filteredList = if (query.isBlank()) {
            allProducts
        } else {
            allProducts.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }

        val sortedList = when (currentSortType) {
            SortType.PRICE_DESC -> filteredList.sortedByDescending { it.price.toDoubleOrNull() ?: 0.0 }
            SortType.PRICE_ASC -> filteredList.sortedBy { it.price.toDoubleOrNull() ?: 0.0 }
            SortType.NAME_AZ -> filteredList.sortedBy { it.name }
            SortType.DEFAULT -> filteredList
        }

        productAdapter.updateProducts(sortedList)
    }
}
