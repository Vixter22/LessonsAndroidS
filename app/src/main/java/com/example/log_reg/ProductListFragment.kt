package com.example.log_reg.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var tvHeaderId: TextView
    private lateinit var tvHeaderName: TextView
    private lateinit var tvHeaderPrice: TextView
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
        tvHeaderId = view.findViewById(R.id.tvHeaderId)
        tvHeaderName = view.findViewById(R.id.tvHeaderName)
        tvHeaderPrice = view.findViewById(R.id.tvHeaderPrice)
        rvProducts = view.findViewById(R.id.rvProducts)

        ivBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Налаштовуємо RecyclerView
        rvProducts.layoutManager = LinearLayoutManager(requireContext())
        productListAdapter = ProductListAdapter(emptyList())
        rvProducts.adapter = productListAdapter

        // Встановлюємо callback для видалення товару з підтвердженням
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

        // Підписка на зміни списку товарів
        database.productDao().getAllProducts().observe(viewLifecycleOwner, Observer { products ->
            allProducts = products
            sortProducts()
        })

        // Обробка кліків для сортування за стовпчиками
        tvHeaderId.setOnClickListener { toggleSort("id") }
        tvHeaderName.setOnClickListener { toggleSort("name") }
        tvHeaderPrice.setOnClickListener { toggleSort("price") }
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
