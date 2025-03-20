package com.example.log_reg

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewCart)
        recyclerView.layoutManager = LinearLayoutManager(context)
        cartAdapter = CartAdapter(emptyList()) { cartDisplayItem ->
            // Callback видалення. Виконуємо видалення елемента з бази даних.
            deleteCartItem(cartDisplayItem.cartItem)
        }
        recyclerView.adapter = cartAdapter

        // Завантаження даних з бази даних за допомогою корутин
        loadCartItems()
    }

    private fun loadCartItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(requireContext())
                // Отримання списку товарів із кошика для конкретного користувача
                val cartItems = withContext(Dispatchers.IO) {
                    db.cartItemDao().getCartItemsForUser(userId)
                }
                // Отримання ProductDao та формування списку для адаптера
                val cartDisplayItems = withContext(Dispatchers.IO) {
                    cartItems.mapNotNull { cartItem ->
                        val product = db.productDao().getProductByIdSync(cartItem.productId)
                        if (product != null) {
                            com.example.log_reg.data.CartDisplayItem(cartItem, product)
                        } else null
                    }
                }
                cartAdapter.updateList(cartDisplayItems)
            } catch (ex: Exception) {
                Log.e("CartFragment", "Помилка завантаження даних кошика: ${ex.message}")
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
                // Після видалення перезавантажуємо список
                loadCartItems()
            } catch (ex: Exception) {
                Log.e("CartFragment", "Помилка видалення товару: ${ex.message}")
            }
        }
    }
}
