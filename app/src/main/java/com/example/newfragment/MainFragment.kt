package com.example.newfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.newfragment.databinding.FragmentMainBinding
import android.widget.Toast

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toast.makeText(requireContext(), "You are in Main Fragment", Toast.LENGTH_SHORT).show()

        binding.btnGoToSecond.setOnClickListener {
            findNavController().navigate(R.id.action_main_to_second)
        }

        binding.btnGoToThird.setOnClickListener {
            findNavController().navigate(R.id.action_main_to_third)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}