package com.bor.foodtruck.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bor.foodtruck.FoodTruckApplication
import com.bor.foodtruck.dao.OrderDao
import com.bor.foodtruck.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(entities = [Order::class], version = 1)
abstract class OrderDatabase : RoomDatabase() {

    abstract fun orderDao(): OrderDao

    fun getDatabase(
        date: Date
    ): OrderDatabase {
        val sdf = SimpleDateFormat("MMMM", Locale.getDefault())
        synchronized(this) {
            val instance = Room.databaseBuilder(
                FoodTruckApplication.instance,
                OrderDatabase::class.java,
                "orders_${sdf.format(date)}")
                .build()
            INSTANCE = instance
            return instance
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: OrderDatabase? = null
    }

}