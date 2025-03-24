package com.example.log_reg

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
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

    // Приклад userId – можна отримувати з сесії або як аргумент
    private val userId: Int = 1
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var recyclerViewOrder: RecyclerView

    // Поля для введення даних отримувача (ім'я та email)
    private lateinit var etRecipientName: EditText
    private lateinit var etRecipientEmail: EditText

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

        // Знаходимо поля для введення імені та email отримувача
        etRecipientName = view.findViewById(R.id.etRecipientName)
        etRecipientEmail = view.findViewById(R.id.etRecipientEmail)

        // Завантаження даних замовлення
        loadOrderItems()
        // Завантаження контактних даних користувача
        loadUserContactDetails()
    }

    private fun loadOrderItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(requireContext())
                val cartItems = withContext(Dispatchers.IO) {
                    db.cartItemDao().getCartItemsForUser(userId)
                }
                val orderItems = withContext(Dispatchers.IO) {
                    cartItems.mapNotNull { cartItem ->
                        val product = db.productDao().getProductByIdSync(cartItem.productId)
                        if (product != null) {
                            CartDisplayItem(cartItem, product)
                        } else null
                    }
                }
                orderAdapter.updateList(orderItems)
            } catch (ex: Exception) {
                // Обробка помилки (наприклад, логування)
            }
        }
    }

    private fun loadUserContactDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Отримуємо username із SharedPreferences, як реалізовано у ProfileFragment
            val sessionPref = requireActivity()
                .getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val currentUsername = sessionPref.getString("current_user", null)
            if (currentUsername != null) {
                val db = AppDatabase.getInstance(requireContext())
                val user = withContext(Dispatchers.IO) {
                    db.userDao().getUserSync(currentUsername)
                }
                user?.let {
                    // Встановлюємо ім'я та email у відповідні поля
                    etRecipientName.setText(it.name)
                    etRecipientEmail.setText(it.email)
                }
            }
        }
    }
}
