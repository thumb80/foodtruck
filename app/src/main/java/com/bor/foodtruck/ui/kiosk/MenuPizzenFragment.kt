package com.bor.foodtruck.ui.kiosk

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bor.foodtruck.FoodTruckApplication
import com.bor.foodtruck.FoodTruckApplication.Companion.order
import com.bor.foodtruck.R
import com.bor.foodtruck.activity.HomeActivity
import com.bor.foodtruck.databinding.FragmentMenuPizzenBinding
import com.bor.foodtruck.ext.OrderQtyDialog
import com.bor.foodtruck.model.Item
import com.bor.foodtruck.model.MenuListAdapter
import com.bor.foodtruck.viewmodel.SharedViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class MenuPizzenFragment(
    val items: List<Item>,
    val drawable: Drawable,
    var isZutaten: Boolean
    ): Fragment() {

    private lateinit var binding: FragmentMenuPizzenBinding
    private lateinit var adapter: MenuListAdapter
    private val viewModel: SharedViewModel by sharedViewModel<SharedViewModel>()
    private var orderSb = StringBuilder()
    private var zutatenPrice = 0.0

    private lateinit var completeOrder: Button
    private lateinit var continueOrder: TextView

    companion object {
        var isFirstItem = true
        var isContinuation = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuPizzenBinding.inflate(layoutInflater)

        continueOrder = binding.root.findViewById<TextView>(R.id.continue_order)
        completeOrder = binding.root.findViewById<Button>(R.id.complete_order)

        if (isZutaten) {
            viewModel.setOrderStringBuilder(orderSb)

            binding.zutatenBackground.visibility = View.VISIBLE
            binding.zutatenHeader.visibility = View.VISIBLE
            if (order.size != 0)
                binding.completeOrder.visibility = View.VISIBLE

            viewModel.orderSb.observe(viewLifecycleOwner) {
                binding.zutatenHeader.text = it.toString()
                continueOrder.visibility = if (it.toString().isEmpty()) View.INVISIBLE else View.VISIBLE
            }
        } else {
            binding.zutatenBackground.visibility = View.INVISIBLE
            binding.pizzenBackground.background = ResourcesCompat.getDrawable(resources, R.drawable.pizzen_background, null)
            binding.zutatenHeader.visibility = View.GONE
        }

        adapter = MenuListAdapter(items, drawable, isZutaten,
            {
                if (isZutaten && isFirstItem) {

                isFirstItem = false
                isContinuation = false
                zutatenPrice += it.price

                orderSb.append(it.name)
                viewModel.setOrderStringBuilder(orderSb)

                viewModel.invokeOrderObserver()

            } else if (isZutaten) {

                if (orderSb.contains(it.name))
                    Toast.makeText(requireContext(), getString(R.string.zutaten_already_inserted), Toast.LENGTH_SHORT).show()
                else {

                    isContinuation = false
                    zutatenPrice += it.price

                    orderSb.append(", ").append(it.name)
                    viewModel.setOrderStringBuilder(orderSb)

                    viewModel.invokeOrderObserver()

                }

            } else {
                val dialog = OrderQtyDialog(it)
                dialog.show(childFragmentManager, null)
            }
            },
            { it, _ ->
                if (isZutaten) {
                    removeCallbackImplementation(orderSb, it)
                }
            }
        )

        val layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false)
        binding.menuList.layoutManager = layoutManager
        binding.menuList.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backToHome.setOnClickListener {

            isFirstItem = true
            orderSb = StringBuilder()
            zutatenPrice = 0.0
            viewModel.setOrderStringBuilder(orderSb)

            (activity as HomeActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.kiosk_container, HomeFragment())
                .commit()

        }

        viewModel.order.observe(viewLifecycleOwner) {

            if (isZutaten && orderSb.toString().isNotEmpty()) {

                if (isFirstItem)
                    continueOrder.visibility = View.INVISIBLE
                else
                    continueOrder.visibility = View.VISIBLE

                continueOrder.setOnClickListener {

                    if (orderSb.toString().contains(",")) {
                        val tuple = evaluateOrder(orderSb)

                        val orderPosition = if (tuple.first) tuple.second else order.lastIndex + 1

                        if (tuple.first) {
                            viewModel.appendOrder(Triple(tuple.third, orderSb.toString(), zutatenPrice), orderPosition)
                        } else {
                            viewModel.addItemToOrder(Triple(tuple.third, orderSb.toString(), zutatenPrice))
                        }

                        isFirstItem = true
                        isContinuation = true
                        orderSb = StringBuilder()
                        zutatenPrice = 0.0
                        viewModel.setOrderStringBuilder(orderSb)
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.zutaten_threshold), Toast.LENGTH_LONG).show()
                    }
                }

                completeOrder.visibility = View.VISIBLE
                completeOrder.setOnClickListener {

                    if (orderSb.toString().contains(",")) {
                        if (!isContinuation) {
                            val tuple = evaluateOrder(orderSb)

                            val orderPosition = if (tuple.first) tuple.second else order.lastIndex + 1

                            if (tuple.first) {
                                viewModel.appendOrder(Triple(tuple.third, orderSb.toString(), zutatenPrice), orderPosition)
                            } else {
                                viewModel.addItemToOrder(Triple(tuple.third, orderSb.toString(), zutatenPrice))
                            }
                        }


                        val gson = Gson()
                        val type = object : TypeToken<List<Item>>() {}.type
                        val jsonElement = gson.fromJson<List<Item>>(viewModel.loadRawGetranke(requireContext(), "getranke.json"), type)

                        isFirstItem = true
                        (activity as HomeActivity).supportFragmentManager.beginTransaction()
                            .replace(R.id.kiosk_container, MenuGetrankeFragment(jsonElement, AppCompatResources.getDrawable(requireContext(), R.drawable.getranke)!!))
                            .commit()
                    } else if (orderSb.toString().isEmpty()) {
                        val gson = Gson()
                        val type = object : TypeToken<List<Item>>() {}.type
                        val jsonElement = gson.fromJson<List<Item>>(viewModel.loadRawGetranke(requireContext(), "getranke.json"), type)

                        isFirstItem = true
                        (activity as HomeActivity).supportFragmentManager.beginTransaction()
                            .replace(R.id.kiosk_container, MenuGetrankeFragment(jsonElement, AppCompatResources.getDrawable(requireContext(), R.drawable.getranke)!!))
                            .commit()
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.zutaten_threshold), Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                if (it.size != 0) {
                    completeOrder.visibility = View.VISIBLE
                    completeOrder.setOnClickListener {
                        val gson = Gson()
                        val type = object : TypeToken<List<Item>>() {}.type
                        val jsonElement = gson.fromJson<List<Item>>(viewModel.loadRawGetranke(requireContext(), "getranke.json"), type)
                        isFirstItem = true
                        (activity as HomeActivity).supportFragmentManager.beginTransaction()
                            .replace(R.id.kiosk_container, MenuGetrankeFragment(jsonElement, AppCompatResources.getDrawable(requireContext(), R.drawable.getranke)!!))
                            .commit()
                    }
                } else {
                    completeOrder.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun removeLeadingSpaces(sb: StringBuilder): StringBuilder {
        var index = 0
        while (index < sb.length && sb[index].isWhitespace()) {
            index++
        }
        if (index > 0) {
            sb.delete(0, index)
        }
        return sb
    }

    private fun evaluateOrder(orderSb: StringBuilder): Triple<Boolean, Int, Int> {
        var position = -1
        var qty = 1
        var tuple = Triple(false, position, qty)
        order.forEachIndexed  { index, triple ->
            if (triple.second.contains(",")) {
                if (triple.second.split(",").size == orderSb.toString().split(",").size) {
                    val orderSplit = triple.second.split(",")
                    val newOrderSplit = orderSb.toString().split(",")
                    val orders = orderSplit.map { it.replace("\\s".toRegex(), "") }
                    val newOrders = newOrderSplit.map { it.replace("\\s".toRegex(), "") }
                    if (orders.sorted() == newOrders.sorted()) {
                        position = index
                        qty = triple.first + 1
                        return Triple(true, position, qty)
                    }
                }
            } else {
                if (triple.second == orderSb.toString()) {
                    position = index
                    qty = triple.first + 1
                    return Triple(true, position, qty)
                }
            }
        }
        return tuple
    }

    private fun removeCallbackImplementation(orderSb: StringBuilder, item: Item) {
        if (orderSb.isNotBlank() && orderSb.isNotEmpty() && orderSb.contains(item.name, true)) {

            zutatenPrice -= item.price
            rebuildOrderString(orderSb, item)
            viewModel.setOrderStringBuilder(removeLeadingSpaces(orderSb))

            if (orderSb.isBlank()) {
                isFirstItem = true
                Toast.makeText(requireContext(), getString(R.string.zutaten_erase_order_Text), Toast.LENGTH_LONG).show()
            }

        } else if (!orderSb.contains(item.name))
            Toast.makeText(requireContext(), getString(R.string.element_not_in_order_text, item.name), Toast.LENGTH_LONG).show()
    }

    private fun rebuildOrderString(orderSb: StringBuilder, item: Item) {
        if (orderSb.contains(", ${item.name}", true))
            orderSb.replace(orderSb.indexOf(", ${item.name}"), orderSb.indexOf(", ${item.name}") + item.name.length + 2, "")
        else if (orderSb.contains("${item.name},", true))
            orderSb.replace(orderSb.indexOf("${item.name},"), orderSb.indexOf("${item.name},") + item.name.length + 1, "")
        else
            orderSb.replace(orderSb.indexOf(item.name), orderSb.indexOf(item.name) + item.name.length, "")

    }

}