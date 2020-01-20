package com.systematic.netcore.adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.systematic.netcore.R
import com.systematic.netcore.objects.CustomerObject
import com.systematic.netcore.objects.InstallationObject
import kotlinx.android.synthetic.main.item_customer_list.view.*

class CustomerListAdapter (val activity : Activity, val context : Context, val customerList : ArrayList<CustomerObject>, val listener: (Int)-> Unit): RecyclerView.Adapter<CustomerListAdapter.InstallationViewHolder>() {

    var cc: Context? = null
    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstallationViewHolder {
        return InstallationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_customer_list,parent,false))
    }

    override
    fun getItemCount(): Int {
      return customerList.size
    }

    override
    fun onBindViewHolder(holder: InstallationViewHolder, position: Int) {
        holder.showData(customerList[position],listener,position)
    }

    class InstallationViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun showData(item: CustomerObject, listener: (Int) -> Unit, position: Int){
            itemView.tvName.text = item.name
            itemView.tvAddress.text = item.address
            itemView.tvPhoneNo.text = item.phoneNo


            //Log.d("GetContext",item.address)
            Log.d("GetContext",item.phoneNo)

            itemView.llCustomerList.setOnClickListener{
                listener(position)

                Log.d(" name ---,",item.name)

            }
        }
    }
}