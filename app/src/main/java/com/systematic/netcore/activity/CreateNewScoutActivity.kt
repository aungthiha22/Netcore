package com.systematic.netcore.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.sqlite.db.SimpleSQLiteQuery
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.systematic.netcore.BuildConfig
import com.systematic.netcore.R
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.database.model.*
import com.systematic.netcore.fragment.ListScoutFragment
import com.systematic.netcore.objects.ScoutObject
import com.systematic.netcore.utility.BitmapUtility
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import kotlinx.android.synthetic.main.activity_create_new_scout.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapPrimitive
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CreateNewScoutActivity : AppCompatActivity(), View.OnClickListener, LocationListener, OnMapReadyCallback {

    val TAG = "CreateNewScoutActivity"

    private lateinit var mMap: GoogleMap
    private var imgLayoutHeight: Int = 0
    private var imgLayoutWidth: Int = 0

    private var gpsLat: Double = 0.0
    private var gpsLng: Double = 0.0
    private var cellLat: Double = 0.0
    private var cellLng: Double = 0.0
    private var pickupLat: Double = 0.0
    private var pickupLng: Double = 0.0

    private val capturedImages: Vector<File> = Vector<File>()
    private val capturedImageURIs: Vector<Uri> = Vector<Uri>()
    private val capturedImageAbsPath: Vector<String> = Vector<String>()

    private val capturedImagesReal: Vector<File> = Vector<File>()
    private val capturedImageURIsReal: Vector<Uri> = Vector<Uri>()
    private val capturedImageAbsPathReal: Vector<String> = Vector<String>()

    private val capturedImageNamePath: ArrayList<String> = ArrayList()
    private val capturedImageDataStringPath: ArrayList<String> = ArrayList()
    private val deletedImagePath: ArrayList<String> = ArrayList()


    private lateinit var tempDirPath: String
    private lateinit var scoutImgDirPath: String

    private var displayW: Int = 0
    private var displayH: Int = 0

    //LocationManager >>
    private val MY_PERMISSIONS_REQUEST_GPS_LOCATION = 1
    private val MY_PERMISSIONS_REQUEST_CAMERA = 2
    // <<
    private val REQUEST_TAKE_CUSTOM_GPS = 1
    private val REQUEST_TAKE_PHOTO = 2

    internal var defaultSQLDateFormat = SimpleDateFormat("yyyyMMdd")
    internal var displayDateFormatAtUI = SimpleDateFormat("dd/MM/yyyy")
    internal var defaultDateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss")

    var is_edit = ""
    var objectScout = ScoutObject()

    var font: Typeface? = null

    var cityDataArrayList: MutableList<City>? = null
    var townshipDataArrayList: MutableList<Township>? = null
    var scoutTypeDataArrayList: MutableList<ScoutType>? = null

    internal val mode = Activity.MODE_PRIVATE
    internal val MyPREFS = "MyPreference"

    var user_id = ""

    var selectedDate = ""

    var timeStamp = ""

    private var myLocale: Locale? = null

    var image_id_generated = ""
    var image_id_generated_arr_temp: ArrayList<String> = ArrayList()

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        hideNavBar()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_scout)

        user_id = SettingPreference.getDataFromSharedPref(Constants.keyUserId, this).toString()
        Log.i("user_id", user_id)

        font = Typeface.createFromAsset(assets, "zawgyi_one2008.ttf")

        setScoutNo()

        loadViewWidthAndHeight()
        loadMapFragment()
        loadImagesPath()
        loadSpinnerData()

        setDatePickerForEditText(etxtScoutOn)
        if (intent.getStringExtra(Constants.key_selected_date) != null) {
            selectedDate = intent.getStringExtra(Constants.key_selected_date)
        }
        Log.i("selectedDate", selectedDate)

        if (intent.getDoubleExtra("pickupLat", 0.0) != null) {
            pickupLat = intent.getDoubleExtra("pickupLat", 0.0)
            pickupLng = intent.getDoubleExtra("pickupLng", 0.0)
            gpsLat = intent.getDoubleExtra("gpsLat", 0.0)
            gpsLng = intent.getDoubleExtra("gpsLng", 0.0)
            cellLat = intent.getDoubleExtra("cellLat", 0.0)
            cellLng = intent.getDoubleExtra("cellLng", 0.0)
        }

        //checkGpsLocation()

        measureDynamicHeight()

        //Set OnClickListener >>
        llBtnGetLoc.setOnClickListener(this)
        llBtnGetPicture.setOnClickListener(this)
        llBtnSave.setOnClickListener(this)
        btnAddImage.setOnClickListener(this)
        imageButtonTakePicture.setOnClickListener(this)
        imageButtonSave.setOnClickListener(this)
        ibBack.setOnClickListener(this)
        //Set OnClickListener <<

        //acTxtCompanyName.setText("Test")
        //acTxtContactPersonName.setText("t")

        deleteFlagPref("no")

        if (intent.getStringExtra(Constants.key_is_edit) != null) {
            is_edit = intent.getStringExtra(Constants.key_is_edit)
            if (is_edit == "yes") {

                objectScout = intent.getSerializableExtra(Constants.key_object) as ScoutObject
                etxtScoutNo.setText(objectScout.scoutNo.toString())
                var dateFromDbArr = objectScout.scoutOn.split(" ")
                Log.i("first", dateFromDbArr[0])
                var first = dateFromDbArr[0].split("-")
                var day = first[2]
                var month = first[1]
                var year = first[0]
                var scoutOn = "$day/$month/$year"
                etxtScoutOn.setText(scoutOn)
                acTxtCompanyName.setText(objectScout.companyName)
                acTxtContactPersonName.setText(objectScout.contactPerson)
                etxtContactPersonInfo.setText(objectScout.contactInformation)
                etxtAddress.setText(objectScout.contactAddress)
                etxtRemark.setText(objectScout.remark)



                for (i in 0 until townshipDataArrayList!!.size) {
                    if (townshipDataArrayList!![i].TownshipID == objectScout.townshipID) {
                        spinTownship.setSelection(i)
                    }
                }

                for (i in 0 until scoutTypeDataArrayList!!.size) {
                    if (scoutTypeDataArrayList!![i].ScoutTypeID == objectScout.scoutTypeId) {
                        spinScoutType.setSelection(i)
                    }
                }

                gpsLat = objectScout.gpsLat.toDouble()
                gpsLng = objectScout.gpsLng.toDouble()
                cellLat = objectScout.cellLat.toDouble()
                cellLng = objectScout.cellLng.toDouble()
                pickupLat = objectScout.userLat.toDouble()
                pickupLng = objectScout.userLng.toDouble()

                Log.i("pickupLat", pickupLat.toString())
                Log.i("gpsLat", gpsLat.toString())

                doAsync {
                    val database = AppDatabase.getInstance(this@CreateNewScoutActivity)
                    val imageScoutById = database.getImageScoutDAO().getImageByScoutID(objectScout.scoutID)
                    if (imageScoutById.isNotEmpty()) {
                        uiThread {
                            btnAddImage.visibility = View.GONE
                        }

                        var imageScoutList: List<ImageScout> = ArrayList()

                        Log.i(
                            "is_saved",
                            SettingPreference.getDataFromSharedPref(
                                Constants.key_is_saved,
                                this@CreateNewScoutActivity
                            ).toString()
                        )

                        if (SettingPreference.getDataFromSharedPref(
                                Constants.key_is_saved,
                                this@CreateNewScoutActivity
                            ).toString().equals("yes")
                        ) {
                            imageScoutList = database.getImageScoutDAO().getImageByFlagYes(objectScout.scoutID)
                        } else {
                            imageScoutList = database.getImageScoutDAO().getImageByFlagNo(objectScout.scoutID, "No")
                        }

                        //list = dbH_Image.getImage(objectScout.scoutID, dbH_Image.tableName_Image)
                        Log.i("list_size", imageScoutList.size.toString())

                        for (i in 0 until imageScoutList.size) {
                            Log.i("image_pos", i.toString())
                            var imageScout = imageScoutList[i]
                            val file = File(imageScout.ImagePath)
                            val uri = Uri.fromFile(file)

                            capturedImages.add(file)
                            capturedImageURIs.add(uri)
                            capturedImageAbsPath.add(file.absolutePath)

                            capturedImagesReal.add(file)
                            capturedImageURIsReal.add(uri)
                            capturedImageAbsPathReal.add(file.absolutePath)

                            capturedImageNamePath.add(imageScout.ImageName)
                            capturedImageDataStringPath.add(imageScout.ImageData)

                            imgLayoutHeight =
                                SettingPreference.getDataFromSharedPref(
                                    Constants.key_imageLayout_Height,
                                    this@CreateNewScoutActivity
                                ).toString()
                                    .toInt()
                            Log.i("imgLayoutHeight_pref", imgLayoutHeight.toString())

                            Log.i("capturedImageAbsPath", capturedImageAbsPath.lastElement())
                            val bmpOut = BitmapFactory.decodeFile(capturedImageAbsPath.lastElement())

                            if (bmpOut != null) {
                                uiThread {
                                    addImageInLayout(bmpOut, i)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v) {
            llBtnGetLoc -> {
                val intent = Intent(this, MapChoice::class.java)
                Log.i("is_edit", is_edit)
                /*if (intent.getStringExtra(Constant.key_is_edit) != null) {
                    is_edit = intent.getStringExtra(Constant.key_is_edit)*/
                if (is_edit.equals("yes")) {
                    Log.i("is_edit_go", "yes")
                    intent.putExtra(Constants.key_is_edit, "yes")
                    intent.putExtra(Constants.key_latitude, pickupLat)
                    intent.putExtra(Constants.key_longitude, pickupLng)
                    intent.putExtra("gpsLat", gpsLat)
                    intent.putExtra("gpsLng", gpsLng)
                    intent.putExtra("cellLat", cellLat)
                    intent.putExtra("cellLng", cellLng)
                }
                //}
                intent.putExtra(Constants.key_selected_date, selectedDate)
                startActivityForResult(intent, REQUEST_TAKE_CUSTOM_GPS)
            }
            llBtnGetPicture -> {
                takePicture()//this is work.. but bitMap got thumbnail.
            }

            llBtnSave -> {
                save()
            }

            imageButtonSave -> {
                Log.i("selectedDate_save", selectedDate)
                save()
            }

            ibBack -> {
                goToMainActivity()
            }

            btnAddImage -> {
                takePicture()
            }

            imageButtonTakePicture -> {
                takePicture()
            }
        }
    }

    private fun save() {
        if (validate()) {
            // val tblName = dbh.dbhScout.TableName

            val recordId = UUID.randomUUID().toString()
            Log.i("recordId", recordId)

            val scoutNo = etxtScoutNo.text.toString()
            val companyName = acTxtCompanyName.text.toString()
            val contactPersonName = acTxtContactPersonName.text.toString()
            val contactPersonInfo = etxtContactPersonInfo.text.toString()
            val address = etxtAddress.text.toString()
            val remark = etxtRemark.text.toString()

            val calendar = GregorianCalendar()

            val scoutOn = selectedDate
            Log.i("scoutOn", scoutOn.toString())
            val todayDate = defaultDateTimeFormat.format(calendar.time)
            Log.i("todayDate", todayDate.toString())

            val scoutType = spinScoutType.selectedItem.toString()
            Log.i("scoutType", scoutType)
            val scoutTypeId = scoutTypeDataArrayList!![spinScoutType.selectedItemPosition].ScoutTypeID
            Log.i("scoutTypeId", scoutTypeId.toString())
            val city = spinCity.selectedItem.toString()
            Log.i("city", city)
            val cityId = cityDataArrayList!![spinCity.selectedItemPosition].CityID
            Log.i("cityId", cityId.toString())
            val township = spinTownship.selectedItem.toString()
            Log.i("township", township)
            val townshipId = townshipDataArrayList!![spinTownship.selectedItemPosition].TownshipID
            Log.i("townshipId", townshipId.toString())

            val scoutObject = Scout()
            scoutObject.ScoutID = recordId
            scoutObject.ScoutTypeID = scoutTypeId
            scoutObject.ScoutNo = scoutNo
            scoutObject.ScoutOn = scoutOn
            scoutObject.TownshipID = townshipId
            scoutObject.CityID = cityId
            scoutObject.CompanyName = companyName
            scoutObject.ContactPerson = contactPersonName
            scoutObject.ContactInformation = contactPersonInfo
            scoutObject.ContactAddress = address
            scoutObject.Remark = remark
            scoutObject.GpsLat = gpsLat.toString()
            scoutObject.GpsLon = gpsLng.toString()
            scoutObject.CellLat = cellLat.toString()
            scoutObject.CellLon = cellLng.toString()
            scoutObject.UserLat = pickupLat.toString()
            scoutObject.UserLon = pickupLng.toString()
            scoutObject.UploadedBy = user_id
            scoutObject.UploadedOn = ""
            scoutObject.CreatedBy = user_id
            scoutObject.CreatedOn = todayDate
            scoutObject.ModifiedBy = user_id
            scoutObject.ModifiedOn = todayDate

            doAsync {
                val database = AppDatabase.getInstance(this@CreateNewScoutActivity)
                if (is_edit != "yes") {

                    database.getScoutDAO().insert(scoutObject)

                    for (i in 0 until capturedImageAbsPathReal.size) {

                        val calendar = Calendar.getInstance()
                        val todayDate = defaultDateTimeFormat.format(calendar.time)

                        image_id_generated = UUID.randomUUID().toString()
                        image_id_generated_arr_temp.add(image_id_generated)
                        val imageScout = ImageScout()
                        imageScout.ImageID = image_id_generated
                        Log.i("img_name", capturedImageNamePath.get(i))
                        imageScout.ImageName = capturedImageNamePath.get(i)
                        imageScout.ScoutID = recordId
                        imageScout.ImagePath = capturedImageAbsPathReal.get(i).toString()
                        imageScout.ImageData = capturedImageDataStringPath.get(i)
                        imageScout.Active = "1"
                        imageScout.CreatedBy = user_id
                        imageScout.ModifiedBy = user_id
                        imageScout.CreatedOn = todayDate
                        imageScout.ModifiedOn = todayDate
                        imageScout.LastAction = ""

                        database.getImageScoutDAO().insert(imageScout)
                    }

                    uiThread {
                        Toast.makeText(this@CreateNewScoutActivity, "Successfully saved.", Toast.LENGTH_SHORT).show()
                        saveFlagPref("yes")
                        goToList()
                    }

                } else {

                    val updateQuery = SimpleSQLiteQuery(
                        "update Scout set ScoutTypeID = '$scoutTypeId', ScoutNo = '$scoutNo', ScoutOn = '$scoutOn'," +
                                " TownshipID = '$townshipId', CityID = '$cityId', CompanyName = '$companyName', Remark = '$remark', ContactPerson = '$contactPersonName', ContactInformation = '$contactPersonInfo', ContactAddress = '$address'," +
                                " GpsLat = '$gpsLat', GpsLon = '$gpsLng', CellLat = '$cellLat', CellLon = '$cellLng', UserLat = '$pickupLat', UserLon = '$pickupLng', ModifiedOn = '$todayDate' where ScoutID = '$recordId'"
                    )
                    database.getScoutDAO().update(updateQuery)

                    database.getImageScoutDAO().deleteById(objectScout.scoutID)
                    saveFlagPref("yes")

                    uiThread {
                        Toast.makeText(this@CreateNewScoutActivity, "Successfully updated.", Toast.LENGTH_SHORT).show()
                    }

                    goToList()
                }
            }

            SettingPreference.putDataToSharefPref(Constants.key_last_city, cityId, this)
            SettingPreference.putDataToSharefPref(Constants.key_last_township, townshipId, this)
        }
    }

    override fun onBackPressed() {
        goToMainActivity()
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Constants.key_go_to, Constants.key_new_sacout)
        startActivity(intent)
        finish()
    }

    fun saveFlagPref(str: String) {
        SettingPreference.putDataToSharefPref(Constants.key_is_saved, str, this@CreateNewScoutActivity)
    }

    fun deleteFlagPref(str: String) {
        SettingPreference.putDataToSharefPref(Constants.key_is_saved, str, this@CreateNewScoutActivity)
    }

    private fun measureDynamicHeight() {
        llImgs.tag = true
        llImgs.viewTreeObserver.addOnGlobalLayoutListener {
            //            promoScrollView.viewTreeObserver.removeOnGlobalLayoutListener(promoScrollView.viewTreeObserver.getGlobal)
            val isInit: Boolean = llImgs.tag as Boolean
            if (!isInit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    llImgs.viewTreeObserver.removeOnGlobalLayoutListener { this }
                }
                return@addOnGlobalLayoutListener
            }
            //return@addOnGlobalLayoutListener

            llImgs.tag = false

            imgLayoutHeight = llImgs.measuredHeight
            imgLayoutWidth = llImgs.measuredWidth
            Log.i("imgLayoutHeight", imgLayoutHeight.toString())
            Log.i("imgLayoutWidth", imgLayoutWidth.toString())

            SettingPreference.putDataToSharefPref(
                Constants.key_imageLayout_Height,
                imgLayoutHeight,
                this@CreateNewScoutActivity
            )

            val param = LinearLayout.LayoutParams(svhImages.measuredWidth, LinearLayout.LayoutParams.MATCH_PARENT)
            btnAddImage.layoutParams = param
        }
    }

    private fun hideNavBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun loadMapFragment() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            (supportFragmentManager.findFragmentById(R.id.ShowNearestLocationMapViewMapFrag) as SupportMapFragment)
        mapFragment.getMapAsync(this)
    }

    private fun loadImagesPath() {
        val appPicturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        /**
         * Save all user captured images for temporary in this folder.
         * Delete all files after user save current form.
         * Also delete all files after form load (onCreate).
         * In edit >> Copy all saved images from scoutImgDir to tempDir (of a record).
         */
        tempDirPath = "$appPicturesDir/temp"
        /**
         * Move user selected files from temp (Directory) to this folder when save button press.
         */
        scoutImgDirPath = "$appPicturesDir/ScoutImages"

//        val absPath = "/storage/emulated/0/Android/data/com.systematic_solution.marketing_scout/files/Pictures/aa.jpg"
//        val absPath = "/storage/emulated/0/Android/data/com.systematic_solution.marketing_scout/files/Pictures/bb.jpg"
//        val absPath = "/storage/emulated/0/Android/data/com.systematic_solution.marketing_scout/files/Pictures/ImgFromHuawei.jpg"
//        val absPath = "/storage/emulated/0/Android/data/com.systematic_solution.marketing_scout/files/Pictures/ImgFromHuawei_50R.jpg"
//        val myBitmap = BitmapFactory.decodeFile(absPath)
//        imgV.setImageBitmap(myBitmap)
    }

    private fun setScoutNo() {
        doAsync {
            val database = AppDatabase.getInstance(this@CreateNewScoutActivity)
            val scoutList = database.getScoutDAO().getScoutByUserId(user_id)
            Log.d(TAG, "ScoutNo ${scoutList.size}")
            val scoutNo = "SC-" + String.format("%010d", scoutList.size + 1)
            etxtScoutNo.setText(scoutNo)
        }
        //etxtScoutNo.isEnabled = false
    }

    private fun loadViewWidthAndHeight() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayW = displayMetrics.heightPixels
        displayH = displayMetrics.widthPixels
    }

    private fun loadSpinnerData() {

        doAsync {
            val database = AppDatabase.getInstance(this@CreateNewScoutActivity)

            //Scout Type >>
            var scoutType = ScoutType()
            scoutType.ScoutTypeID = ""
            scoutType.ScoutTypeNameEng = ""
            scoutType.ScoutTypeNameZawGyi = "အမ်ိဳးအစားေရြးပါ"
            scoutType.ScoutTypeNameUnicode = ""

            scoutTypeDataArrayList = database.getScoutTypeDAO().getScoutType() as MutableList
            scoutTypeDataArrayList!!.add(0, scoutType)

            Log.i("scoutTypeDataArrayList", scoutTypeDataArrayList?.size.toString())
            var scoutTypeData = arrayOfNulls<String>(scoutTypeDataArrayList!!.size)
            for (i in 0 until scoutTypeDataArrayList!!.size) {
                scoutTypeData[i] = scoutTypeDataArrayList!![i].ScoutTypeNameZawGyi
            }
            uiThread {
                // Create an ArrayAdapter using a simple spinner layout and languages array
                val scoutTypeAdapter = ArrayAdapter(this@CreateNewScoutActivity, R.layout.spinner_item, scoutTypeData)
                // Set layout to use when the list of choices appear
                scoutTypeAdapter.setDropDownViewResource(R.layout.spinner_item)
                // Set Adapter to Spinner
                spinScoutType.adapter = scoutTypeAdapter

                for (i in 0 until scoutTypeDataArrayList!!.size) {
                    if (intent.getStringExtra(Constants.key_is_edit) != null) {
                        is_edit = intent.getStringExtra(Constants.key_is_edit)
                        if (is_edit == "yes") {
                            objectScout = intent.getSerializableExtra(Constants.key_object) as ScoutObject
                            if (scoutTypeDataArrayList!![i].ScoutTypeID == objectScout.scoutTypeId) {
                                spinScoutType.setSelection(i)
                                break
                            }
                        }
                    }
                }
            }


            //City >>
            //val cityData = arrayOf("Yangon", "Mandalay", "Naypyitaw", "Taungyi", "Thanlyin")
            var city = City()
            city.CityID = ""
            city.CityNameEng = ""
            city.CityNameZawgyi = "ၿမိဳ႕ေရြးပါ"
            city.CityNameUnicode = ""

            cityDataArrayList = database.getCityDAO().getCity() as MutableList
            cityDataArrayList!!.add(0, city)


            Log.i("cityDataArrayList", cityDataArrayList?.size.toString())
            var cityData = arrayOfNulls<String>(cityDataArrayList!!.size)
            for (i in 0 until cityDataArrayList!!.size) {
                if (cityDataArrayList!![i].CityNameZawgyi == "" || cityDataArrayList!![i].CityNameZawgyi == "null") {
                    cityData[i] = cityDataArrayList!![i].CityNameEng
                } else {
                    cityData[i] = cityDataArrayList!![i].CityNameZawgyi
                }
            }

            uiThread {
                // Create an ArrayAdapter using a simple spinner layout and languages array
                val cityAdapter = ArrayAdapter(this@CreateNewScoutActivity, R.layout.spinner_item, cityData)
                // Set layout to use when the list of choices appear
                cityAdapter.setDropDownViewResource(R.layout.spinner_item)
                // Set Adapter to Spinner
                spinCity.adapter = cityAdapter


                for (i in 0 until cityDataArrayList!!.size) {
                    if (intent.getStringExtra(Constants.key_is_edit) != null) {
                        is_edit = intent.getStringExtra(Constants.key_is_edit)
                        if (is_edit == "yes") {
                            objectScout = intent.getSerializableExtra(Constants.key_object) as ScoutObject
                            if (cityDataArrayList!![i].CityID == objectScout.cityID) {
                                spinCity.setSelection(i)
                                break
                            }
                        }
                    } else {
                        var lastCityID = SettingPreference.getDataFromSharedPref(
                            Constants.key_last_city,
                            this@CreateNewScoutActivity
                        ).toString()
                        if (lastCityID != "") {
                            if (lastCityID == cityDataArrayList!![i].CityID) {
                                spinCity.setSelection(i)
                                break
                            }
                        }
                    }
                }
                // <<
            }


            //Township >>
            //val townshipData = arrayOf("Latha", "Insein", "Tamwe", "Hlaing", "Hlaing Tharyar", "Kyeemyindaing", "Bahan")
            var township = Township()
            township.TownshipID = ""
            township.TownshipNameEng = ""
            township.TownshipNameZawgyi = "ၿမိဳ႕နယ္ေရြးပါ"
            township.TownshipNameUnicode = ""

            townshipDataArrayList =
                database.getTownshipDAO().getTownshipByCityID(cityDataArrayList!![spinCity.selectedItemPosition].CityID) as MutableList<Township>

            townshipDataArrayList!!.add(0, township)
            Log.i("townshipDataArrayList", townshipDataArrayList!!.size.toString())
            var townshipData = arrayOfNulls<String>(townshipDataArrayList!!.size)
            for (i in 0 until townshipDataArrayList!!.size) {
                if (townshipDataArrayList!![i].TownshipNameZawgyi == "") {
                    townshipData[i] = townshipDataArrayList!![i].TownshipNameEng
                } else {
                    townshipData[i] = townshipDataArrayList!![i].TownshipNameZawgyi
                }
            }

            uiThread {
                // Create an ArrayAdapter using a simple spinner layout and languages array
                val townshipAdapter = ArrayAdapter(this@CreateNewScoutActivity, R.layout.spinner_item, townshipData)
                // Set layout to use when the list of choices appear
                townshipAdapter.setDropDownViewResource(R.layout.spinner_item)
                // Set Adapter to Spinner
                spinTownship.adapter = townshipAdapter

                // <<
            }

        }

        spinCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                spinTownship.setSelection(0)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                if (spinCity.selectedItem.toString() == "ၿမိဳ႕ေရြးပါ") {
                    spinTownship.setSelection(0)
                } else {

                    doAsync {
                        val database = AppDatabase.getInstance(this@CreateNewScoutActivity)
                        townshipDataArrayList =
                            database.getTownshipDAO().getTownshipByCityID(cityDataArrayList!![position].CityID) as MutableList<Township>

                        Log.i("townshipDataArrayList", townshipDataArrayList!!.size.toString())
                        var townshipData = arrayOfNulls<String>(townshipDataArrayList!!.size)
                        for (i in 0 until townshipDataArrayList!!.size) {
                            Log.i(
                                "EditScoutNavActivity",
                                "NameZgTownship" + townshipDataArrayList!![i].TownshipNameZawgyi
                            )
                            if (townshipDataArrayList!![i].TownshipNameZawgyi == "" || townshipDataArrayList!![i].TownshipNameZawgyi == "null") {
                                townshipData[i] = townshipDataArrayList!![i].TownshipNameEng
                            } else {
                                townshipData[i] = townshipDataArrayList!![i].TownshipNameZawgyi
                            }
                        }

                        uiThread {
                            // Create an ArrayAdapter using a simple spinner layout and languages array
                            val townshipAdapter =
                                ArrayAdapter(this@CreateNewScoutActivity, R.layout.spinner_item, townshipData)
                            // Set Adapter to Spinner
                            spinTownship.adapter = townshipAdapter
                            // Set Adapter to Spinner
                            townshipAdapter.notifyDataSetChanged()
                        }

                        val last_township_id = SettingPreference.getDataFromSharedPref(
                            Constants.key_last_township,
                            this@CreateNewScoutActivity
                        )
                        if (!last_township_id.equals("")) {
                            for (j in 0 until townshipDataArrayList!!.size) {
                                if (last_township_id == townshipDataArrayList!![j].TownshipID) {
                                    uiThread {
                                        spinTownship.setSelection(j)
                                    }
                                }
                            }
                        }

                        if (intent.getStringExtra(Constants.key_is_edit) != null) {
                            if (intent.getStringExtra(Constants.key_is_edit) == "yes") {
                                for (i in 0 until townshipDataArrayList!!.size) {
                                    if (townshipDataArrayList!![i].TownshipID == objectScout.townshipID) {
                                        Log.i("t_name_spin", townshipDataArrayList!![i].TownshipNameEng)
                                        Log.i("t_id_spin", townshipDataArrayList!![i].TownshipID)
                                        Log.i("t_id_db", objectScout.townshipID)
                                        uiThread {
                                            spinTownship.setSelection(i)
                                        }
                                    }
                                }
                            }
                        }
                    }//end doAsync
                } //end else
            }//end ItemSelected
        }

        var lastCityID = SettingPreference.getDataFromSharedPref(Constants.key_last_city, this).toString()
        var lastTownshipID = SettingPreference.getDataFromSharedPref(Constants.key_last_township, this).toString()

        /*doAsync {
            val database = AppDatabase.getInstance(context = this@CreateNewScoutActivity)
            cityDataArrayList = database.getCityDAO().getCity() as MutableList
            Log.d(TAG, "cityDataArrayList ${cityDataArrayList!!.size}")

            if (lastCityID != "") {
                for (i in 0 until cityDataArrayList!!.size) {
                    if (lastCityID == cityDataArrayList!![i].CityID) {
                        uiThread {
                            spinCity.setSelection(i)
                        }
                    }
                }
            }


            townshipDataArrayList = database.getTownshipDAO().getTownship() as MutableList<Township>
            if (lastTownshipID != "") {
                for (i in 0 until townshipDataArrayList!!.size) {
                    if (lastTownshipID == townshipDataArrayList!![i].TownshipID) {
                        uiThread {
                            spinTownship.setSelection(i)
                        }

                    }
                }
            }

        }*/

    }

    private fun setDatePickerForEditText(editText: EditText) {

        val langPref = "Language"
        val prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE)
        val language = prefs.getString(langPref, "")
        if (!language.equals("en")) {
            changeLang("en")
        }

        val c = Calendar.getInstance()
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat)
        val dateSetListener = DatePickerDialog.OnDateSetListener { it, year, monthOfYear, dayOfMonth ->
            //            val cal = Calendar.getInstance()
            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, monthOfYear)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            editText.setText(sdf.format(c.time))

            selectedDate = defaultDateTimeFormat.format(c.time)
            Log.i("selectedDate", "$selectedDate check")

            changeLang(language!!)
        }

        val dpd = DatePickerDialog(
            this,
            dateSetListener,
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )
        editText.setText(sdf.format(c.time))
        editText.setOnClickListener {
            dpd.show()
        }
        selectedDate = defaultDateTimeFormat.format(c.time)
        Log.i("selectedDate1", selectedDate)
    }

    //Image Capture from camera app >>
    private fun takePicture() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        MY_PERMISSIONS_REQUEST_CAMERA
                    )
                    // MY_PERMISSIONS_REQUEST_CAMERA is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                //this code is ok for bitmap return >>

                dispatchTakePictureIntent()
            }
        } else {
            dispatchTakePictureIntent()
        }
    }

    //Image Capture from camera app from google doc >>
    @SuppressLint("LongLogTag")
    private fun dispatchTakePictureIntent() {
        val takePictureIntent: Intent

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            takePictureIntent = Intent("android.media.action.IMAGE_CAPTURE")
        else
            takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        takePictureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            var photoFileReal: File? = null
            try {

                photoFile = createImageFileForTemporary()
                //photoFileReal = createImageFileForReal(timeStamp)
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI: Uri

                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    photoURI = Uri.fromFile(photoFile)
                } else {
                    photoURI = FileProvider.getUriForFile(
                        this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile
                    )
                }
                Log.i("photoURI", photoURI.toString())
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureIntent.putExtra("return-data", true)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)

                capturedImages.add(photoFile)
                capturedImageURIs.add(photoURI)
                capturedImageAbsPath.add(photoFile.absolutePath)

                capturedImagesReal.add(photoFile)
                capturedImageURIsReal.add(photoURI)
                capturedImageAbsPathReal.add(photoFile.absolutePath)

                Log.i("capturedImageAbsPath_s", capturedImageAbsPath.size.toString())
                Log.i("capturedImageAbsPathReal_s", capturedImageAbsPathReal.size.toString())
            }

        }
    }

    @Throws(IOException::class)
    private fun createImageFileForTemporary(): File {
        // Create an image file name
        timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "MktScout_Temp_$timeStamp"
        Log.i("imageFileName", imageFileName)
        capturedImageNamePath.add(imageFileName)
        val storageDir = File(tempDirPath)
        storageDir.mkdirs()
        Log.i("storageDir", storageDir.absolutePath)
//        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File("$storageDir/$imageFileName.jpg")

        Log.i("imagePathTemp", image.absolutePath)

        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = image.absolutePath
        return image
    }

    @Throws(IOException::class)
    private fun createImageFileForReal(timeStamp: String): File {
        // Create an image file name

        val imageFileName = "MktScout_Temp_$timeStamp"
        Log.i("imageFileName", imageFileName)
        capturedImageNamePath.add(imageFileName)
        val storageDir = File(scoutImgDirPath)
        storageDir.mkdirs()
//        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File("$storageDir/$imageFileName.jpg")

        Log.i("imagePathReal", image.absolutePath)

        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = image.absolutePath
        return image
    }

    //Image Capture from camera app from google doc <<

    //MapView >>
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setUpMap()
    }

    private fun setUpMap() {
        mMap.uiSettings.isZoomControlsEnabled = true

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_GPS_LOCATION
            )
            return
        }

        mMap.isMyLocationEnabled = true

        var lm = getSystemService(LOCATION_SERVICE) as LocationManager

        //Get the best provider >>
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_COARSE
        criteria.isCostAllowed = false
        val provider = lm.getBestProvider(criteria, false)
        // <<

//        var provider = LocationManager.GPS_PROVIDER
        var location = lm.getLastKnownLocation(provider)
        if (location != null) {
            // add location to the location listener for location changes
            //var test = "Prov : " + location!!.provider + " Lat : " + location!!.latitude + " Lon : " + location!!.longitude

            Log.i("location is", " not null")

            var currentLatitude = 0.0
            var currentLongitude = 0.0

            if (intent.getStringExtra(Constants.key_is_edit) != null) {
                is_edit = intent.getStringExtra(Constants.key_is_edit)
                if (is_edit.equals("yes")) {
                    Log.i("is_edit_map", "yes")
                    Log.i("pickupLat", pickupLat.toString())
                    currentLatitude = pickupLat
                    currentLongitude = pickupLng
                } else {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                }
            }

            val latLng = LatLng(currentLatitude, currentLongitude)
            mMap.addMarker(MarkerOptions().position(latLng).title("Pickup Location"))

            val zoomLevel = 16.0f //This goes up to 21
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
        }
    }
    // <<

    //Location >>
    private fun checkGpsLocation(){
        //Check permission for gps
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
//            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
//                    LocationService.MY_PERMISSION_ACCESS_COURSE_LOCATION );
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_GPS_LOCATION
                )

                // MY_PERMISSIONS_REQUEST_GPS_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted[
//            val lm = getSystemService(LOCATION_SERVICE) as LocationManager
//            val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//            txtGpsLat.text = location.longitude.toString()
//            txtGpsLng.text = location.latitude.toString()
//            txtGpsProvider.text = location.provider


            var lm = getSystemService(LOCATION_SERVICE) as LocationManager
            var provider = LocationManager.GPS_PROVIDER
//            var provider = LocationManager.NETWORK_PROVIDER
            lm?.requestLocationUpdates(provider, 500L, 1f, this)//add listener
            var location = lm.getLastKnownLocation(provider)
            if (location != null) {
                // add location to the location listener for location changes
                onLocationChanged(location)
            }

            provider = LocationManager.NETWORK_PROVIDER
            lm?.requestLocationUpdates(provider, 500L, 1f, this)//add listener
            location = lm.getLastKnownLocation(provider)
            if (location != null) {
                // add location to the location listener for location changes
                onLocationChanged(location)
            }

//            provider = LocationManager.PASSIVE_PROVIDER
//            lm?.requestLocationUpdates(provider, 500L, 1f, this)//add listener
//            location = lm.getLastKnownLocation(provider)
//            if (location != null) {
//                // add location to the location listener for location changes
//                onLocationChanged(location)
//            }
        }
//        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
//        val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//        val longitude = location.longitude
//        val latitude = location.latitude
    }
    // <<

    //LocationListener >>
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_GPS_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    checkGpsLocation()
                    setUpMap()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    takePicture()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onLocationChanged(location: Location?) {
        // Do something with the location
        when (location!!.provider) {
            LocationManager.GPS_PROVIDER -> {
                gpsLat = location!!.latitude
                Log.i("gpsLat", gpsLat.toString());
                gpsLng = location!!.longitude
                txtGpsLat.text = gpsLat.toString()
                txtGpsLng.text = gpsLng.toString()
                txtGpsProvider.text = location!!.provider
            }
            LocationManager.NETWORK_PROVIDER -> {
                cellLat = location!!.latitude
                Log.i("cellLat", cellLat.toString())
                cellLng = location!!.longitude
                txtNetworkLat.text = cellLat.toString()
                txtNetworkLng.text = cellLng.toString()
                txtNetworkProvider.text = location!!.provider
            }
            else -> {
                etxtPassiveLat.setText(location!!.latitude.toString())
                etxtPassiveLng.setText(location!!.latitude.toString())
                txtPassiveProvider.text = location!!.provider
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.i(
            "onStatusChanged: ",
            "Do something with the status: $status"
        )
    }

    override fun onProviderEnabled(provider: String?) {
        Log.i(
            "onProviderEnabled: ",
            "Do something with the provider: $provider"
        )
    }

    override fun onProviderDisabled(provider: String?) {
        Log.i(
            "onProviderDisabled: ",
            "Do something with Provider: $provider"
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_CUSTOM_GPS && resultCode == Activity.RESULT_OK) {
            //val  strEditText = data.getStringExtra("editTextView") as String
            if (data == null) return

            val resultLat: Double = data.getDoubleExtra("pickupLat", 0.0)
            val resultLng: Double = data.getDoubleExtra("pickupLng", 0.0)
            val gpsLat: Double = data.getDoubleExtra("gpsLat", 0.0)
            val gpsLng: Double = data.getDoubleExtra("gpsLng", 0.0)

            if (resultLat == 0.0 || resultLng == 0.0)
                return

            pickupLat = resultLat
            pickupLng = resultLng
            this.gpsLat = gpsLat
            this.gpsLng = gpsLng

            Log.i("pickupLat", pickupLat.toString())
            Log.i("pickupLng", pickupLng.toString())
            Log.i("gpsLat", gpsLat.toString())
            Log.i("gpsLng", gpsLng.toString())

            etxtPassiveLat.setText(pickupLat.toString())
            etxtPassiveLng.setText(pickupLng.toString())
            txtPassiveProvider.text = "Custom Choice"

            // Add a user choice loc marker and move the camera
            mMap.clear()
            val piclupLoc = LatLng(pickupLat, pickupLng)
            mMap.addMarker(MarkerOptions().position(piclupLoc).title("Pickup Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(piclupLoc))
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            //Build.VERSION_CODES.N
            val captureCount = capturedImages.count()
            if (captureCount > 0) btnAddImage.visibility = View.GONE
            else btnAddImage.visibility = View.VISIBLE

            val currentIndex = captureCount - 1
            val imgFile: File = capturedImages.lastElement()
            val imgFileReal: File = capturedImagesReal.lastElement()

            val bitmap = BitmapFactory.decodeFile(capturedImageAbsPath.lastElement())

            val imageView = ImageView(this)
            rotateImage(imageView, bitmap, currentIndex, 900)

            //val bmpOut = resizeAndOverwriteImage(imgFile, 900)// I reduce the image size for avoid slow.
            //Log.i("currentIndex", currentIndex.toString())
            //addImageInLayout(bmpOut, currentIndex)

            //add bytearrayString from bitmap to array
            var imageString = BitmapUtility.newInstance().convetStringFromBitmap(capturedImageAbsPath.lastElement())
            Log.i("imageString", imageString)
            capturedImageDataStringPath.add(imageString)

            if (is_edit == "yes") {

                doAsync {
                    val database = AppDatabase.getInstance(this@CreateNewScoutActivity)
                    val calendar = Calendar.getInstance()
                    val todayDate = defaultDateTimeFormat.format(calendar.time)

                    val imageScout = ImageScout()
                    imageScout.ImageID = UUID.randomUUID().toString()
                    Log.i("img_name", capturedImageNamePath.last())
                    imageScout.ImageName = capturedImageNamePath.last()
                    imageScout.ScoutID = objectScout.scoutID
                    imageScout.ImagePath = capturedImageAbsPathReal.lastElement()
                    imageScout.ImageData = capturedImageDataStringPath.last()
                    imageScout.Active = "1"
                    imageScout.CreatedBy = user_id
                    imageScout.ModifiedBy = user_id
                    imageScout.CreatedOn = todayDate
                    imageScout.ModifiedOn = todayDate
                    imageScout.LastAction = ""

                    database.getImageScoutDAO().insert(imageScout)
                }
            }

            svEditScout.scrollTo(0, svEditScout.bottom)//move scrollView to bottom
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("LongLogTag")
    private fun addImageInLayout(bitmapImg: Bitmap, index: Int) {
        val btnDelH = (15 * imgLayoutHeight) / 100//20% of LinearLayout
        val spaceH = 8//(5 * imgLayoutHeight)/100

        var imgOrgW = 0
        var imgOrgH = 0

        imgOrgW = bitmapImg.width
        imgOrgH = bitmapImg.height

        Log.i("imgOrgW", imgOrgW.toString())
        Log.i("imgOrgH", imgOrgH.toString())

        var resizeH = imgLayoutHeight - (btnDelH + spaceH)
        var resizeW = (imgOrgW * resizeH) / imgOrgH

        Log.i("resizeH", resizeH.toString())
        Log.i("resizeW", resizeW.toString())

        //Create ImageView
        val imgVCaptured = ImageView(this)
        imgVCaptured.layoutParams = LinearLayout.LayoutParams(resizeW, resizeH)
        Log.i("bitmap", bitmapImg.toString())
        imgVCaptured.setImageBitmap(bitmapImg)

        deleteFlagPref("no")

        getCorrectCameraOrientation()

        //Create Delete Button with text
        val btnDel = TextView(this, null, R.style.TextViewDefaultStyle)
        val btnDelParam: LinearLayout.LayoutParams
        btnDelParam = LinearLayout.LayoutParams(resizeW, btnDelH)
        /*if (rotate == 90) {
            btnDelParam = LinearLayout.LayoutParams(resizeH, btnDelH)
        }
        else {
            btnDelParam = LinearLayout.LayoutParams(resizeW, btnDelH)
        }*/
        btnDelParam.setMargins(0, spaceH, 0, 0)
        btnDel.layoutParams = btnDelParam
        btnDel.background = ContextCompat.getDrawable(this, R.drawable.rounded_button_del)
        btnDel.gravity = Gravity.CENTER
        btnDel.isClickable = true
        btnDel.text = getString(R.string.delete)
        btnDel.setTextColor(Color.parseColor("#ffffff"))
        btnDel.tag = capturedImageAbsPath[index]

        //Create Vertical Linear Layout to add Image and Delete Button
        val linearLayout = LinearLayout(this)
        val param =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
        param.setMargins(8, 0, 8, 0)
        linearLayout.layoutParams = param
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.addView(imgVCaptured)
        linearLayout.addView(btnDel)

        //Add Linear Layout to Image Scrow view.
        llImgs.addView(linearLayout)


        btnDel.setOnClickListener {
            val delIndex = capturedImageAbsPathReal.indexOf(it.tag)

            Log.i("capturedImageAbsPath_s", capturedImageAbsPath.size.toString())
            Log.i("capturedImageAbsPathReal_s", capturedImageAbsPathReal.size.toString())

            deletedImagePath.add(capturedImageAbsPathReal.get(delIndex))//save path before delete

            //delete func isn't for temp folder
            if (!is_edit.equals("yes")) {
                capturedImagesReal.removeElementAt(delIndex)
                capturedImageURIsReal.removeElementAt(delIndex)
                capturedImageAbsPathReal.removeElementAt(delIndex)
                capturedImageDataStringPath.removeAt(delIndex)//need to check later
                capturedImageNamePath.removeAt(delIndex)
            }

            llImgs.removeView(linearLayout)

            //Check need to show capture img button >>
            val captureCount = capturedImages.count()
            if (captureCount > 0) btnAddImage.visibility = View.GONE
            else btnAddImage.visibility = View.VISIBLE
            // <<

            saveFlagPref("no")
            Log.i("button", "del")
            if (is_edit.equals("yes")) {

                doAsync {
                    val database = AppDatabase.getInstance(this@CreateNewScoutActivity)

                    for (i in 0 until deletedImagePath.size) {
                        database.getImageScoutDAO().changeStatusForDelete(deletedImagePath[i])
                    }
                }

            }
            deleteFlagPref("yes")
        }
    }

    private fun resizeAndOverwriteImage(imgFile: File, maxWH: Int): Bitmap {
        val bmpOut = resizeImage(imgFile, maxWH)

        try {
            val fOutputStream = FileOutputStream(imgFile)
            bmpOut.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream)
            fOutputStream.flush()
            fOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bmpOut
    }

    private fun resizeImage(imgFile: File, maxWH: Int): Bitmap {
        val bmp = BitmapFactory.decodeFile(capturedImageAbsPath.lastElement())
        val imgOrgW = bmp.width
        val imgOrgH = bmp.height
        var resizeW = maxWH
        var resizeH = maxWH
        if (imgOrgW > imgOrgH) { // reduce base on w
            resizeH = (imgOrgH * resizeW) / imgOrgW
        } else {// reduce base on h
            resizeW = (imgOrgW * resizeH) / imgOrgH
        }

        return Bitmap.createScaledBitmap(bmp, resizeW, resizeH, false)
    }

    @SuppressLint("NewApi")
    fun rotateImage(imgVCaptured: ImageView, bitmapImg: Bitmap, currentIndex: Int, maxWH: Int) {
        var rotate = 0
        Log.i("imgPath", capturedImageAbsPathReal.lastElement())
        val imgFile = File(capturedImageAbsPathReal.lastElement())
        val exif = ExifInterface(imgFile.absolutePath)
        var orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        Log.i("orientation", orientation.toString())

        //Log.i("pot_or_land", )

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                rotate = 270
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                rotate = 180
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                rotate = 90
            }
        }
        Log.i("rotate", rotate.toString())

        //****** Image rotation ******//
        var matrix = Matrix()
        matrix.postRotate(rotate.toFloat())
        var bitmapImgRot = Bitmap.createBitmap(bitmapImg, 0, 0, bitmapImg.width, bitmapImg.height, matrix, true)
        /*imgVCaptured.setImageBitmap(bitmapImgRot)
        imgVCaptured.scaleType = ImageView.ScaleType.FIT_XY*/

        //val bmpOut = Bitmap.createScaledBitmap(bitmapImgRot, (bitmapImgRot.getWidth() / 10), (bitmapImgRot.getHeight() / 10), false)

        val imgOrgW = bitmapImgRot.width
        val imgOrgH = bitmapImgRot.height
        var resizeW = maxWH
        var resizeH = maxWH
        if (imgOrgW > imgOrgH) { // reduce base on w
            resizeH = (imgOrgH * resizeW) / imgOrgW
        } else {// reduce base on h
            resizeW = (imgOrgW * resizeH) / imgOrgH
        }

        val bmpOut = Bitmap.createScaledBitmap(bitmapImgRot, resizeW, resizeH, false)

        addImageInLayout(bmpOut, currentIndex)

        try {
            val fOutputStream = FileOutputStream(imgFile)
            bmpOut.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream)
            fOutputStream.flush()
            fOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCorrectCameraOrientation() {
        val rotation = windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        Log.i("rotation", rotation.toString())
    }

    private fun validate(): Boolean {
        if (spinScoutType.selectedItem.toString() == "အမ်ိဳးအစားေရြးပါ") {
            showWarningDialogForSpinner("ScoutType")
            return false
        }
        if (spinCity.selectedItem.toString() == "ၿမိဳ႕ေရြးပါ") {
            showWarningDialogForSpinner("City")
            return false
        }
        if (spinTownship.selectedItem.toString() == "ၿမိဳ႕နယ္ေရြးပါ") {
            showWarningDialogForSpinner("Township")
            return false
        }
        if (etxtScoutNo.text.isEmpty()) {
            etxtScoutOn.requestFocus()
            etxtScoutOn.error = "Please enter this field."
            return false
        }
        if (acTxtCompanyName.text.isEmpty()) {
            acTxtCompanyName.requestFocus()
            acTxtCompanyName.error = "Please enter this field."
            return false
        }
        return true
    }

    fun showWarningDialogForSpinner(str: String) {
        var dialog = MaterialDialog.Builder(this)
            .title("Warning!")
            .content("Need to select $str")
            .onPositive { dialog, which ->

            }
            .positiveText(getString(R.string.ok))
            .show()

        var title = dialog.titleView
        title.setTypeface(font)

        var message = dialog.contentView
        message?.setTypeface(font)
    }

    fun uploadData() {
//        var uploadData = UploadData(this@CreateNewScoutActivity, applicationContext, user_id, font!!, "yes")
//        uploadData.upload()
    }

    private fun goToList() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Constants.key_go_to, Constants.key_new_sacout)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    fun changeLang(lang: String) {
        if (lang.equals("", ignoreCase = true))
            return
        myLocale = Locale(lang)
        Locale.setDefault(myLocale)
        val config = android.content.res.Configuration()
        config.locale = myLocale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        if (myLocale != null) {
            newConfig.locale = myLocale
            Locale.setDefault(myLocale)
            baseContext.resources.updateConfiguration(newConfig, baseContext.resources.displayMetrics)
        }
    }

}
