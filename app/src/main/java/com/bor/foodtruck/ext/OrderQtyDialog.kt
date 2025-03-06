package com.bor.foodtruck.ext

import android.app.AlertDialog.Builder
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.bor.foodtruck.FoodTruckApplication
import com.bor.foodtruck.R
import com.bor.foodtruck.databinding.DialogOrderBinding
import com.bor.foodtruck.model.Item
import com.bor.foodtruck.viewmodel.SharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class OrderQtyDialog(val item: Item): DialogFragment() {

    private lateinit var binding: DialogOrderBinding
    private lateinit var builder: Builder
    private var isItemInOrder = false
    private var position: Int = -1
    private var qty: Int = 1

    private val viewModel: SharedViewModel by sharedViewModel<SharedViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogOrderBinding.inflate(layoutInflater)
        builder = Builder(activity)

        val itemName = if (item.name.contains("\n"))
            item.name.split("\n")[0]
        else
            item.name

        FoodTruckApplication.order.forEachIndexed  { index, triple ->
            val name = if (item.name.contains("\n"))
                    item.name.split("\n")[0]
                else
                    item.name
            if (name == triple.second) {
                position = index
                isItemInOrder = true
                qty = triple.first + 1
                return@forEachIndexed
            }
        }

        viewModel.setQty(qty)

        if (isItemInOrder) {

            binding.confirmQuantity.setSafeOnClickListener {
                viewModel.qty.value?.let { quantity ->
                    if (item.isPizzen == 1) {
                        if (quantity > qty) {
                            FoodTruckApplication.orderPizzeCount += quantity - qty
                        } else if (quantity < qty) {
                            FoodTruckApplication.orderPizzeCount -= qty - quantity
                        }
                    }
                    val tuple = Triple(quantity, itemName, quantity * item.price)
                    viewModel.updateItemOrder(tuple, position)
                }
                viewModel.resetQty()
                dismiss()
            }
        } else {
            binding.confirmQuantity.setSafeOnClickListener {
                viewModel.qty.value?.let { qty ->
                    if (item.isPizzen == 1)
                        FoodTruckApplication.orderPizzeCount += qty
                    viewModel.addItemToOrder(Triple(qty, itemName, qty * item.price))
                }
                viewModel.resetQty()
                dismiss()
            }
        }

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

        binding.root.background = resources.getDrawable(R.color.colorGreenAccent)
        builder.setView(binding.root)

        viewModel.qty.observe(this) {
            binding.dialogOrderText.text = "${viewModel.qty.value}".plus("   ").plus(itemName)
        }

        return builder.create()
    }

}