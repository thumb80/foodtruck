package com.bor.foodtruck.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bor.foodtruck.R
import com.bor.foodtruck.activity.AdminActivity
import com.bor.foodtruck.databinding.FragmentZutatenBinding
import com.bor.foodtruck.ext.ItemEditDialog
import com.bor.foodtruck.model.EditListAdapter
import com.bor.foodtruck.model.Item
import com.bor.foodtruck.viewmodel.SharedViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ZutatenFragment: Fragment() {

    private lateinit var binding: FragmentZutatenBinding
    private val viewModel: SharedViewModel by sharedViewModel<SharedViewModel>()
    private lateinit var adapter: EditListAdapter
    private val gson = Gson()
    private val type = object : TypeToken<List<Item>>() {}.type
    private var jsonElement: List<Item>? = null

    companion object {
        var isInitializing = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentZutatenBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.checkIngredientFile(requireContext())) {
            viewModel.prepareZutatenFile(requireContext())
        }

        jsonElement = gson.fromJson<List<Item>>(viewModel.loadZutaten(requireContext(), "zutaten.json"), type)
        adapter = EditListAdapter(
            null,
            jsonElement,
            true,
            childFragmentManager,
            { _, item, position ->
                context?.let { viewModel.deleteItem(it, "zutaten.json", item, adapter) }
                if (position == 0)
                    isInitializing = false
            },
            "zutaten.json"
        )
        val layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false)

        binding.zutatenList.layoutManager = layoutManager
        binding.zutatenList.adapter = adapter

        binding.backToHome.setOnClickListener {
            (activity as AdminActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.admin_container, AdminMenuFragment())
                .commit()
        }

        binding.saveZutaten.setOnClickListener {
            val dialog: ItemEditDialog
            if (!isInitializing) {
                isInitializing = true
                dialog = ItemEditDialog(
                    null,
                    null,
                    "zutaten.json",
                    adapter,
                    true
                )
            } else {
                dialog = ItemEditDialog(
                    null,
                    null,
                    "zutaten.json",
                    adapter,
                    false
                )
            }
            dialog.isCancelable = false
            dialog.show(childFragmentManager, null)
        }
    }

}