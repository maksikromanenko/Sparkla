package com.example.sparkla.util

import com.example.sparkla.api.ProductData

object ProductRepository {
    private var products: List<ProductData> = emptyList()
    private val productMap = mutableMapOf<Int, ProductData>()

    fun setProducts(productList: List<ProductData>) {
        products = productList
        productMap.clear()
        products.forEach { product ->
            productMap[product.pk] = product
        }
    }

    fun getProductById(id: Int): ProductData? {
        return productMap[id]
    }

    fun getAllProducts(): List<ProductData> {
        return products
    }
}