package com.bikcode.nilo.presentation.ui.fragment.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.bikcode.nilo.R
import com.bikcode.nilo.data.model.ProductDTO
import com.bikcode.nilo.databinding.FragmentDetailBinding
import com.bikcode.nilo.presentation.listener.MainAux
import com.bikcode.nilo.presentation.util.Constants
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.storage.FirebaseStorage

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private var product: ProductDTO? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getProduct()
        setupButtons()
    }

    private fun setupButtons() {
        product?.let { productDTO ->
            with(binding) {
                ibSubDetail.setOnClickListener {
                    if(productDTO.newQuantity > 1) {
                        productDTO.newQuantity -= 1
                        setNewQuantity(productDTO)
                    }
                }

                ibSumDetail.setOnClickListener {
                    if(productDTO.newQuantity < productDTO.quantity) {
                        productDTO.newQuantity += 1
                        setNewQuantity(productDTO)
                    }
                }

                efabAddToCart.setOnClickListener {
                    productDTO.newQuantity = tieQuantityDetail.text.toString().toInt()
                    addToCart(productDTO)
                    activity?.onBackPressed()
                }
            }
        }
    }

    override fun onDestroyView() {
        (activity as? MainAux)?.showButton(isVisible = true)
        super.onDestroyView()
        _binding = null
    }

    private fun getProduct() {
        product = (activity as? MainAux)?.getProductSelected()
        product?.let {
            with(binding) {
                tvProductNameDetail.text = it.name
                tvDescriptionDetail.text = it.description
                tvAvailable.text = getString(R.string.available_label, it.quantity)
                setNewQuantity(it)

                /*Glide.with(this@DetailFragment)
                    .load(it.imgUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.ic_access_time)
                    .error(R.drawable.ic_broken_image)
                    .into(imgProductDetail)*/

                context?.let { context ->
                    val productRef = FirebaseStorage.getInstance().reference
                        .child(product!!.sellerId)
                        .child(Constants.PATH_PRODUCTS_IMAGES)
                        .child(product!!.id!!)

                    productRef.listAll().addOnSuccessListener { imgList ->
                        val detailAdapter = DetailAdapter(imgList.items, context)
                        vpProductImages.apply {
                            adapter = detailAdapter
                        }
                    }
                }
            }
        }
    }

    private fun addToCart(product: ProductDTO) {
        (activity as? MainAux)?.addToCart(product)
    }

    private fun setNewQuantity(product: ProductDTO) {
        with(binding) {
            tieQuantityDetail.setText(product.newQuantity.toString())
            val newQuantityStr = getString(
                R.string.total_label,
                product.totalPrice(),
                product.newQuantity,
                product.totalPrice()
            )
            tvTotalPriceDetail.text = HtmlCompat.fromHtml(newQuantityStr, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }
}