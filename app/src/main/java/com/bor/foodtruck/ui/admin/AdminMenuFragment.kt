package com.bor.foodtruck.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bor.foodtruck.R
import com.bor.foodtruck.activity.AdminActivity
import com.bor.foodtruck.activity.HomeActivity
import com.bor.foodtruck.databinding.FragmentAdminMenuBinding

class AdminMenuFragment: Fragment() {

    private lateinit var binding: FragmentAdminMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAdminMenuBinding.inflate(layoutInflater)

        val dayTotal = binding.root.findViewById<Button>(R.id.day_total)
        val historicalTotal = binding.root.findViewById<Button>(R.id.historical_total)
        val adminPizzen = binding.root.findViewById<Button>(R.id.admin_pizzen)
        val adminZutaten = binding.root.findViewById<Button>(R.id.admin_zutaten)
        val adminGetranke = binding.root.findViewById<Button>(R.id.admin_getranke)
        val startKioskMode = binding.root.findViewById<TextView>(R.id.start_kiosk_mode)

        dayTotal.setOnClickListener {
            (activity as AdminActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.admin_container, DayTotalFragment())
                .commit()
        }

        historicalTotal.setOnClickListener {
            (activity as AdminActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.admin_container, HistoricalTotalFragment())
                .commit()
        }

        adminPizzen.setOnClickListener {
            (activity as AdminActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.admin_container, PizzenFragment())
                .commit()
        }

        adminZutaten.setOnClickListener {
            (activity as AdminActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.admin_container, ZutatenFragment())
                .commit()
        }

        adminGetranke.setOnClickListener {
            (activity as AdminActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.admin_container, GetrankeFragment())
                .commit()
        }

        startKioskMode.setOnClickListener {
            val intent = Intent(requireContext(), HomeActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

}