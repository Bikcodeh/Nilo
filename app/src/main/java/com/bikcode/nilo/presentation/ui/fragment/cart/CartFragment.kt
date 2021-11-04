package com.bikcode.nilo.presentation.ui.fragment.cart

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bikcode.nilo.data.model.ProductDTO
import com.bikcode.nilo.databinding.FragmentCartBinding
import com.bikcode.nilo.presentation.adapter.ProductCartAdapter
import com.bikcode.nilo.presentation.listener.MainAux
import com.bikcode.nilo.presentation.listener.OnCartListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CartFragment : BottomSheetDialogFragment(), OnCartListener {

    private var binding: FragmentCartBinding? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private lateinit var productCarAdapter: ProductCartAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentCartBinding.inflate(LayoutInflater.from(activity))
        binding?.let {
            val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
            bottomSheetDialog.setContentView(it.root)
            bottomSheetBehavior = BottomSheetBehavior.from(it.root.parent as View)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            setupRecycler()
            setupButtons()
            getProducts()

            return bottomSheetDialog
        }
        return super.onCreateDialog(savedInstanceState)
    }

    private fun getProducts() {
        (activity as? MainAux)?.getProductsCart()?.forEach {
            productCarAdapter.add(it)
        }
    }

    private fun setupButtons() {
        binding?.let {
            it.ibCancelCart.setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    private fun setupRecycler() {
        binding?.let {
            productCarAdapter = ProductCartAdapter()
            productCarAdapter.setListener(this)

            it.rvProductsCart.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = productCarAdapter
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun setQuantity(product: ProductDTO) {
        TODO("Not yet implemented")
    }

    override fun showTotal(total: Double) {
        TODO("Not yet implemented")
    }
}