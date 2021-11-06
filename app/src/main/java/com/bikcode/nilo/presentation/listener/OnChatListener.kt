package com.bikcode.nilo.presentation.listener

import com.bikcode.nilo.data.model.Message

interface OnChatListener {
    fun deleteMessage(message: Message)
}