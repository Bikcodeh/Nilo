package com.bikcode.nilo.presentation.listener

import com.bikcode.nilo.data.model.ProductDTO

interface OnCartListener {
    fun setQuantity(product: ProductDTO)
    fun showTotal(total: Double)
}