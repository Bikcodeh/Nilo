package com.bikcode.nilo.presentation.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.bikcode.nilo.R
import com.bikcode.nilo.data.model.ProductDTO
import com.bikcode.nilo.databinding.ActivityMainBinding
import com.bikcode.nilo.presentation.adapter.ProductAdapter
import com.bikcode.nilo.presentation.listener.OnProductListener
import com.bikcode.nilo.presentation.ui.fragment.cart.CartFragment
import com.bikcode.nilo.presentation.util.Constants.PRODUCTS_COLLECTION
import com.bikcode.nilo.presentation.util.showToast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity(), OnProductListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private lateinit var productAdapter: ProductAdapter
    private lateinit var firestoreListener: ListenerRegistration

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val response = IdpResponse.fromResultIntent(it.data)

            if (it.resultCode == RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Not logged", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (response == null) {
                    Toast.makeText(this, "See you", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    response.error?.let {
                        if (it.errorCode == ErrorCodes.NO_NETWORK) {
                            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error code: ${it.errorCode}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configAuth()
        setupRecycler()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnShoppingCart.setOnClickListener {
            val fragment = CartFragment()
            fragment.show(supportFragmentManager.beginTransaction(), CartFragment::class.java.simpleName)
        }
    }

    private fun setupRecycler() {
        productAdapter = ProductAdapter()
        productAdapter.setListener(this)
        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(
                this@MainActivity,
                3,
                GridLayoutManager.HORIZONTAL,
                false
            )
            adapter = productAdapter
        }
    }

    private fun configAuth() {

        firebaseAuth = FirebaseAuth.getInstance()

        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser != null) {
                supportActionBar?.title = auth.currentUser?.displayName
                binding.lyProgress.visibility = View.GONE
                binding.nsvProducts.visibility = View.VISIBLE
            } else {
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build()
                )

                resultLauncher.launch(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build()
                )
            }
        }
    }

    private fun setupFirestoreRealtime() {
        val db = FirebaseFirestore.getInstance()
        val productRef = db.collection(PRODUCTS_COLLECTION)

        firestoreListener = productRef.addSnapshotListener { snapshots, error ->
            if(error != null) {
                showToast(R.string.error_fetching_data)
                return@addSnapshotListener
            }

            for(snapshot in snapshots!!.documentChanges) {
                val product = snapshot.document.toObject(ProductDTO::class.java)
                product.id = snapshot.document.id
                when(snapshot.type) {
                    DocumentChange.Type.ADDED -> productAdapter.add(product)
                    DocumentChange.Type.REMOVED -> productAdapter.delete(product)
                    DocumentChange.Type.MODIFIED -> productAdapter.update(product)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
        setupFirestoreRealtime()

    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
        firestoreListener.remove()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sign_out -> {
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Log out", Toast.LENGTH_SHORT).show()
                    }.addOnCompleteListener {
                        if (it.isSuccessful) {
                            binding.nsvProducts.visibility = View.GONE
                            binding.lyProgress.visibility = View.VISIBLE
                        } else {
                            Toast.makeText(this, "A problem has occurred", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(product: ProductDTO) {

    }
}