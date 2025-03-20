package com.example.log_reg

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ініціалізація RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Отримання екземпляра бази даних та DAO
        val db = AppDatabase.getInstance(requireContext())
        val productDao = db.productDao()
        val wishlistDao = db.wishlistItemDao()
        val cartItemDao = db.cartItemDao()

        // Ініціалізація адаптера (userId = 1, перевірте, що в таблиці User існує запис з id 1)
        productAdapter = ProductAdapter(emptyList(), wishlistDao, cartItemDao, 1)
        recyclerView.adapter = productAdapter

        // Обробка кліку на іконку вішліста для переходу до WishlistFragment
        val heartIcon = view.findViewById<ImageView>(R.id.ivWishlist)
        heartIcon.setOnClickListener {
            val wishlistFragment = WishlistFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, wishlistFragment)
                .addToBackStack(null)
                .commit()
        }

        // Вставка тестових продуктів, якщо таблиця порожня
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

        // Спостереження за списком продуктів через LiveData
        productDao.getAllProducts().observe(viewLifecycleOwner) { productList ->
            productAdapter.updateList(productList)
        }
    }
}
