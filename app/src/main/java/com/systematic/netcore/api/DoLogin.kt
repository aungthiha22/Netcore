package com.systematic.netcore.api

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.telecom.Call
import android.util.Log
import android.widget.Toast
import com.systematic.netcore.R
import com.systematic.netcore.activity.MainActivity
import com.systematic.netcore.database.AppDatabase
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

class DoLogin {
    var TAG = "DoLogin"

    var activity: Activity
    var context: Context
    var userCode = ""
    var password = ""
    var signKey = ""
    var userID = ""
    var userName = ""
    var orgID = ""

    var webServiceUrl = ""
    var latestModifiedDateForUser = ""

    var asmxName = Constants.asmx_name

    constructor(activity: Activity, context: Context, userCode: String, password: String) {
        this.activity = activity
        this.context = context
        this.userCode = userCode
        this.password = password

        signKey = Common.getSHAKey(context)

        webServiceUrl = SettingPreference.getDataFromSharedPref(Constants.key_web_service_url, activity).toString()
        Log.d(TAG, "webServiceUrl $webServiceUrl")
    }

    fun login() {
        if (Common.isConnected(context)) {
            CallLogin().execute()
        } else {
            Common.showPoorConnectionToast(context)
        }
    }

    inner class CallLogin : AsyncTask<Void, Void, String>() {
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

                /*var methodName = "DoLogin"
                var soapObject = SoapObject(webServiceNamespace, methodName)*/
                var methodName = "DoLogin"
                var soapObject = SoapObject(webServiceNamespace,methodName)

                val database = AppDatabase.getInstance(context = activity!!)
//                val modifiedDateUserList = database.getUserDAO().getLastModifiedOn()
//                if (modifiedDateUserList.isNotEmpty()) {
//                    latestModifiedDateForUser = modifiedDateUserList[0].ModifiedOn
//                }

               /* soapObject.addProperty(WebServiceKey.KeyForRequestData.key_user_code, userCode)
                soapObject.addProperty(WebServiceKey.KeyForRequestData.key_user_password, password)
                //soapObject.addProperty(WebServiceKey.KeyForRequestData.key_user_modified_date, latestModifiedDateForUser)
                soapObject.addProperty(WebServiceKey.KeyForRequestData.key_user_sign, signKey)*/

                soapObject.addProperty(WebServiceKey.KeyForRequestData.key_user_code,userCode)
                soapObject.addProperty(WebServiceKey.KeyForRequestData.key_user_password,password)
                soapObject.addProperty(WebServiceKey.KeyForRequestData.key_user_sign,signKey)

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
                Log.i(TAG, "resultForDoLogin = ${webServiceResult}")

                var jsonString = webServiceResult.toString()
                Log.i(TAG, "jsonString = ${jsonString}")

                var jsonObject = JSONObject(jsonString)

                var responseData = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_data)
                var responseInfo = jsonObject.getString(WebServiceKey.KeyForResponse.key_response_info)

                if (responseData != "") {

                    val userList = database.getUserDAO().getUser()

                    var jsonObject = JSONObject(responseData)

                    userID = jsonObject.getString(WebServiceKey.KeyForUserResponse.key_user_id)
                    userName = jsonObject.getString(WebServiceKey.KeyForUserResponse.key_user_name)
                    Log.i("userNameFromApi", userName)
//                    if (userCode == jsonObject.getString("UserCode")) {
//                        Log.i("UserCode", jsonObject.getString("UserCode"))
                    SettingPreference.putDataToSharefPref(Constants.keyUserId, userID, activity)
                    SettingPreference.putDataToSharefPref(Constants.keyUserName, userName, activity)

//                    }
                    //val userDeviceID = jsonObject.getString(WebServiceKey.KeyForUserResponse.key_user_deviceID)
                    orgID = jsonObject.getString(WebServiceKey.KeyForUserResponse.key_org_id)
                    val orgCode = jsonObject.getString(WebServiceKey.KeyForUserResponse.key_org_code)

                    val userLogin = User()
                    userLogin.UserID = userID
                    userLogin.UserCode = userCode
                    userLogin.UserName = userName
                    userLogin.Password = password
                    userLogin.OrgID = orgID
                    userLogin.OrgCode = orgCode

                    for (j in 0 until userList.size) {
                        if (userID == userList[j].UserID)
                            database.getUserDAO().deleteById(userID)
                        break
                    }

                    database.getUserDAO().insert(userLogin)
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
                //SettingPreference.putDataToSharefPref(Constants.keyLastReceiptCount, "RPA000001", activity)
                SettingPreference.putDataToSharefPref(Constants.key_login_state, "1", activity)
                val city = GetCity(activity!!, context!!)
                city.getCity()

            } else {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
            }
        }
    }
}