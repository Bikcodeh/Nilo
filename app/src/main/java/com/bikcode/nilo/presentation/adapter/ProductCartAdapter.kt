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
        } else {
            update(productDTO)
        }
    }

    fun update(productDTO: ProductDTO) {
        products.indexOf(productDTO).also {
            if(it != -1) {
                products[it] = productDTO
                notifyItemChanged(it)
            }
        }
    }

    fun delete(productDTO: ProductDTO) {
        products.indexOf(productDTO).also {
            if(it != -1) {
                products.removeAt(it)
                notifyItemRemoved(it)
            }
        }
    }

    inner class ProductCartViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val binding = ItemProductCartBinding.bind(view)

        fun bind(productDTO: ProductDTO) {
            with(binding) {
                tvNameCart.text = productDTO.name
                tvQuantityCart.text = productDTO.quantity.toString()

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
                listener.setQuantity(product)
            }

            binding.ibSub.setOnClickListener {
                listener.setQuantity(product)
            }
        }
    }
}