package com.bikcode.nilo.presentation.listener

import com.bikcode.nilo.data.model.OrderDTO

interface OrderAux {
    fun getOrderSelected(): OrderDTO?
}