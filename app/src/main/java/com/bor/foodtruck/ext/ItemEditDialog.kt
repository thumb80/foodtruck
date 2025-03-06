package com.bor.foodtruck.ext

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bor.foodtruck.R
import com.bor.foodtruck.databinding.EditItemLayoutBinding
import com.bor.foodtruck.model.EditListAdapter
import com.bor.foodtruck.model.Item
import com.bor.foodtruck.viewmodel.SharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ItemEditDialog(
    val item: Item?,
    val position: Int?,
    val fileName: String,
    val instance: EditListAdapter,
    val isZutatenInit: Boolean
): DialogFragment() {

    private lateinit var binding: EditItemLayoutBinding
    private lateinit var builder: AlertDialog.Builder
    private val viewModel: SharedViewModel by sharedViewModel<SharedViewModel>()
    private var itemName: String? = null
    private var itemPrice: String? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = EditItemLayoutBinding.inflate(layoutInflater)
        builder = AlertDialog.Builder(activity)

        itemName = item?.name
        itemPrice = item?.price.toString()

        if (itemName != null) {
            binding.editItemText.setText(itemName as CharSequence, TextView.BufferType.EDITABLE)
            binding.editItemPrice.setText(itemPrice.toString() as CharSequence, TextView.BufferType.EDITABLE)
        } else {
            binding.editItemText.setHint(R.string.insert_name_text)
            binding.editItemPrice.setHint(R.string.insert_price_text)
        }


        binding.editItemText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable?) {
                itemName = s?.toString()
            }

        })

        binding.editItemPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable?) {
                itemPrice = s?.toString()
            }

        })

        builder.setView(binding.root)

        viewModel.checkEditsItemFilled(binding.editItemText, binding.editItemPrice)

        viewModel.areEditsItemFilled.observe(this) {
            if (it.first && it.second) {
                if (item != null) {
                    val item = itemName?.let { name ->
                        itemPrice?.let { price ->
                            if (fileName.contains("pizzen") || fileName.contains("zutaten"))
                                Item(item.id, name, price.toDouble(), 1)
                            else
                                Item(item.id, name, price.toDouble(), null)
                        }
                    }
                    binding.confirmEditItem.setOnClickListener {
                        context?.let { ctx ->
                            if (item != null && position != null) {
                                viewModel.modifyItem(ctx, fileName, item, instance, position)
                            }
                        }
                        dismiss()
                    }
                } else {
                    val item = itemName?.let {name ->
                        itemPrice?.let { price ->
                            if (fileName.contains("pizzen") || fileName.contains("zutaten"))
                                Item(instance.getLastIndex(), name, price.toDouble(), 1)
                            else
                                Item(instance.getLastIndex(), name, price.toDouble(), null)
                        }
                    }
                    binding.confirmEditItem.setOnClickListener {
                        context?.let { ctx ->
                            if (item != null) {
                                if (!isZutatenInit)
                                    viewModel.insertItem(ctx, fileName, item, instance)
                                else
                                    viewModel.insertZutaten(ctx, fileName, item, instance)
                            }
                        }
                        dismiss()
                    }
                }
            }
        }

        return builder.create()
    }

}