package com.bikcode.nilo.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bikcode.nilo.R
import com.bikcode.nilo.data.model.ProductDTO
import com.bikcode.nilo.databinding.ItemProductCartBinding
import com.bikcode.nilo.presentation.listener.OnCartListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class ProductCartAdapter: RecyclerView.Adapter<ProductCartAdapter.ProductCartViewHolder>() {

    private val products: MutableList<ProductDTO> = mutableListOf()
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductCartViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_product_cart, parent, false)
        return ProductCartViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductCartViewHolder, position: Int) {
        val product = products[position]
        holder.setListeners(product)
        holder.bind(product)

    }

    override fun getItemCount(): Int = products.count()

    fun setData(products: List<ProductDTO>) {
        this.products.clear()
        this.products.addAll(products)
    }
    private lateinit var listener: OnCartListener

    fun setListener(listener: OnCartListener) {
        this.listener = listener
    }
    fun add(productDTO: ProductDTO) {
        if(products.contains(productDTO).not()) {
            products.add(productDTO)
            notifyItemInserted(products.count() - 1)
            calculateTotal()
        } else {
            update(productDTO)
        }
    }

    fun update(productDTO: ProductDTO) {
        products.indexOf(productDTO).also {
            if(it != -1) {
                products[it] = productDTO
                notifyItemChanged(it)
                calculateTotal()
            }
        }
    }

    fun delete(productDTO: ProductDTO) {
        products.indexOf(productDTO).also {
            if(it != -1) {
                products.removeAt(it)
                notifyItemRemoved(it)
                calculateTotal()
            }
        }
    }

    private fun calculateTotal(){
        var result = 0.0
        for(product in products) {
            result += product.totalPrice()
        }
        listener.showTotal(result)
    }

    inner class ProductCartViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val binding = ItemProductCartBinding.bind(view)

        fun bind(productDTO: ProductDTO) {
            with(binding) {
                tvNameCart.text = productDTO.name
                tvQuantityCart.text = productDTO.newQuantity.toString()

                Glide.with(context)
                    .load(productDTO.imgUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .circleCrop()
                    .into(imgProductCart)
            }
        }

        fun setListeners(product: ProductDTO) {
            binding.ibSum.setOnClickListener {
                product.newQuantity += 1
                listener.setQuantity(product)
            }

            binding.ibSub.setOnClickListener {
                product.newQuantity -= 1
                listener.setQuantity(product)
            }
        }
    }
}