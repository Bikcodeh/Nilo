package com.bikcode.nilo.presentation.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.bikcode.nilo.R
import com.bikcode.nilo.data.model.ProductDTO
import com.bikcode.nilo.databinding.ActivityMainBinding
import com.bikcode.nilo.presentation.adapter.ProductAdapter
import com.bikcode.nilo.presentation.listener.MainAux
import com.bikcode.nilo.presentation.listener.OnProductListener
import com.bikcode.nilo.presentation.ui.fragment.cart.CartFragment
import com.bikcode.nilo.presentation.ui.fragment.detail.DetailFragment
import com.bikcode.nilo.presentation.ui.fragment.profile.ProfileFragment
import com.bikcode.nilo.presentation.util.Constants.PRODUCTS_COLLECTION
import com.bikcode.nilo.presentation.util.Constants.PROPERTY_TOKEN
import com.bikcode.nilo.presentation.util.Constants.TOKENS_COLLECTION
import com.bikcode.nilo.presentation.util.Constants.USERS_COLLECTION
import com.bikcode.nilo.presentation.util.showToast
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest

class MainActivity : AppCompatActivity(), OnProductListener, MainAux {

    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private lateinit var productAdapter: ProductAdapter
    private lateinit var firestoreListener: ListenerRegistration
    private var queryPagination: Query? = null
    private var productSelected: ProductDTO? = null
    private val products = mutableListOf<ProductDTO>()
    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val response = IdpResponse.fromResultIntent(it.data)

            if (it.resultCode == RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show()

                    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                    val token = preferences.getString(PROPERTY_TOKEN, null)
                    token?.let {
                        val db = FirebaseFirestore.getInstance()
                        val tokenMap = hashMapOf(Pair(PROPERTY_TOKEN, token))
                        db.collection(USERS_COLLECTION)
                            .document(user.uid)
                            .collection(TOKENS_COLLECTION)
                            .add(tokenMap).addOnSuccessListener {
                                preferences.edit {
                                    putString(PROPERTY_TOKEN, null)
                                }
                            }
                            .addOnFailureListener {

                            }
                    }
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
            fragment.show(supportFragmentManager.beginTransaction(),
                CartFragment::class.java.simpleName)
        }
    }

    private fun setupRecycler() {
        productAdapter = ProductAdapter(mutableListOf(ProductDTO()))
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

    override fun loadMore() {
        val db = FirebaseFirestore.getInstance()
        val productRef = db.collection(PRODUCTS_COLLECTION)

        queryPagination?.let {
            it.addSnapshotListener { snapshots, error ->
                if (error != null) {
                    showToast(R.string.error_fetching_data)
                    return@addSnapshotListener
                }

                snapshots?.let { items ->
                    val lastItem = items.documents[items.size() - 1]
                    queryPagination = productRef.startAfter(lastItem).limit(2)

                    for (snapshot in items.documentChanges) {
                        val product = snapshot.document.toObject(ProductDTO::class.java)
                        product.id = snapshot.document.id
                        when (snapshot.type) {
                            DocumentChange.Type.ADDED -> productAdapter.add(product)
                            DocumentChange.Type.REMOVED -> productAdapter.delete(product)
                            DocumentChange.Type.MODIFIED -> productAdapter.update(product)
                        }
                    }
                }
            }
        }
    }

    private fun configAuth() {

        firebaseAuth = FirebaseAuth.getInstance()

        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser != null) {
                updateTitle(auth.currentUser!!)
                binding.lyProgress.visibility = View.GONE
                binding.nsvProducts.visibility = View.VISIBLE
            } else {
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build(),
                    AuthUI.IdpConfig.FacebookBuilder().build(),
                    AuthUI.IdpConfig.PhoneBuilder().build()
                )

                val loginView = AuthMethodPickerLayout.Builder(R.layout.view_login)
                    .setEmailButtonId(R.id.btnEmail)
                    .setGoogleButtonId(R.id.btnGoogle)
                    .setFacebookButtonId(R.id.btnFacebook)
                    .setPhoneButtonId(R.id.btnPhone)
                    .setTosAndPrivacyPolicyId(R.id.tvPolicy)
                    .build()


                resultLauncher.launch(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .setTosAndPrivacyPolicyUrls("https://www.facebook.com/",
                            "https://www.facebook.com/")
                        .setAuthMethodPickerLayout(loginView)
                        .setTheme(R.style.LoginTheme)
                        .build()
                )
            }
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                var info = packageManager.getPackageInfo(
                    "com.bikcode.nilo",
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                for (signature in info.signingInfo.apkContentsSigners) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    Log.d(" API >= 28 KeyHas: ", Base64.encodeToString(md.digest(), Base64.DEFAULT))
                }
            } else {
                val info = packageManager.getPackageInfo(
                    "com.bikcode.nilo",
                    PackageManager.GET_SIGNATURES
                )
                for (signature in info.signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    Log.d("API < 28 KeyHash: ", Base64.encodeToString(md.digest(), Base64.DEFAULT))
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun setupFirestoreRealtime() {
        val db = FirebaseFirestore.getInstance()
        val productRef = db.collection(PRODUCTS_COLLECTION)

        firestoreListener = productRef
            .limit(2)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    showToast(R.string.error_fetching_data)
                    return@addSnapshotListener
                }

                snapshots?.let { items ->
                    val lastItem = items.documents[items.size() - 1]
                    queryPagination = productRef.startAfter(lastItem).limit(2)

                    for (snapshot in items.documentChanges) {
                        val product = snapshot.document.toObject(ProductDTO::class.java)
                        product.id = snapshot.document.id
                        when (snapshot.type) {
                            DocumentChange.Type.ADDED -> productAdapter.add(product)
                            DocumentChange.Type.REMOVED -> productAdapter.delete(product)
                            DocumentChange.Type.MODIFIED -> productAdapter.update(product)
                        }
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
            R.id.action_history -> startActivity(Intent(this, OrderActivity::class.java))
            R.id.action_profile -> {
                val fragment = ProfileFragment()
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.containerMain, fragment)
                    .addToBackStack(null)
                    .commit()

                showButton(false)
            }
            R.id.actions_settings -> startActivity(Intent(this, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(product: ProductDTO) {
        val index = products.indexOf(product)
        productSelected = if (index != -1) {
            products[index]
        } else {
            product
        }
        val fragment = DetailFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.containerMain, fragment)
            .addToBackStack(null)
            .commit()
        showButton(isVisible = false)

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
            param(FirebaseAnalytics.Param.ITEM_ID, product.id!!)
            param(FirebaseAnalytics.Param.ITEM_NAME, product.name!!)
        }
    }

    override fun getProductsCart(): MutableList<ProductDTO> = products

    override fun getProductSelected(): ProductDTO? = productSelected

    override fun showButton(isVisible: Boolean) {
        val visible = if (isVisible) View.VISIBLE else View.GONE
        binding.btnShoppingCart.visibility = visible
    }

    override fun addToCart(productDTO: ProductDTO) {
        val index = products.indexOf(productDTO)
        if (index != -1) {
            products[index] = productDTO
        } else {
            products.add(productDTO)
        }

        updateTotal()
    }

    override fun updateTitle(user: FirebaseUser) {
        supportActionBar?.title = user.displayName
    }

    override fun updateTotal() {
        var total = 0.0
        products.forEach { productDTO ->
            total += productDTO.totalPrice()
        }

        if (total == 0.0) {
            binding.tvTotal.text = getString(R.string.product_empty_cart)
        } else {
            binding.tvTotal.text = getString(R.string.cart_full, total)
        }
    }

    override fun clearCart() {
        products.clear()
    }
}