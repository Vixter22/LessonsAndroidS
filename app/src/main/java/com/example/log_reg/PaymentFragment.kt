package com.example.log_reg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

class PaymentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Обробка кліку на стрілочку назад – повертаємося до попереднього фрагмента
        val backArrow = view.findViewById<ImageView>(R.id.ivBack)
        backArrow.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}
