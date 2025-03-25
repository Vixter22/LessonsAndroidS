package com.example.log_reg

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.log_reg.data.AppDatabase
import com.example.log_reg.data.Order
import com.example.log_reg.data.OrderItem
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryFragment : Fragment() {

    private val userId: Int = 1
    private lateinit var rvOrderHistory: RecyclerView
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter
    private var orderHistoryList: MutableList<OrderHistoryDisplayItem> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ivBackOrder = view.findViewById<ImageView>(R.id.ivBackOrder)
        ivBackOrder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        rvOrderHistory = view.findViewById(R.id.rvOrderHistory)
        rvOrderHistory.layoutManager = LinearLayoutManager(context)
        orderHistoryAdapter = OrderHistoryAdapter(orderHistoryList)
        rvOrderHistory.adapter = orderHistoryAdapter

        loadOrderHistory()
    }

    private fun loadOrderHistory() {
        Thread {
            try {
                val db = AppDatabase.getInstance(requireContext())
                // Отримуємо замовлення для користувача
                val orders: List<Order> = db.orderDao().getOrdersForUser(userId)
                val displayList = mutableListOf<OrderHistoryDisplayItem>()
                orders.forEach { order ->
                    // Отримуємо позиції замовлення
                    val orderItems: List<OrderItem> = db.orderItemDao().getOrderItemsForOrder(order.id)
                    // Для кожного OrderItem отримуємо дані продукту
                    val itemDisplayList = orderItems.mapNotNull { orderItem ->
                        val product = db.productDao().getProductByIdSync(orderItem.productId)
                        if (product != null) {
                            OrderItemDisplay(orderItem, product)
                        } else null
                    }
                    displayList.add(OrderHistoryDisplayItem(order, itemDisplayList))
                }
                // Сортуємо замовлення за датою (новіші зверху)
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                displayList.sortByDescending { sdf.parse(it.order.orderDate) }
                requireActivity().runOnUiThread {
                    orderHistoryList.clear()
                    orderHistoryList.addAll(displayList)
                    orderHistoryAdapter.notifyDataSetChanged()
                }
            } catch (ex: Exception) {
                Log.e("OrderHistoryFragment", "Помилка завантаження історії замовлень: ${ex.message}")
            }
        }.start()
    }
}
