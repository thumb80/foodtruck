package com.bor.foodtruck.ext

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.bor.foodtruck.FoodTruckApplication
import com.bor.foodtruck.R
import com.bor.foodtruck.databinding.DialogOrderBinding
import com.bor.foodtruck.model.EditListAdapter
import com.bor.foodtruck.model.Item
import com.bor.foodtruck.viewmodel.SharedViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class OrderEditQtyDialog(
    val item: Triple<Int,String,Double>,
    val instance: EditListAdapter,
    val position: Int
): DialogFragment() {

    private lateinit var binding: DialogOrderBinding
    private lateinit var builder: AlertDialog.Builder
    private var isGetranke = false

    private val viewModel: SharedViewModel by sharedViewModel<SharedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gson = Gson()
        val type = object : TypeToken<List<Item>>() {}.type
        val jsonElement = gson.fromJson<List<Item>>(viewModel.loadRawGetranke(requireContext(), "getranke.json"), type)
        jsonElement.forEach {
            if (it.name.contains(item.second)) {
                isGetranke = true
                return
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogOrderBinding.inflate(layoutInflater)
        builder = AlertDialog.Builder(activity)

        viewModel.setQty(item.first)

        binding.addQtyButton.setSafeOnClickListener {
            viewModel.qty.value?.plus(1)?.let { qty ->
                viewModel.setQty(qty)
            }
        }

        binding.removeQtyButton.setSafeOnClickListener {
            viewModel.qty.value?.let {
                if (it > 1)
                    viewModel.qty.value?.minus(1)?.let { qty ->
                        viewModel.setQty(qty)
                    }
            }
        }

        binding.confirmQuantity.setSafeOnClickListener {
            viewModel.qty.value?.let {
                if (!isGetranke) {
                    if (it > item.first) {
                        FoodTruckApplication.orderPizzeCount += it - item.first
                    } else if (it < item.first) {
                        FoodTruckApplication.orderPizzeCount -= item.first - it
                    }
                }
                val singleItemPrice = item.third/item.first
                var newItemPrice = 0.0
                if (it == 1) {
                    newItemPrice = singleItemPrice
                } else if (it > 1) {
                    newItemPrice = it * singleItemPrice
                }
                viewModel.editOrder(
                    Triple(it, item.second, newItemPrice),
                    instance,
                    position
                )
                instance.listener.invoke(null, null, position)
            }
            viewModel.resetQty()
            dismiss()
        }

        binding.root.background = resources.getDrawable(R.color.colorGreenAccent)
        builder.setView(binding.root)

        viewModel.qty.observe(this) {
            binding.dialogOrderText.text = "${viewModel.qty.value}".plus("   ").plus(item.second)
        }

        return builder.create()
    }

}