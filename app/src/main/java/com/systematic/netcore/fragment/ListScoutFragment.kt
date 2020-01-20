package com.systematic.netcore.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.systematic.netcore.R
import com.systematic.netcore.activity.CreateNewScoutActivity
import com.systematic.netcore.adapter.ScoutListAdapter
import com.systematic.netcore.api.GetCity
import com.systematic.netcore.api.UploadSacoutData
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.objects.ScoutObject
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import com.systematic.netcore.utility.WebServiceKey
import kotlinx.android.synthetic.main.fragment_list_scout.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

class ListScoutFragment : Fragment(){
    var TAG = "ListScoutFragment"
    internal var defaultSQLDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss")

    var rbTypeToCheck = "Day"
    var flag = "1"// 1 is for hide day and 2 is for hide day and month

    var font: Typeface? = null

    var user_id = ""

    var todayDate = ""
    var defaultDateForFilter = ""

    var filter_state = "day"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        user_id = SettingPreference.getDataFromSharedPref(Constants.keyUserId, activity!!).toString()

        font = Typeface.createFromAsset(activity!!.assets, "zawgyi_one2008.ttf")

        return inflater.inflate(R.layout.fragment_list_scout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setDatePickerForEditText(edtView)

        // Loads scout data into the ArrayList
        val checkDateArr = edtView?.text.toString().split("/")
        val day = checkDateArr[0]
        val month = checkDateArr[1]
        val year = checkDateArr[2]
        Log.i("year-month-day", "$year-$month-$day")
        defaultDateForFilter = "$year-$month-$day"

        loadList(defaultDateForFilter)

        filter_state = SettingPreference.getDataFromSharedPref(Constants.key_filter_state, activity!!).toString()
        Log.i("filter_state", filter_state)
        val c = Calendar.getInstance()
        val myFormat_Month = "MM/yyyy"
        val myFormat_Year = "yyyy"

        when (filter_state) {
            "day" -> rbDay.isChecked = true

            "month" -> {
                rbMonth.isChecked = true

                val sdf = SimpleDateFormat(myFormat_Month)
                var monthStr = sdf.format(c.time)
                edtView.setText(monthStr)
                val checkDateArr = edtView?.text.toString().split("/")
                val month = checkDateArr[0]
                val year = checkDateArr[1]
                Log.i("year-month", "$year-$month")
                loadList("$year-$month")

            }

            "year" -> {
                rbYear.isChecked = true

                val sdf = SimpleDateFormat(myFormat_Year)
                var yearStr = sdf.format(c.time)
                edtView.setText(yearStr)
                loadList(yearStr)
            }
        }

        radioButtonActionToChangeEditText()

        txtCreate.setOnClickListener {
            checkDownloadedOrNot()
        }

        txtUpload.setOnClickListener {
            uploadData()
        }

//        imageButtonUpload.setOnClickListener {
//            uploadData()
//        }
//
//        imageButtonAdd.setOnClickListener {
//            checkDownloadedOrNot()
//        }

        //createParamInfo()
        createParamInfoJson()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity!!.menuInflater.inflate(R.menu.menu_add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_add) {
            val intent = Intent(activity!!, CreateNewScoutActivity::class.java)
            activity!!.startActivity(intent)
            activity!!.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkDownloadedOrNot() {
        if (SettingPreference.getDataFromSharedPref(Constants.key_is_downloaded, activity!!).toString() != "") {
            if (SettingPreference.getDataFromSharedPref(Constants.key_is_downloaded, activity!!) != "yes") {
                var dialog = MaterialDialog.Builder(activity!!)
                    .title(getString(R.string.no_data_title))
                    .content(getString(R.string.no_data_message))
                    .onPositive { dialog, which ->
                        downlaodCityTownshipAndScoutType()
                    }
                    .positiveText(getString(R.string.get_data))
                    .negativeText(getString(R.string.dont_get_data))
                    .show()

                var title = dialog.titleView
                title.setTypeface(font)

                var message = dialog.contentView
                message?.setTypeface(font)

                var positiveButton = dialog.getActionButton(DialogAction.POSITIVE)
                positiveButton.setTypeface(font)

                var negativeButton = dialog.getActionButton(DialogAction.NEGATIVE)
                negativeButton.setTypeface(font)
            } else {
//                val intent = Intent(this, EditScoutNavActivity::class.java)
//                startActivity(intent)
//                overridePendingTransition(0, 0)
//                finish()
            }
        }
    }

    fun downlaodCityTownshipAndScoutType(){
        val cityTownshipScoutType = GetCity(activity!!, activity!! )
        cityTownshipScoutType.getCity()
    }

    fun Toolbar.changeToolbarFont() {
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if (view is TextView && view.text == title) {
                view.typeface = Typeface.createFromAsset(view.context.assets, "zawgyi_one2008.ttf")
                break
            }
        }
    }

    private fun loadList(date: String) {
        Log.i("load", "list")
        val itemList: ArrayList<ScoutObject> = ArrayList()

        doAsync {
            val database = AppDatabase.getInstance(context = activity!!)

            val scout = database.getScoutDAO().getScout()
            Log.i(TAG, "ScoutList ${scout.size}")
            val scoutList = database.getScoutDAO().getAllWithFilter("$date%", user_id)
            Log.i(TAG, "Date $date")
            Log.i(TAG, "ScoutSize ${scoutList.size}")

            val countAllScout = database.getScoutDAO().getAllByID(user_id)
            if (countAllScout.isEmpty()) {
                uiThread {
                    textViewMessage.setText(getString(R.string.error_no_data))
                }
            }

            if (scoutList.isNotEmpty()) {
                uiThread {
                    textViewMessage.visibility = View.GONE
                }
            } else {
                uiThread {
                    textViewMessage.visibility = View.VISIBLE
                }
            }

            uiThread {
                txtListCount.text = scoutList.size.toString() + " ခု"
            }

            if (scoutList.isNotEmpty()) {
                for (i in 0 until scoutList.size) {
                    val objectScout = ScoutObject()
                    objectScout.scoutID = scoutList[i].ScoutID
                    objectScout.scoutOn = scoutList[i].ScoutOn

                    var scoutOn = objectScout.scoutOn
                    var scoutDate = defaultSQLDateFormat.parse(scoutOn, ParsePosition(0))
                    Log.i("scoutDate", scoutDate.toString())
                    Log.i("time_scoutDate", scoutDate.time.toString())
                    Log.i("timeAgo", getTimeAgo(scoutDate.time))

                    objectScout.timeAgo = getTimeAgo(scoutDate.time)
                    objectScout.companyName = scoutList[i].CompanyName
                    objectScout.contactPerson = scoutList[i].ContactPerson
                    objectScout.contactAddress = scoutList[i].ContactAddress
                    objectScout.contactInformation = scoutList[i].ContactInformation
                    objectScout.remark = scoutList[i].Remark
                    objectScout.cityID = scoutList[i].CityID
                    Log.i("CityID", scoutList[i].CityID)
                    objectScout.townshipID = scoutList[i].TownshipID
                    Log.i("TownshipId", scoutList[i].TownshipID)
                    val townshipList = database.getTownshipDAO().getTownshipById(scoutList[i].TownshipID)
                    if (townshipList.isNotEmpty()) {
                        objectScout.townshipName = townshipList[0].TownshipNameZawgyi
                    }

                    objectScout.scoutTypeId = scoutList[i].ScoutTypeID
                    val scoutTypeList = database.getScoutTypeDAO().getScoutTypeById(scoutList[i].ScoutTypeID)
                    if (scoutTypeList.isNotEmpty()) {
                        objectScout.scoutType = scoutTypeList[0].ScoutTypeNameZawGyi
                    }

                    objectScout.scoutNo = scoutList[i].ScoutNo
                    objectScout.gpsLat = scoutList[i].GpsLat
                    objectScout.gpsLng = scoutList[i].GpsLon
                    objectScout.cellLat = scoutList[i].CellLat
                    objectScout.cellLng = scoutList[i].CellLon
                    objectScout.userLat = scoutList[i].UserLat
                    objectScout.userLng = scoutList[i].UserLon
                    objectScout.uploadOn = scoutList[i].UploadedOn
                    objectScout.createOn = scoutList[i].CreatedOn
                    objectScout.modifiedOn = scoutList[i].ModifiedOn

                    itemList.add(objectScout)
                }
            }

            uiThread {
                // Creates a vertical Layout Manager
                rvScout.layoutManager = LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)

                // You can use GridLayoutManager if you want multiple columns. Enter the number of columns as a parameter.
                rvScout.adapter = ScoutListAdapter(itemList) {
                    Log.i("here", "onClick: " + itemList[it].scoutID.toString())
                    val intent = Intent(activity!!, CreateNewScoutActivity::class.java)
                    var objectScout = ScoutObject()
                    objectScout.scoutID = itemList[it].scoutID
                    objectScout.scoutOn = itemList[it].scoutOn
                    objectScout.companyName = itemList[it].companyName
                    objectScout.contactPerson = itemList[it].contactPerson
                    objectScout.contactAddress = itemList[it].contactAddress
                    objectScout.contactInformation = itemList[it].contactInformation
                    objectScout.remark = itemList[it].remark
                    objectScout.cityID = itemList[it].cityID
                    objectScout.townshipID = itemList[it].townshipID
                    objectScout.scoutTypeId = itemList[it].scoutTypeId
                    objectScout.scoutNo = itemList[it].scoutNo
                    objectScout.gpsLat = itemList[it].gpsLat
                    objectScout.gpsLng = itemList[it].gpsLng
                    objectScout.cellLat = itemList[it].cellLat
                    objectScout.cellLng = itemList[it].cellLng
                    objectScout.userLat = itemList[it].userLat
                    objectScout.userLng = itemList[it].userLng
                    intent.putExtra(Constants.key_object, objectScout)
                    intent.putExtra(Constants.key_is_edit, "yes")
                    startActivity(intent)
                    activity!!.finish()
                }

                rvScout!!.adapter!!.notifyDataSetChanged()
            }

        }
        /*rvScout.adapter = ScoutListAdapter(itemList) {
            val intent = Intent(this, TestImage::class.java)
            startActivity(intent)
        }*/
    }

    private fun setDatePickerForEditText(editText: EditText) {
        val c = Calendar.getInstance()
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat)
        val dateSetListener = DatePickerDialog.OnDateSetListener { dpd, year, monthOfYear, dayOfMonth ->
            //            val cal = Calendar.getInstance()
            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, monthOfYear)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            editText.setText(sdf.format(c.time))

            Log.i("dateSet", "listener")

            val checkDateArr = edtView?.text.toString().split("/")
            val day = checkDateArr[0]
            val month = checkDateArr[1]
            val year = checkDateArr[2]
            Log.i("year-month-day", "$year-$month-$day")
            loadList("$year-$month-$day")
        }

        val dialog = datePickerDialog(editText)

        val dpd = DatePickerDialog(
            activity!!,
            dateSetListener,
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )
        editText.setText(sdf.format(c.time))
        editText.setOnClickListener {
            //dpd.show()
            if (radioButtonAction() == "Day") {
                dpd.show()
            } else if (radioButtonAction() == "Month") {

                dialog.show()

                flag = "1"

                //Hide Day Selector
                val day = dialog.findViewById<View>(Resources.getSystem().getIdentifier("android:id/day", null, null))
                if (day != null) {
                    day.visibility = View.GONE
                }

                //Show Month Selector
                val month =
                    dialog.findViewById<View>(Resources.getSystem().getIdentifier("android:id/month", null, null))
                if (month != null) {
                    month.visibility = View.VISIBLE
                }
            } else if (radioButtonAction().equals("Year")) {

                dialog.show()

                flag = "2"

                //Hide Day Selector
                val day = dialog.findViewById<View>(Resources.getSystem().getIdentifier("android:id/day", null, null))
                if (day != null) {
                    day.visibility = View.GONE
                }

                //Hide Month Selector
                val month =
                    dialog.findViewById<View>(Resources.getSystem().getIdentifier("android:id/month", null, null))
                if (month != null) {
                    month.visibility = View.GONE
                }
            }
        }
    }

    private fun radioButtonActionToChangeEditText() {
        val c = Calendar.getInstance()
        val myFormat_Date = "dd/MM/yyyy"
        val myFormat_Month = "MM/yyyy"
        val myFormat_Year = "yyyy"

        rbDay.setOnClickListener {
            val sdf = SimpleDateFormat(myFormat_Date)
            var date = sdf.format(c.time)
            edtView.setText(date)
            val checkDateArr = edtView?.text.toString().split("/")
            val day = checkDateArr[0]
            val month = checkDateArr[1]
            val year = checkDateArr[2]
            Log.i("year-month-day", "$year-$month-$day")
            loadList("$year-$month-$day")

            putFilterStateToPref("day")
        }

        rbMonth.setOnClickListener {
            val sdf = SimpleDateFormat(myFormat_Month)
            var monthStr = sdf.format(c.time)
            edtView.setText(monthStr)
            val checkDateArr = edtView?.text.toString().split("/")
            val month = checkDateArr[0]
            val year = checkDateArr[1]
            Log.i("year-month", "$year-$month")
            loadList("$year-$month")

            putFilterStateToPref("month")
        }

        rbYear.setOnClickListener {
            val sdf = SimpleDateFormat(myFormat_Year)
            var yearStr = sdf.format(c.time)
            edtView.setText(yearStr)
            loadList(yearStr)

            putFilterStateToPref("year")
        }
    }

    @SuppressLint("SetTextI18n")
    fun datePickerDialog(editText: EditText): DatePickerDialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Date Picker Dialog
        val datePickerDialog = DatePickerDialog(
            activity!!,
            android.R.style.Theme_Holo_Light_Dialog_MinWidth,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in textbox
                //date.text = "$dayOfMonth $monthOfYear, $year"

                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, monthOfYear)
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                if (flag.equals("1")) {
                    val myFormat_Month = "MM/yyyy"
                    val sdf = SimpleDateFormat(myFormat_Month)
                    var monthStr = sdf.format(c.time)
                    Log.i("monthStr", monthStr)
                    editText.setText(monthStr)
                    val checkDateArr = edtView?.text.toString().split("/")
                    val month = checkDateArr[0]
                    val year = checkDateArr[1]
                    Log.i("year-month", "$year-$month")
                    loadList("$year-$month")
                } else {
                    editText.setText("$year")
                    loadList("$year")
                }
            },
            year,
            month,
            day
        )
        // Show Date Picker

        return datePickerDialog


    }

    private fun radioButtonAction(): String {
        var value = String()
        if (rbDay.isChecked) {
            value = "Day"
        } else if (rbMonth.isChecked) {
            value = "Month"
        } else {
            value = "Year"
        }
        return value
    }

    inner class CallUploadScoutData : AsyncTask<Void, Void, String>() {
        var webServiceMessage = ""
        val loading = activity!!.indeterminateProgressDialog("Uploading...")
        var webServiceUrl = ""
        var webServiceNamespace = resources.getString(R.string.WebServiceNameSpace)

        override fun onPreExecute() {
            loading.setCancelable(false)
            loading.show()
        }

        override fun doInBackground(vararg params: Void?): String {
            try {
//                var methodName = "SaveScoutInfoJson"
//                var soapObject = SoapObject(webServiceNamespace, methodName)
//
//                soapObject.addProperty(KeyForWebService.UploadScoutData.key_info, createParamInfoJson())
//
//                var soapSerializationEnvelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
//                soapSerializationEnvelope.dotNet = true
//                soapSerializationEnvelope.setOutputSoapObject(soapObject)
//
//                try {
//                    val mySharedPreference = this@ListScoutFragment.getSharedPreferences(MyPREFS, mode)
//                    val myEditor = mySharedPreference.edit()
//                    webServiceUrl = mySharedPreference.getString(Constant.key_web_service_url, "")
//                } catch (e: Exception) {
//                    Log.e("exception occur", e.message)
//                }
//
//                var httpTransportSE = HttpTransportSE(webServiceUrl + "WebServiceCRM.asmx")
//                httpTransportSE.debug = true
//                httpTransportSE.call(
//                    resources.getString(R.string.WebServiceSOAPActionURL) + methodName,
//                    soapSerializationEnvelope
//                )
//
//                var webServiceResult = soapSerializationEnvelope.response as SoapPrimitive
//                Log.i("result", webServiceResult.toString())

                webServiceMessage = "success"
            } catch (e: Exception) {
                Log.e("exception occurs", e.message)
            }
            return webServiceMessage
        }

        override fun onPostExecute(result: String?) {
            if (result == "success") {
                loading.dismiss()
//                val calendar = Calendar.getInstance()
//                todayDate = defaultSQLDateFormat.format(calendar.time)
//                Log.i("todayDate", todayDate)
//                val dbH = DatabaseHelper(this@ListScoutFragment)
//                dbH.dbhScout.updateFlagForUploadComplete(todayDate, user_id)
//                Toast.makeText(applicationContext, getString(R.string.upload_complete), Toast.LENGTH_SHORT).show()
//                loadList(defaultDateForFilter)
            } else {
               // Toast.makeText(applicationContext, getString(R.string.upload_fail), Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun createParamInfoJson(): String {
        var infoString = ""
        val itemList: ArrayList<ScoutObject> = ArrayList()
        doAsync {
            val database = AppDatabase.getInstance(context = activity!!)
            val scoutList = database.getScoutDAO().getAllByID(user_id)

            if (scoutList.isNotEmpty()) {
                for (i in 0 until scoutList.size) {
                    val scoutObject = ScoutObject()
                    scoutObject.scoutID = scoutList[i].ScoutID
                    scoutObject.scoutOn = scoutList[i].ScoutOn
                    scoutObject.companyName = scoutList[i].CompanyName
                    scoutObject.contactPerson = scoutList[i].ContactPerson
                    scoutObject.contactAddress = scoutList[i].ContactAddress
                    scoutObject.contactInformation = scoutList[i].ContactInformation
                    scoutObject.remark = scoutList[i].Remark
                    scoutObject.cityID = scoutList[i].CityID
                    scoutObject.townshipID = scoutList[i].TownshipID
                    scoutObject.scoutTypeId = scoutList[i].ScoutTypeID
                    scoutObject.active = scoutList[i].Active.toInt()
                    scoutObject.uploadOn = scoutList[i].UploadedOn
                    scoutObject.createOn = scoutList[i].CreatedOn
                    scoutObject.modifiedOn = scoutList[i].ModifiedOn
                    scoutObject.uploadBy = scoutList[i].UploadedBy
                    scoutObject.createBy = scoutList[i].CreatedBy
                    scoutObject.modifiedBy = scoutList[i].ModifiedBy
                    scoutObject.gpsLat = scoutList[i].GpsLat
                    scoutObject.gpsLng = scoutList[i].GpsLon
                    scoutObject.cellLat = scoutList[i].CellLat
                    scoutObject.cellLng = scoutList[i].CellLon
                    scoutObject.userLat = scoutList[i].UserLat
                    scoutObject.userLng = scoutList[i].UserLon
                    scoutObject.scoutNo = scoutList[i].ScoutNo

                    itemList.add(scoutObject)
                }
            }
        }


        var jsonArray = JSONArray()
        var defaultDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.sss", Locale.US)
        for (i in 0 until itemList.size) {
            val objectScout = itemList.get(i)

            val jsonObject = JSONObject()
            jsonObject.put(WebServiceKey.UploadScoutData.key_scout_id, objectScout.scoutID)
            jsonObject.put(WebServiceKey.UploadScoutData.key_township_id, objectScout.townshipID)
            jsonObject.put(WebServiceKey.UploadScoutData.key_scoutType_id, objectScout.scoutTypeId)
            jsonObject.put(WebServiceKey.UploadScoutData.key_contact_person, objectScout.contactPerson)
            jsonObject.put(WebServiceKey.UploadScoutData.key_contact_information, objectScout.contactInformation)
            jsonObject.put(WebServiceKey.UploadScoutData.key_contact_address, objectScout.contactAddress)
            jsonObject.put(WebServiceKey.UploadScoutData.key_company_name, objectScout.companyName)
            jsonObject.put(WebServiceKey.UploadScoutData.key_scout_on, objectScout.scoutOn)
            //jsonObject.put(KeyForWebService.UploadScoutData.key_scout_on, "")
            jsonObject.put(WebServiceKey.UploadScoutData.key_scout_no, objectScout.scoutNo)
            jsonObject.put(WebServiceKey.UploadScoutData.key_uploaded_by, objectScout.uploadBy)
            jsonObject.put(WebServiceKey.UploadScoutData.key_uploaded_on, objectScout.uploadOn)
            //jsonObject.put(KeyForWebService.UploadScoutData.key_uploaded_on, "")
            jsonObject.put(WebServiceKey.UploadScoutData.key_remark, objectScout.remark)
            jsonObject.put(WebServiceKey.UploadScoutData.key_gps_lat, objectScout.gpsLat)
            jsonObject.put(WebServiceKey.UploadScoutData.key_gps_lon, objectScout.gpsLng)
            jsonObject.put(WebServiceKey.UploadScoutData.key_cell_lat, objectScout.cellLat)
            jsonObject.put(WebServiceKey.UploadScoutData.key_cell_lon, objectScout.cellLng)
            jsonObject.put(WebServiceKey.UploadScoutData.key_user_lat, objectScout.userLat)
            jsonObject.put(WebServiceKey.UploadScoutData.key_user_lon, objectScout.userLng)
            jsonObject.put(WebServiceKey.UploadScoutData.key_active, objectScout.active)
            jsonObject.put(WebServiceKey.UploadScoutData.key_created_by, objectScout.createBy)
            jsonObject.put(WebServiceKey.UploadScoutData.key_created_on, objectScout.createOn)
            //jsonObject.put(KeyForWebService.UploadScoutData.key_created_on, "")
            jsonObject.put(WebServiceKey.UploadScoutData.key_modified_by, objectScout.modifiedBy)
            jsonObject.put(WebServiceKey.UploadScoutData.key_modified_on, objectScout.modifiedOn)
            //jsonObject.put(KeyForWebService.UploadScoutData.key_modified_on, "")
            jsonObject.put(WebServiceKey.UploadScoutData.key_last_action, objectScout.lastAction)

            jsonArray.put(jsonObject)
        }

        /*val jsonObjectScoutData = JSONObject()
        jsonObjectScoutData.put(KeyForWebService.UploadScoutData.key_crm_scout, jsonArray)*/

        infoString = jsonArray.toString()

        Log.i("infoString", infoString)

        return infoString
    }

    fun checkActiveStatus(b: Int): String {
        when (b) {
            1 -> return "true"
            0 -> return "false"
        }
        return "true"
    }

    fun getTimeAgo(time: Long): String {

        var SECOND_MILLIS = 1000
        var MINUTE_MILLIS = 60 * SECOND_MILLIS
        var HOUR_MILLIS = 60 * MINUTE_MILLIS
        var DAY_MILLIS = 24 * HOUR_MILLIS

        /*var time = time
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }*/

        val calendar = GregorianCalendar()
        val now = calendar.timeInMillis
        Log.i("nowDate", calendar.time.toString())
        Log.i("time_now", now.toString())
        if (time > now || time <= 0) {
            return ""
        }

        // TODO: localize
        val diff = now - time
        Log.i("time_diff", diff.toString())
        return if (diff < MINUTE_MILLIS) {
            "just now"
        } else if (diff < 2 * MINUTE_MILLIS) {
            "a minute ago"
        } else if (diff < 50 * MINUTE_MILLIS) {
            var res = diff / MINUTE_MILLIS
            "$res minutes ago"
        } else if (diff < 90 * MINUTE_MILLIS) {
            "an hour ago"
        } else if (diff < 24 * HOUR_MILLIS) {
            var res = diff / HOUR_MILLIS
            "$res hours ago"
        } else if (diff < 48 * HOUR_MILLIS) {
            "yesterday"
        } else {
            var res = diff / DAY_MILLIS
            "$res days ago"
        }
    }

    fun uploadData() {
        var uploadData = UploadSacoutData(activity!!, context!!, user_id, font!!, "yes")
        uploadData.upload()
    }

    fun putFilterStateToPref(str: String) {
        SettingPreference.putDataToSharefPref(Constants.key_filter_state, str, activity!!)
    }
}
