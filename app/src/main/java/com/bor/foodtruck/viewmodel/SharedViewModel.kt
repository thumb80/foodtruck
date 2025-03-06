package com.bor.foodtruck.viewmodel

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bor.foodtruck.FoodTruckApplication
import com.bor.foodtruck.R
import com.bor.foodtruck.database.OrderDatabase
import com.bor.foodtruck.model.EditListAdapter
import com.bor.foodtruck.model.Item
import com.bor.foodtruck.model.Order
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.Date


class SharedViewModel(private val orderDb: OrderDatabase): ViewModel() {

    private var _qty: MutableLiveData<Int> = MutableLiveData(1)
    val qty: LiveData<Int> get() = _qty

    private var countName: Int = 0
    private var countPrice: Int = 0

    private var _countOrder: MutableLiveData<Int> = MutableLiveData(0)
    val countOrder: LiveData<Int> get() = _countOrder

    private var _dayGain: MutableLiveData<Double> = MutableLiveData()
    val dayGain: LiveData<Double> get() = _dayGain

    private var _monthGain: MutableLiveData<Double> = MutableLiveData()
    val monthGain: LiveData<Double> get() = _monthGain

    private var _pizzenCount: MutableLiveData<Int> = MutableLiveData()
    val pizzenCount: LiveData<Int> get() = _pizzenCount

    private var _order: MutableLiveData<ArrayList<Triple<Int,String, Double>>> = MutableLiveData()
    val order: LiveData<ArrayList<Triple<Int,String, Double>>> get() = _order

    private var _areEditsItemFilled: MutableLiveData<Pair<Boolean,Boolean>> = MutableLiveData()
    val  areEditsItemFilled: LiveData<Pair<Boolean,Boolean>> get() = _areEditsItemFilled

    private var _orderSb: MutableLiveData<StringBuilder> = MutableLiveData()
    val orderSb: LiveData<StringBuilder> get() = _orderSb

    fun checkIngredientFile(
        context: Context
    ): Boolean {
        val fis: FileOutputStream
        return try {
            fis = context.openFileOutput("zutaten.json", Context.MODE_APPEND)
            fis.channel.size() != 0L
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun prepareZutatenFile(
        context: Context
    ) {
        val fis: FileOutputStream
        try {
            val file = File(context.filesDir, "zutaten.json")
            fis = FileOutputStream(file)
            fis.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun insertZutaten(
        context: Context,
        fileName: String,
        item: Item,
        instance: EditListAdapter
    ): Boolean {
        val gson = Gson()
        val json: String?
        var jsonElement: List<Item>?
        val type = object : TypeToken<List<Item>>() {}.type
        try {
            val file = File(context.filesDir, fileName)
            val `is`: InputStream = file.inputStream()
            val size = `is`.available()
            val buffer = size.let { ByteArray(it) }
            `is`.read(buffer)
            json = String(buffer, Charset.defaultCharset())
            jsonElement = gson.fromJson<List<Item>>(json, type)
            if (jsonElement == null) {
                jsonElement = arrayListOf<Item>()
                jsonElement.add(item)
            } else {
                (jsonElement as ArrayList<Item>).add(item)
            }
            instance.items = jsonElement
            instance.notifyDataSetChanged()
            val jsonArr = JsonArray()
            val jsonType = object : TypeToken<Item>() {}.type
            jsonElement.forEach {
                val jsonEl = gson.toJsonTree(it, jsonType)
                val jsonObj = JsonObject()
                jsonObj.add("id", jsonEl.asJsonObject.get("id"))
                jsonObj.add("name", jsonEl.asJsonObject.get("name"))
                jsonObj.add("price", jsonEl.asJsonObject.get("price"))
                jsonArr.add(jsonObj)
            }
            val outStream = FileOutputStream(file)
            IOUtils.copy(`is`, outStream)
            val fis = FileOutputStream(file)
            fis.write(jsonArr.toString().toByteArray())
            fis.close()
            `is`.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
            return false
        }
        return true
    }

    fun loadRawPizzen(
        context: Context,
        fileName: String
    ): String? {
        val json: String? = try {
            var file: File? = null
            val filesDirFile = File(context.filesDir, fileName)
            var fos: FileOutputStream? = null
            val `is`: InputStream = if (filesDirFile.exists())
                FileUtils.openInputStream(filesDirFile)
            else {
                file = filesDirFile
                fos = FileOutputStream(file)
                context.resources.openRawResource(R.raw.pizzen)
            }
            val size = `is`.available()
            val buffer = size.let { ByteArray(it) }
            `is`.read(buffer)
            if (file != null) {
                IOUtils.copy(`is`, fos)
                fos?.write(buffer)
            }
            `is`.close()
            fos?.close()
            String(buffer, Charset.defaultCharset())
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    fun loadRawGetranke(
        context: Context,
        fileName: String
    ): String? {
        val json: String? = try {
            var file: File? = null
            val filesDirFile = File(context.filesDir, fileName)
            var fos: FileOutputStream? = null
            val `is`: InputStream = if (filesDirFile.exists())
                FileUtils.openInputStream(filesDirFile)
            else {
                file = filesDirFile
                fos = FileOutputStream(file)
                context.resources.openRawResource(R.raw.getranke)
            }
            val size = `is`.available()
            val buffer = size.let { ByteArray(it) }
            `is`.read(buffer)
            if (file != null) {
                IOUtils.copy(`is`, fos)
                fos?.write(buffer)
            }
            `is`.close()
            fos?.close()
            String(buffer, Charset.defaultCharset())
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    fun insertItem(
        context: Context,
        fileName: String,
        item: Item,
        instance: EditListAdapter
    ): Boolean {
        val json: String?
        val gson = Gson()
        val type = object : TypeToken<List<Item>>() {}.type
        try {
            val `is`: InputStream = File(context.filesDir, fileName).inputStream()
            val size = `is`.available()
            val buffer = size.let { ByteArray(it) }
            `is`.read(buffer)
            json = String(buffer, Charset.defaultCharset())
            val jsonElement = gson.fromJson<List<Item>>(json, type)
            (jsonElement as ArrayList<Item>).add(item)
            instance.items = jsonElement
            instance.notifyDataSetChanged()
            val jsonArr = JsonArray()
            val jsonType = object : TypeToken<Item>() {}.type
            jsonElement.forEach {
                val jsonEl = gson.toJsonTree(it, jsonType)
                val jsonObj = JsonObject()
                jsonObj.add("id", jsonEl.asJsonObject.get("id"))
                jsonObj.add("name", jsonEl.asJsonObject.get("name"))
                jsonObj.add("price", jsonEl.asJsonObject.get("price"))
                jsonObj.add("isPizzen", jsonEl.asJsonObject.get("isPizzen"))
                jsonArr.add(jsonObj)
            }
            val file = File(context.filesDir, fileName)
            val outStream = FileOutputStream(file)
            IOUtils.copy(`is`, outStream)
            val fis = FileOutputStream(file)
            fis.write(jsonArr.toString().toByteArray())
            fis.close()
            `is`.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
            return false
        }
        return true
    }

    fun modifyItem(
        context: Context,
        fileName: String,
        item: Item,
        instance: EditListAdapter,
        position: Int
    ): Boolean {
        val json: String?
        val gson = Gson()
        val type = object : TypeToken<List<Item>>() {}.type
        try {
            val `is`: InputStream = File(context.filesDir, fileName).inputStream()
            val size = `is`.available()
            val buffer = size.let { ByteArray(it) }
            `is`.read(buffer)
            json = String(buffer, Charset.defaultCharset())
            val jsonElement = gson.fromJson<List<Item>>(json, type)
            (jsonElement as ArrayList<Item>)[position] = item
            instance.items = jsonElement
            instance.notifyDataSetChanged()
            val jsonArr = JsonArray()
            val jsonType = object : TypeToken<Item>() {}.type
            jsonElement.forEach {
                val jsonEl = gson.toJsonTree(it, jsonType)
                val jsonObj = JsonObject()
                jsonObj.add("id", jsonEl.asJsonObject.get("id"))
                jsonObj.add("name", jsonEl.asJsonObject.get("name"))
                jsonObj.add("price", jsonEl.asJsonObject.get("price"))
                jsonObj.add("isPizzen", jsonEl.asJsonObject.get("isPizzen"))
                jsonArr.add(jsonObj)
            }
            val file = File(context.filesDir, fileName)
            val outStream = FileOutputStream(file)
            IOUtils.copy(`is`, outStream)
            val fis = FileOutputStream(file)
            fis.write(jsonArr.toString().toByteArray())
            fis.close()
            `is`.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
            return false
        }
        return true
    }

    fun deleteItem(
        context: Context,
        fileName: String,
        item: Item?,
        instance: EditListAdapter
    ): Boolean {
        val json: String?
        val gson = Gson()
        val type = object : TypeToken<List<Item>>() {}.type
        try {
            val `is`: InputStream = File(context.filesDir, fileName).inputStream()
            val size = `is`.available()
            val buffer = size.let { ByteArray(it) }
            `is`.read(buffer)
            json = String(buffer, Charset.defaultCharset())
            val jsonElement = gson.fromJson<List<Item>>(json, type)
            (jsonElement as ArrayList<Item>).remove(item)
            instance.items = jsonElement
            instance.notifyDataSetChanged()
            val jsonArr = JsonArray()
            val jsonType = object : TypeToken<Item>() {}.type
            jsonElement.forEach {
                val jsonEl = gson.toJsonTree(it, jsonType)
                val jsonObj = JsonObject()
                jsonObj.add("id", jsonEl.asJsonObject.get("id"))
                jsonObj.add("name", jsonEl.asJsonObject.get("name"))
                jsonObj.add("price", jsonEl.asJsonObject.get("price"))
                jsonObj.add("isPizzen", jsonEl.asJsonObject.get("isPizzen"))
                jsonArr.add(jsonObj)
            }
            val file = File(context.filesDir, fileName)
            val outStream = FileOutputStream(file)
            IOUtils.copy(`is`, outStream)
            val fis = FileOutputStream(file)
            fis.write(jsonArr.toString().toByteArray())
            fis.close()
            `is`.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
            return false
        }
        return true
    }

    fun loadZutaten(
        context: Context,
        fileName: String
    ): String? {
        var json: String? = null
        json = try {
            val `is`: InputStream = File(context.filesDir, fileName).inputStream()
            val size = `is`.available()
            val buffer = size.let { ByteArray(it) }
            `is`.read(buffer)
            `is`.close()
            String(buffer, Charset.defaultCharset())
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    fun setQty(qty: Int) {
        _qty.postValue(qty)
    }

    fun resetQty() {
        _qty.postValue(1)
    }

    fun setOrder(value: ArrayList<Triple<Int,String,Double>>) {
        _order.postValue(value)
    }

    fun invokeOrderObserver() {
        _order.postValue(FoodTruckApplication.order)
    }

    fun addItemToOrder(value: Triple<Int, String, Double>) {
        FoodTruckApplication.order.add(value)
        _order.postValue(FoodTruckApplication.order)
    }

    fun appendOrder(value: Triple<Int, String, Double>, position: Int) {
        FoodTruckApplication.order[position] = Triple(
            value.first,
            value.second,
            FoodTruckApplication.order[position].third + value.third
        )
        _order.postValue(FoodTruckApplication.order)
    }

    fun updateItemOrder(value: Triple<Int, String, Double>, position: Int) {
        FoodTruckApplication.order[position] = Triple(
            value.first,
            value.second,
            value.third
        )
        _order.postValue(FoodTruckApplication.order)
    }

    fun updateOrder(value: Triple<Int, String, Double>, position: Int) {
        FoodTruckApplication.order[position] = Triple(
            value.first,
            value.second,
            FoodTruckApplication.order[position].third - value.third
        )
        _order.postValue(FoodTruckApplication.order)
    }

    fun removeOrder(value: Triple<Int, String, Double>) {
        FoodTruckApplication.orderPizzeCount = FoodTruckApplication.orderPizzeCount - 1
        FoodTruckApplication.order.remove(value)
        _order.postValue(FoodTruckApplication.order)
    }

    fun editOrder(value: Triple<Int, String, Double>, instance: EditListAdapter, position: Int) {
        FoodTruckApplication.order[position] = Triple(
            value.first,
            FoodTruckApplication.order[position].second,
            value.third
        )
        _order.postValue(FoodTruckApplication.order)
        instance.notifyItemChanged(position)
    }

    fun eraseOrder() {
        _order.postValue(arrayListOf())
        FoodTruckApplication.order = arrayListOf()
    }

    fun setCountOrder(value: Int) {
        _countOrder.postValue(value)
    }

    fun insertOrder(vararg orders: Order) {
        val thread = Thread {
            orderDb.orderDao().insertAll(*orders)
        }
        thread.start()
    }

    suspend fun getAllOrder(): ArrayList<Order> {
        var ret: ArrayList<Order>
        withContext(Dispatchers.IO) {
            ret = orderDb.orderDao().getAll() as ArrayList<Order>
        }
        return ret
    }

    suspend fun getDayOrder(): ArrayList<Order> {
        var ret: ArrayList<Order>
        withContext(Dispatchers.IO) {
            ret = orderDb.orderDao().getDayOrder() as ArrayList<Order>
        }
        return ret
    }

    fun setDayGain(value: Double) {
        _dayGain.postValue(value)
    }

    fun setMonthGain(value: Double) {
        _monthGain.postValue(value)
    }

    fun setPizzenCount(value: Int) {
        _pizzenCount.postValue(value)
    }

    fun checkEditsItemFilled(
        firstEdit: AppCompatEditText,
        secondEdit: AppCompatEditText
    ) {

        countName = firstEdit.text?.length ?: 0
        countPrice = secondEdit.text?.length ?: 0

        firstEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                countName = s?.length ?: 0
                _areEditsItemFilled.postValue(Pair(countName!= 0, countPrice != 0))
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        secondEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                countPrice = s?.length ?: 0
                _areEditsItemFilled.postValue(Pair(countName != 0, countPrice != 0))
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    fun setOrderStringBuilder(value: StringBuilder) {
        _orderSb.postValue(value)
    }

    private fun loadDatabase(
        date: Date
    ) : OrderDatabase {
        return orderDb.getDatabase(date)
    }

    suspend fun getAllDatabaseOrder(
        date: Date
    ): ArrayList<Order> {
        var ret: ArrayList<Order>
        val db = loadDatabase(date)
        withContext(Dispatchers.IO) {
            ret = db.orderDao().getAll() as ArrayList<Order>
        }
        return ret
    }

}