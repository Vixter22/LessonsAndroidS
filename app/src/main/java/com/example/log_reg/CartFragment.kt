package com.example.log_reg

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
import com.example.log_reg.data.AppDatabase
import com.example.log_reg.data.CartDisplayItem
import com.example.log_reg.data.CartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartFragment : Fragment() {

    // Приклад userId – отримуйте його з сесії або передавайте як аргумент
    private val userId: Int = 1
    private lateinit var cartAdapter: CartAdapter
    private lateinit var tvEmptyCart: TextView
    private lateinit var tvTotalCost: TextView
    private lateinit var btnPay: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Переходимо до WishlistFragment при кліку на іконку сердечка
        val wishlistIcon = view.findViewById<ImageView>(R.id.ivWishlist)
        wishlistIcon.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WishlistFragment())
                .addToBackStack(null)
                .commit()
        }

        tvEmptyCart = view.findViewById(R.id.tvEmptyCart)
        tvTotalCost = view.findViewById(R.id.tvTotalCost)
        btnPay = view.findViewById(R.id.btnPay)
        btnPay.backgroundTintList = null

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewCart)
        recyclerView.layoutManager = LinearLayoutManager(context)
        cartAdapter = CartAdapter(
            emptyList(),
            onDeleteClick = { cartDisplayItem ->
                deleteCartItem(cartDisplayItem.cartItem)
            },
            onIncreaseClick = { cartDisplayItem ->
                updateCartItemQuantity(cartDisplayItem, cartDisplayItem.cartItem.quantity + 1)
            },
            onDecreaseClick = { cartDisplayItem ->
                // Логіка зменшення: якщо кількість більше 1, зменшуємо, інакше – видаляємо товар
                if (cartDisplayItem.cartItem.quantity > 1) {
                    updateCartItemQuantity(cartDisplayItem, cartDisplayItem.cartItem.quantity - 1)
                } else {
                    deleteCartItem(cartDisplayItem.cartItem)
                }
            }
        )
        recyclerView.adapter = cartAdapter

        btnPay.setOnClickListener {
            // Логіка оплати поки що не реалізована
        }

        loadCartItems()
    }

    private fun loadCartItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(requireContext())
                val cartItems = withContext(Dispatchers.IO) {
                    db.cartItemDao().getCartItemsForUser(userId)
                }
                val cartDisplayItems = withContext(Dispatchers.IO) {
                    cartItems.mapNotNull { cartItem ->
                        val product = db.productDao().getProductByIdSync(cartItem.productId)
                        if (product != null) {
                            com.example.log_reg.data.CartDisplayItem(cartItem, product)
                        } else null
                    }
                }
                cartAdapter.updateList(cartDisplayItems)
                updateTotalCost(cartDisplayItems)
                tvEmptyCart.visibility = if (cartDisplayItems.isEmpty()) View.VISIBLE else View.GONE
            } catch (ex: Exception) {
                Log.e("CartFragment", "Помилка завантаження даних кошика: ${ex.message}")
            }
        }
    }

    private fun updateTotalCost(cartDisplayItems: List<CartDisplayItem>) {
        val totalCost = cartDisplayItems.sumOf { it.product.price * it.cartItem.quantity }
        tvTotalCost.text = "Загальна вартість: $totalCost грн"
    }

    private fun updateCartItemQuantity(item: CartDisplayItem, newQuantity: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(requireContext())
                val updatedItem = item.cartItem.copy(quantity = newQuantity)
                withContext(Dispatchers.IO) {
                    db.cartItemDao().insert(updatedItem)
                }
                loadCartItems()
            } catch (ex: Exception) {
                Log.e("CartFragment", "Помилка оновлення кількості: ${ex.message}")
            }
        }
    }

    private fun deleteCartItem(cartItem: CartItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(requireContext())
                withContext(Dispatchers.IO) {
                    db.cartItemDao().delete(cartItem)
                }
                loadCartItems()
            } catch (ex: Exception) {
                Log.e("CartFragment", "Помилка видалення товару: ${ex.message}")
            }
        }
    }
}
