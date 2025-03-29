package com.example.log_reg

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
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

    private var userId: Int = -1
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var recyclerViewOrder: RecyclerView
    private lateinit var etRecipientName: EditText
    private lateinit var etRecipientEmail: EditText
    private lateinit var etCity: EditText
    private lateinit var etDepartment: EditText
    private lateinit var tvTotalCost: TextView
    private lateinit var btnPay: Button
    private lateinit var nestedScrollView: NestedScrollView

    // Поля для оплати картою
    private lateinit var etCardNumber: EditText
    private lateinit var etExpiry: EditText
    private lateinit var etCVV: EditText

    private var orderItems: List<CartDisplayItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Отримання userId з SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("current_user_id", -1)
        if (userId == -1) {
            Log.e("PaymentFragment", "User ID не знайдено в SharedPreferences")
        } else {
            Log.d("PaymentFragment", "Поточний userId: $userId")
        }

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

        // Ініціалізація полів оплати картою
        etCardNumber = view.findViewById(R.id.etCardNumber)
        etExpiry = view.findViewById(R.id.etExpiry)
        etCVV = view.findViewById(R.id.etCVV)

        // TextWatcher для форматування номера картки (вставка пробілів після кожних 4 цифр)
        etCardNumber.addTextChangedListener(object : TextWatcher {
            var isFormatting = false
            var previousText = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s?.toString() ?: ""
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val digitsOnly = s.toString().replace(" ", "")
                val limited = if (digitsOnly.length > 16) digitsOnly.substring(0, 16) else digitsOnly
                val formatted = StringBuilder()
                for (i in limited.indices) {
                    formatted.append(limited[i])
                    if ((i + 1) % 4 == 0 && i != limited.lastIndex) {
                        formatted.append(" ")
                    }
                }
                val cursorPosition = etCardNumber.selectionStart
                etCardNumber.setText(formatted.toString())
                val newCursorPosition = (cursorPosition + (formatted.toString().length - previousText.length))
                    .coerceAtMost(formatted.length)
                etCardNumber.setSelection(newCursorPosition)
                isFormatting = false
            }
        })

        // TextWatcher для форматування терміну дії у форматі "MM/YY"
        etExpiry.addTextChangedListener(object : TextWatcher {
            var isUpdating = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true
                var digits = s.toString().replace("[^\\d]".toRegex(), "")
                if (digits.length > 4) digits = digits.substring(0, 4)
                val formatted = if (digits.length >= 3) {
                    digits.substring(0, 2) + "/" + digits.substring(2)
                } else {
                    digits
                }
                etExpiry.setText(formatted)
                etExpiry.setSelection(formatted.length)
                isUpdating = false
            }
        })

        // Обмеження для поля CVV до 3 символів
        etCVV.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(3))

        val radioGroup = view.findViewById<RadioGroup>(R.id.rgPaymentMethod)
        val cardPaymentFields = view.findViewById<LinearLayout>(R.id.llCardPaymentFields)
        val rbCardPayment = view.findViewById<RadioButton>(R.id.rbCardPayment)
        val rbCashOnDelivery = view.findViewById<RadioButton>(R.id.rbCashOnDelivery)

        tvTotalCost = view.findViewById(R.id.tvTotalCost)
        btnPay = view.findViewById(R.id.btnPay)
        btnPay.backgroundTintList = null

        val redColor = Color.RED
        val defaultColor = Color.GRAY

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

        loadOrderItems()
        loadUserContactDetails()

        btnPay.setOnClickListener {
            processOrder()
        }
    }

    private fun loadOrderItems() {
        Thread {
            try {
                val db = AppDatabase.getInstance(requireContext())
                val cartItems = db.cartItemDao().getCartItemsForUser(userId)
                val loadedItems = cartItems.mapNotNull { cartItem ->
                    val product = db.productDao().getProductByIdSync(cartItem.productId)
                    product?.let { CartDisplayItem(cartItem, it) }
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
        val totalCost = items.sumOf { it.product.price * it.cartItem.quantity }
        tvTotalCost.text = "Вартість замовлення: $totalCost грн"
    }

    private fun loadUserContactDetails() {
        Thread {
            val sessionPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val currentUsername = sessionPref.getString("current_user", null)
            if (currentUsername != null) {
                val db = AppDatabase.getInstance(requireContext())
                val user = db.userDao().getUserSync(currentUsername)
                user?.let {
                    requireActivity().runOnUiThread {
                        etRecipientName.setText(it.name)
                        etRecipientEmail.setText(it.email)
                    }
                }
            }
        }.start()
    }

    private fun processOrder() {
        val radioGroup = requireView().findViewById<RadioGroup>(R.id.rgPaymentMethod)
        if (radioGroup.checkedRadioButtonId == -1) {
            showToast("Будь ласка, оберіть спосіб оплати")
            return
        }

        val recipientName = etRecipientName.text.toString().trim()
        val recipientEmail = etRecipientEmail.text.toString().trim()
        val city = etCity.text.toString().trim()
        val department = etDepartment.text.toString().trim()

        if (recipientName.isEmpty()) {
            showToast("Будь ласка, введіть ім'я отримувача")
            return
        }
        if (recipientEmail.isEmpty()) {
            showToast("Будь ласка, введіть пошту отримувача")
            return
        }
        if (city.isEmpty()) {
            showToast("Будь ласка, введіть місто доставки")
            return
        }
        if (department.isEmpty()) {
            showToast("Будь ласка, введіть відділення доставки")
            return
        }

        val isCardPayment = radioGroup.checkedRadioButtonId == R.id.rbCardPayment
        if (isCardPayment) {
            val cardNumber = etCardNumber.text.toString().trim()
            val expiry = etExpiry.text.toString().trim()
            val cvv = etCVV.text.toString().trim()

            if (cardNumber.isEmpty()) {
                showToast("Будь ласка, введіть номер карти")
                return
            }
            if (!cardNumber.replace(" ", "").matches(Regex("^\\d{16}$"))) {
                showToast("Номер карти має містити рівно 16 цифр")
                return
            }
            if (expiry.isEmpty()) {
                showToast("Будь ласка, введіть термін дії карти")
                return
            }
            if (!expiry.matches(Regex("^(0[1-9]|1[0-2])/\\d{2}$"))) {
                showToast("Термін дії має бути у форматі MM/YY, де MM від 01 до 12")
                return
            }
            if (cvv.isEmpty()) {
                showToast("Будь ласка, введіть CVV")
                return
            }
            if (!cvv.matches(Regex("^\\d{3}$"))) {
                showToast("CVV має складатися з 3 цифр")
                return
            }
        }

        Thread {
            try {
                val deliveryInfo = "$city, $department"
                val paymentMethod = if (radioGroup.checkedRadioButtonId == R.id.rbCashOnDelivery)
                    "при отриманні" else "карта"
                val totalCost = orderItems.sumOf { it.product.price * it.cartItem.quantity }
                val orderDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

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
                val orderId = db.orderDao().insertOrder(newOrder)

                val orderItemsList = orderItems.map {
                    OrderItem(
                        orderId = orderId.toInt(),
                        productId = it.product.id!!,
                        quantity = it.cartItem.quantity,
                        price = it.product.price
                    )
                }
                db.orderItemDao().insertOrderItems(orderItemsList)

                orderItems.forEach {
                    db.cartItemDao().delete(it.cartItem)
                }

                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Ваше замовлення сформовано! Дякую за покупку!", Toast.LENGTH_LONG).show()
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

    private fun showToast(message: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
