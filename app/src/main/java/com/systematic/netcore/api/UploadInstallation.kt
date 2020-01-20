package com.systematic.netcore.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.gson.Gson
import com.systematic.netcore.R
import com.systematic.netcore.activity.InstallationInfoActivity
import com.systematic.netcore.activity.TestingActivity
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.objects.UploadInstallationObj
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

class UploadInstallation {
    var TAG = "SaveInstallaions"

    var activity: Activity? = null
    var context: Context? = null
    var installationNo = ""
    var installationId = ""
    var customerId = ""
    var signKey=""
    var userId = ""
    var webServiceUrl = ""
    var webServiceNamespace = ""
    var webServiceMessage = ""
    var asmxName = Constants.asmx_name



    constructor(activity: Activity, context: Context, installNo: String, instllId: String, customerId: String) {
        this.activity = activity
        this.context = context
        this.installationNo = installNo
        this.installationId = instllId
        this.customerId = customerId

        signKey = Common.getSHAKey(context)

        webServiceUrl = SettingPreference.getDataFromSharedPref(Constants.key_web_service_url, activity).toString()
        userId = SettingPreference.getDataFromSharedPref(Constants.keyUserId, activity).toString()
    }

    fun uploadInstallation() {
        if (Common.isConnected(context!!)) {
            GetInstallation().execute()
        } else {
            Common.showPoorConnectionToast(context!!)
        }
    }


    inner class GetInstallation : AsyncTask<Void, Void, String>() {
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

                var methodName = "SaveInstallaions"
                var soapObject = SoapObject(webServiceNamespace, methodName)

                val database = AppDatabase.getInstance(context = activity!!)
                val InstallationList = database.getInstallationDAO().getInstallation()
                Log.i(TAG, "InstallationListForUpload : ${InstallationList.size}")

                val uploadInstallationObj = UploadInstallationObj()
                uploadInstallationObj.InstallationList = InstallationList

                val installationJsonString = Gson().toJson(uploadInstallationObj)
                Log.i(TAG, "InstallationJsonString : $installationJsonString")

               // soapObject.addProperty("info",installationJsonString)
               // soapObject.addProperty("sign",signKey)

                var soapSerializationEnvelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
                soapSerializationEnvelope.dotNet = true
                soapSerializationEnvelope.setOutputSoapObject(soapObject)

                var httpTransportSE = HttpTransportSE(webServiceUrl + asmxName)
                httpTransportSE.debug = true
                httpTransportSE.call(activity!!.resources.getString(R.string.WebServiceSOAPActionURL) + methodName,
                    soapSerializationEnvelope)

                var webServiceResult = soapSerializationEnvelope.response as SoapPrimitive
                Log.i(TAG,"resultForInstallation = ${webServiceResult}")

                var jsonString = webServiceResult.toString()
                Log.i(TAG, "jsonStringForInstallation = ${jsonString}")

                var jsonObject = JSONObject(jsonString)

                var responseData = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_data)
                var responseInfo = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_info)

                if (!responseInfo.equals("")) {
                    var jsonObject = JSONObject(responseInfo)
                    response_info_code = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_info_code)
                    response_info_message = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_info_message)

                }

//                if (response_info_code == "0"){
//                    val updateReceiptStatus = SimpleSQLiteQuery("update Receipt set SaveForMobile = 'No' ")
//                    database.getReceiptDAO().update(updateReceiptStatus)
//
//                    Log.d(TAG,"UploadSuccess $response_info_message")
//                }

            } catch (e: Exception) {
                Log.e("exception occurs", e.message)
                response_info_message = e.message.toString()
            }
            return response_info_message
        }

        override fun onPostExecute(result: String?) {
            loading.dismiss()

            val intent = Intent(activity,InstallationInfoActivity::class.java)
            intent.putExtra(Constants.keyInstallationID,installationId)
            intent.putExtra(Constants.keyInstallationNo,installationNo)
            intent.putExtra(Constants.keyCustomerID,customerId)
            activity!!.startActivity(intent)
            activity!!.finish()

            Toast.makeText(context,result, Toast.LENGTH_SHORT).show()
        }

    }
}