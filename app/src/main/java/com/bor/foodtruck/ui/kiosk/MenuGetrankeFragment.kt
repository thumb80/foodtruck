package com.bor.foodtruck.ui.kiosk

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bor.foodtruck.R
import com.bor.foodtruck.activity.HomeActivity
import com.bor.foodtruck.databinding.FragmentMenuGetrankeBinding
import com.bor.foodtruck.ext.CustomDialog
import com.bor.foodtruck.ext.OrderQtyDialog
import com.bor.foodtruck.model.Item
import com.bor.foodtruck.model.MenuListAdapter
import com.bor.foodtruck.viewmodel.SharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MenuGetrankeFragment(val items: List<Item>, val drawable: Drawable): Fragment() {

    private lateinit var binding: FragmentMenuGetrankeBinding
    private lateinit var dialog: CustomDialog
    private lateinit var adapter: MenuListAdapter
    private val viewModel: SharedViewModel by sharedViewModel<SharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuGetrankeBinding.inflate(layoutInflater)

        adapter = MenuListAdapter(items, drawable, false, {
            val dialog = OrderQtyDialog(it)
            dialog.show(childFragmentManager, null)
        },
        { _,_ ->

        })

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
            if (activity is HomeActivity)
                (activity as HomeActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.kiosk_container, HomeFragment())
                    .commit()
        }

        viewModel.order.observe(viewLifecycleOwner) {
            val completeOrder = binding.root.findViewById<Button>(R.id.complete_order)
            if (it.size != 0) {
                completeOrder.visibility = VISIBLE
                completeOrder.setOnClickListener {
                    dialog = CustomDialog(getString(R.string.confirm_order), null, null, {
                        (activity as HomeActivity).supportFragmentManager.beginTransaction()
                            .replace(R.id.kiosk_container, OrderFragment())
                            .commit()
                        dialog.dismiss()
                    }, {
                        dialog.dismiss()
                    })
                    dialog.isCancelable = false
                    dialog.show(childFragmentManager, null)
                }
            } else {
                completeOrder.visibility = INVISIBLE
            }
        }

    }

}