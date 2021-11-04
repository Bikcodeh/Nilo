package com.bikcode.nilo.presentation.listener

import com.bikcode.nilo.data.model.ProductDTO

interface MainAux {
    fun getProductsCart(): MutableList<ProductDTO>
}