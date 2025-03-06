package com.bor.foodtruck.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.bor.foodtruck.model.Order

@Dao
interface OrderDao {

    @Query("SELECT * FROM `Order`")
    fun getAll(): List<Order>

    @Insert
    fun insertAll(vararg orders: Order)

    @Delete
    fun delete(order: Order)

    @Query("SELECT * FROM `Order` WHERE strftime('%Y-%m-%d', order_date / 1000, 'unixepoch') = date()")
    fun getDayOrder(): List<Order>
}