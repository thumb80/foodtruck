package com.bor.foodtruck.activity

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bor.foodtruck.R
import com.bor.foodtruck.databinding.ActivityFoodTrackBinding
import com.bor.foodtruck.ext.StartDialog
import com.bor.foodtruck.util.Util
import com.bor.foodtruck.viewmodel.SharedViewModel
import org.koin.java.KoinJavaComponent

class FoodTruckActivity: AppCompatActivity() {

    private lateinit var binding: ActivityFoodTrackBinding
    private lateinit var dialog: StartDialog
    private val permissionCode = 1
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFoodTrackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = StartDialog(
            getString(R.string.select_modality),
            getString(R.string.admin_menu),
            getString(R.string.kiosk_menu),{
                startActivity(Intent(this, AdminActivity::class.java))
                dialog.dismiss()
            },
            {
                startActivity(Intent(this, HomeActivity::class.java))
                dialog.dismiss()
            })

        checkBluetoothPermissions()

        dialog.isCancelable = false
    }

    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission required")
            .setMessage("Some permissions are need to be allowed to use this app without any problems.")
            .setPositiveButton(
                "Settings"
            ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        if (alertDialog == null) {
            alertDialog = builder.create()
            if (!alertDialog!!.isShowing) {
                alertDialog!!.show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothOperations()
            } else {
                Toast.makeText(
                    this,
                    "Bluetooth permissions are required for this app",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ), permissionCode
            )
        } else {
            startBluetoothOperations()
        }
    }

    private fun startBluetoothOperations() {
        Util.writeLog("Permission granted")
        Util.appendLog("Detecting connected devices...")

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            dialog.show(supportFragmentManager, null)

        } else {
            showPermissionDialog()
        }

    }

}