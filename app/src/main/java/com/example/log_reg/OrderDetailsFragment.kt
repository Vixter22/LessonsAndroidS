package com.example.log_reg.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.log_reg.R
import com.example.log_reg.data.AppDatabase
import com.example.log_reg.data.Order
import com.example.log_reg.data.OrderItem
import com.example.log_reg.data.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Допоміжний клас для відображення товару разом з інформацією про продукт
data class OrderItemDisplay(val orderItem: OrderItem, val product: Product)

class OrderDetailsFragment : Fragment() {

    companion object {
        private const val ARG_ORDER_ID = "order_id"

        fun newInstance(orderId: Int): OrderDetailsFragment {
            val fragment = OrderDetailsFragment()
            val args = Bundle()
            args.putInt(ARG_ORDER_ID, orderId)
            fragment.arguments = args
            return fragment
        }
    }

    // Загальні дані замовлення
    private lateinit var tvOrderNumber: TextView
    private lateinit var tvUserId: TextView
    private lateinit var tvOrderDate: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvRecipientName: TextView
    private lateinit var tvRecipientEmail: TextView
    private lateinit var tvDeliveryInfo: TextView
    private lateinit var tvPaymentMethod: TextView
    private lateinit var tvTotalCost: TextView

    // Кнопка для зміни статусу
    private lateinit var btnChangeStatus: Button

    // RecyclerView для товарів замовлення
    private lateinit var rvOrderItems: RecyclerView

    // Поточне замовлення (щоб можна було оновлювати статус)
    private var currentOrder: Order? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Прив'язка view для загальних даних
        tvOrderNumber = view.findViewById(R.id.tvOrderNumber)
        tvUserId = view.findViewById(R.id.tvUserId)
        tvOrderDate = view.findViewById(R.id.tvOrderDate)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvRecipientName = view.findViewById(R.id.tvRecipientName)
        tvRecipientEmail = view.findViewById(R.id.tvRecipientEmail)
        tvDeliveryInfo = view.findViewById(R.id.tvDeliveryInfo)
        tvPaymentMethod = view.findViewById(R.id.tvPaymentMethod)
        tvTotalCost = view.findViewById(R.id.tvTotalCost)
        btnChangeStatus = view.findViewById(R.id.btnChangeStatus)
        btnChangeStatus.backgroundTintList = null

        // Прив'язка RecyclerView для товарів замовлення
        rvOrderItems = view.findViewById(R.id.rvOrderItems)
        rvOrderItems.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Обробка кліку для кнопки "Назад"
        view.findViewById<ImageView>(R.id.ivBack)?.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Отримання id замовлення з аргументів
        val orderId = arguments?.getInt(ARG_ORDER_ID) ?: return

        // Завантаження даних про замовлення з БД
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(requireContext())
                // Отримання даних замовлення
                val order: Order? = withContext(Dispatchers.IO) {
                    db.orderDao().getOrderById(orderId)
                }
                if (order != null) {
                    currentOrder = order
                    // Встановлення загальної інформації
                    tvOrderNumber.text = order.id.toString()
                    tvUserId.text = order.userId.toString()
                    tvOrderDate.text = order.orderDate
                    tvStatus.text = order.status
                    tvRecipientName.text = order.recipientName
                    tvRecipientEmail.text = order.recipientEmail
                    tvDeliveryInfo.text = order.deliveryInfo
                    tvPaymentMethod.text = order.paymentMethod
                    tvTotalCost.text = order.totalCost.toString()

                    // Налаштування кнопки зміни статусу
                    btnChangeStatus.setOnClickListener {
                        showChangeStatusDialog(order)
                    }

                    // Завантаження позицій замовлення
                    val orderItemDisplays = withContext(Dispatchers.IO) {
                        val orderItems: List<OrderItem> = db.orderItemDao().getOrderItemsForOrder(order.id)
                        orderItems.mapNotNull { orderItem ->
                            val product: Product? = db.productDao().getProductByIdSync(orderItem.productId)
                            if (product != null) OrderItemDisplay(orderItem, product) else null
                        }
                    }
                    rvOrderItems.adapter = OrderItemAdapter(orderItemDisplays)
                } else {
                    tvOrderNumber.text = "Замовлення не знайдено."
                }
            } catch (ex: Exception) {
                Log.e("OrderDetailsFragment", "Error loading order details: ${ex.message}")
                tvOrderNumber.text = "Помилка завантаження даних."
            }
        }
    }

    // Метод для відображення діалогу зміни статусу
    private fun showChangeStatusDialog(order: Order) {
        val statusOptions = arrayOf("оформлено", "виконано", "скасовано")
        AlertDialog.Builder(requireContext())
            .setTitle("Виберіть новий статус")
            .setItems(statusOptions) { _, which ->
                val newStatus = statusOptions[which]
                updateOrderStatus(order, newStatus)
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    // Метод для оновлення статусу замовлення в БД
    private fun updateOrderStatus(order: Order, newStatus: String) {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(requireContext())
                // Оновлюємо об'єкт замовлення
                val updatedOrder = order.copy(status = newStatus)
                withContext(Dispatchers.IO) {
                    db.orderDao().updateOrder(updatedOrder)
                }
                currentOrder = updatedOrder
                tvStatus.text = newStatus
            } catch (ex: Exception) {
                Log.e("OrderDetailsFragment", "Error updating order status: ${ex.message}")
            }
        }
    }
}
