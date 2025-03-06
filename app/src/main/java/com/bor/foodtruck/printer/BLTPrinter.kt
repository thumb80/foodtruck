package com.bor.foodtruck.printer

import android.widget.Toast
import com.bor.foodtruck.FoodTruckApplication.Companion.instance
import com.bor.foodtruck.R
import com.bor.foodtruck.util.Util
import com.example.hoinprinterlib.HoinPrinter
import com.example.hoinprinterlib.common.Constant
import com.example.hoinprinterlib.common.ErrorCode
import com.example.hoinprinterlib.module.PrinterCallback
import com.example.hoinprinterlib.module.PrinterEvent
import java.io.Closeable

class BLTPrinter: Closeable {

    private var bluetoothPrinter: HoinPrinter

    companion object {

        @Volatile private var INSTANCE: BLTPrinter? = null

        fun getInstance(): BLTPrinter {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BLTPrinter().also { INSTANCE = it }
            }
        }
    }

    init {
        // Bluetooth BLTPrinter
        val bltCallback: PrinterCallback = object : PrinterCallback {
            override fun onState(p0: Int) {
                if (p0 == Constant.BT_STATE_DISCONNECTED) {
                    Util.appendLog("disconnesso dalla stampante BLT")
                } else if (p0 == Constant.BT_STATE_CONNECTED) {
                    Util.appendLog("connesso dalla stampante BLT")
                }
            }

            override fun onError(p0: Int) {
                if (p0 == ErrorCode.BT_CONNECTION_LOST || p0 == ErrorCode.CONTEXT_ERROR || p0 == ErrorCode.DEVICE_NOT_CONNECTED || p0 == ErrorCode.IMAGE_NOT_FONUD || p0 == ErrorCode.NULL_POINTER_EXCEPTION) {
                    Util.appendLog("connessione alla stampante BLT in errore -> codice : ${p0}")
                }
            }

            override fun onEvent(p0: PrinterEvent?) {
                Util.appendLog("evento ${p0?.event}")
            }

        }
        bluetoothPrinter = HoinPrinter.getInstance(instance, 1, bltCallback)
        Util.appendLog("Initializing BLTPrinter, paired devices size : ${bluetoothPrinter.pairedDevice.size}")
    }

    fun connect(
        address: String?
    ): Boolean {
        try {
            Util.appendLog("Connecting.... to BLT device : $address")
            bluetoothPrinter.connect(address)
            bluetoothPrinter.setCenter(true)
            bluetoothPrinter.switchType(true)
            Util.appendLog("Init BLTPrinter $address successful")
            return true
        } catch (e: Exception) {
            Util.appendLog("Init BLTPrinter $address error : ${e.localizedMessage}")
            Toast.makeText(instance, instance.getString(R.string.printer_disconnected), Toast.LENGTH_LONG).show()
            return false
        }
    }

    fun printBLT(
        text: String,
        doubleH: Boolean,
        doubleW: Boolean,
        bold: Boolean,
        center: Boolean
    ) {
        bluetoothPrinter.printText(text,doubleH, doubleW, bold, center)
    }

    fun printImage(
        path: String,
        center: Boolean
    ) {
        bluetoothPrinter.printImage(path, center)
    }

    override fun close() {
        bluetoothPrinter.destroy()
        INSTANCE = null
    }

}