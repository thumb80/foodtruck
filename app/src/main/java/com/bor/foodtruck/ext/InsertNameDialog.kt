package com.bor.foodtruck.ext

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.DialogFragment
import com.bor.foodtruck.R
import com.bor.foodtruck.databinding.DialogInsertNameBinding

class InsertNameDialog(
    private val positiveClickListener: (View) -> Unit,
    private val negativeClickListener: (View) -> Unit
): DialogFragment() {

    private lateinit var binding: DialogInsertNameBinding
    private lateinit var builder: AlertDialog.Builder

    var orderOwner: String = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogInsertNameBinding.inflate(layoutInflater)
        builder = AlertDialog.Builder(activity)

        binding.insertNameEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                orderOwner = s?.toString().toString()
            }

        })

        binding.confirmName.setSafeOnClickListener(positiveClickListener)
        binding.escConfirmName.setSafeOnClickListener(negativeClickListener)

        binding.root.background = resources.getDrawable(R.color.colorGreenAccent)
        builder.setView(binding.root)

        return builder.create()
    }
}