package com.bor.foodtruck.ext

import android.app.AlertDialog.Builder
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.bor.foodtruck.R
import com.bor.foodtruck.databinding.DialogCustomBinding

class CustomDialog(
    private val text: String,
    private val firstText: String?,
    private val secondText: String?,
    private val positiveClickListener: (View) -> Unit,
    private val negativeClickListener: (View) -> Unit
): DialogFragment() {

    private lateinit var binding: DialogCustomBinding
    private lateinit var builder: Builder

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogCustomBinding.inflate(layoutInflater)
        builder = Builder(activity)

        binding.root.background = resources.getDrawable(R.color.colorGreenAccent)
        binding.dialogText.text = text
        binding.jaText.text = firstText ?: getString(R.string.ja)
        binding.neinText.text = secondText ?: getString(R.string.nein)

        binding.jaText.setSafeOnClickListener(positiveClickListener)
        binding.neinText.setSafeOnClickListener(negativeClickListener)

        builder.setView(binding.root)

        return builder.create()
    }

}