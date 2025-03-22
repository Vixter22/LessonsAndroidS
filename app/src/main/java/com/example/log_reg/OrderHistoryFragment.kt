package com.example.log_reg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

class OrderHistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Завантаження розмітки фрагмента
        return inflater.inflate(R.layout.fragment_order_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Налаштування кнопки "Назад"
        val ivBackOrder = view.findViewById<ImageView>(R.id.ivBackOrder)
        ivBackOrder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}
