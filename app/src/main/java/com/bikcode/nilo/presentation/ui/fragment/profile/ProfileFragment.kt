package com.bikcode.nilo.presentation.ui.fragment.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bikcode.nilo.R
import com.bikcode.nilo.databinding.FragmentProfileBinding
import com.bikcode.nilo.presentation.listener.MainAux
import com.bikcode.nilo.presentation.util.Constants.MY_PHOTO
import com.bikcode.nilo.presentation.util.Constants.PATH_PRODUCTS_IMAGES
import com.bikcode.nilo.presentation.util.Constants.PATH_PROFILE
import com.bikcode.nilo.presentation.util.showToast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding get() = _binding!!
    private var photoSelectedUri: Uri? = null
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                photoSelectedUri = it.data?.data

                Glide.with(this@ProfileFragment)
                    .load(photoSelectedUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_access_time)
                    .error(R.drawable.ic_broken_image)
                    .centerCrop()
                    .circleCrop()
                    .into(binding.ibProfile)
            }
        }

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
                //tieUriPhoto.setText(user.photoUrl.toString())
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
            //binding.tieUriPhoto.clearFocus()
            binding.tieFullName.clearFocus()
            FirebaseAuth.getInstance().currentUser?.let { user ->
                if (photoSelectedUri == null) {
                    updateUserProfile(user, Uri.parse(""))
                } else {
                    uploadReduceImage(user)
                }
            }
        }

        binding.ibProfile.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
            resultLauncher.launch(it)
        }
    }

    private fun setupActionBar() {
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            it.supportActionBar?.title = getString(R.string.profile_title)
            setHasOptionsMenu(true)
        }
    }

    private fun updateUserProfile(user: FirebaseUser, uri: Uri) {
        val profileUpdate = UserProfileChangeRequest
            .Builder()
            .setDisplayName(binding.tieFullName.text.toString().trim())
            .setPhotoUri(uri)
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

    private fun uploadReduceImage(user: FirebaseUser) {

        val profileRef =
            FirebaseStorage.getInstance().reference.child(user.uid).child(PATH_PROFILE)
                .child(MY_PHOTO)

        photoSelectedUri?.let { uri ->
            _binding?.let { binding ->
                binding.progressBar.isVisible = true

                getBitmapFromUri(uri)?.let { bitmap ->
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)

                    profileRef.putBytes(baos.toByteArray())
                        .addOnProgressListener {
                            val progress = (100 * it.bytesTransferred / it.totalByteCount).toInt()
                            it.run {
                                binding.progressBar.progress = progress
                                binding.tvProgress.text = String.format("%s%%", progress)
                            }
                        }
                        .addOnSuccessListener {
                            it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                                updateUserProfile(user, downloadUrl)
                            }
                        }.addOnFailureListener {
                            context?.showToast(R.string.error_sending_image)
                        }
                        .addOnCompleteListener {
                            binding.progressBar.isVisible = false
                            binding.tvProgress.text = ""
                        }
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        activity?.let {
            val bitmap = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(it.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(it.contentResolver, uri)
            }
            return getResizedImage(bitmap, 256)
        }
        return null
    }

    private fun getResizedImage(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        if (width <= maxSize && height <= maxSize) return image

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height / bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
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
            setHasOptionsMenu(false)
        }
    }
}