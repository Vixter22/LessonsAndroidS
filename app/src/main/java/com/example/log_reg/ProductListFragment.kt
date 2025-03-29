package com.example.log_reg.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.log_reg.R
import com.example.log_reg.data.AppDatabase
import com.example.log_reg.data.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductListFragment : Fragment() {

    private lateinit var ivBack: ImageView
    private lateinit var ivAddProduct: ImageView
    private lateinit var tvHeaderId: TextView
    private lateinit var tvHeaderName: TextView
    private lateinit var tvHeaderPrice: TextView
    private lateinit var etSearch: EditText
    private lateinit var rvProducts: RecyclerView
    private lateinit var productListAdapter: ProductListAdapter

    private var currentSortField: String = "id"
    private var ascending: Boolean = false
    private var allProducts: List<Product> = emptyList()

    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_product_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivBack = view.findViewById(R.id.ivBack)
        ivAddProduct = view.findViewById(R.id.ivAddProduct)
        tvHeaderId = view.findViewById(R.id.tvHeaderId)
        tvHeaderName = view.findViewById(R.id.tvHeaderName)
        tvHeaderPrice = view.findViewById(R.id.tvHeaderPrice)
        etSearch = view.findViewById(R.id.etSearch)
        rvProducts = view.findViewById(R.id.rvProducts)

        ivBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        ivAddProduct.setOnClickListener {
            // Відкриваємо фрагмент для додавання товару
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddProductFragment())
                .addToBackStack(null)
                .commit()
        }

        // Налаштовуємо RecyclerView
        rvProducts.layoutManager = LinearLayoutManager(requireContext())
        productListAdapter = ProductListAdapter(emptyList())
        rvProducts.adapter = productListAdapter

        // Callback для видалення товару
        productListAdapter.onDeleteClick = { product ->
            AlertDialog.Builder(requireContext())
                .setTitle("Видалення товару")
                .setMessage("Ви впевнені, що хочете видалити цей товар?")
                .setPositiveButton("Так") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        database.productDao().deleteProduct(product.id ?: 0)
                    }
                }
                .setNegativeButton("Ні", null)
                .show()
        }

        database = AppDatabase.getInstance(requireContext())

        // Первинне завантаження всіх товарів
        database.productDao().getAllProducts().observe(viewLifecycleOwner, Observer { products ->
            allProducts = products
            sortProducts()
        })

        // Обробка кліків для сортування за стовпчиками
        tvHeaderId.setOnClickListener { toggleSort("id") }
        tvHeaderName.setOnClickListener { toggleSort("name") }
        tvHeaderPrice.setOnClickListener { toggleSort("price") }

        // Обробка пошуку: при зміні тексту оновлюємо список товарів
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    // Якщо поле порожнє – відображаємо всі товари
                    database.productDao().getAllProducts().observe(viewLifecycleOwner, Observer { products ->
                        allProducts = products
                        sortProducts()
                    })
                } else {
                    // Інакше – шукаємо товари за назвою
                    database.productDao().getProductsByName(query).observe(viewLifecycleOwner, Observer { products ->
                        allProducts = products
                        sortProducts()
                    })
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun toggleSort(field: String) {
        if (currentSortField == field) {
            ascending = !ascending
        } else {
            currentSortField = field
            ascending = true
        }
        sortProducts()
    }

    private fun sortProducts() {
        lifecycleScope.launch(Dispatchers.Default) {
            val sorted = when (currentSortField) {
                "id" -> if (ascending) allProducts.sortedBy { it.id } else allProducts.sortedByDescending { it.id }
                "name" -> if (ascending) allProducts.sortedBy { it.name } else allProducts.sortedByDescending { it.name }
                "price" -> if (ascending) allProducts.sortedBy { it.price } else allProducts.sortedByDescending { it.price }
                else -> allProducts
            }
            withContext(Dispatchers.Main) {
                productListAdapter.setProducts(sorted)
            }
        }
    }
}
