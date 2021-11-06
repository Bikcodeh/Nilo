package com.bikcode.nilo.presentation.ui.fragment.cart

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bikcode.nilo.R
import com.bikcode.nilo.data.model.OrderDTO
import com.bikcode.nilo.data.model.ProductDTO
import com.bikcode.nilo.data.model.ProductOrder
import com.bikcode.nilo.databinding.FragmentCartBinding
import com.bikcode.nilo.presentation.adapter.ProductCartAdapter
import com.bikcode.nilo.presentation.listener.MainAux
import com.bikcode.nilo.presentation.listener.OnCartListener
import com.bikcode.nilo.presentation.ui.activity.OrderActivity
import com.bikcode.nilo.presentation.util.Constants.REQUESTS_COLLECTION
import com.bikcode.nilo.presentation.util.showToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartFragment : BottomSheetDialogFragment(), OnCartListener {

    private var binding: FragmentCartBinding? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private lateinit var productCarAdapter: ProductCartAdapter

    protected var totalPrice = 0.0

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

            it.efaPay.setOnClickListener {
                requestOrder()
            }
        }
    }

    private fun requestOrder() {

        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            enableUI(false)
            val products = hashMapOf<String, ProductOrder>()
            productCarAdapter.getProducts().forEach { product ->
                products.put(
                    product.id!!,
                    ProductOrder(
                        id = product.id!!,
                        name = product.name!!,
                        quantity = product.newQuantity,
                        sellerId = product.sellerId
                    )
                )
            }
            val order = OrderDTO(clientId = currentUser.uid, products = products, totalPrice = totalPrice, status = 1)
            val db = FirebaseFirestore.getInstance()

            db.collection(REQUESTS_COLLECTION)
                .add(order)
                .addOnSuccessListener {
                    dismiss()
                    (activity as? MainAux)?.clearCart()
                    context?.showToast(R.string.buy_done)
                    startActivity(Intent(context, OrderActivity::class.java))

                }.addOnFailureListener {
                    context?.showToast(R.string.insert_error)
                }.addOnCompleteListener {
                    enableUI(true)
                }
        }
    }

    private fun enableUI(enable: Boolean) {
        binding?.let {
            it.ibCancelCart.isEnabled = enable
            it.efaPay.isEnabled = enable
            it.root.isEnabled = enable
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
        (activity as? MainAux)?.updateTotal()
        binding = null
        super.onDestroyView()
    }

    override fun setQuantity(product: ProductDTO) {
        productCarAdapter.update(product)
    }

    override fun showTotal(total: Double) {
        totalPrice = total
        binding?.let {
            it.tvTotalCart.text = getString(R.string.cart_full, totalPrice)
        }
    }
}