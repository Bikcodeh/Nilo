package com.bikcode.nilo.presentation.ui.fragment.promo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bikcode.nilo.R
import com.bikcode.nilo.databinding.FragmentPromoBinding
import com.bikcode.nilo.presentation.listener.MainAux
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig

class PromoFragment : Fragment() {

    private var _binding: FragmentPromoBinding? = null
    private val binding: FragmentPromoBinding get() = _binding!!

    private var mainTitle: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPromoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configRemoteConfig()
        setupActionBar()
    }

    private fun setupActionBar() {
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            mainTitle = it.supportActionBar?.title.toString()
            it.supportActionBar?.title = getString(R.string.offer_title)
            setHasOptionsMenu(true)
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun configRemoteConfig() {
        val remoteConfig = Firebase.remoteConfig

        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val isPromoDay = remoteConfig.getBoolean("isPromoDay")
                    val promCounter = remoteConfig.getLong("promCounter")
                    val percentaje = remoteConfig.getDouble("percentaje")
                    val photoUrl = remoteConfig.getString("photoUrl")
                    val message = remoteConfig.getString("message")


                    with(binding) {
                        tvMessage.text = message
                        tvPercentaje.text = percentaje.toString()
                        Glide.with(this@PromoFragment)
                            .load(photoUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .centerCrop()
                            .placeholder(R.drawable.ic_access_time)
                            .error(R.drawable.ic_local_offer)
                            .into(imgPromo)
                    }
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        _binding = null
        (activity as? MainAux)?.showButton(true)
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setDisplayHomeAsUpEnabled(false)
            it.supportActionBar?.title = mainTitle
            setHasOptionsMenu(false)
        }
    }
}