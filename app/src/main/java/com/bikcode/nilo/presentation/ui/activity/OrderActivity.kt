package com.bikcode.nilo.presentation.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bikcode.nilo.R
import com.bikcode.nilo.data.model.OrderDTO
import com.bikcode.nilo.databinding.ActivityOrderBinding
import com.bikcode.nilo.presentation.adapter.OrderAdapter
import com.bikcode.nilo.presentation.listener.OnOrderListener
import com.bikcode.nilo.presentation.listener.OrderAux
import com.bikcode.nilo.presentation.ui.fragment.chat.ChatFragment
import com.bikcode.nilo.presentation.ui.fragment.track.TrackFragment
import com.bikcode.nilo.presentation.util.Constants.PROP_CLIENT_ID
import com.bikcode.nilo.presentation.util.Constants.PROP_DATE
import com.bikcode.nilo.presentation.util.Constants.PROP_STATUS
import com.bikcode.nilo.presentation.util.Constants.REQUESTS_COLLECTION
import com.bikcode.nilo.presentation.util.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderActivity : AppCompatActivity(), OnOrderListener, OrderAux{

    private lateinit var binding: ActivityOrderBinding
    private val ordersAdapter: OrderAdapter by lazy { OrderAdapter(this) }
    private var orderSelected: OrderDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupFirestore()
    }

    private fun setupFirestore() {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            val db = FirebaseFirestore.getInstance()
            db.collection(REQUESTS_COLLECTION)
                .orderBy(PROP_DATE, Query.Direction.DESCENDING)
                .whereEqualTo(PROP_CLIENT_ID, user.uid)
                //.whereIn(PROP_STATUS, listOf(1, 2))
                //.whereNotIn(PROP_STATUS, listOf(4))
                //.whereGreaterThan(PROP_STATUS, 2)
                //.whereLessThan(PROP_STATUS, 4)
                //.whereEqualTo(PROP_STATUS, 3)
                //.whereGreaterThanOrEqualTo(PROP_STATUS, 2)
                .get()
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
    }

    private fun setupRecycler() {
        binding.rvOrder.apply {
            layoutManager = LinearLayoutManager(this@OrderActivity)
            adapter = ordersAdapter
        }
    }

    override fun onTrack(order: OrderDTO) {
        orderSelected = order
        val fragment = TrackFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.containerOrder, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onStartChat(order: OrderDTO) {
        orderSelected = order

        val fragment = ChatFragment()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.containerOrder, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun getOrderSelected(): OrderDTO? = orderSelected
}