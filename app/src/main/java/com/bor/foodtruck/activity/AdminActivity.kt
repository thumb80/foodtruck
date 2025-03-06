package com.bor.foodtruck.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bor.foodtruck.R
import com.bor.foodtruck.databinding.ActivityAdminBinding
import com.bor.foodtruck.ui.admin.AdminMenuFragment

class AdminActivity: AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.admin_container, AdminMenuFragment())
            .commit()

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }

        })
    }
}