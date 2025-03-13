package com.example.log_reg

import android.os.Bundle
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

        // Ініціалізуємо RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        // "2" означає два стовпчики

        // Ініціалізуємо адаптер із порожнім списком
        productAdapter = ProductAdapter(emptyList())
        recyclerView.adapter = productAdapter

        // Обробка кліку на сердечко
        val heartIcon = view.findViewById<ImageView>(R.id.ivWishlist)
        heartIcon.setOnClickListener {
            // Перехід до WishlistFragment
            val wishlistFragment = WishlistFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, wishlistFragment) // R.id.fragmentContainer – контейнер для фрагментів
                .addToBackStack(null)
                .commit()
        }

        // Отримуємо DAO
        val db = AppDatabase.getInstance(requireContext())
        val productDao = db.productDao()

        // Перевіряємо, чи є в БД хоч один продукт; якщо немає — додаємо 4 тестові
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val existingProducts = productDao.getAllProductsSync()
            if (existingProducts.isEmpty()) {
                productDao.insertAll(
                    listOf(
                        Product(
                            name = "Локшина Miliket зі смаком курки",
                            description = "",
                            price = 34.99,
                            image = "https://images.prom.ua/6453514608_w640_h640_6453514608.jpg",
                            stock = 1
                        ),
                        Product(
                            name = "Локшина Miliket зі смаком креветок",
                            description = "",
                            price = 34.99,
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
            }
        }

        // Спостерігаємо за списком продуктів через LiveData
        productDao.getAllProducts().observe(viewLifecycleOwner) { productList ->
            productAdapter.updateList(productList)
        }
    }
}
