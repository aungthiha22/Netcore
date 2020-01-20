package com.systematic.netcore.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.systematic.netcore.R
import com.systematic.netcore.activity.MainActivity
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.objects.UploadInstallationInfoObj
import com.systematic.netcore.utility.Common
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import com.systematic.netcore.utility.WebServiceKey
import org.jetbrains.anko.indeterminateProgressDialog
import org.json.JSONObject
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapPrimitive
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE


class UploadInstallationInfo {
    val TAG = "Upload"

    var activity: Activity? = null
    var context: Context? = null
    var signKey=""
    var userId = ""

    var webServiceUrl = ""
    var webServiceNamespace = ""

    var webServiceMessage = ""

    var asmxName = "asmx_name"

    constructor(activity: Activity, context: Context) {
        this.activity = activity
        this.context = context

        signKey = Common.getSHAKey(context)

        webServiceUrl = SettingPreference.getDataFromSharedPref(Constants.key_web_service_url, activity).toString()
        userId = SettingPreference.getDataFromSharedPref(Constants.keyUserId, activity).toString()
    }

    fun uploadInstallationInfo() {
        if (Common.isConnected(context!!)) {
            GetInstallationInfo().execute()
        } else {
            Common.showPoorConnectionToast(context!!)
        }
    }


    inner class GetInstallationInfo : AsyncTask<Void, Void, String>() {
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

                var methodName = "SaveInstallaionInfos"
                var soapObject = SoapObject(webServiceNamespace, methodName)

                val database = AppDatabase.getInstance(context = activity!!)
                val InstallationList = database.getInstallationInfoDAO().getInstallationInfo()
                Log.i(TAG, "InstallationInfoListForUpload : ${InstallationList.size}")

                val uploadInstallationInfoObj = UploadInstallationInfoObj()
                uploadInstallationInfoObj.InstallationInfoList = InstallationList

                val installationInfoJsonString = Gson().toJson(uploadInstallationInfoObj)
                Log.i(TAG, "InstallationInfoJsonString : $installationInfoJsonString")

                soapObject.addProperty("info",installationInfoJsonString)
                soapObject.addProperty("sign",signKey)

                var soapSerializationEnvelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
                soapSerializationEnvelope.dotNet = true
                soapSerializationEnvelope.setOutputSoapObject(soapObject)

                var httpTransportSE = HttpTransportSE(webServiceUrl + asmxName)
                httpTransportSE.debug = true
                httpTransportSE.call(activity!!.resources.getString(R.string.WebServiceSOAPActionURL) + methodName,
                    soapSerializationEnvelope)

                var webServiceResult = soapSerializationEnvelope.response as SoapPrimitive
                Log.i(TAG,"resultForInstallationInfo = ${webServiceResult}")

                var jsonString = webServiceResult.toString()
                Log.i(TAG, "jsonStringForInstallationInfo = ${jsonString}")

                var jsonObject = JSONObject(jsonString)

                var responseData = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_data)
                var responseInfo = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_info)

                Log.d(TAG , "Response Data _____ : ${responseData}")
                if (!responseInfo.equals("")) {
                    var jsonObject = JSONObject(responseInfo)
                    response_info_code = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_info_code)
                    response_info_message = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_info_message)
                }

            } catch (e: Exception) {
               // Log.e("exception occurs", e.message)
                response_info_message = e.message.toString()
            }
            return response_info_message
        }

        override fun onPostExecute(result: String?) {
            loading.dismiss()

            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra(Constants.key_go_to, Constants.key_installation)
            activity!!.startActivity(intent)
            activity!!.finish()

            Toast.makeText(context,result, Toast.LENGTH_SHORT).show()
        }

    }
}