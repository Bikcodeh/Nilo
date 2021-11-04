package com.bikcode.nilo.presentation.ui.fragment.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bikcode.nilo.R
import com.bikcode.nilo.data.model.ProductDTO
import com.bikcode.nilo.databinding.FragmentDetailBinding
import com.bikcode.nilo.presentation.listener.MainAux
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

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
        getProduct()
        super.onViewCreated(view, savedInstanceState)
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
                tvTotalPriceDetail.text = getString(R.string.total_label,
                    it.totalPrice(),
                    it.newQuantity,
                    it.totalPrice())

                Glide.with(this@DetailFragment)
                    .load(it.imgUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.ic_access_time)
                    .error(R.drawable.ic_broken_image)
                    .into(imgProductDetail)
            }
        }
    }
}