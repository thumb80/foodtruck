package com.bor.foodtruck.model

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.bor.foodtruck.R
import com.bor.foodtruck.databinding.MenuListBinding
import com.bor.foodtruck.ext.setSafeOnClickListener

class MenuListAdapter(val items: List<Item>, val drawable: Drawable, val isZutaten: Boolean, val listener: (Item) -> Unit, val removeCallback: (Item, Int) -> Unit): RecyclerView.Adapter<MenuListAdapter.MenuListViewHolder>() {

    private lateinit var binding: MenuListBinding

    inner class MenuListViewHolder(
        val binding: MenuListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {

            val addToOrder = binding.root.findViewById<Button>(R.id.add_to_order)

            binding.menuListImage.setBackgroundDrawable(drawable)
            binding.menuListItem.text = item.name
            addToOrder.text = String.format("%.2f", item.price)

            if (isZutaten) {
                binding.removeZutaten.visibility = View.VISIBLE
                binding.removeZutaten.setSafeOnClickListener {
                    removeCallback.invoke(item, adapterPosition)
                }
            } else {
                binding.removeZutaten.visibility = View.GONE
            }

            binding.addToOrder.setSafeOnClickListener {
                listener.invoke(item)
            }

            /*binding.menuListItem.setSafeOnClickListener {
                listener.invoke(item)
            }*/

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuListViewHolder {
        binding = MenuListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuListViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MenuListViewHolder, position: Int) {
        holder.bind(items[position])
    }

}