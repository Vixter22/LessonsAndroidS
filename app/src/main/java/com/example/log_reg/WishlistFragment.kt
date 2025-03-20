package com.example.log_reg

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.log_reg.data.AppDatabase

class WishlistFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var wishlistAdapter: WishlistAdapter
    private lateinit var emptyTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wishlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Налаштування кнопки "Назад"
        val ivBack = view.findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        emptyTextView = view.findViewById(R.id.tvEmptyWishlist)

        recyclerView = view.findViewById(R.id.recyclerViewWishlist)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Отримання актуального userId із SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("current_user_id", -1)
        if (userId == -1) {
            Toast.makeText(requireContext(), "Користувача не знайдено, перейдіть на екран логіну", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
            return
        }

        val db = AppDatabase.getInstance(requireContext())
        val wishlistDao = db.wishlistItemDao()

        wishlistAdapter = WishlistAdapter(emptyList(), wishlistDao, userId, viewLifecycleOwner.lifecycleScope)
        recyclerView.adapter = wishlistAdapter

        // Спостереження за змінами у вішлісті
        wishlistDao.getWishlistProducts(userId).observe(viewLifecycleOwner) { wishlistProducts ->
            if (wishlistProducts.isEmpty()) {
                emptyTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                wishlistAdapter.updateList(wishlistProducts)
            }
        }
    }
}
