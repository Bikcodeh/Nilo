package com.bikcode.nilo.presentation.listener

import com.bikcode.nilo.data.model.OrderDTO

interface OnOrderListener {
    fun onTrack(order: OrderDTO)
    fun onStartChat(order: OrderDTO)
}