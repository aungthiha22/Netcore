package com.systematic.netcore.api

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.telecom.Call
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.systematic.netcore.R
import com.systematic.netcore.activity.MainActivity
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.database.model.*
import com.systematic.netcore.utility.Common
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import com.systematic.netcore.utility.WebServiceKey
import org.jetbrains.anko.indeterminateProgressDialog
import org.json.JSONArray
import org.json.JSONObject
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapPrimitive
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE

class GetProductGroup {
    var TAG = "GetProductGroup"

    var activity: Activity
    var context: Context
    var signKey = ""

    var cityID = ""

    var webServiceUrl = ""
    var latestModifiedDateForUser = ""

    var asmxName = Constants.asmx_name

    constructor(activity: Activity, context: Context) {
        this.activity = activity
        this.context = context

        signKey = Common.getSHAKey(context)

        webServiceUrl = SettingPreference.getDataFromSharedPref(Constants.key_web_service_url, activity).toString()
        Log.d(TAG, "webServiceUrl $webServiceUrl")
    }

    fun getProductGroup() {
        if (Common.isConnected(context)) {
            CallGetProductGroup().execute()
        } else {
            Common.showPoorConnectionToast(context)
        }
    }

    inner class CallGetProductGroup : AsyncTask<Void, Void, String>() {
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

                var methodName = "GetProductGroup"
                var soapObject = SoapObject(webServiceNamespace, methodName)

                val database = AppDatabase.getInstance(context = activity!!)
                val modifiedDateCityList = database.getCityDAO().getLastModifiedOn()
                if (modifiedDateCityList.isNotEmpty()) {
                    latestModifiedDateForUser = modifiedDateCityList[0].ModifiedOn
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
                Log.i(TAG, "resultForGetProductGroup= ${webServiceResult}")

                var jsonString = webServiceResult.toString()
                Log.i(TAG, "jsonStringForProductGroup = ${jsonString}")

                var jsonObject = JSONObject(jsonString)

                var responseData = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_data)
                var responseInfo = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_info)

                Log.i(TAG, "responseDataForProductGroup : $responseData")
                val productGroupList = Gson().fromJson(responseData, Array<ProductGroup>::class.java)
                Log.i(TAG, "productGroupList : ${productGroupList.size}")

                if (productGroupList.isNotEmpty()) {

                    val getProductGroupListFromDb = database.getProductGroupDAO().getProductGroup()

                    //database.getReceiptDAO().delete()

                    productGroupList.forEach {

                        for (i in 0 until getProductGroupListFromDb.size){
                            if (it.ProductGroupID == getProductGroupListFromDb[i].ProductGroupID){
                                Log.i(TAG, "ProductGroupID : ${it.ProductGroupID}")
                                database.getProductGroupDAO().deleteById(it.ProductGroupID)
                                break
                            }
                        }

                        database.getProductGroupDAO().insert(it)
                        Log.i(TAG, "ProductGroupName ___ : ${it.ProductGroupName}")
                        Log.i(TAG, "ProductGroup ID ____ : ${it.ProductGroupID}")
                    }
                }

                if (!responseInfo.equals("")) {
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

                val product = GetProduct(activity!!, context!!)
                product.getProduct()

            } else {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
            }
        }

    }

}