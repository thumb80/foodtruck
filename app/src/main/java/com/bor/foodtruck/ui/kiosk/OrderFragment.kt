package com.bor.foodtruck.ui.kiosk

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bor.foodtruck.FoodTruckApplication.Companion.foodTruckPrinterClient
import com.bor.foodtruck.FoodTruckApplication.Companion.foodTruckPrinterManager
import com.bor.foodtruck.FoodTruckApplication.Companion.clientPrinterAddress
import com.bor.foodtruck.FoodTruckApplication.Companion.managerPrinterAddress
import com.bor.foodtruck.FoodTruckApplication.Companion.order
import com.bor.foodtruck.FoodTruckApplication.Companion.orderNumber
import com.bor.foodtruck.FoodTruckApplication.Companion.orderPizzeCount
import com.bor.foodtruck.R
import com.bor.foodtruck.activity.HomeActivity
import com.bor.foodtruck.databinding.FragmentOrderBinding
import com.bor.foodtruck.ext.CustomDialog
import com.bor.foodtruck.ext.InsertNameDialog
import com.bor.foodtruck.ext.ReconnectDialog
import com.bor.foodtruck.model.EditListAdapter
import com.bor.foodtruck.model.Item
import com.bor.foodtruck.model.Order
import com.bor.foodtruck.printer.BLTPrinter
import com.bor.foodtruck.util.Util
import com.bor.foodtruck.viewmodel.SharedViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Random
import kotlin.math.floor

class OrderFragment: Fragment() {

    private lateinit var binding: FragmentOrderBinding
    private val viewModel: SharedViewModel by sharedViewModel<SharedViewModel>()
    private lateinit var adapter: EditListAdapter
    private lateinit var dialog: CustomDialog
    private lateinit var innerDialog: CustomDialog
    private lateinit var insertNameDialog: InsertNameDialog
    private lateinit var reconnectDialogKunden: ReconnectDialog
    private lateinit var reconnectDialogManager: ReconnectDialog

    private var dbSb = StringBuilder()
    private var sb = StringBuilder().append("\n\n\n")
    private var totalPrice = 0.00

    val date = System.currentTimeMillis()
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    val mDate = sdf.format(date)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderBinding.inflate(layoutInflater)

        binding.orderTotal.text =
            getString(R.string.order_total, computePrice(order))

        adapter = EditListAdapter(
            order,
            null,
            false,
            childFragmentManager,
            { orderItem, _, _ ->
                var isGetranke = false
                val gson = Gson()
                val type = object : TypeToken<List<Item>>() {}.type
                val jsonElement = gson.fromJson<List<Item>>(
                    viewModel.loadRawGetranke(
                        requireContext(),
                        "getranke.json"
                    ), type
                )
                jsonElement.forEach {
                    if (orderItem?.second?.let { name -> it.name.contains(name) } == true) {
                        isGetranke = true
                        println("isGetranke")
                    }
                }
                if (!isGetranke)
                    orderPizzeCount -= orderItem?.first ?: 0
                var countOrder = 0
                order.forEach {
                    countOrder += it.first
                }
                viewModel.setCountOrder(countOrder)
                viewModel.setOrder(order)
                binding.orderTotal.text =
                    getString(R.string.order_total, computePrice(order))
                if (order.size == 0) {
                    orderPizzeCount = 0
                    (activity as HomeActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.kiosk_container, HomeFragment())
                        .commit()
                }
            },
            null
        )

        val eraseOrder =
            (activity as HomeActivity).findViewById<AppCompatImageView>(R.id.order_erase)
        eraseOrder.visibility = VISIBLE

        eraseOrder.setOnClickListener {
            viewModel.eraseOrder()
            viewModel.setCountOrder(0)
            (activity as HomeActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.kiosk_container, HomeFragment())
                .commit()
        }

        val layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )

        binding.orderList.layoutManager = layoutManager
        binding.orderList.adapter = adapter

        initClientPrinter()

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.order.observe(viewLifecycleOwner) {
            dbSb = StringBuilder()
            sb = StringBuilder().append("\n\n\n")
            totalPrice = 0.00
            if (!it.isEmpty()) {
                it.forEach {
                    sb.append("${it.second} x${it.first}  ${it.third} CHF *")
                    dbSb.append("${it.first} ${it.second}")
                    totalPrice += it.third
                }
                binding.confirmOrder.text = getString(R.string.er_bestätigt)
            }
        }

        binding.backToHome.setOnClickListener {
            (activity as HomeActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.kiosk_container, HomeFragment())
                .commit()
        }

        binding.confirmOrder.setOnClickListener {

            dialog = CustomDialog(
                getString(R.string.complete_order),
                null,
                null,
                // ho selezionato sì
                {
                    orderNumber++
                    printClientReceipt()
                    Util.appendLog("print BLT_cliente ended")
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            initManagerPrinter()
                            Util.appendLog("init manager printer")
                        },
                        1000
                    )
                    innerDialog = CustomDialog(
                        getString(R.string.inner_dialog_text),
                        null,
                        null,
                        // ho selezionato sì
                        {
                            insertNameDialog = InsertNameDialog(
                                {
                                    if (insertNameDialog.orderOwner.isNotEmpty()) {
                                        printManager(false)
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            getString(R.string.order_name_missing),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                {
                                    printManager(true)
                                }
                            )
                            innerDialog.dismiss()
                            insertNameDialog.isCancelable = false
                            insertNameDialog.show(childFragmentManager, null)
                        },
                        // ho selezionato no
                        {
                            printManager(true)
                        }
                    )
                    innerDialog.isCancelable = false
                    innerDialog.show(childFragmentManager, null)
                    dialog.dismiss()
                },
                // ho selezionato no
                {
                    dialog.dismiss()
                }
            )
            dialog.isCancelable = false
            dialog.show(childFragmentManager, null)
        }

    }

    private fun computePrice(
        orders: ArrayList<Triple<Int,String,Double>>
    ): String {
        var ret = 0.00
        orders.forEach {
            ret += it.third
        }
        return String.format("%.2f", ret)
    }

    private fun getPathForRawResource(
        context: Context,
        resId: Int
    ): String {
        val inputStream = context.resources.openRawResource(resId)
        val outputFile = File(context.filesDir, "temp.jpeg")

        inputStream.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return outputFile.absolutePath
    }

    private fun initClientPrinter() {
        foodTruckPrinterClient = BLTPrinter.getInstance()
        Util.appendLog("connecting kunden printer")
        foodTruckPrinterClient.connect(clientPrinterAddress)
    }

    private fun initManagerPrinter() {
        foodTruckPrinterManager = BLTPrinter.getInstance()
        Util.appendLog("connecting manager printer")
        foodTruckPrinterManager.connect(managerPrinterAddress)
    }

    private fun printClientReceipt() {
        val header = Util.getReceiptHeader(
            mDate
        )
        val food = Util.getReceiptFoodClient(
            viewModel = viewModel,
            context = requireContext(),
            orderText = sb.toString()
        )
        val clientFooter = Util.getClientReceiptFooter(
            totalPrice,
            orderNumber.toString()
        )

        println(food)

        foodTruckPrinterClient.printImage(getPathForRawResource(requireContext(), R.raw.receipt_header), true)
        foodTruckPrinterClient.printBLT(header, false, false, true, false)
        foodTruckPrinterClient.printBLT(food, false, false, true, false)
        foodTruckPrinterClient.printBLT(clientFooter, true, true, true, false)
        foodTruckPrinterClient.printBLT("**********", true, true, true, true)
        foodTruckPrinterClient.printBLT("\n", false, false, false, false)
        foodTruckPrinterClient.close()
    }

    private fun printManagerReceipt(isNumberFooter: Boolean) {
        val headerManager = Util.getReceiptHeader(
            mDate
        )

        val foodManager = Util.getReceiptFoodManager(
            viewModel = viewModel,
            context = requireContext(),
            orderText = sb.toString()
        )

        val footer = if (!isNumberFooter) Util.getManagerReceiptFooter(
            owner = getString(R.string.order_owner_text, insertNameDialog.orderOwner),
            number = getString(R.string.order_text_number, orderNumber.toString()),
            totalPrice = totalPrice
        ) else Util.getManagerReceiptNumberFooter(
            number = getString(R.string.order_text_number, orderNumber.toString()),
            totalPrice = totalPrice
        )

        println(foodManager)

        foodTruckPrinterManager.printBLT(headerManager, false, false, true, false)
        foodTruckPrinterManager.printBLT(foodManager, true, true, true, false)
        foodTruckPrinterManager.printBLT(footer, true, true, true, false)
        foodTruckPrinterManager.printBLT("**********", true, true, true, true)
        foodTruckPrinterManager.printBLT("\n", false, false, false, false)
        foodTruckPrinterManager.close()
    }

    private fun printManager(isNumberFooter: Boolean) {
        if (isNumberFooter) {

            insertOrder(true)
            printManagerReceipt(true)
            Util.appendLog("print BLT_manager ended")

        } else {

            insertOrder(false)
            printManagerReceipt(false)
            Util.appendLog("print BLT_manager ended")

        }

        if (orderNumber == 999)
                orderNumber = 0

        (activity as HomeActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.kiosk_container, HomeFragment())
            .commit()
    }

    private fun insertOrder(isNumberFooter: Boolean) {

        val order = Order(
            id = Random().nextInt(),
            orderOwner = if (isNumberFooter) null else insertNameDialog.orderOwner,
            orderText = dbSb.toString(),
            orderPrice = floor(totalPrice * 100) / 100,
            orderDate = date,
            orderNumber = orderNumber,
            orderPizzenCount = orderPizzeCount
        )
        viewModel.insertOrder(order)
        viewModel.eraseOrder()
        viewModel.setCountOrder(0)
        orderPizzeCount = 0
    }

    private fun reinitClientPrinter() {
        reconnectDialogKunden =
            ReconnectDialog {
                Util.appendLog("reinit client printer")
                initClientPrinter()
                reconnectDialogKunden.dismiss()
            }
        reconnectDialogKunden.isCancelable = false
        reconnectDialogKunden.show(childFragmentManager, null)
    }

    private fun reinitManagerPrinter() {
        reconnectDialogManager = ReconnectDialog {
            Util.appendLog("reinit manager printer")
            initManagerPrinter()
            reconnectDialogManager.dismiss()
        }
        reconnectDialogManager.isCancelable = false
        reconnectDialogManager.show(childFragmentManager, null)
    }

}