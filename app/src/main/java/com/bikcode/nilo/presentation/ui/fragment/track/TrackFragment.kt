package com.bikcode.nilo.presentation.ui.fragment.track

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bikcode.nilo.R
import com.bikcode.nilo.data.model.OrderDTO
import com.bikcode.nilo.databinding.FragmentTrackBinding
import com.bikcode.nilo.presentation.listener.OrderAux
import com.bikcode.nilo.presentation.util.Constants.REQUESTS_COLLECTION
import com.bikcode.nilo.presentation.util.showToast
import com.google.firebase.firestore.FirebaseFirestore

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

    override fun onDestroy() {
        super.onDestroy()
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setDisplayShowHomeEnabled(false)
            it.supportActionBar?.title = getString(R.string.order_history)
            setHasOptionsMenu(false)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getOrder() {
        order = (activity as? OrderAux)?.getOrderSelected()

        order?.let {
            updateUI(it)
            getOrderRealtime(it.id)
            setupActionBar()
        }
    }

    private fun setupActionBar() {
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setDisplayShowHomeEnabled(true)
            it.supportActionBar?.title = getString(R.string.track_title)
            setHasOptionsMenu(true)
        }
    }

    private fun getOrderRealtime(orderId: String) {
        val db = FirebaseFirestore.getInstance()
        val orderRef = db.collection(REQUESTS_COLLECTION).document(orderId)
        
        orderRef.addSnapshotListener { snapshot, error ->
            if(error != null) {
                activity?.showToast(R.string.error_fetching_data)
                return@addSnapshotListener
            }

            if(snapshot != null && snapshot.exists()) {
                val order = snapshot.toObject(OrderDTO::class.java)
                order?.let {
                    it.id = snapshot.id

                    updateUI(it)
                }
            }
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