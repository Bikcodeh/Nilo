package com.bikcode.nilo.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bikcode.nilo.R
import com.bikcode.nilo.data.model.OrderDTO
import com.bikcode.nilo.databinding.ItemOrderBinding
import com.bikcode.nilo.presentation.listener.OnOrderListener

class OrderAdapter(private val listener: OnOrderListener): RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private val orders = mutableListOf<OrderDTO>()
    private lateinit var context: Context

    inner class OrderViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val binding = ItemOrderBinding.bind(view)

        fun bind(orderDTO: OrderDTO) {
            with(binding) {
                var names = ""
                orderDTO.products.forEach { order ->
                    names += "${order.value.name}, "
                }
                tvProductNames.text = names.dropLast(2)
                tvTotalPrice.text = orderDTO.totalPrice.toString()
                tvId.text = orderDTO.id
                tvStatus.text = orderDTO.status.toString()
            }
        }

        fun setListener(orderDTO: OrderDTO) {
            with(binding) {
                btnTrack.setOnClickListener {
                    listener.onTrack(orderDTO)
                }

                chpChat.setOnClickListener {
                    listener.onStartChat(orderDTO)
                }
            }
        }

    }

    fun add(orderDTO: OrderDTO) {
        orders.add(orderDTO)
        notifyItemInserted(orders.count()  -1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.setListener(order)
        holder.bind(order)
    }

    override fun getItemCount(): Int = orders.count()
}