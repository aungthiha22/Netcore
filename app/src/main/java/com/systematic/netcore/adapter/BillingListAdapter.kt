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

class BillingListAdapter (val activity : Activity, val context : Context, val billingList : ArrayList<SaleOrderObject>, val listener: (Int)-> Unit): RecyclerView.Adapter<BillingListAdapter.BillingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingViewHolder {
        return BillingViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_sale_order,parent,false))
    }

    override fun getItemCount(): Int {
      return billingList.size
    }

    override fun onBindViewHolder(holder: BillingViewHolder, position: Int) {
        holder.showData(activity,context,billingList[position], listener, position)
    }

    class BillingViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        fun showData(activity : Activity,context : Context,item: SaleOrderObject, listener: (Int)->Unit, position: Int){

            itemView.tvCustomerName.text = item.customerName
            itemView.tvProduct.text = item.orderNo
            itemView.tvAmount.text = item.totalAmount


            itemView.llCustomerList.setOnClickListener {
                listener(position)
            }
        }
    }
}