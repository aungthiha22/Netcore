package com.systematic.netcore.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import androidx.sqlite.db.SimpleSQLiteQuery
import com.afollestad.materialdialogs.MaterialDialog
import com.systematic.netcore.R
import com.systematic.netcore.activity.MainActivity
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.objects.ScoutObject
import com.systematic.netcore.utility.Common
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import com.systematic.netcore.utility.WebServiceKey
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapPrimitive
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UploadSacoutData {

    val TAG = "UploadSacoutData"
    var activity: Activity? = null
    var context: Context? = null

    var user_id = ""
    var todayDate = ""
    internal var defaultSQLDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss")
    var font: Typeface? = null
    var is_back = ""

    internal val mode = Activity.MODE_PRIVATE
    internal val MyPREFS = "MyPreference"

    var scoutIdToUpload = ""
    var scoutIdList: ArrayList<String> = ArrayList()
    var uploaded_count = 0

    constructor(activity: Activity?, context: Context?, user_id: String, font: Typeface, is_back: String) {
        this.activity = activity
        this.context = context
        this.user_id = user_id
        this.font = font
        this.is_back = is_back
    }

    fun upload() {

        var dialog = MaterialDialog.Builder(activity!!)
                .title("Upload Data")
                .content("Are you sure you want to do?")
                .onPositive { dialog, which ->
                    if (Common.isConnected(context!!)) {

                        doAsync {
                            val database = AppDatabase.getInstance(context = activity!!)
                            val sacoutList = database.getScoutDAO().getAllByUserID(user_id)
                            Log.d(TAG,"sacoutList ${sacoutList!!.size}")

                            activity!!.runOnUiThread {
                                if (sacoutList.isEmpty()) {
                                        Toast.makeText(
                                            context,
                                            activity!!.getString(R.string.no_data_to_upload),
                                            Toast.LENGTH_SHORT
                                        ).show()

                                } else {
                                    for (i in 0 until sacoutList.size) {
                                        scoutIdList.add(sacoutList[i].ScoutID)
                                    }

                                    Log.d(TAG, "scoutIdList ${scoutIdList!!.size}")

                                    CallUploadScoutData().execute()
                                }
                            }
                        }

                    } else {
                        Toast.makeText(context, activity!!.getString(R.string.poor_connection), Toast.LENGTH_SHORT).show()
                    }
                }
                .positiveText(activity!!.getString(R.string.yes))
                .negativeText(activity!!.getString(R.string.no))
                .show()

        var title = dialog.titleView
        title.setTypeface(font)

        var message = dialog.contentView
        message?.setTypeface(font)
    }

    inner class CallUploadScoutData: AsyncTask<Void, Void, String>() {
        var webServiceMessage = ""

        var webServiceUrl = ""
        var webServiceNamespace = activity!!.resources.getString(R.string.WebServiceNameSpace)

        override fun onPreExecute() {
            //loading.setCancelable(false)
            //loading.show()
            Log.i("pre", "pre")
        }

        override fun doInBackground(vararg params: Void?): String {
            Log.i("do", "do")
            try {
                var methodName = "SaveScoutInfoJson"
                var soapObject = SoapObject(webServiceNamespace, methodName)
                Log.i("scoutIdList", scoutIdList!!.size.toString())
                if (scoutIdList!!.size != 0) {
                    scoutIdToUpload = scoutIdList!![uploaded_count]
                }
                Log.i("scoutIdToUpload", scoutIdToUpload)
                soapObject.addProperty(WebServiceKey.UploadScoutData.key_info, createParamInfoJsonByScoutId(scoutIdToUpload))
                soapObject.addProperty(WebServiceKey.UploadScoutData.key_sign, Common.getSHAKey(activity!!))

                var soapSerializationEnvelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
                soapSerializationEnvelope.dotNet = true
                soapSerializationEnvelope.setOutputSoapObject(soapObject)

                try {
                    webServiceUrl = SettingPreference.getDataFromSharedPref(Constants.key_web_service_url,activity!!).toString()

                }catch (e: Exception) {
                    Log.e("exception occur", e.message)
                }

                var httpTransportSE = HttpTransportSE(webServiceUrl  + Constants.asmx_name)//2 min
                httpTransportSE.debug = true
                httpTransportSE.call(activity!!.resources.getString(R.string.WebServiceSOAPActionURL) + methodName,
                        soapSerializationEnvelope)

                var webServiceResult = soapSerializationEnvelope.response as SoapPrimitive
                Log.i("result", webServiceResult.toString())

                webServiceMessage = "success"
            } catch (e: Exception) {
                Log.e("exception occurs", e.message)
                webServiceMessage = e.message.toString()
            }
            return webServiceMessage
        }

        override fun onPostExecute(result: String?) {
            //loading.dismiss()
            if (result == "success") {
                val calendar = Calendar.getInstance()
                todayDate = defaultSQLDateFormat.format(calendar.time)
                Log.i("todayDate", todayDate)

                doAsync {
                    val database = AppDatabase.getInstance(context = activity!!)
                    if (uploaded_count < scoutIdList!!.size - 1) {
                        CallUploadScoutData().execute()
                        uploaded_count += 1
                    }
                    else {

                        val updateQuery = SimpleSQLiteQuery(
                            "update Scout set UploadedOn = '$todayDate' where CreatedBy = '$user_id'"
                        )
                        database.getScoutDAO().update(updateQuery)

                        uploaded_count = 0
                        CallUploadScoutImage().execute()
                    }
                }

            }
            else if (webServiceMessage.contains("failed to connect")) {
                Toast.makeText(activity!!.applicationContext, "Fail to connect server.", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(activity!!.applicationContext, activity!!.resources.getString(R.string.upload_fail), Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun createParamInfoJsonByScoutId(scoutId: String): String {
        var infoString = ""
        val itemList: List<ScoutObject> = ArrayList()

        doAsync {
            val database = AppDatabase.getInstance(context = activity!!)
            val scoutList = database.getScoutDAO().getAllForUploadByScoutId(user_id, scoutId)

            var jsonArray = JSONArray()
            var defaultDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.sss", Locale.US)
            for (i in 0 until scoutList.size) {
                val objectScout = scoutList[i]

                val jsonObject = JSONObject()
                jsonObject.put(WebServiceKey.UploadScoutData.key_scout_id, objectScout.ScoutID)
                jsonObject.put(WebServiceKey.UploadScoutData.key_township_id, objectScout.TownshipID)
                jsonObject.put(WebServiceKey.UploadScoutData.key_scoutType_id, objectScout.ScoutTypeID)
                jsonObject.put(WebServiceKey.UploadScoutData.key_contact_person, objectScout.ContactPerson)
                jsonObject.put(WebServiceKey.UploadScoutData.key_contact_information, objectScout.ContactInformation)
                jsonObject.put(WebServiceKey.UploadScoutData.key_contact_address, objectScout.ContactAddress)
                jsonObject.put(WebServiceKey.UploadScoutData.key_company_name, objectScout.CompanyName)
                jsonObject.put(WebServiceKey.UploadScoutData.key_scout_on, objectScout.ScoutOn)
                //jsonObject.put(KeyForWebService.UploadScoutData.key_scout_on, "")
                jsonObject.put(WebServiceKey.UploadScoutData.key_scout_no, user_id + " " + objectScout.ScoutNo)
                jsonObject.put(WebServiceKey.UploadScoutData.key_uploaded_by, objectScout.UploadedBy)
                jsonObject.put(WebServiceKey.UploadScoutData.key_uploaded_on, objectScout.UploadedOn)
                //jsonObject.put(KeyForWebService.UploadScoutData.key_uploaded_on, "")
                jsonObject.put(WebServiceKey.UploadScoutData.key_remark, objectScout.Remark)
                jsonObject.put(WebServiceKey.UploadScoutData.key_gps_lat, objectScout.GpsLat)
                jsonObject.put(WebServiceKey.UploadScoutData.key_gps_lon, objectScout.GpsLon)
                jsonObject.put(WebServiceKey.UploadScoutData.key_cell_lat, objectScout.CellLat)
                jsonObject.put(WebServiceKey.UploadScoutData.key_cell_lon, objectScout.CellLon)
                jsonObject.put(WebServiceKey.UploadScoutData.key_user_lat, objectScout.UserLat)
                jsonObject.put(WebServiceKey.UploadScoutData.key_user_lon, objectScout.UserLon)
                jsonObject.put(WebServiceKey.UploadScoutData.key_active, objectScout.Active)
                jsonObject.put(WebServiceKey.UploadScoutData.key_created_by, objectScout.CreatedBy)
                jsonObject.put(WebServiceKey.UploadScoutData.key_created_on, objectScout.CreatedOn)
                //jsonObject.put(KeyForWebService.UploadScoutData.key_created_on, "")
                jsonObject.put(WebServiceKey.UploadScoutData.key_modified_by, objectScout.ModifiedBy)
                jsonObject.put(WebServiceKey.UploadScoutData.key_modified_on, objectScout.ModifiedOn)
                //jsonObject.put(KeyForWebService.UploadScoutData.key_modified_on, "")
                jsonObject.put(WebServiceKey.UploadScoutData.key_last_action, objectScout.LastAction)

                jsonArray.put(jsonObject)
            }

            /*val jsonObjectScoutData = JSONObject()
        jsonObjectScoutData.put(KeyForWebService.UploadScoutData.key_crm_scout, jsonArray)*/

            infoString = jsonArray.toString()
        }

        Log.i("infoString", infoString)

        return infoString
    }

    inner class CallUploadScoutImage: AsyncTask<Void, Void, String>() {
        var webServiceMessage = ""
        //val loading = activity!!.indeterminateProgressDialog("Uploading Sacout Image...")
        var webServiceUrl = ""
        var webServiceNamespace = activity!!.resources.getString(R.string.WebServiceNameSpace)

        override fun onPreExecute() {
            //loading.setCancelable(false)
            //loading.show()
        }

        override fun doInBackground(vararg params: Void?): String {
            try {
                var methodName = "SaveScoutImage"
                Log.i("webSerNS", webServiceNamespace + "(check)")
                var soapObject = SoapObject(webServiceNamespace, methodName)
                if (scoutIdList!!.size != 0) {
                    scoutIdToUpload = scoutIdList!![uploaded_count]
                }
                soapObject.addProperty(WebServiceKey.UploadSacoutImage.key_info, createImageDataJsonByScoutId(scoutIdToUpload))
                Log.i("signKey", Common.getSHAKey(activity!!))
                soapObject.addProperty(WebServiceKey.UploadScoutData.key_sign, Common.getSHAKey(activity!!))

                var soapSerializationEnvelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
                soapSerializationEnvelope.dotNet = true
                soapSerializationEnvelope.setOutputSoapObject(soapObject)

                try {
                    webServiceUrl = SettingPreference.getDataFromSharedPref(Constants.key_web_service_url,activity!!).toString()
                }catch (e: Exception) {
                    Log.e("exception occur", e.message)
                }

                var httpTransportSE = HttpTransportSE(webServiceUrl  + Constants.asmx_name)//2 min
                //var httpTransportSE = HttpTransportSE("http://192.168.100.55:8080/Services/WebServiceCRM.asmx")
                httpTransportSE.debug = true
                httpTransportSE.call(activity!!.resources.getString(R.string.WebServiceSOAPActionURL) + methodName,
                        soapSerializationEnvelope)

                Log.i("soapUrl", activity!!.resources.getString(R.string.WebServiceSOAPActionURL) + "(check)")

                var webServiceResult = soapSerializationEnvelope.response as SoapPrimitive
                Log.i("result", webServiceResult.toString())

                webServiceMessage = "success"
            } catch (e: Exception) {
                Log.e("exception occurs", e.message)
                webServiceMessage = e.message.toString()
            }
            return webServiceMessage
        }

        override fun onPostExecute(result: String?) {
            //loading.dismiss()
            if (result == "success") {
                val calendar = Calendar.getInstance()
                todayDate = defaultSQLDateFormat.format(calendar.time)
                Log.i("todayDate", todayDate)

                doAsync {
                    val database = AppDatabase.getInstance(context = activity!!)

                    if (uploaded_count < scoutIdList!!.size - 1) {
                        CallUploadScoutImage().execute()
                        uploaded_count += 1
                    }
                    else {
                        uiThread {
                            Toast.makeText(activity!!.applicationContext, activity!!.resources.getString(R.string.upload_complete), Toast.LENGTH_SHORT).show()
                        }

                       database.getImageScoutDAO().changeStatusForUploadComplete()

                        Log.i("is_back", is_back)
                        if (is_back.equals("yes")) {
                            Log.i("is_back", "go_to_list")
                            val intent = Intent(activity, MainActivity::class.java)
                            intent.putExtra(Constants.key_go_to,Constants.key_new_sacout)
                            activity!!.startActivity(intent)
                            activity!!.overridePendingTransition(0, 0)
                            activity!!.finish()
                        }
                    }
                }

            }
            else if (webServiceMessage.contains("failed to connect")) {
                Toast.makeText(activity!!.applicationContext, "Fail to connect server.", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(activity!!.applicationContext, activity!!.resources.getString(R.string.upload_fail), Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun createImageDataJson(): String {
        var imageDataJson = ""

        doAsync {
            val database = AppDatabase.getInstance(context = activity!!)

            val imageList = database.getImageScoutDAO().getImageScoutByActive()

            val jsonArray = JSONArray()
            for (i in 0 until imageList.size) {
                val imageScout = imageList[i]
                val jsonObject = JSONObject()
                jsonObject.put(WebServiceKey.UploadSacoutImage.key_id, imageScout.ImageID)
                jsonObject.put(WebServiceKey.UploadSacoutImage.key_name, imageScout.ImageName)
                jsonObject.put(WebServiceKey.UploadSacoutImage.key_scout_id, imageScout.ScoutID)
                jsonObject.put(WebServiceKey.UploadSacoutImage.key_path, imageScout.ImagePath)
                jsonObject.put(WebServiceKey.UploadSacoutImage.key_data, imageScout.ImageData)
                jsonObject.put(WebServiceKey.UploadScoutData.key_active, imageScout.Active)
                jsonObject.put(WebServiceKey.UploadScoutData.key_created_by, imageScout.CreatedBy)
                jsonObject.put(WebServiceKey.UploadScoutData.key_modified_by, imageScout.ModifiedBy)
                jsonObject.put(WebServiceKey.UploadScoutData.key_created_on, imageScout.CreatedOn)
                jsonObject.put(WebServiceKey.UploadScoutData.key_modified_on, imageScout.ModifiedOn)
                jsonObject.put(WebServiceKey.UploadScoutData.key_last_action, imageScout.LastAction)

                jsonArray.put(jsonObject)
            }

            imageDataJson = jsonArray.toString()
            Log.i("imageDataJson", imageDataJson)

        }

        return imageDataJson
    }

    fun createImageDataJsonByScoutId(scoutId: String): String {
        var imageDataJson = ""

        doAsync {
            val database = AppDatabase.getInstance(context = activity!!)

            val imageList = database.getImageScoutDAO().getImageByScoutID(scoutId)

            val jsonArray = JSONArray()
            for (i in 0 until imageList.size) {
                val imageScout = imageList[i]
                val jsonObject = JSONObject()
                jsonObject.put(WebServiceKey.UploadSacoutImage.key_id, imageScout.ImageID)
                jsonObject.put(WebServiceKey.UploadSacoutImage.key_name, imageScout.ImageName)
                jsonObject.put(WebServiceKey.UploadSacoutImage.key_scout_id, imageScout.ScoutID)
                jsonObject.put(WebServiceKey.UploadSacoutImage.key_path, imageScout.ImagePath)
                jsonObject.put(WebServiceKey.UploadSacoutImage.key_data, imageScout.ImageData)
                jsonObject.put(WebServiceKey.UploadScoutData.key_active, imageScout.Active)
                jsonObject.put(WebServiceKey.UploadScoutData.key_created_by, imageScout.CreatedBy)
                jsonObject.put(WebServiceKey.UploadScoutData.key_modified_by, imageScout.ModifiedBy)
                jsonObject.put(WebServiceKey.UploadScoutData.key_created_on, imageScout.CreatedOn)
                jsonObject.put(WebServiceKey.UploadScoutData.key_modified_on, imageScout.ModifiedOn)
                jsonObject.put(WebServiceKey.UploadScoutData.key_last_action, imageScout.LastAction)

                jsonArray.put(jsonObject)
            }

            imageDataJson = jsonArray.toString()
        }

        Log.i("imageDataJson", imageDataJson)
        writeIntoFile(imageDataJson)

        return imageDataJson
    }

    fun writeIntoFile(str: String) {
/*
File file = new File(mcoContext.getFilesDir(),"mydir");
    if(!file.exists()){
        file.mkdir();
    }

    try{
        File gpxfile = new File(file, sFileName);
        FileWriter writer = new FileWriter(gpxfile);
        writer.append(sBody);
        writer.flush();
        writer.close();

    }catch (Exception e){
        e.printStackTrace();

    }
*/

        val file = File(context!!.getExternalFilesDir(null), "mydir")
        if (!file.exists()) {
            file.mkdir()
        }

        try {
            val gpxfile = File(file, "myfile.txt")
            val writer = FileWriter(gpxfile)
            writer.append(createImageDataJson())
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}