package com.example.log_reg

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.log_reg.data.AppDatabase
import com.example.log_reg.data.CartDisplayItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentFragment : Fragment() {

    private val userId: Int = 1
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var recyclerViewOrder: RecyclerView
    private lateinit var etRecipientName: EditText
    private lateinit var etRecipientEmail: EditText
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

        // Ініціалізація NestedScrollView
        nestedScrollView = view.findViewById(R.id.nestedScrollView)

        // Ініціалізація RecyclerView для відображення даних замовлення
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

        // Поля введення контактних даних отримувача
        etRecipientName = view.findViewById(R.id.etRecipientName)
        etRecipientEmail = view.findViewById(R.id.etRecipientEmail)

        // Елементи для вибору способу оплати
        val radioGroup = view.findViewById<RadioGroup>(R.id.rgPaymentMethod)
        val cardPaymentFields = view.findViewById<LinearLayout>(R.id.llCardPaymentFields)
        val rbCardPayment = view.findViewById<RadioButton>(R.id.rbCardPayment)
        val rbCashOnDelivery = view.findViewById<RadioButton>(R.id.rbCashOnDelivery)

        // Елементи нижнього контейнера
        tvTotalCost = view.findViewById(R.id.tvTotalCost)
        btnPay = view.findViewById(R.id.btnPay)
        btnPay.backgroundTintList = null

        // Визначення кольорів: червоний для обраного, сірий для неактивного
        val redColor = Color.RED
        val defaultColor = Color.GRAY

        // Початкова установка UI для способу оплати
        when (radioGroup.checkedRadioButtonId) {
            R.id.rbCardPayment -> {
                cardPaymentFields.visibility = View.VISIBLE
                rbCardPayment.buttonTintList = ColorStateList.valueOf(redColor)
                rbCashOnDelivery.buttonTintList = ColorStateList.valueOf(defaultColor)
            }
            R.id.rbCashOnDelivery -> {
                cardPaymentFields.visibility = View.GONE
                rbCashOnDelivery.buttonTintList = ColorStateList.valueOf(redColor)
                rbCardPayment.buttonTintList = ColorStateList.valueOf(defaultColor)
            }
            else -> {
                cardPaymentFields.visibility = View.GONE
                rbCardPayment.buttonTintList = ColorStateList.valueOf(defaultColor)
                rbCashOnDelivery.buttonTintList = ColorStateList.valueOf(defaultColor)
            }
        }

        // Слухач для зміни вибору способу оплати
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCardPayment -> {
                    cardPaymentFields.visibility = View.VISIBLE
                    rbCardPayment.buttonTintList = ColorStateList.valueOf(redColor)
                    rbCashOnDelivery.buttonTintList = ColorStateList.valueOf(defaultColor)
                    // Скролимо до кінця, щоб користувач бачив з'явлені поля
                    nestedScrollView.post {
                        nestedScrollView.smoothScrollTo(0, nestedScrollView.getChildAt(0).height)
                    }
                }
                R.id.rbCashOnDelivery -> {
                    cardPaymentFields.visibility = View.GONE
                    rbCashOnDelivery.buttonTintList = ColorStateList.valueOf(redColor)
                    rbCardPayment.buttonTintList = ColorStateList.valueOf(defaultColor)
                }
            }
        }

        // Обробка кліку кнопки "Завершити оформлення"
        btnPay.setOnClickListener {
            Log.d("PaymentFragment", "Завершити оформлення клікнуто")
            // Подальша логіка обробки замовлення може бути додана тут
        }

        // Завантаження даних замовлення та контактних даних користувача
        loadOrderItems()
        loadUserContactDetails()
    }

    private fun loadOrderItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(requireContext())
                val cartItems = withContext(Dispatchers.IO) {
                    db.cartItemDao().getCartItemsForUser(userId)
                }
                orderItems = withContext(Dispatchers.IO) {
                    cartItems.mapNotNull { cartItem ->
                        val product = db.productDao().getProductByIdSync(cartItem.productId)
                        if (product != null) {
                            CartDisplayItem(cartItem, product)
                        } else null
                    }
                }
                orderAdapter.updateList(orderItems)
                updateTotalCost(orderItems)
            } catch (ex: Exception) {
                Log.e("PaymentFragment", "Помилка завантаження даних замовлення: ${ex.message}")
            }
        }
    }

    private fun updateTotalCost(items: List<CartDisplayItem>) {
        val totalCost = items.sumOf { it.product.price * it.cartItem.quantity }
        tvTotalCost.text = "Вартість замовлення: $totalCost грн"
    }

    private fun loadUserContactDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            val sessionPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val currentUsername = sessionPref.getString("current_user", null)
            if (currentUsername != null) {
                val db = AppDatabase.getInstance(requireContext())
                val user = withContext(Dispatchers.IO) {
                    db.userDao().getUserSync(currentUsername)
                }
                user?.let {
                    etRecipientName.setText(it.name)
                    etRecipientEmail.setText(it.email)
                }
            }
        }
    }
}
