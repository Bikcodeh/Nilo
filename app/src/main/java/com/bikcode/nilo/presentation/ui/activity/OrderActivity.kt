package com.bikcode.nilo.presentation.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bikcode.nilo.R
import com.bikcode.nilo.data.model.OrderDTO
import com.bikcode.nilo.databinding.ActivityOrderBinding
import com.bikcode.nilo.presentation.adapter.OrderAdapter
import com.bikcode.nilo.presentation.listener.OnOrderListener
import com.bikcode.nilo.presentation.util.Constants.REQUESTS_COLLECTION
import com.bikcode.nilo.presentation.util.showToast
import com.google.firebase.firestore.FirebaseFirestore

class OrderActivity : AppCompatActivity(), OnOrderListener {

    private lateinit var binding: ActivityOrderBinding
    private val ordersAdapter: OrderAdapter by lazy { OrderAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupFirestore()
    }

    private fun setupFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection(REQUESTS_COLLECTION).get()
            .addOnSuccessListener {
                for(document in it) {
                    val order = document.toObject(OrderDTO::class.java)
                    order.id = document.id
                    ordersAdapter.add(order)
                }
            }.addOnFailureListener {
                showToast(R.string.error_fetching_data)
            }
    }

    private fun setupRecycler() {
        binding.rvOrder.apply {
            layoutManager = LinearLayoutManager(this@OrderActivity)
            adapter = ordersAdapter
        }
    }

    override fun onTrack(order: OrderDTO) {
        TODO("Not yet implemented")
    }

    override fun onStartChat(order: OrderDTO) {
        TODO("Not yet implemented")
    }
}