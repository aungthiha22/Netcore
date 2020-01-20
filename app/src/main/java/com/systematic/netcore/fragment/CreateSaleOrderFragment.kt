package com.systematic.netcore.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.location.Criteria
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.systematic.netcore.BuildConfig
import com.systematic.netcore.R
import com.systematic.netcore.activity.MapChoice
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.objects.KeyPairForCustomer
import com.systematic.netcore.objects.KeyPairForProduct
import com.systematic.netcore.objects.KeyPairForProductGroup
import com.systematic.netcore.utility.BitmapUtility
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import kotlinx.android.synthetic.main.activity_installation_detail.*
import kotlinx.android.synthetic.main.fragment_create_sale_order.*
import kotlinx.android.synthetic.main.fragment_create_sale_order.btnAddImage
import kotlinx.android.synthetic.main.fragment_create_sale_order.llBtnGetPicture
import kotlinx.android.synthetic.main.fragment_create_sale_order.llImgs
import kotlinx.android.synthetic.main.fragment_create_sale_order.spCustomerName
import kotlinx.android.synthetic.main.fragment_create_sale_order.spProduct
import kotlinx.android.synthetic.main.fragment_create_sale_order.spProductGroup
import kotlinx.android.synthetic.main.fragment_create_sale_order.svEditScout
import kotlinx.android.synthetic.main.fragment_create_sale_order.svhImages
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.jetbrains.anko.windowManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CreateSaleOrderFragment : Fragment(), View.OnClickListener, OnMapReadyCallback {

    var TAG = "CreateSaleOrderFragment"

    private var cusName = "Please Select"
    private var prodGroupName = "Please Select"
    private var prodName = "Please Select"
    private var saveCustomerStatus = "New"
    private var selectCustomerID = ""
    private var selectProductGroupID = ""
    private var selectProductID = ""
    private var keyPairForCustomerList = ArrayList<KeyPairForCustomer>()
    private var keyPairForProductGroupList = ArrayList<KeyPairForProductGroup>()
    private var keyPairForProductList = ArrayList<KeyPairForProduct>()

    var is_edit = ""
    var selectedDate = ""

    //LocationManager >>
    private val MY_PERMISSIONS_REQUEST_GPS_LOCATION = 1
    private val MY_PERMISSIONS_REQUEST_CAMERA = 2

    private val REQUEST_TAKE_CUSTOM_GPS = 1
    private val REQUEST_TAKE_PHOTO = 2

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

    var timeStamp = ""
    private lateinit var tempDirPath: String
    private lateinit var saleOrderImgDirPath: String
    private var displayW: Int = 0
    private var displayH: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_sale_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        switchNewOrExistCustomer.setOnClickListener(this)
        llBtnGetPicture.setOnClickListener(this)
        llBtnGetLoc.setOnClickListener(this)
        txBtnSave.setOnClickListener(this)

        getDataFromDb()
        switchForChooseCustomer()
        loadViewWidthAndHeight()
        loadMapFragment()
        loadImagesPath()
        measureDynamicHeight()

        setMultiSpinnerForProductGroup()
    }

    override fun onClick(view: View?) {
        when (view) {
            llBtnGetPicture -> takePicture()

            llBtnGetLoc -> {
                val intent = Intent(activity!!, MapChoice::class.java)
                Log.i("is_edit", is_edit)
                /*if (intent.getStringExtra(Constant.key_is_edit) != null) {
                    is_edit = intent.getStringExtra(Constant.key_is_edit)*/
                if (is_edit == "yes") {
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
            txBtnSave ->{
                Toast.makeText(context!! , "Button : ",Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun getDataFromDb() {
        doAsync {
            val database = AppDatabase.getInstance(context = activity!!)
            val customerList = database.getCustomerDAO().getCustomer()
            for (i in 0 until customerList.size) {
                val keyPairForCustomer = KeyPairForCustomer()
                keyPairForCustomer.id = customerList[i].CustomerID
                keyPairForCustomer.name = customerList[i].CustomerNameEng
                keyPairForCustomer.email = customerList[i].Email
                keyPairForCustomer.phoneNo = customerList[i].PhoneNo
                keyPairForCustomer.address = customerList[i].Address

                keyPairForCustomerList.add(keyPairForCustomer)
            }
        }
    }

    private fun switchForChooseCustomer() {
        switchNewOrExistCustomer.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
            if (!isChecked) {

                saveCustomerStatus = "New"

                llExistCustomer.visibility = View.GONE
                llNewCustomer.visibility = View.VISIBLE

            } else {
                saveCustomerStatus = ""

                llExistCustomer.visibility = View.VISIBLE
                llNewCustomer.visibility = View.GONE

                etNewCustomerName.setText("")
                etNewCustomerEmail.setText("")
                etNewCustomerPhoneNo.setText("")
                etNewCustomerAddress.setText("")

//                if (receiptIdFromPref.isNotEmpty()){
//                    cusName = tempForCustomerName
//                }
//                else {
//                    cusName = "Please Select"
//                }

                setMultiSpinnerForCustomer()
            }
        })
    }

    private fun setMultiSpinnerForCustomer() {
        spCustomerName.setDataForCustomer(keyPairForCustomerList, cusName, -1) { items ->
            selectCustomerID = SettingPreference.getDataFromSharedPref(Constants.keyCustomerID, activity!!).toString()
            Log.d(TAG, "selectCustomerID $selectCustomerID")
        }
    }

    private fun setMultiSpinnerForProductGroup() {
        doAsync {
            val database = AppDatabase.getInstance(context = activity!!)

//            val productList = database.getProductDAO().getProductById(productID)
//            if (productList.isNotEmpty()) {
//                val productGroupList = database.getProductGroupDAO().getProductGroupById(productList[0].ProductGroupID)
//                for (i in 0 until productGroupList.size) {
//                    prodGroupName = productGroupList[i].ProductGroupName
//                }
//            }

            activity!!.runOnUiThread {
                setMultiSpinnerForProduct()

                spProductGroup.setDataForProductGroup(keyPairForProductGroupList, prodGroupName, -1) { items ->
                    selectProductGroupID = SettingPreference.getDataFromSharedPref(
                        Constants.keyProductGroupID,
                        activity!!
                    ).toString()
                    Log.d(TAG, "selectProductGroupID $selectProductGroupID")

                    doAsync {
                        val database = AppDatabase.getInstance(context = activity!!)
                        val productList = database.getProductDAO().getProductByGroupId(selectProductGroupID)
                        Log.i(TAG, "productList = ${productList.size}")
                        for (i in 0 until productList.size) {
                            val keyPairForProduct = KeyPairForProduct()
                            keyPairForProduct.id = productList[i].ProductID
                            keyPairForProduct.name = productList[i].ProductName

                            keyPairForProductList.add(keyPairForProduct)
                        }

                        uiThread {
                            if (keyPairForProductList.isNotEmpty()) {
                                prodName = keyPairForProductList[0].name
                                selectProductID = keyPairForProductList[0].id

                                SettingPreference.putDataToSharefPref(
                                    Constants.keyProductID,
                                    selectProductID,
                                    activity!!
                                ).toString()

                            } else {
                                prodName = "Please Select"
                            }
                            setMultiSpinnerForProduct()
                        }
                    }
                }
            }
        }

    }

    private fun setMultiSpinnerForProduct() {

//        doAsync {
//            val database = AppDatabase.getInstance(context = activity!!)
//
//            val productList = database.getProductDAO().getProductById(productID)
//            for (i in 0 until productList.size) {
//                prodName = productList[i].ProductName
//
//                uiThread {
//                    etInstallationFee.setText(productList[i].InstallationFees)
//                    etCost.setText(productList[i].Cost)
//                }
//
//                val productGroupList = database.getProductGroupDAO().getProductGroupById(productList[i].ProductGroupID)
//                for (i in 0 until productGroupList.size) {
//                    prodGroupName = productGroupList[i].ProductGroupName
//                }
//            }
//
//        }
        spProduct.setDataForProduct(keyPairForProductList, prodName, -1) { items ->
            selectProductID = SettingPreference.getDataFromSharedPref(Constants.keyProductID, activity!!).toString()
            Log.d(TAG, "selectProductID $selectProductID")
        }

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        setUpMap()
    }

    private fun setUpMap() {
        mMap.uiSettings.isZoomControlsEnabled = true

        if (ActivityCompat.checkSelfPermission(
                context!!,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_GPS_LOCATION
            )
            return
        }

        mMap.isMyLocationEnabled = true

        var lm = activity!!.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

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

            if (activity!!.intent.getStringExtra(Constants.key_is_edit) != null) {
                is_edit = activity!!.intent.getStringExtra(Constants.key_is_edit)
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

    private fun loadMapFragment() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            (childFragmentManager.findFragmentById(R.id.ShowNearestLocationMapViewMapFrag) as SupportMapFragment)
        mapFragment.getMapAsync(this)
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

            val imageView = ImageView(context!!)
            rotateImage(imageView, bitmap, currentIndex, 900)

            //val bmpOut = resizeAndOverwriteImage(imgFile, 900)// I reduce the image size for avoid slow.
            //Log.i("currentIndex", currentIndex.toString())
            //addImageInLayout(bmpOut, currentIndex)

            //add bytearrayString from bitmap to array
            var imageString = BitmapUtility.newInstance().convetStringFromBitmap(capturedImageAbsPath.lastElement())
            Log.i("imageString", imageString)
            capturedImageDataStringPath.add(imageString)

//            if (is_edit == "yes") {
//
//                doAsync {
//                    val database = AppDatabase.getInstance(this@CreateNewScoutActivity)
//                    val calendar = Calendar.getInstance()
//                    val todayDate = defaultDateTimeFormat.format(calendar.time)
//
//                    val imageScout = ImageScout()
//                    imageScout.ImageID = UUID.randomUUID().toString()
//                    Log.i("img_name", capturedImageNamePath.last())
//                    imageScout.ImageName = capturedImageNamePath.last()
//                    imageScout.ScoutID = objectScout.scoutID
//                    imageScout.ImagePath = capturedImageAbsPathReal.lastElement()
//                    imageScout.ImageData = capturedImageDataStringPath.last()
//                    imageScout.Active = "1"
//                    imageScout.CreatedBy = user_id
//                    imageScout.ModifiedBy = user_id
//                    imageScout.CreatedOn = todayDate
//                    imageScout.ModifiedOn = todayDate
//                    imageScout.LastAction = ""
//
//                    database.getImageScoutDAO().insert(imageScout)
//                }
//            }

            svEditScout.scrollTo(0, svEditScout.bottom)//move scrollView to bottom
        }
    }

    private fun loadImagesPath() {
        val appPicturesDir = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
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
        saleOrderImgDirPath = "$appPicturesDir/ScoutImages"

//        val absPath = "/storage/emulated/0/Android/data/com.systematic_solution.marketing_scout/files/Pictures/aa.jpg"
//        val absPath = "/storage/emulated/0/Android/data/com.systematic_solution.marketing_scout/files/Pictures/bb.jpg"
//        val absPath = "/storage/emulated/0/Android/data/com.systematic_solution.marketing_scout/files/Pictures/ImgFromHuawei.jpg"
//        val absPath = "/storage/emulated/0/Android/data/com.systematic_solution.marketing_scout/files/Pictures/ImgFromHuawei_50R.jpg"
//        val myBitmap = BitmapFactory.decodeFile(absPath)
//        imgV.setImageBitmap(myBitmap)
    }

    private fun loadViewWidthAndHeight() {
        val displayMetrics = DisplayMetrics()
        context!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayW = displayMetrics.heightPixels
        displayH = displayMetrics.widthPixels
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
                activity!!
            )

            val param = LinearLayout.LayoutParams(svhImages.measuredWidth, LinearLayout.LayoutParams.MATCH_PARENT)
            btnAddImage.layoutParams = param
        }
    }

    //Image Capture from camera app >>
    private fun takePicture() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(context!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.CAMERA)) {

                } else {
                    ActivityCompat.requestPermissions(
                        activity!!,
                        arrayOf(Manifest.permission.CAMERA),
                        MY_PERMISSIONS_REQUEST_CAMERA
                    )
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
        if (takePictureIntent.resolveActivity(context!!.packageManager) != null) {
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
                        context!!,
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
        val imgVCaptured = ImageView(context!!)
        imgVCaptured.layoutParams = LinearLayout.LayoutParams(resizeW, resizeH)
        Log.i("bitmap", bitmapImg.toString())
        imgVCaptured.setImageBitmap(bitmapImg)
        getCorrectCameraOrientation()

        //Create Delete Button with text
        val btnDel = TextView(context!!, null, R.style.TextViewDefaultStyle)
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
        btnDel.background = ContextCompat.getDrawable(context!!, R.drawable.rounded_button_del)
        btnDel.gravity = Gravity.CENTER
        btnDel.isClickable = true
        btnDel.text = getString(R.string.delete)
        btnDel.setTextColor(Color.parseColor("#ffffff"))
        btnDel.tag = capturedImageAbsPath[index]

        //Create Vertical Linear Layout to add Image and Delete Button
        val linearLayout = LinearLayout(context!!)
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
//            if (!is_edit.equals("yes")) {
            capturedImagesReal.removeElementAt(delIndex)
            capturedImageURIsReal.removeElementAt(delIndex)
            capturedImageAbsPathReal.removeElementAt(delIndex)
            capturedImageDataStringPath.removeAt(delIndex)//need to check later
            capturedImageNamePath.removeAt(delIndex)
            // }

            llImgs.removeView(linearLayout)

            //Check need to show capture img button >>
            val captureCount = capturedImages.count()
            if (captureCount > 0) btnAddImage.visibility = View.GONE
            else btnAddImage.visibility = View.VISIBLE
        }
    }

    private fun getCorrectCameraOrientation() {
        val rotation = context!!.windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        Log.i("rotation", rotation.toString())
    }

}