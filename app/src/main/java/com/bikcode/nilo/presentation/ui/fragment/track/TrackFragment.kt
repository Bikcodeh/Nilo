package com.bikcode.nilo.presentation.ui.fragment.track

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bikcode.nilo.data.model.OrderDTO
import com.bikcode.nilo.databinding.FragmentTrackBinding
import com.bikcode.nilo.presentation.listener.OrderAux

class TrackFragment: Fragment() {

    private var _binding: FragmentTrackBinding? = null
    private val binding: FragmentTrackBinding get() = _binding!!

    private var order: OrderDTO? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getOrder()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getOrder() {
        order = (activity as? OrderAux)?.getOrderSelected()

        order?.let {
            updateUI(it)
        }
    }

    private fun updateUI(orderDTO: OrderDTO) {
        with(binding) {
            progressBar.progress = orderDTO.status * (100 / 3) - 15

            cbOrdered.isChecked = orderDTO.status > 0
            cbPreparing.isChecked = orderDTO.status > 1
            cbSent.isChecked = orderDTO.status > 2
            cbDelivered.isChecked = orderDTO.status >3
        }
    }
}