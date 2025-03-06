package com.bor.foodtruck.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bor.foodtruck.databinding.HistoricalOrderListBinding
import com.google.gson.JsonArray
import com.google.gson.JsonElement

class HistoricalOrderListAdapter(val orders: JsonArray): RecyclerView.Adapter<HistoricalOrderListAdapter.HistoricalOrderListViewHolder>() {

    private lateinit var binding: HistoricalOrderListBinding

    inner class HistoricalOrderListViewHolder(
        val binding: HistoricalOrderListBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(order: JsonElement) {

            if (order.asJsonObject.get("orderOwner") != null)
                binding.orderListOwner.text = order.asJsonObject.get("orderOwner").asString
            else if (order.asJsonObject.get("orderNumber") != null)
                binding.orderListOwner.text = order.asJsonObject.get("orderNumber").asString
            binding.orderListItem.text = order.asJsonObject.get("orderText").asString
            binding.orderPrice.text = if (order.asJsonObject.get("orderPrice").asString.length == 4)
                order.asJsonObject.get("orderPrice").asString.plus("0")
            else
                order.asJsonObject.get("orderPrice").asString

        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoricalOrderListViewHolder {
        binding = HistoricalOrderListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoricalOrderListViewHolder(binding)
    }

    override fun getItemCount(): Int = orders.size()

    override fun onBindViewHolder(holder: HistoricalOrderListViewHolder, position: Int) {
        holder.bind(orders.get(position))
    }

}