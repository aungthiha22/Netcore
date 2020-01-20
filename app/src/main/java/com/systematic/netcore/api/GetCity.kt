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

class GetCity {
    var TAG = "GetCity"

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

    fun getCity() {
        if (Common.isConnected(context)) {
            CallGetCity().execute()
        } else {
            Common.showPoorConnectionToast(context)
        }
    }

    inner class CallGetCity : AsyncTask<Void, Void, String>() {
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

                var methodName = "GetCity"
                var soapObject = SoapObject(webServiceNamespace, methodName)

                val database = AppDatabase.getInstance(context = activity!!)
                val modifiedDateCityList = database.getCityDAO().getLastModifiedOn()
                if (modifiedDateCityList.isNotEmpty()) {
                    latestModifiedDateForUser = modifiedDateCityList[0].ModifiedOn
                }

                soapObject.addProperty(WebServiceKey.KeyForRequestData.key_user_modified_date, latestModifiedDateForUser)
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
                Log.i(TAG, "resultForGetCity= ${webServiceResult}")

                var jsonString = webServiceResult.toString()
                Log.i(TAG, "jsonStringForCity = ${jsonString}")

                var jsonObject = JSONObject(jsonString)

                var responseData = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_data)
                var responseInfo = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_info)

                val cityList = Gson().fromJson(responseData, Array<City>::class.java)
                Log.i(TAG, "cityList : ${cityList.size}")

                if (cityList.isNotEmpty()) {

                    val getCityListFromDb = database.getCityDAO().getCity()

                    cityList.forEach {
                        Log.i(TAG, "name : ${it.CityNameZawgyi}")

                        for (i in 0 until getCityListFromDb.size){
                            if (it.CityID == getCityListFromDb[i].CityID){
                                Log.i(TAG, "ReceiptID : ${it.CityID}")
                                database.getCityDAO().deleteById(it.CityID)
                                break
                            }
                        }

                        database.getCityDAO().insert(it)
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

                val township = GetTownship(activity!!, context!!)
                township.getTownship()

            } else {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
            }
        }

    }

}