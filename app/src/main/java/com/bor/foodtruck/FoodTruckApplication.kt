package com.bor.foodtruck

import android.app.Application
import android.os.Environment
import com.bor.foodtruck.di.appModule
import com.bor.foodtruck.printer.BLTPrinter
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.File
import java.io.IOException
import java.util.Date


class FoodTruckApplication: Application() {

    companion object {
        lateinit var instance: FoodTruckApplication

        lateinit var order: ArrayList<Triple<Int,String,Double>>
        var orderPizzeCount: Int = 0
        var orderNumber: Int = 0

        var logFile: File? = null

        lateinit var foodTruckPrinterClient: BLTPrinter
        lateinit var foodTruckPrinterManager: BLTPrinter

        const val clientPrinterAddress = "XX:XX:XX:XX:XX:XX"
        const val managerPrinterAddress = "XX:XX:XX:XX:XX:XX"
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        startKoin {
            androidContext(this@FoodTruckApplication)
            modules(appModule(Date(System.currentTimeMillis())))
        }

        order = arrayListOf()

        val logFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "log")
        if (!logFolder.exists())
            logFolder.mkdir()
        logFile = File(logFolder, "Logs-${System.currentTimeMillis()}.txt")

        if (!logFile!!.exists()) {
            try {
                logFile!!.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}