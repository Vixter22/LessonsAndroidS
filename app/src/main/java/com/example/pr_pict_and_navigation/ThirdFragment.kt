package com.example.pr_pict_and_navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class ThirdFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_third, container, false)

        val textView = view.findViewById<TextView>(R.id.textViewTitle)
        val buttonToPhotoFragment = view.findViewById<Button>(R.id.button_to_photo_fragment)
        val buttonToSecondFragment = view.findViewById<Button>(R.id.button_to_second_fragment)

        textView.text = "Третій фрагмент"

        val message = arguments?.getString("source")
        message?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        buttonToPhotoFragment.setOnClickListener { navigateToFragment(PhotoFragment(), "Я прийшов з третього фрагмента") }
        buttonToSecondFragment.setOnClickListener { navigateToFragment(SecondFragment(), "Я прийшов з третього фрагмента") }

        return view
    }

    private fun navigateToFragment(fragment: Fragment, message: String) {
        val bundle = Bundle()
        bundle.putString("source", message)
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
