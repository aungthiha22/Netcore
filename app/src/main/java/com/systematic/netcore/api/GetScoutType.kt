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
import com.systematic.netcore.database.model.City
import com.systematic.netcore.database.model.ScoutType
import com.systematic.netcore.database.model.User
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

class GetScoutType {
    var TAG = "GetScoutType"

    var activity: Activity
    var context: Context
    var signKey = ""

    var userID = ""

    var webServiceUrl = ""
    var latestModifiedDateForUser = ""

    var asmxName = Constants.asmx_name

    constructor(activity: Activity, context: Context) {
        this.activity = activity
        this.context = context

        signKey = Common.getSHAKey(context)

        webServiceUrl = SettingPreference.getDataFromSharedPref(Constants.key_web_service_url, activity).toString()
        userID = SettingPreference.getDataFromSharedPref(Constants.keyUserId, activity).toString()
        Log.d(TAG, "webServiceUrl $webServiceUrl")
        Log.d(TAG, "userID $userID")
    }

    fun getScoutType() {
        if (Common.isConnected(context)) {
            CallGetScoutType().execute()
        } else {
            Common.showPoorConnectionToast(context)
        }
    }

    inner class CallGetScoutType : AsyncTask<Void, Void, String>() {
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

                var methodName = "GetScoutType"
                var soapObject = SoapObject(webServiceNamespace, methodName)

                val database = AppDatabase.getInstance(context = activity!!)
                val modifiedDateCityList = database.getScoutTypeDAO().getLastModifiedOn()
                if (modifiedDateCityList.isNotEmpty()) {
                    latestModifiedDateForUser = modifiedDateCityList[0].ModifiedOn
                }

                soapObject.addProperty("modifiedDate", latestModifiedDateForUser)
                soapObject.addProperty(WebServiceKey.KeyForRequestData.key_user_id, "")
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
                Log.i(TAG, "resultForScoutType= ${webServiceResult}")

                var jsonString = webServiceResult.toString()
                Log.i(TAG, "jsonStringForScoutType = ${jsonString}")

                var jsonObject = JSONObject(jsonString)

                var responseData = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_data)
                var responseInfo = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_info)

                Log.i(TAG, "responseDataForScoutType : $responseData")

                val scoutTypeList = Gson().fromJson(responseData, Array<ScoutType>::class.java)
                Log.i(TAG, "scoutTypeList : ${scoutTypeList.size}")

                if (scoutTypeList.isNotEmpty()) {

                    val getScoutTypeListFromDb = database.getScoutTypeDAO().getScoutType()

                    //database.getReceiptDAO().delete()

                    scoutTypeList.forEach {
                        Log.i(TAG, "name : ${it.ScoutTypeNameEng}")

                        for (i in 0 until getScoutTypeListFromDb.size){
                            if (it.ScoutTypeID == getScoutTypeListFromDb[i].ScoutTypeID){
                                Log.i(TAG, "ReceiptID : ${it.ScoutTypeID}")
                                database.getScoutTypeDAO().deleteById(it.ScoutTypeID)
                                break
                            }
                        }

                        database.getScoutTypeDAO().insert(it)
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

                val customerGroup = GetCustomerGroup(activity!!, context!!)
                customerGroup.getCustomerGroup()

            } else {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun downloadDataFromWebService() {
//        val customers = GetCustomer(activity!!,context!!)
//        customers.getCustomer()
    }

}