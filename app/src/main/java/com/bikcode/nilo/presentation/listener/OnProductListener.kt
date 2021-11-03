package com.bikcode.nilo.presentation.listener

import com.bikcode.nilo.data.model.ProductDTO

interface OnProductListener {
    fun onClick(product: ProductDTO)
}