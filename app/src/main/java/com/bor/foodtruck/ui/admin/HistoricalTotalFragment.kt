package com.bor.foodtruck.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bor.foodtruck.R
import com.bor.foodtruck.activity.AdminActivity
import com.bor.foodtruck.databinding.FragmentHistoricalTotalBinding
import com.bor.foodtruck.model.HistoricalOrderListAdapter
import com.bor.foodtruck.model.Order
import com.bor.foodtruck.viewmodel.SharedViewModel
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HistoricalTotalFragment: Fragment() {

    companion object {
        private var currentDayInstance: Calendar? = null
    }

    private lateinit var binding: FragmentHistoricalTotalBinding
    val monthDateFormat = SimpleDateFormat("MMMM", Locale.getDefault())
    private val viewModel: SharedViewModel by sharedViewModel<SharedViewModel>()
    private lateinit var adapter: HistoricalOrderListAdapter
    private var orders: ArrayList<Order> = arrayListOf()
    private var dayGain = 0.0
    private var monthGain = 0.0
    private var pizzenCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoricalTotalBinding.inflate(layoutInflater)

        currentDayInstance = Calendar.getInstance(TimeZone.getDefault())

        viewLifecycleOwner.lifecycleScope.launch {
            orders = arrayListOf()
            orders = viewModel.getAllDatabaseOrder(currentDayInstance?.time ?: Date(System.currentTimeMillis()))
            collectOrders(orders)
            computeMonthGain(currentDayInstance?.time)
            displayCurrentEvents(currentDayInstance?.time)
        }

        binding.monthView.text = currentDayInstance?.time?.let {
            monthDateFormat.format(it).capitalize()
        }

        binding.calendarView.setLocale(TimeZone.getDefault(), Locale.getDefault())
        binding.calendarView.setUseThreeLetterAbbreviation(true)

        binding.calendarView.setListener(object :
            CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date?) {
                displayCurrentEvents(dateClicked)
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date?) {
                binding.monthView.text = monthDateFormat
                    .format(firstDayOfNewMonth?.time)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                binding.calendarView.setCurrentDate(firstDayOfNewMonth)

                orders.clear()

                CoroutineScope(Dispatchers.Main).launch {
                    firstDayOfNewMonth?.let {
                        orders = viewModel.getAllDatabaseOrder(it)
                        collectOrders(orders)
                        computeMonthGain(it)
                        displayCurrentEvents(it)
                    }
                }
            }

        })

        binding.nextMonth.setOnClickListener {
            binding.calendarView.shouldSelectFirstDayOfMonthOnScroll(true)
            binding.calendarView.scrollRight()
        }

        binding.prevMonth.setOnClickListener {
            binding.calendarView.scrollLeft()
        }

        binding.backToHome.setOnClickListener {
            (activity as AdminActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.admin_container, AdminMenuFragment())
                .commit()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.dayGain.observe(viewLifecycleOwner) {
            binding.totalDayText.text = getString(R.string.total_day_text, String.format("%.2f", it))
        }

        viewModel.monthGain.observe(viewLifecycleOwner) {
            binding.monthTotal.text = getString(R.string.total_month_text, String.format("%.2f", it))
        }

        viewModel.pizzenCount.observe(viewLifecycleOwner) {
            binding.pizzenCountText.text = getString(R.string.pizzen_count_text, it.toString())
        }

    }

    private fun collectOrders(orders: ArrayList<Order>) {
        if (orders.isNotEmpty()) {
            for (order in orders) {
                order.orderDate?.let { orderDate ->
                    var isAlreadyPresent = false
                    var sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    binding.calendarView.getEvents(sdf.parse(sdf.format(orderDate))).forEach {
                        if (it.timeInMillis == orderDate) {
                            isAlreadyPresent = true
                            return@forEach
                        }
                    }
                    if (!isAlreadyPresent) {
                        binding.calendarView.addEvent(context?.getColor(R.color.colorWhite)?.let { color ->
                            Event(
                                color,
                                orderDate,
                                order
                            )
                        })
                    }
                }
            }
        }
        else {
            showEmpty()
        }
    }

    private fun computeMonthGain(date: Date?) {
        val sdf = SimpleDateFormat("MM", Locale.getDefault())
        monthGain = 0.0
        orders.forEach {
            if (sdf.format(it.orderDate)?.equals(date?.let { date -> sdf.format(date) }) == true)
                monthGain += it.orderPrice!!
        }
        viewModel.setMonthGain(monthGain)
    }

    private fun displayCurrentEvents(currentDate: Date?) {

        binding.noData.visibility = View.INVISIBLE

        val events: List<Event> = binding.calendarView.getEvents(currentDate).orEmpty()

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        dayGain = 0.0
        pizzenCount = 0
        viewModel.setPizzenCount(pizzenCount)
        viewModel.setDayGain(dayGain)

        if (events.isNotEmpty()) {

            val orders = JsonArray(events.size)
            for (event in events)
            {
                orders.add(JsonParser().parse(GsonBuilder().setLenient().create().toJson(event.data)))
            }
            orders.forEach {
                val instant = it.asJsonObject.get("orderDate")?.asLong
                val dateOrder = instant?.let { milliseconds -> Date(milliseconds) }
                if (sdf.format(dateOrder).equals(sdf.format(currentDate))) {
                    dayGain += it.asJsonObject.get("orderPrice").asDouble
                    viewModel.setDayGain(dayGain)
                    pizzenCount += it.asJsonObject.get("orderPizzenCount").asInt
                    viewModel.setPizzenCount(pizzenCount)
                }
            }

            val layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
            binding.ordersRecycler.layoutManager = layoutManager
            adapter = HistoricalOrderListAdapter(orders)
            binding.ordersRecycler.adapter = adapter
            hideEmpty()
        }
        else {
            binding.ordersRecycler.adapter = null
            showEmpty()
        }
    }

    private fun showEmpty() {
        binding.noData.visibility = View.VISIBLE
        binding.noData.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
        binding.ordersRecycler.visibility = View.INVISIBLE
    }

    private fun hideEmpty() {
        binding.noData.visibility = View.INVISIBLE
        binding.ordersRecycler.visibility = View.VISIBLE
    }

}