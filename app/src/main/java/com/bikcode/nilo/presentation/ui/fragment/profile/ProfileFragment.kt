package com.bikcode.nilo.presentation.ui.fragment.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bikcode.nilo.R
import com.bikcode.nilo.databinding.FragmentProfileBinding
import com.bikcode.nilo.presentation.listener.MainAux
import com.bikcode.nilo.presentation.util.showToast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUser()
        configButtons()
    }

    private fun getUser() {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            with(binding) {
                tieFullName.setText(user.displayName)
                tieUriPhoto.setText(user.photoUrl.toString())
                Glide.with(this@ProfileFragment)
                    .load(user.photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_access_time)
                    .error(R.drawable.ic_broken_image)
                    .centerCrop()
                    .circleCrop()
                    .into(ibProfile)
            }
            setupActionBar()
        }
    }

    private fun configButtons() {
        binding.btnUpdateProfile.setOnClickListener {
            binding.tieUriPhoto.clearFocus()
            binding.tieFullName.clearFocus()
            updateUserProfile()
        }
    }

    private fun setupActionBar() {
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            it.supportActionBar?.title = getString(R.string.profile_title)
            setHasOptionsMenu(true)
        }
    }

    private fun updateUserProfile() {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            val profileUpdate = UserProfileChangeRequest
                .Builder()
                .setDisplayName(binding.tieFullName.text.toString().trim())
                .setPhotoUri(Uri.parse(binding.tieUriPhoto.text.toString().trim()))
                .build()

            user.updateProfile(profileUpdate)
                .addOnSuccessListener {
                    (activity as? MainAux)?.updateTitle(user)
                    context?.showToast(R.string.profile_update)
                    activity?.onBackPressed()
                }.addOnFailureListener {
                    context?.showToast(R.string.error_profile_update)
                }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
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
            setHasOptionsMenu(false)
        }
    }
}