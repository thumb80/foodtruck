package com.bor.foodtruck.ext

import android.app.AlertDialog.Builder
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.bor.foodtruck.R
import com.bor.foodtruck.databinding.DialogReconnectBinding

class ReconnectDialog(
    private val clickListener: (View) -> Unit
): DialogFragment() {

    private lateinit var binding: DialogReconnectBinding
    private lateinit var builder: Builder

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogReconnectBinding.inflate(layoutInflater)
        builder = Builder(activity)

        binding.root.background = resources.getDrawable(R.color.colorGreenAccent)

        binding.jaText?.setSafeOnClickListener(clickListener)

        builder.setView(binding.root)

        return builder.create()
    }

}