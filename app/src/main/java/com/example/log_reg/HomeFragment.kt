package com.example.log_reg

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.log_reg.data.AppDatabase
import com.example.log_reg.data.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private var currentLiveData: LiveData<List<Product>>? = null
    private var currentUserId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Отримуємо userId з SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        currentUserId = sharedPref.getInt("current_user_id", -1)

        if (currentUserId == -1) {
            Log.e("HomeFragment", "User ID не знайдено в SharedPreferences")
        } else {
            Log.d("HomeFragment", "Поточний userId: $currentUserId")
        }

        // Ініціалізація RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Отримання бази даних та DAO
        val db = AppDatabase.getInstance(requireContext())
        val productDao = db.productDao()
        val wishlistDao = db.wishlistItemDao()
        val cartItemDao = db.cartItemDao()

        // Ініціалізація адаптера з актуальним userId
        productAdapter = ProductAdapter(emptyList(), wishlistDao, cartItemDao, currentUserId)
        recyclerView.adapter = productAdapter

        // Клік по сердечку (перехід до Wishlist)
        val heartIcon = view.findViewById<ImageView>(R.id.ivWishlist)
        heartIcon.setOnClickListener {
            val wishlistFragment = WishlistFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, wishlistFragment)
                .addToBackStack(null)
                .commit()
        }

        // Вставка тестових продуктів
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val existingProducts = productDao.getAllProductsSync()
                if (existingProducts.isEmpty()) {
                    productDao.insertAll(
                        listOf(
                            Product(
                                name = "Локшина Miliket зі смаком курки",
                                description = "",
                                price = 34.50,
                                image = "https://images.prom.ua/6453514608_w640_h640_6453514608.jpg",
                                stock = 1
                            ),
                            Product(
                                name = "Локшина Miliket зі смаком креветок",
                                description = "",
                                price = 34.50,
                                image = "https://images.prom.ua/6450223248_w640_h640_lokshina-shvidkogo-prigotuvannya.jpg",
                                stock = 1
                            ),
                            Product(
                                name = "Локшина Том Ям зі смаком креветок",
                                description = "",
                                price = 64.50,
                                image = "https://images.prom.ua/6453611360_w640_h640_lokshina-shvidkogo-prigotuvannya.jpg",
                                stock = 1
                            ),
                            Product(
                                name = "Локшина Hao Hao зі смаком креветок",
                                description = "",
                                price = 25.0,
                                image = "https://images.prom.ua/6453560647_w640_h640_lokshina-shvidkogo-prigotuvannya.jpg",
                                stock = 1
                            )
                        )
                    )
                    Log.d("HomeFragment", "Тестові продукти вставлено.")
                }
            } catch (ex: Exception) {
                Log.e("HomeFragment", "Помилка вставки продуктів: ${ex.message}")
            }
        }

        // Пошук
        val searchEditText = view.findViewById<EditText>(R.id.etSearch)

        fun observeProducts(query: String) {
            currentLiveData?.removeObservers(viewLifecycleOwner)
            currentLiveData = if (query.isEmpty()) {
                productDao.getAllProducts()
            } else {
                productDao.getProductsByName(query)
            }
            currentLiveData?.observe(viewLifecycleOwner) { productList ->
                productAdapter.updateList(productList)
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                val query = editable?.toString()?.trim() ?: ""
                observeProducts(query)
            }
        })

        // Стартове спостереження
        observeProducts("")
    }
}
