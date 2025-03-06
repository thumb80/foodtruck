package com.bor.foodtruck.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bor.foodtruck.R
import com.bor.foodtruck.activity.AdminActivity
import com.bor.foodtruck.databinding.FragmentPizzenBinding
import com.bor.foodtruck.ext.ItemEditDialog
import com.bor.foodtruck.model.EditListAdapter
import com.bor.foodtruck.model.Item
import com.bor.foodtruck.viewmodel.SharedViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PizzenFragment: Fragment() {

    private lateinit var binding: FragmentPizzenBinding
    private val viewModel: SharedViewModel by sharedViewModel<SharedViewModel>()
    private lateinit var adapter: EditListAdapter
    private val gson = Gson()
    private val type = object : TypeToken<List<Item>>() {}.type
    private var jsonElement: List<Item> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPizzenBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        jsonElement = gson.fromJson<List<Item>>(viewModel.loadRawPizzen(requireContext(), "pizzen.json"), type)
        adapter = EditListAdapter(
            null,
            jsonElement,
            true,
            childFragmentManager,
            { _, item, position ->
                context?.let { viewModel.deleteItem(it, "pizzen.json", item, adapter) }
            },
            "pizzen.json"
        )
        val layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false)

        binding.pizzenList.layoutManager = layoutManager
        binding.pizzenList.adapter = adapter

        binding.backToHome.setOnClickListener {
            (activity as AdminActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.admin_container, AdminMenuFragment())
                .commit()
        }

        binding.savePizzen.setOnClickListener {
            val dialog = ItemEditDialog(
                null,
                null,
                "pizzen.json",
                adapter,
                false
            )
            dialog.show(childFragmentManager, null)
        }
    }
}