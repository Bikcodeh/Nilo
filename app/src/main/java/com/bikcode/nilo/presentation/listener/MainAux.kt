package com.bikcode.nilo.presentation.listener

import com.bikcode.nilo.data.model.ProductDTO
import com.google.firebase.auth.FirebaseUser

interface MainAux {
    fun getProductsCart(): MutableList<ProductDTO>
    fun getProductSelected(): ProductDTO?
    fun showButton(isVisible: Boolean)
    fun addToCart(productDTO: ProductDTO)
    fun updateTotal()
    fun clearCart()
    fun updateTitle(user: FirebaseUser)
}