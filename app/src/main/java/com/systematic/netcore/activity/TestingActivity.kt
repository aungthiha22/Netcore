package com.systematic.netcore.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.systematic.netcore.R
import com.systematic.netcore.api.UploadInstallation
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.database.model.Installation
import com.systematic.netcore.objects.InstallationObject
import com.systematic.netcore.objects.UploadInstallationObj
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.WebServiceKey
import kotlinx.android.synthetic.main.fragment_list_installation.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.json.JSONObject
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapPrimitive
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TestingActivity : AppCompatActivity(),View.OnClickListener{

    var TAG = "InstallationListFragment"
    var activity: Activity? = null
    var selectDate = ""
    var statusForCheckAllDate = ""

    private lateinit var dpd: DatePickerDialog
    private val calendar = Calendar.getInstance()
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    private val installationList : ArrayList<InstallationObject> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_list_installation)


    }

    override fun onClick(view: View?) {
        when (view){

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
                /*val InstallationList = database.getInstallationDAO().getInstallation()
                val uploadInstallationObj = UploadInstallationObj()
                uploadInstallationObj.InstallationList = InstallationList
                val installationJsonString = Gson().toJson(uploadInstallationObj)*/
                val InstallationList = database.getInstallationDAO().getInstallation()



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

        }

    }
}