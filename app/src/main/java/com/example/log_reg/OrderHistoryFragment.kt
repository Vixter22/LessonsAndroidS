package com.example.log_reg

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.log_reg.data.AppDatabase
import com.example.log_reg.data.Order
import com.example.log_reg.data.OrderItem
import java.text.SimpleDateFormat
import java.util.Locale

class OrderHistoryFragment : Fragment() {

    // Ідентифікатор користувача
    private var userId: Int = -1
    // RecyclerView для відображення історії замовлень
    private lateinit var recyclerViewOrderHistory: RecyclerView
    // Адаптер для RecyclerView
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter
    // Список для відображення даних історії замовлень
    private var orderHistoryDisplayList: MutableList<OrderHistoryDisplayItem> = mutableListOf()
    // TextView для повідомлення про порожню історію замовлень
    private lateinit var textViewEmptyOrderHistory: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Завантаження макету фрагмента
        return inflater.inflate(R.layout.fragment_order_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Отримання ідентифікатора користувача з SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("current_user_id", -1)
        if (userId == -1) {
            Log.e("OrderHistoryFragment", "Ідентифікатор користувача не знайдено в SharedPreferences")
        } else {
            Log.d("OrderHistoryFragment", "Поточний ідентифікатор користувача: $userId")
        }

        // Налаштування кнопки "Назад"
        val imageViewBackOrder = view.findViewById<ImageView>(R.id.ivBackOrder)
        imageViewBackOrder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Ініціалізація TextView для повідомлення про порожню історію замовлень
        textViewEmptyOrderHistory = view.findViewById(R.id.tvEmptyHistory)
        // Ініціалізація RecyclerView для історії замовлень
        recyclerViewOrderHistory = view.findViewById(R.id.rvOrderHistory)
        recyclerViewOrderHistory.layoutManager = LinearLayoutManager(context)
        orderHistoryAdapter = OrderHistoryAdapter(orderHistoryDisplayList)
        recyclerViewOrderHistory.adapter = orderHistoryAdapter

        // Завантаження даних історії замовлень
        loadOrderHistory()
    }

    private fun loadOrderHistory() {
        Thread {
            try {
                // Отримання екземпляра бази даних
                val databaseInstance = AppDatabase.getInstance(requireContext())
                // Отримання списку замовлень для поточного користувача
                val listOfOrders: List<Order> = databaseInstance.orderDao().getOrdersForUser(userId)
                // Створення списку для відображення даних замовлень
                val displayList: MutableList<OrderHistoryDisplayItem> = mutableListOf()

                // Для кожного замовлення отримуємо позиції та дані продуктів
                listOfOrders.forEach { order ->
                    // Отримання списку позицій замовлення
                    val listOfOrderItems: List<OrderItem> = databaseInstance.orderItemDao().getOrderItemsForOrder(order.id)
                    // Отримання даних продуктів для кожної позиції замовлення
                    val orderItemDisplayList = listOfOrderItems.mapNotNull { orderItem ->
                        val product = databaseInstance.productDao().getProductByIdSync(orderItem.productId)
                        if (product != null) {
                            OrderItemDisplay(orderItem, product)
                        } else {
                            null
                        }
                    }
                    // Додавання даних замовлення до списку для відображення
                    displayList.add(OrderHistoryDisplayItem(order, orderItemDisplayList))
                }

                // Сортування замовлень за датою (новіші замовлення спочатку)
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                displayList.sortByDescending { simpleDateFormat.parse(it.order.orderDate) }

                // Оновлення UI на головному потоці
                requireActivity().runOnUiThread {
                    orderHistoryDisplayList.clear()
                    orderHistoryDisplayList.addAll(displayList)
                    orderHistoryAdapter.notifyDataSetChanged()

                    // Якщо список замовлень порожній, відображається повідомлення про порожню історію замовлень
                    if (orderHistoryDisplayList.isEmpty()) {
                        textViewEmptyOrderHistory.visibility = View.VISIBLE
                        recyclerViewOrderHistory.visibility = View.GONE
                    } else {
                        textViewEmptyOrderHistory.visibility = View.GONE
                        recyclerViewOrderHistory.visibility = View.VISIBLE
                    }
                }
            } catch (exception: Exception) {
                Log.e("OrderHistoryFragment", "Помилка завантаження історії замовлень: ${exception.message}")
            }
        }.start()
    }
}
