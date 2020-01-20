package com.systematic.netcore.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.systematic.netcore.R
import com.systematic.netcore.activity.CustomerDetailsActivity
import com.systematic.netcore.adapter.CustomerListAdapter
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.database.model.Customer
import com.systematic.netcore.objects.CustomerObject
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import kotlinx.android.synthetic.main.fragment_customer_list.*
import org.jetbrains.anko.doAsync

class CustomerListFragment : Fragment(),View.OnClickListener {

    var TAG = "CustomerListFragment"
    private val customerList : ArrayList<CustomerObject> = ArrayList()

    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_customer_list, container, false)
    }

    override
    fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getDataFromDb()

        setUpRecyclerViewTest(customerList)

        ivSearchCustomer.setOnClickListener(this)

        etCustomerName.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (etCustomerName.length() == 0){
                    getDataFromDb()
                    setUpRecyclerViewTest(customerList)
                }
            }

        })
    }

    override
    fun onClick(view: View?) {
       when (view){
           ivSearchCustomer -> {
               val strCusNoOrCode = etCustomerName.text.toString()
               if (TextUtils.isEmpty(strCusNoOrCode)){
                   etCustomerName.error = "Please Enter Customer Name Or Customer Number"
               }else{
                   getDataByCustomerNoFromDb(strCusNoOrCode)
                   setUpRecyclerViewTest(customerList)

               }
           }
       }
    }


    /*private fun setUpRecyclerView(customerList : ArrayList<CustomerObject>){
        rvCustomerList.layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
        rvCustomerList.adapter = CustomerListAdapter(activity!!,context!!,customerList){

            SettingPreference.putDataToSharefPref(Constants.keyCustomerID,customerList[it].id,activity!!)
            Log.d (TAG,"customerID ${customerList[it].id}")
            val intent = Intent (activity!!,CustomerDetailsActivity::class.java)
            activity!!.startActivity(intent)
            activity!!.finish()
        }
        rvCustomerList.adapter?.notifyDataSetChanged()
    }*/
    private fun  setUpRecyclerViewTest(list: ArrayList<CustomerObject>){
        rvCustomerList.layoutManager = LinearLayoutManager(context,RecyclerView.VERTICAL,false)
        rvCustomerList.adapter = CustomerListAdapter(activity!!,context!!,list){
            SettingPreference.putDataToSharefPref(Constants.keyCustomerID,list[it].id,activity!!)

            val intent = Intent(activity,CustomerDetailsActivity::class.java)
            activity!!.startActivity(intent)
            activity!!.finish()
        }
        rvCustomerList.adapter?.notifyDataSetChanged()
    }

    private fun getDataFromDb(){
        customerList.clear()
        doAsync {
            val database = AppDatabase.getInstance(context = activity!!)
            val customers = database.getCustomerDAO().getCustomer()
            for (i in 0 until customers.size){
                val customer = CustomerObject()
                customer.id = customers[i].CustomerID
                customer.name = customers[i].CustomerNameEng
                customer.address = customers[i].Address
                customer.phoneNo = customers[i].PhoneNo
                customerList.add(customer)
            }
            Log.d(TAG ,"Customers list ${customers.size}")
            Log.d(TAG ,"Table data list ${customerList.size}")

        }
    }

    private fun getDataByCustomerNoFromDb(codeOrName : String){
        customerList.clear()
        doAsync {
            val database = AppDatabase.getInstance(context = activity!!)
            val customers = database.getCustomerDAO().getCusByCusNoAndName(codeOrName)
            for (i in 0 until customers.size){
                val customer = CustomerObject()
                customer.id = customers[i].CustomerID
                customer.name = customers[i].CustomerNameEng
                customer.address = customers[i].Address
                customer.phoneNo = customers[i].PhoneNo
                customerList.add(customer)
            }
            Log.d(TAG ,"Customers name list ${customers.size}")
            Log.d(TAG ,"Table data name list ${customerList.size}")
        }
    }
}