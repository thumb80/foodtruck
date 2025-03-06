package com.bor.foodtruck.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bor.foodtruck.R
import com.bor.foodtruck.activity.AdminActivity
import com.bor.foodtruck.databinding.FragmentDayTotalBinding
import com.bor.foodtruck.viewmodel.SharedViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class DayTotalFragment: Fragment() {

    private lateinit var binding: FragmentDayTotalBinding
    private val viewModel: SharedViewModel by sharedViewModel<SharedViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDayTotalBinding.inflate(layoutInflater)

        viewLifecycleOwner.lifecycleScope.launch {
            val orders = viewModel.getDayOrder()
            if (orders.isNotEmpty()) {
                var totalDay = 0.0
                orders.forEach {
                    totalDay += it.orderPrice!!
                }
                binding.dayTotalText.text = getString(R.string.day_total_text, String.format("%.2f", totalDay))
            } else {
                binding.dayTotalText.text = getString(R.string.day_total_text_off)
            }
        }

        binding.backToHome.setOnClickListener {
            (activity as AdminActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.admin_container, AdminMenuFragment())
                .commit()
        }

        return binding.root
    }

}