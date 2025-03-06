package com.bor.foodtruck.ui.kiosk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.bor.foodtruck.R
import com.bor.foodtruck.activity.HomeActivity
import com.bor.foodtruck.databinding.FragmentHomeBinding
import com.bor.foodtruck.model.Item
import com.bor.foodtruck.viewmodel.SharedViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class HomeFragment: Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: SharedViewModel by sharedViewModel<SharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val gson = Gson()

        binding = FragmentHomeBinding.inflate(layoutInflater)

        (activity as HomeActivity).findViewById<AppCompatImageView>(R.id.order_erase).visibility = INVISIBLE
        viewModel.checkIngredientFile(requireContext())

        val isPizzenNotEmpty = viewModel.loadRawPizzen(requireContext(), "pizzen.json") != "" && viewModel.loadRawPizzen(requireContext(), "pizzen.json") != "[]"
        val isZutatenNotEmpty = viewModel.loadZutaten(requireContext(), "zutaten.json") != "" && viewModel.loadZutaten(requireContext(), "zutaten.json") != "[]"
        val isGetrankeNotEmpty = viewModel.loadRawGetranke(requireContext(), "getranke.json") != "" && viewModel.loadRawGetranke(requireContext(), "getranke.json") != "[]"

        if (isPizzenNotEmpty) {
            binding.pizzenMenu.visibility = View.VISIBLE
            binding.pizzenMenu.setOnClickListener {
                val type = object : TypeToken<List<Item>>() {}.type
                val jsonElement = gson.fromJson<List<Item>>(viewModel.loadRawPizzen(requireContext(), "pizzen.json"), type)
                (activity as HomeActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.kiosk_container, MenuPizzenFragment(jsonElement, AppCompatResources.getDrawable(requireContext(), R.drawable.pizzen)!!, false), "PIZZEN")
                    .commit()
            }
        }

        if (isZutatenNotEmpty) {
            binding.ingredientMenu.visibility = View.VISIBLE
            binding.ingredientMenu.setOnClickListener {
                val type = object : TypeToken<List<Item>>() {}.type
                val jsonElement = gson.fromJson<List<Item>>(viewModel.loadZutaten(requireContext(), "zutaten.json"), type)
                (activity as HomeActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.kiosk_container, MenuPizzenFragment(jsonElement, AppCompatResources.getDrawable(requireContext(), R.drawable.zutaten_drawable)!!, true), "ZUTATEN")
                    .commit()
            }
        }

        if (isGetrankeNotEmpty) {
            binding.getrankeMenu.visibility = View.VISIBLE
            binding.getrankeMenu.setOnClickListener {
                val type = object : TypeToken<List<Item>>() {}.type
                val jsonElement = gson.fromJson<List<Item>>(viewModel.loadRawGetranke(requireContext(), "getranke.json"), type)
                (activity as HomeActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.kiosk_container, MenuGetrankeFragment(jsonElement, AppCompatResources.getDrawable(requireContext(), R.drawable.getranke)!!), "GETRANKE")
                    .commit()
            }
        }

        if (!isPizzenNotEmpty && !isZutatenNotEmpty && !isGetrankeNotEmpty)
            binding.emptyMenu.visibility = View.VISIBLE

        return binding.root
    }


}