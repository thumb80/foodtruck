package com.bor.foodtruck.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Order(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "order_owner") val orderOwner: String?,
    @ColumnInfo(name = "order_text") val orderText: String?,
    @ColumnInfo(name = "order_price") val orderPrice: Double?,
    @ColumnInfo(name = "order_date") val orderDate: Long?,
    @ColumnInfo(name = "order_number") val orderNumber: Int?,
    @ColumnInfo(name = "order_pizzen_count") val orderPizzenCount: Int?

)