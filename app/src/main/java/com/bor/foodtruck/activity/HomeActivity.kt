package com.bor.foodtruck.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.bor.foodtruck.FoodTruckApplication
import com.bor.foodtruck.FoodTruckApplication.Companion.order
import com.bor.foodtruck.R
import com.bor.foodtruck.databinding.ActivityHomeBinding
import com.bor.foodtruck.ui.kiosk.HomeFragment
import com.bor.foodtruck.ui.kiosk.OrderFragment
import com.bor.foodtruck.viewmodel.SharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity: AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: SharedViewModel by viewModel()
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.countOrder.observe(this) {
            binding.orderQty.text = it.toString()
        }

        viewModel.order.observe(this) {
            var countOrder = 0
            order.forEach {
                countOrder += it.first
            }
            viewModel.setCountOrder(countOrder)
        }

        binding.orderImage.setOnClickListener {
            if (!binding.orderQty.text.equals("0"))
                supportFragmentManager.beginTransaction()
                    .replace(R.id.kiosk_container, OrderFragment())
                    .commit()
            else
                Toast.makeText(this, getString(R.string.empty_order), Toast.LENGTH_SHORT).show()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.kiosk_container, HomeFragment())
            .commit()

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()

                // Check if back button was pressed within 2 seconds
                if (currentTime - backPressedTime < 2000) {
                    finishAffinity()  // Close the app completely
                } else {
                    // Show toast to prompt user to press back again to exit
                    Toast.makeText(this@HomeActivity, "Premere di nuovo per uscire", Toast.LENGTH_SHORT).show()

                    // Update backPressedTime to current time
                    backPressedTime = currentTime
                }
            }

        })

    }

}