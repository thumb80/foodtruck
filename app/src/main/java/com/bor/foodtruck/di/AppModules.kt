package com.bor.foodtruck.di

import androidx.room.Room
import com.bor.foodtruck.database.OrderDatabase
import com.bor.foodtruck.viewmodel.SharedViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun appModule(date: Date) = module {

    // ViewModel Dependency
    viewModel { SharedViewModel(get()) }

    // Database dependency
    single {
        val sdf = SimpleDateFormat("MMMM", Locale.getDefault())
        Room
         .databaseBuilder(
             androidApplication(),
             OrderDatabase::class.java,
             "orders_${sdf.format(date)}"
         )
         .build()
    }

    // DAO dependency
    single {
        get<OrderDatabase>().orderDao()
    }

}