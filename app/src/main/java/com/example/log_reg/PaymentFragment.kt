package com.example.log_reg

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.log_reg.data.*
import java.text.SimpleDateFormat
import java.util.*

class PaymentFragment : Fragment() {

    private val userId: Int = 1
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var recyclerViewOrder: RecyclerView
    private lateinit var etRecipientName: EditText
    private lateinit var etRecipientEmail: EditText
    private lateinit var etCity: EditText
    private lateinit var etDepartment: EditText
    private lateinit var tvTotalCost: TextView
    private lateinit var btnPay: Button
    private lateinit var nestedScrollView: NestedScrollView

    // Збережемо завантажені дані замовлення для розрахунку вартості
    private var orderItems: List<CartDisplayItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обробка кліку на стрілку "назад"
        val ivBack = view.findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        nestedScrollView = view.findViewById(R.id.nestedScrollView)

        recyclerViewOrder = view.findViewById(R.id.recyclerViewOrder)
        recyclerViewOrder.layoutManager = LinearLayoutManager(context)
        recyclerViewOrder.addItemDecoration(
            DividerItemDecoration(
                recyclerViewOrder.context,
                (recyclerViewOrder.layoutManager as LinearLayoutManager).orientation
            )
        )
        orderAdapter = OrderAdapter(emptyList())
        recyclerViewOrder.adapter = orderAdapter

        etRecipientName = view.findViewById(R.id.etRecipientName)
        etRecipientEmail = view.findViewById(R.id.etRecipientEmail)
        etCity = view.findViewById(R.id.etCity)
        etDepartment = view.findViewById(R.id.etDepartment)

        val radioGroup = view.findViewById<RadioGroup>(R.id.rgPaymentMethod)
        val cardPaymentFields = view.findViewById<LinearLayout>(R.id.llCardPaymentFields)
        val rbCardPayment = view.findViewById<RadioButton>(R.id.rbCardPayment)
        val rbCashOnDelivery = view.findViewById<RadioButton>(R.id.rbCashOnDelivery)

        tvTotalCost = view.findViewById(R.id.tvTotalCost)
        btnPay = view.findViewById(R.id.btnPay)
        btnPay.backgroundTintList = null

        val redColor = Color.RED
        val defaultColor = Color.GRAY

        // Початкова установка способу оплати
        when (radioGroup.checkedRadioButtonId) {
            R.id.rbCardPayment -> {
                cardPaymentFields.visibility = View.VISIBLE
                rbCardPayment.buttonTintList = android.content.res.ColorStateList.valueOf(redColor)
                rbCashOnDelivery.buttonTintList = android.content.res.ColorStateList.valueOf(defaultColor)
            }
            R.id.rbCashOnDelivery -> {
                cardPaymentFields.visibility = View.GONE
                rbCashOnDelivery.buttonTintList = android.content.res.ColorStateList.valueOf(redColor)
                rbCardPayment.buttonTintList = android.content.res.ColorStateList.valueOf(defaultColor)
            }
            else -> {
                cardPaymentFields.visibility = View.GONE
                rbCardPayment.buttonTintList = android.content.res.ColorStateList.valueOf(defaultColor)
                rbCashOnDelivery.buttonTintList = android.content.res.ColorStateList.valueOf(defaultColor)
            }
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCardPayment -> {
                    cardPaymentFields.visibility = View.VISIBLE
                    rbCardPayment.buttonTintList = android.content.res.ColorStateList.valueOf(redColor)
                    rbCashOnDelivery.buttonTintList = android.content.res.ColorStateList.valueOf(defaultColor)
                    nestedScrollView.post {
                        nestedScrollView.smoothScrollTo(0, nestedScrollView.getChildAt(0).height)
                    }
                }
                R.id.rbCashOnDelivery -> {
                    cardPaymentFields.visibility = View.GONE
                    rbCashOnDelivery.buttonTintList = android.content.res.ColorStateList.valueOf(redColor)
                    rbCardPayment.buttonTintList = android.content.res.ColorStateList.valueOf(defaultColor)
                }
            }
        }

        // Завантаження даних замовлення та контактних даних користувача
        loadOrderItems()
        loadUserContactDetails()

        // Обробка кліку кнопки "Завершити оформлення"
        btnPay.setOnClickListener {
            Thread {
                try {
                    // Збір даних з форм
                    val recipientName = etRecipientName.text.toString().trim()
                    val recipientEmail = etRecipientEmail.text.toString().trim()
                    val city = etCity.text.toString().trim()
                    val department = etDepartment.text.toString().trim()
                    val deliveryInfo = "$city, $department"
                    val paymentMethod = if (radioGroup.checkedRadioButtonId == R.id.rbCashOnDelivery)
                        "при отриманні"
                    else
                        "карта"
                    // Обчислення загальної вартості замовлення
                    val totalCost = orderItems.sumByDouble { it.product.price * it.cartItem.quantity }

                    // Формування дати оформлення замовлення
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val orderDate = dateFormat.format(Date())

                    // Створення об'єкта Order
                    val newOrder = Order(
                        userId = userId,
                        orderDate = orderDate,
                        status = "pending",
                        recipientName = recipientName,
                        recipientEmail = recipientEmail,
                        deliveryInfo = deliveryInfo,
                        paymentMethod = paymentMethod,
                        totalCost = totalCost
                    )

                    val db = AppDatabase.getInstance(requireContext())
                    // Вставка замовлення та отримання згенерованого id
                    val orderId = db.orderDao().insertOrder(newOrder)

                    // Формування списку OrderItem на основі CartDisplayItem
                    val orderItemsList = orderItems.map { cartDisplayItem ->
                        OrderItem(
                            orderId = orderId.toInt(),
                            productId = cartDisplayItem.product.id!!,
                            quantity = cartDisplayItem.cartItem.quantity,
                            price = cartDisplayItem.product.price
                        )
                    }
                    // Запис OrderItem'ів
                    db.orderItemDao().insertOrderItems(orderItemsList)

                    // Очищення кошика – видаляємо кожну позицію
                    orderItems.forEach { cartDisplayItem ->
                        db.cartItemDao().delete(cartDisplayItem.cartItem)
                    }

                    // Повернення в UI-потік для повідомлення та переходу
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Ваше замовлення сформовано! Дякую за покупу!", Toast.LENGTH_LONG).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                } catch (ex: Exception) {
                    Log.e("PaymentFragment", "Помилка оформлення замовлення: ${ex.message}")
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Сталася помилка. Спробуйте пізніше.", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }
    }

    private fun loadOrderItems() {
        Thread {
            try {
                val db = AppDatabase.getInstance(requireContext())
                val cartItems = db.cartItemDao().getCartItemsForUser(userId)
                val loadedItems = cartItems.mapNotNull { cartItem ->
                    val product = db.productDao().getProductByIdSync(cartItem.productId)
                    if (product != null) {
                        CartDisplayItem(cartItem, product)
                    } else null
                }
                orderItems = loadedItems
                requireActivity().runOnUiThread {
                    orderAdapter.updateList(orderItems)
                    updateTotalCost(orderItems)
                }
            } catch (ex: Exception) {
                Log.e("PaymentFragment", "Помилка завантаження даних замовлення: ${ex.message}")
            }
        }.start()
    }

    private fun updateTotalCost(items: List<CartDisplayItem>) {
        val totalCost = items.sumByDouble { it.product.price * it.cartItem.quantity }
        tvTotalCost.text = "Вартість замовлення: $totalCost грн"
    }

    private fun loadUserContactDetails() {
        Thread {
            val sessionPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val currentUsername = sessionPref.getString("current_user", null)
            if (currentUsername != null) {
                val db = AppDatabase.getInstance(requireContext())
                val user = db.userDao().getUserSync(currentUsername)
                if (user != null) {
                    requireActivity().runOnUiThread {
                        etRecipientName.setText(user.name)
                        etRecipientEmail.setText(user.email)
                    }
                }
            }
        }.start()
    }
}
