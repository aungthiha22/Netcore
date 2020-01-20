package com.systematic.netcore.fragment

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.systematic.netcore.R
import com.systematic.netcore.activity.SaleOrderDetailsActivity
import com.systematic.netcore.adapter.SaleOrderListAdapter
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.database.model.SaleOrder
import com.systematic.netcore.objects.SaleOrderObject
import com.systematic.netcore.utility.Common
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import com.systematic.netcore.utility.WebServiceKey
import kotlinx.android.synthetic.main.fragment_sale_order.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapPrimitive
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE

class SaleOrderFragment : Fragment() {
    var TAG = "SaleOrderFragment"

    var signKey = ""
    var webServiceUrl = ""
    var latestModifiedDateForUser = ""

    var asmxName = Constants.asmx_name
    var customerID = ""

    private var saleOrderListForRV = ArrayList<SaleOrderObject>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        webServiceUrl = SettingPreference.getDataFromSharedPref(Constants.key_web_service_url, activity!!).toString()
        signKey = Common.getSHAKey(context!!)

        customerID = SettingPreference.getDataFromSharedPref(Constants.keyCustomerID,activity!!).toString()
        Log.d("SaleOrderFragment", "customerID $customerID")

        return inflater.inflate(R.layout.fragment_sale_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (customerID != ""){
            getDataFromDb()
        }else{
            getSaleOrder()
        }
        //getDataFromDb()
    }

    private fun setupRecyclerView(saleOrderList: ArrayList<SaleOrderObject>) {
        rvSaleOrder.layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
        rvSaleOrder.adapter = SaleOrderListAdapter(activity!!,context!!, saleOrderList) {
            val intent = Intent(this.context, SaleOrderDetailsActivity::class.java)
            intent.putExtra(Constants.keySaleOrderID, saleOrderListForRV[it].saleOrderID)
            intent.putExtra(Constants.keyCustomerName, saleOrderListForRV[it].customerName)
            startActivity(intent)
            activity!!.overridePendingTransition(0, 0)
            activity!!.finish()
        }
        rvSaleOrder.adapter?.notifyDataSetChanged()
    }

    fun getDataFromDb() {
        saleOrderListForRV.clear()
        doAsync {
            val database = AppDatabase.getInstance(context = activity!!)
            //val saleOrderList = database.getSaleOrderDAO().getSaleOrder()
            val saleOrderList = database.getSaleOrderDAO().getSaleOrderByCustomerID(customerID)
            Log.d("SaleOrderFragment", "saleOrderListForRV ${saleOrderListForRV.size}")
            for (i in 0 until saleOrderList.size) {
                val saleOrderObject = SaleOrderObject()
                saleOrderObject.saleOrderID = saleOrderList[i].SaleOrderID
                saleOrderObject.productID = saleOrderList[i].ProductID
                saleOrderObject.customerID = saleOrderList[i].CustomerID

                val customerList = database.getCustomerDAO().getCustomerById(saleOrderList[i].CustomerID)
                for (i in 0 until customerList.size) {
                    saleOrderObject.customerName = customerList[i].CustomerNameEng
                }

                val productList = database.getProductDAO().getProductById(saleOrderList[i].ProductID)
                Log.d("SaleOrderFragment", "productList ${productList.size}")
                for (i in 0 until productList.size) {
                    saleOrderObject.productName = productList[i].ProductName
                }

                saleOrderObject.totalAmount = saleOrderList[i].TotalAmount
                saleOrderObject.orderNo = saleOrderList[i].OrderNo
                saleOrderListForRV.add(saleOrderObject)
            }

            uiThread {
                setupRecyclerView(saleOrderListForRV)
            }

            Log.d("SaleOrderFragment", "saleOrderListForRV ${saleOrderListForRV.size}")
        }
    }

    fun getSaleOrder() {
        if (Common.isConnected(context!!)) {
            CallGetSaleOrder().execute()
        } else {
            Common.showPoorConnectionToast(context!!)
        }
    }

    inner class CallGetSaleOrder : AsyncTask<Void, Void, String>() {
        val loading = activity!!.indeterminateProgressDialog("Loading...")
        var webServiceNamespace = activity!!.resources.getString(R.string.WebServiceNameSpace)

        var response_info_code = ""
        var response_info_message = ""

        override fun onPreExecute() {
            loading.setCancelable(false)
            loading.show()
        }

        override fun doInBackground(vararg params: Void?): String {
            try {

                saleOrderListForRV.clear()

                var methodName = "GetSaleOrder"
                var soapObject = SoapObject(webServiceNamespace, methodName)

                val database = AppDatabase.getInstance(context = activity!!)
                val modifiedDateSaleOrderList = database.getSaleOrderDAO().getLastModifiedOn()
                if (modifiedDateSaleOrderList.isNotEmpty()) {
                    latestModifiedDateForUser = modifiedDateSaleOrderList[0].ModifiedOn
                }

                soapObject.addProperty(WebServiceKey.KeyForRequestData.key_modified_date, latestModifiedDateForUser)
                soapObject.addProperty(WebServiceKey.KeyForRequestData.key_user_sign, signKey)

                var soapSerializationEnvelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
                soapSerializationEnvelope.dotNet = true
                soapSerializationEnvelope.setOutputSoapObject(soapObject)

                var httpTransportSE = HttpTransportSE(webServiceUrl + asmxName)
                httpTransportSE.debug = true
                httpTransportSE.call(
                    activity!!.resources.getString(R.string.WebServiceSOAPActionURL) + methodName,
                    soapSerializationEnvelope
                )

                var webServiceResult = soapSerializationEnvelope.response as SoapPrimitive
                Log.i(TAG, "resultForGetSaleOrder= ${webServiceResult}")

                var jsonString = webServiceResult.toString()
                Log.i(TAG, "jsonStringForSaleOrder = ${jsonString}")

                var jsonObject = JSONObject(jsonString)

                var responseData = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_data)
                var responseInfo = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_info)

                Log.i(TAG, "responseDataForSaleOrder : $responseData")
                val saleOrderList = Gson().fromJson(responseData, Array<SaleOrder>::class.java)
                Log.i(TAG, "saleOrderList : ${saleOrderList.size}")

                if (saleOrderList.isNotEmpty()) {

                    val getSaleOrderListFromDb = database.getSaleOrderDAO().getSaleOrder()

                    //database.getReceiptDAO().delete()

                    saleOrderList.forEach {

                        for (i in 0 until getSaleOrderListFromDb.size) {
                            if (it.SaleOrderID == getSaleOrderListFromDb[i].ProductID) {
                                Log.i(TAG, "SaleOrderID : ${it.SaleOrderID}")
                                database.getSaleOrderDAO().deleteById(it.SaleOrderID)
                                break
                            }
                        }

                        database.getSaleOrderDAO().insert(it)
                    }
                }

                if (responseInfo != "") {
                    var jsonObject = JSONObject(responseInfo)
                    response_info_code = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_info_code)
                    response_info_message = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_info_message)
                }

            } catch (e: Exception) {
                Log.e("exception occurs", e.message)
                response_info_message = e.message.toString()
            }
            return response_info_message
        }

        override fun onPostExecute(result: String?) {
            loading.dismiss()

            //if return str is success to login
            if (response_info_code == "0") {

                doAsync {
                    val database = AppDatabase.getInstance(context = activity!!)
                    val saleOrderList = database.getSaleOrderDAO().getSaleOrder()
                    for (i in 0 until saleOrderList.size){
                        val saleOrderObject = SaleOrderObject()
                        saleOrderObject.saleOrderID = saleOrderList[i].SaleOrderID
                        saleOrderObject.productID = saleOrderList[i].ProductID
                        saleOrderObject.customerID = saleOrderList[i].CustomerID

                        val customerList = database.getCustomerDAO().getCustomerById(saleOrderList[i].CustomerID)
                        for (i in 0 until customerList.size) {
                            saleOrderObject.customerName = customerList[i].CustomerNameEng
                        }

                        val productList = database.getProductDAO().getProductById(saleOrderList[i].ProductID)
                        for (i in 0 until productList.size) {
                            saleOrderObject.productName = productList[i].ProductName
                        }

                        saleOrderObject.totalAmount = saleOrderList[i].TotalAmount
                        saleOrderObject.orderNo = saleOrderList[i].OrderNo

                        saleOrderListForRV.add(saleOrderObject)
                    }

                    uiThread {
                        setupRecyclerView(saleOrderListForRV)
                    }
                }

            } else {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
            }
        }

    }
}