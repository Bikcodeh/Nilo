package com.bikcode.nilo.presentation.ui.fragment.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bikcode.nilo.R
import com.bikcode.nilo.data.model.Message
import com.bikcode.nilo.data.model.OrderDTO
import com.bikcode.nilo.databinding.FragmentChatBinding
import com.bikcode.nilo.presentation.adapter.ChatAdapter
import com.bikcode.nilo.presentation.listener.OnChatListener
import com.bikcode.nilo.presentation.listener.OrderAux

class ChatFragment: Fragment(), OnChatListener {

    private var _binding: FragmentChatBinding? = null
    private val binding: FragmentChatBinding get() = _binding!!
    private var order: OrderDTO? = null
    private val chatAdapter: ChatAdapter by lazy { ChatAdapter(mutableListOf(), this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getOrder()
        setupRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.title = getString(R.string.order_history)
            it.supportActionBar?.setDisplayShowHomeEnabled(false)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun deleteMessage(message: Message) {

    }

    private fun getOrder() {
        order = (activity as? OrderAux)?.getOrderSelected()
        order?.let {
            setupActionBar()
            setupRealtimeDatabase()
        }
    }

    private fun setupActionBar() {
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setDisplayShowHomeEnabled(true)
            it.supportActionBar?.title = getString(R.string.chat_title)
        }
    }

    private fun setupRecyclerView() {
        binding.rvChat.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context).also {
                it.stackFromEnd = true
            }
        }
    }

    private fun setupRealtimeDatabase() {
    }
}