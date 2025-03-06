package com.bor.foodtruck.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bor.foodtruck.FoodTruckApplication
import com.bor.foodtruck.databinding.EditListLayoutBinding
import com.bor.foodtruck.ext.ItemEditDialog
import com.bor.foodtruck.ext.OrderEditQtyDialog
import com.bor.foodtruck.viewmodel.SharedViewModel
import org.koin.java.KoinJavaComponent

private val viewModel: SharedViewModel by KoinJavaComponent.inject(SharedViewModel::class.java)

class EditListAdapter(
    var orderList: ArrayList<Triple<Int,String,Double>>?,
    var items: List<Item>?,
    val isEdit: Boolean,
    val childFragmentManager: FragmentManager?,
    val listener: (orderItem: Triple<Int,String,Double>?, item: Item?, position: Int) -> Unit,
    val fileName: String?
): RecyclerView.Adapter<EditListAdapter.EditListViewHolder>() {

    private lateinit var binding: EditListLayoutBinding
    private lateinit var instance: EditListAdapter

    inner class EditListViewHolder(
        val binding: EditListLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            orderItem: Triple<Int,String,Double>?,
            item: Item?,
            position: Int
        ) {
            if (isEdit) {
                val itemName = if (item?.name?.contains("\n") == true)
                    item.name.split("\n")[0]
                else
                    item?.name
                binding.orderText.text = itemName
                binding.editItem.setOnClickListener {
                    val dialog = fileName?.let { file ->
                        item?.let {
                            ItemEditDialog(
                                it,
                                position,
                                fileName,
                                this@EditListAdapter,
                                false
                            )
                        }
                    }
                    if (childFragmentManager != null) {
                        dialog?.show(childFragmentManager, null)
                    }
                }
            }
            else {
                if (orderItem?.second?.contains("\n") == true) {
                    val orderText = orderItem.second.split("\n")
                    val sb = StringBuilder()
                    orderText.forEach {
                        sb.append(it.plus(" "))
                    }
                    binding.orderText.text = "${orderItem.first}".plus("   ").plus(sb.toString())
                } else
                    binding.orderText.text = "${orderItem?.first}".plus("   ").plus(orderItem?.second)
                binding.editItem.setOnClickListener {
                    val dialog = orderItem?.let { order ->
                        OrderEditQtyDialog(
                            order,
                            instance,
                            position)
                    }
                    if (childFragmentManager != null) {
                        dialog?.show(childFragmentManager,null)
                    }
                }
            }

            binding.removeItem.setOnClickListener {
                if (isEdit) {
                    (items as ArrayList<Item>).remove(item)
                    listener.invoke(null, item, position)
                }
                else {
                    (orderList as ArrayList<Triple<Int, String, Double>>).remove(orderItem)
                    listener.invoke(orderItem, null, position)
                }
                notifyItemRemoved(position)

            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditListViewHolder {
        binding = EditListLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        instance = this
        return EditListViewHolder(binding)
    }

    override fun getItemCount(): Int = if (isEdit) items?.size ?: 0 else orderList?.size ?: 0

    override fun onBindViewHolder(holder: EditListViewHolder, position: Int) {
        holder.bind(orderList?.get(position), items?.get(position),position)
    }

    fun removeItem(orderItem: Triple<Int, String, Double>?, item: Item?, position: Int) {
        if (!isEdit) {
            (orderList as ArrayList<Triple<Int, String, Double>>).remove(orderItem)
        }
        else {
            (items as ArrayList<Item>).remove(item)
            if (fileName != null) {
                viewModel.deleteItem(FoodTruckApplication.instance, fileName, item, this)
            }
        }
        notifyItemRemoved(position)
    }

    fun getLastIndex(): Int {
        return items?.lastIndex ?: 0
    }

}