package com.bor.foodtruck.util

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bor.foodtruck.FoodTruckApplication.Companion.logFile
import com.bor.foodtruck.R
import com.bor.foodtruck.databinding.ViewPdfHeaderBinding
import com.bor.foodtruck.ext.getPartBeforeRegex
import com.bor.foodtruck.ext.isInItemsList
import com.bor.foodtruck.ext.isItemZutaten
import com.bor.foodtruck.ext.removeWhiteSpaces
import com.bor.foodtruck.ext.toPx
import com.bor.foodtruck.model.Item
import com.bor.foodtruck.viewmodel.SharedViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random


object Util {

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun getReceiptHeader(
        date: String
    ): String {
        val sb = StringBuilder()
        sb.append(date)
        sb.append("\n")
        return sb.toString()
    }


    fun getReceiptFoodClient(
        viewModel: SharedViewModel,
        context: Context,
        orderText: String,
    ): String {

        val gson = Gson()
        val type = object : TypeToken<List<Item>>() {}.type
        val getrankeJsonElement = gson.fromJson<List<Item>>(viewModel.loadRawGetranke(context, "getranke.json"), type)
        val getrankeList = getrankeJsonElement.map { it.name }
        val zutatenJsonElement = gson.fromJson<List<Item>>(viewModel.loadRawGetranke(context, "zutaten.json"), type)
        val zutatenList = zutatenJsonElement.map { it.name }

        val orderSplit = orderText.replace("\n", "").split("*")
        val text = StringBuilder()

        text.append("**** Pizzen ****")
        text.append("\n\n")

        orderSplit.forEach { orderString ->
            if (!getrankeList.isInItemsList(orderString)) {
                if (zutatenList.isItemZutaten(orderString))
                    text.append("Wunschpizza: ${orderString.removeWhiteSpaces()}").append("\n")
                else
                    text.append(orderString.removeWhiteSpaces()).append("\n")
            }
        }

        text.append("\n\n")
        text.append("**** Getranke ****")
        text.append("\n\n")

        orderSplit.forEach { orderString ->
            if (getrankeList.isInItemsList(orderString))
                text.append(orderString.removeWhiteSpaces()).append("\n")
        }

        return text.toString()
    }

    fun getReceiptFoodManager(
        viewModel: SharedViewModel,
        context: Context,
        orderText: String,
    ): String {

        val gson = Gson()
        val type = object : TypeToken<List<Item>>() {}.type
        val getrankeJsonElement = gson.fromJson<List<Item>>(viewModel.loadRawGetranke(context, "getranke.json"), type)
        val getrankeList = getrankeJsonElement.map { it.name }
        val zutatenJsonElement = gson.fromJson<List<Item>>(viewModel.loadRawGetranke(context, "zutaten.json"), type)
        val zutatenList = zutatenJsonElement.map { it.name }

        val orderSplit = orderText.replace("\n", "").split("*")
        val text = StringBuilder()

        text.append("**** Pizzen ****")
        text.append("\n\n")

        orderSplit.forEach { orderString ->
            if (!getrankeList.isInItemsList(orderString)) {
                if (zutatenList.isItemZutaten(orderString))
                    text.append("Wunschpizza : ${orderString.getPartBeforeRegex()}").append("\n")
                else
                    text.append(orderString.getPartBeforeRegex()).append("\n")
            }
        }

        text.append("\n\n")
        text.append("**** Getranke ****")
        text.append("\n\n")

        orderSplit.forEach { orderString ->
            if (getrankeList.isInItemsList(orderString))
                text.append(orderString.getPartBeforeRegex()).append("\n")
        }

        return text.toString()
    }

    fun getClientReceiptFooter(
        totalPrice: Double,
        orderNumber: String
    ): String {
        val text = StringBuilder()
        text.append("\n")
        text.append("Bestellnummer : $orderNumber")
        text.append("\n")
        text.append("Gesamt : ${String.format("%.2f", totalPrice)}-CHF")
        text.append("\n")
        return text.toString()
    }

    fun getManagerReceiptFooter(
        owner: String,
        number: String,
        totalPrice: Double
    ): String {
        val text = StringBuilder()
        text.append("\n")
        text.append(owner)
        text.append("\n")
        text.append(number)
        text.append("\n\n")

        text.append("Gesamt : ${String.format("%.2f", totalPrice)}-CHF")
        text.append("\n")
        return text.toString()
    }

    fun getManagerReceiptNumberFooter(
        number: String,
        totalPrice: Double
    ): String {
        val text = StringBuilder()
        text.append("\n")
        text.append(number)
        text.append("\n\n")

        text.append("Gesamt : ${String.format("%.2f", totalPrice)}-CHF")
        text.append("\n\n\n")
        return text.toString()
    }

    fun createPdfDocument(
        context: Context,
        layoutInflater: LayoutInflater,
        owner: String,
        orderText: String,
        totalPrice: Double,
        date: String,
        orderNumber: Int): PdfDocument {

        val orderSplit = orderText.split("*","\n")
        var lineCount = orderSplit.size

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(
            164,
            computeTextHeight(lineCount),
            1
        ).create()

        val page = pdfDocument.startPage(pageInfo)
        val parentView = parentView(context)

        val pdfHeaderView = pdfHeaderView(
            context,
            layoutInflater,
            page,
            orderText,
            totalPrice,
            date,
            orderNumber
        )

        parentView.addView(pdfHeaderView)
        parentView.draw(page.canvas)

        pdfDocument.finishPage(page)
        parentView.removeAllViews()

        //writeToFileSystem(pdfDocument, owner)
        return pdfDocument
    }

    private fun computeTextHeight(
        lineCount: Int
    ): Int {
        return 136.toPx() + 8.toPx() * lineCount
    }

    private fun parentView(context: Context): ConstraintLayout {
        val parentView = ConstraintLayout(context, null, R.style.Theme_FoodTrack)
        val params = ConstraintLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        parentView.layoutParams = params
        return parentView
    }

    private fun pdfHeaderView(
        context: Context,
        layoutInflater: LayoutInflater,
        page: PdfDocument.Page,
        orderText: String,
        totalPrice: Double,
        date: String,
        orderNumber: Int) =
        with(
            ViewPdfHeaderBinding.inflate(
                layoutInflater
            )
        ) {
            val orderSplit = orderText.split("*")
            val text = StringBuilder()

            orderSplit.forEach {
                text.append(it).append("\n")
            }

            val measuredWidth = View.MeasureSpec.makeMeasureSpec(page.canvas.width, View.MeasureSpec.EXACTLY)
            val measuredHeight = View.MeasureSpec.makeMeasureSpec(page.canvas.height, View.MeasureSpec.EXACTLY)
            val spannable = SpannableString(context.resources.getString(R.string.order_number, orderNumber.toString()))
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 14, 14 + orderNumber.toString().length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            this.dateTextPdf.text = date
            this.header.text = context.resources.getString(R.string.enterprise_data_string)
            this.orderTextPdf.text = text.toString()
            this.orderNumberPdf.setText(spannable, TextView.BufferType.SPANNABLE)
            this.totalTextPdf.text = context.resources.getString(R.string.total_pdf_string, totalPrice.toString())
            this.root.measure(measuredWidth, measuredHeight)
            this.root.layout(0, 0, page.canvas.width, page.canvas.height)

            this.root
        }

    fun writeToFileSystem(doc: PdfDocument, owner: String): File {
        val reportFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "report")
        if (!reportFolder.exists())
            reportFolder.mkdir()
        val file = File(
            reportFolder,
            "${owner}_${Random().nextInt(1000)}.pdf"
        )
        doc.writeTo(file.outputStream())
        doc.close()
        return file
    }

    fun writeLog(text: String?) {
        try {
            //BufferedWriter for performance, true to set append to file flag
            val buf = BufferedWriter(FileWriter(logFile, false))
            val sb = StringBuilder()
            sb.append(sdf.format(Date(System.currentTimeMillis())) + "-" + text)
            buf.write(sb.toString())
            buf.newLine()
            buf.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun appendLog(text: String?) {
        try {
            //BufferedWriter for performance, true to set append to file flag
            val buf = BufferedWriter(FileWriter(logFile, true))
            val sb = StringBuilder()
            sb.append(sdf.format(Date(System.currentTimeMillis())) + "-" + text)
            buf.append(sb.toString())
            buf.newLine()
            buf.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}