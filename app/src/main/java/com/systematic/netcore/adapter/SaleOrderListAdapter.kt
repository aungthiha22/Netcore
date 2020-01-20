package com.systematic.netcore.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.systematic.netcore.R
import com.systematic.netcore.objects.SaleOrderObject
import kotlinx.android.synthetic.main.fragment_sale_order.view.*
import kotlinx.android.synthetic.main.fragment_sale_order.view.tvCustomerName
import kotlinx.android.synthetic.main.item_sale_order.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class SaleOrderListAdapter (val activity : Activity, val context : Context, val saleOrderList : ArrayList<SaleOrderObject>, val listener: (Int)-> Unit): RecyclerView.Adapter<SaleOrderListAdapter.InstallationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstallationViewHolder {
        return InstallationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_sale_order,parent,false))
    }

    override fun getItemCount(): Int {
      return saleOrderList.size
    }

    override fun onBindViewHolder(holder: InstallationViewHolder, position: Int) {
        holder.showData(activity,context,saleOrderList[position], listener, position)
    }

    class InstallationViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        fun showData(activity : Activity,context : Context,item: SaleOrderObject, listener: (Int)->Unit, position: Int){

            if (item.orderNo.isNotEmpty()) {
                itemView.tvOrderNo.text = item.orderNo
            }else{
                itemView.tvOrderNo.text = "-"
            }

            itemView.tvCustomerName.text = item.customerName
            itemView.tvProduct.text = item.productName
            itemView.tvAmount.text = item.totalAmount

            itemView.llCustomerList.setOnClickListener {
                listener(position)
            }
        }
    }
}