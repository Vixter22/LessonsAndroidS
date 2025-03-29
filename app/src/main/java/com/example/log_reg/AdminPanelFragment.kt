package com.example.log_reg.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.log_reg.MainActivity
import com.example.log_reg.R

class AdminPanelFragment : Fragment() {

    private lateinit var exitImageView: ImageView
    private lateinit var cardProductList: CardView
    private lateinit var cardOrderList: CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Використовуємо XML макет для адмін-панелі
        return inflater.inflate(R.layout.fragment_admin_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exitImageView = view.findViewById(R.id.iv_exit)
        cardProductList = view.findViewById(R.id.cardProductList)
        cardOrderList = view.findViewById(R.id.cardOrderList)

        exitImageView.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        cardProductList.setOnClickListener {
            // Перехід до фрагмента зі списком товарів
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProductListFragment())
                .addToBackStack(null)
                .commit()
        }

        cardOrderList.setOnClickListener {
            // Перехід до фрагмента зі списком замовлень
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OrderListFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Вихід")
            .setMessage("Ви впевнені, що хочете вийти з акаунту?")
            .setPositiveButton("Так") { _, _ ->
                (activity as? MainActivity)?.logoutUser()
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }
}
