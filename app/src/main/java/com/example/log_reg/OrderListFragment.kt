package com.example.log_reg.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.log_reg.OrderListAdapter
import com.example.log_reg.R
import com.example.log_reg.data.AppDatabase
import com.example.log_reg.data.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var orderListAdapter: OrderListAdapter
    private var allOrders: List<Order> = emptyList()

    // Поля для сортування
    private var currentSortField: String = "id" // може бути "id", "status", "orderDate", "totalCost"
    private var ascending: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_order_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обробка кліку на стрілку "Назад"
        val ivBack = view.findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        recyclerView = view.findViewById(R.id.rvOrderList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        orderListAdapter = OrderListAdapter(emptyList())
        recyclerView.adapter = orderListAdapter

        // Отримуємо посилання на заголовки для сортування
        val tvHeaderOrderId = view.findViewById<TextView>(R.id.tvHeaderOrderId)
        val tvHeaderStatus = view.findViewById<TextView>(R.id.tvHeaderStatus)
        val tvHeaderDate = view.findViewById<TextView>(R.id.tvHeaderDate)
        val tvHeaderTotal = view.findViewById<TextView>(R.id.tvHeaderTotal)

        tvHeaderOrderId.setOnClickListener {
            toggleSort("id")
        }
        tvHeaderStatus.setOnClickListener {
            toggleSort("status")
        }
        tvHeaderDate.setOnClickListener {
            toggleSort("orderDate")
        }
        tvHeaderTotal.setOnClickListener {
            toggleSort("totalCost")
        }

        loadOrders()
    }

    private fun loadOrders() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(requireContext())
                // Завантажуємо всі замовлення (адмінка)
                allOrders = withContext(Dispatchers.IO) {
                    db.orderDao().getAllOrders()
                }
                sortAndDisplayOrders()
            } catch (ex: Exception) {
                Log.e("OrderListFragment", "Помилка завантаження замовлень: ${ex.message}")
            }
        }
    }

    private fun sortAndDisplayOrders() {
        val sorted = when (currentSortField) {
            "id" -> if (ascending) allOrders.sortedBy { it.id } else allOrders.sortedByDescending { it.id }
            "status" -> if (ascending) allOrders.sortedBy { it.status } else allOrders.sortedByDescending { it.status }
            "orderDate" -> if (ascending) allOrders.sortedBy { it.orderDate } else allOrders.sortedByDescending { it.orderDate }
            "totalCost" -> if (ascending) allOrders.sortedBy { it.totalCost } else allOrders.sortedByDescending { it.totalCost }
            else -> allOrders
        }
        orderListAdapter.updateList(sorted)
    }

    private fun toggleSort(field: String) {
        if (currentSortField == field) {
            ascending = !ascending
        } else {
            currentSortField = field
            ascending = true
        }
        sortAndDisplayOrders()
    }
}
