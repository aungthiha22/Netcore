package com.systematic.netcore.activity

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.systematic.netcore.R
import com.systematic.netcore.api.UploadInstallation
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.database.model.Installation
import com.systematic.netcore.objects.KeyPairForCustomer
import com.systematic.netcore.objects.KeyPairForEngineer
import com.systematic.netcore.objects.KeyPairForProduct
import com.systematic.netcore.objects.KeyPairForProductGroup
import com.systematic.netcore.utility.Common
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import kotlinx.android.synthetic.main.activity_installation_detail.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*

class InstallationDetailActivity : AppCompatActivity(), View.OnClickListener {
    var TAG = "InstallationDetailActivity"

    var strInstallationNo = ""
    var strInternetCode = ""
    var strInternetDevice = ""
    var strFiberCableLength = ""
    var strLossRemark = ""
    var strTax = ""
    var strDiscount = ""
    var strConfigFee = ""
    var strRouterFee = ""
    var strInstallationFee = ""
    var strQty = ""
    var strCost = ""
    var strTotalAmt = ""
    var strSpeed = ""
    var strSngps = ""
    var strFiberSwitchQty = ""
    var strDuration = ""

    var totalAmt = 0

    var prefixForInstallation = "INSA"
    var splitInstallation = "000000"

    var saleOrderID = ""
    var installationID = ""
    var productID = ""
    var productGroupID = ""
    var customerID = ""
    var customerName = ""
    var productName = ""
    var keySaleOrderDetails = ""

    var selectInstallationStatus = ""
    var selectedDateForStart = ""
    var selectedDateForEnd = ""
    private var selectCustomerID = ""
    private var selectEngineerID = ""
    private var selectProductGroupID = ""
    private var selectProductID = ""

    private var cusName = "Please Select"
    private var engineerName = "Please Select"
    private var prodGroupName = "Please Select"
    private var prodName = "Please Select"
    private var userName = ""
    private var userID = ""

    private val dateTimeFormatForDb = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss", Locale.US)

    private var keyPairForCustomerList = ArrayList<KeyPairForCustomer>()
    private var keyPairForEngineerList = ArrayList<KeyPairForEngineer>()
    private var keyPairForProductGroupList = ArrayList<KeyPairForProductGroup>()
    private var keyPairForProductList = ArrayList<KeyPairForProduct>()

    private var statusListString = arrayOfNulls<String>(5)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installation_detail)

        if (supportActionBar != null) {
            supportActionBar!!.title = "Installation"
            supportActionBar!!.setHomeAsUpIndicator(resources.getDrawable(R.drawable.ic_arrow_back))
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        userID = SettingPreference.getDataFromSharedPref(Constants.keyUserId, this).toString()
        userName = SettingPreference.getDataFromSharedPref(Constants.keyUserName, this).toString()

        if (intent.getStringExtra(Constants.keyCustomerID) != null && intent.getStringExtra(Constants.keyProductID) != null) {
            customerID = intent.getStringExtra(Constants.keyCustomerID)
            productID = intent.getStringExtra(Constants.keyProductID)
        }

        if (intent.getStringExtra(Constants.keySaleOrderID) != null && intent.getStringExtra(Constants.keyCustomerName) != null && intent.getStringExtra(
                Constants.key_go_to)!= null ) {
            saleOrderID = intent.getStringExtra(Constants.keySaleOrderID)
            customerName = intent.getStringExtra(Constants.keyCustomerName)
            keySaleOrderDetails = intent.getStringExtra(Constants.key_go_to)

            cusName = customerName
            selectCustomerID = customerID
        }

        //come from InstallationListFragment
        if (intent.getStringExtra(Constants.key_go_to) != null){
            if (intent.getStringExtra(Constants.key_go_to) == Constants.key_installation_details){
                installationID = intent.getStringExtra(Constants.keyInstallationID)
                customerID = intent.getStringExtra(Constants.keyCustomerID)
                productID = intent.getStringExtra(Constants.keyProductID)
                customerName = intent.getStringExtra(Constants.keyCustomerName)

                showInstallDataFromDb(installationID)

                cusName = customerName
                selectCustomerID = customerID
            }
        }

        Log.d(TAG, "customerID $customerID")
        Log.d(TAG, "productID $productID")


        showDatePickerDialog()
        showDatePickerDialogForEndDate()

        getDataFromDb()

        btnSave.setOnClickListener(this)

        setMultiSpinnerForCustomer()
        setMultiSpinnerForEngineer()
        setMultiSpinnerForProductGroup()
        setMultiSpinnerForProduct()
        bindDataFromSpinnerForInstallStatus()

        etQty.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                if (etQty.length() != 0) {
                    if (etInstallationFee.length() != 0 && etCost.length() != 0) {
                        val cost = Integer.parseInt(etQty.text.toString()) * Integer.parseInt(etCost.text.toString())
                        totalAmt = cost + Integer.parseInt(etInstallationFee.text.toString())
                        etTotalAmt.setText(totalAmt.toString())
                    } else if (etInstallationFee.length() == 0 && etCost.length() != 0) {
                        val cost = Integer.parseInt(etQty.text.toString()) * Integer.parseInt(etCost.text.toString())
                        etTotalAmt.setText(cost.toString())
                    } else if (etCost.length() == 0) {
                        etTotalAmt.setText(etInstallationFee.text.toString())
                    }
                }else{
                    if (etInstallationFee.length() != 0){
                        if (etInstallationFee.length()!=0 && etCost.length() !=0){
                            val amt = Integer.parseInt(etInstallationFee.text.toString()) + Integer.parseInt(etCost.text.toString())
                            etTotalAmt.setText(amt.toString())
                        }else if (etCost.length() == 0){
                            etTotalAmt.setText(etInstallationFee.text.toString())
                        }
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })

        etCost.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                if (etCost.length() != 0) {
                    if (etInstallationFee.length() != 0 && etQty.length() != 0) {
                        val cost = Integer.parseInt(etQty.text.toString()) * Integer.parseInt(etCost.text.toString())
                        totalAmt = cost + Integer.parseInt(etInstallationFee.text.toString())
                        etTotalAmt.setText(totalAmt.toString())
                    } else if (etInstallationFee.length() == 0 && etQty.length() != 0) {
                        val cost = Integer.parseInt(etQty.text.toString()) * Integer.parseInt(etCost.text.toString())
                        etTotalAmt.setText(cost.toString())
                    } else if (etQty.length() == 0) {
                        etTotalAmt.setText(etInstallationFee.text.toString())
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })

        etInstallationFee.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                if (etInstallationFee.length() != 0) {
                    if (etQty.length() != 0 && etCost.length() != 0) {
                        val cost = Integer.parseInt(etQty.text.toString()) * Integer.parseInt(etCost.text.toString())
                        totalAmt = cost + Integer.parseInt(etInstallationFee.text.toString())
                        etTotalAmt.setText(totalAmt.toString())
                    }  else if (etCost.length() != 0) {
                        val amt = Integer.parseInt(etInstallationFee.text.toString()) + Integer.parseInt(etCost.text.toString())
                        etTotalAmt.setText(amt.toString())
                    }else {
                        etTotalAmt.setText(etInstallationFee.text.toString())
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            if (keySaleOrderDetails != "") {
                if (keySaleOrderDetails == Constants.key_sale_order_details) {
                    goToSaleOrderDetailsActivity()
                }
            } else {
                goToMainActivity()
            }
        } else if (id == R.id.action_save) {
            saveInstallation()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {

        when (view) {
            btnSave -> saveInstallation()
        }
    }

    private fun saveInstallation() {
        getText()

        installationID = UUID.randomUUID().toString()

        val installation = Installation()
        installation.InstallationID = installationID
        installation.InstallationNo = strInstallationNo
        installation.CustomerID = selectCustomerID
        installation.InstallationUserID = selectEngineerID
        installation.ProductID = selectProductID
        installation.InternetDeveice = strInternetDevice
        installation.FiberCableLength = strFiberCableLength
        installation.FiberSwitchQty = strFiberSwitchQty
        installation.Remark = ""
        installation.SNGPS = strSngps
        installation.LossRemark = strLossRemark
        installation.InstallationStartDate = selectedDateForStart
        installation.InstallationEndDate = selectedDateForEnd
        installation.Duration = strDuration
        installation.Discount = strDiscount
        installation.InstallationFee = strInstallationFee
        installation.RouterFee = strRouterFee
        installation.ConfigFee = strConfigFee
        installation.RelocationFee = ""
        installation.InstallationStatus = selectInstallationStatus
        installation.InstallationTotalAmount = strTotalAmt
        installation.Active = "1"
        installation.CreatedBy = userID
        installation.CreatedOn = dateTimeFormatForDb.format(Calendar.getInstance().time)
        installation.ModifiedBy = userID
        installation.ModifiedOn = dateTimeFormatForDb.format(Calendar.getInstance().time)
        installation.LastAction = ""
        installation.Speed = strSpeed
        installation.InternetCode = strInternetCode
        installation.Tax = strTax
        installation.ProductQty = strQty
        installation.SaleOrderID = saleOrderID

        doAsync {
            val database = AppDatabase.getInstance(context = this@InstallationDetailActivity)
            database.getInstallationDAO().insert(installation)

            uiThread {

                if (Common.isConnected(this@InstallationDetailActivity)) {
                    val uploadInstallation = UploadInstallation(
                        this@InstallationDetailActivity,
                        this@InstallationDetailActivity,
                        strInstallationNo,
                        installationID,
                        customerID
                    )
                    uploadInstallation.uploadInstallation()
                } else {
                    goToInstallationInfoActivity()
                }
            }
        }

    }


    private fun goToInstallationInfoActivity() {
        val intent = Intent(this@InstallationDetailActivity, InstallationInfoActivity::class.java)
        intent.putExtra(Constants.keyInstallationID, installationID)
        intent.putExtra(Constants.keyInstallationNo, strInstallationNo)
        intent.putExtra(Constants.keyCustomerID, selectCustomerID)
        startActivity(intent)
        finish()
    }

    private fun getText() {
        strInstallationNo = tvInstallationNoInfo.text.toString()
        strInternetCode = etInternetCode.text.toString()
        strInternetDevice = etInternetDevice.text.toString()
        strFiberCableLength = etFiberCableLength.text.toString()
        strLossRemark = etLossReamrk.text.toString()
        strTax = etTax.text.toString()
        strDiscount = etDiscount.text.toString()
        strConfigFee = etConfigFee.text.toString()
        strRouterFee = etRouterFee.text.toString()
        strInstallationFee = etInstallationFee.text.toString()
        strQty = etQty.text.toString()
        strCost = etCost.text.toString()
        strTotalAmt = etTotalAmt.text.toString()
        strSpeed = etSpeed.text.toString()
        strSngps = etSngps.text.toString()
        strDuration = etDuration.text.toString()
        strFiberSwitchQty = etFiberSwitchQty.text.toString()
    }

    private fun showDatePickerDialog() {
        val c = Calendar.getInstance(Locale.ENGLISH)
        val dateSetListener = DatePickerDialog.OnDateSetListener { it, year, monthOfYear, dayOfMonth ->
            //            val cal = Calendar.getInstance()

            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, monthOfYear)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            tvStartDateInfo.text = Constants.dateTimeFormatForDataShow.format(c.time)

            // selectedDateForStart = Constants.dateTimeFormatForDataShow.format(c.time)
            selectedDateForStart = Constants.dateTimeFormatForUpload.format(c.time)
            Log.i("selectedDate", "$selectedDateForStart check")
        }

        val dpd = DatePickerDialog(
            this,
            dateSetListener,
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )
        tvStartDateInfo.text = Constants.dateTimeFormatForDataShow.format(c.time)
        tvStartDateInfo.setOnClickListener {
            dpd.show()
        }
        selectedDateForStart = Constants.dateTimeFormatForUpload.format(c.time)
        Log.i("selectedDate1", selectedDateForStart)
    }

    private fun showDatePickerDialogForEndDate() {
        val c = Calendar.getInstance(Locale.ENGLISH)
        val dateSetListener = DatePickerDialog.OnDateSetListener { it, year, monthOfYear, dayOfMonth ->
            //            val cal = Calendar.getInstance()

            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, monthOfYear)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            tvEndDateInfo.text = Constants.dateTimeFormatForDataShow.format(c.time)

            selectedDateForEnd = Constants.dateTimeFormatForUpload.format(c.time)
            Log.i("selectedDate", "$selectedDateForEnd check")
        }

        val dpd = DatePickerDialog(
            this,
            dateSetListener,
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )
        tvEndDateInfo.text = Constants.dateTimeFormatForDataShow.format(c.time)
        tvEndDateInfo.setOnClickListener {
            dpd.show()
        }
        selectedDateForEnd = Constants.dateTimeFormatForUpload.format(c.time)
        Log.i("selectedDate1", selectedDateForEnd)
    }

    override fun onBackPressed() {
        if (keySaleOrderDetails != "") {
            if (keySaleOrderDetails == Constants.key_sale_order_details) {
                goToSaleOrderDetailsActivity()
            }
        } else {
            goToMainActivity()
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Constants.key_go_to, Constants.key_installation)
        startActivity(intent)
        finish()
    }

    private fun goToSaleOrderDetailsActivity() {
        val intent = Intent(this, SaleOrderDetailsActivity::class.java)
        intent.putExtra(Constants.keySaleOrderID, saleOrderID)
        intent.putExtra(Constants.keyCustomerName, customerName)
        startActivity(intent)
        finish()
    }

    private fun showInstallDataFromDb(installId : String){
        doAsync {
            val database = AppDatabase.getInstance(context = this@InstallationDetailActivity)
            val installList = database.getInstallationDAO().getInstallationById(installId)
            if (installList.isNotEmpty()){
                uiThread {
                    etInternetCode.setText(installList[0].InternetCode)
                    etInternetDevice.setText(installList[0].InternetDeveice)
                    etFiberCableLength.setText(installList[0].FiberCableLength)
                    etLossReamrk.setText(installList[0].LossRemark)
                    etDuration.setText(installList[0].Duration)
                    etTax.setText(installList[0].Tax)
                    etDiscount.setText(installList[0].Discount)
                    etConfigFee.setText(installList[0].ConfigFee)
                    etRouterFee.setText(installList[0].RouterFee)
                    etQty.setText(installList[0].ProductQty)
                    etSpeed.setText(installList[0].Speed)
                    etSngps.setText(installList[0].SNGPS)
                    etFiberSwitchQty.setText(installList[0].FiberSwitchQty)
                }
            }
        }
    }
    private fun getDataFromDb() {
        doAsync {
            val database = AppDatabase.getInstance(context = this@InstallationDetailActivity)
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


            val keyPairForEngineer = KeyPairForEngineer()
            keyPairForEngineer.id = userID
            keyPairForEngineer.name = userName

            keyPairForEngineerList.add(keyPairForEngineer)

            val productGroupList = database.getProductGroupDAO().getProductGroup()
            for (i in 0 until productGroupList.size) {
                val keyPairForProductGroup = KeyPairForProductGroup()
                keyPairForProductGroup.id = productGroupList[i].ProductGroupID
                keyPairForProductGroup.name = productGroupList[i].ProductGroupName

                keyPairForProductGroupList.add(keyPairForProductGroup)
            }


            val installationList = database.getInstallationDAO().getInstallation()
            if (installationList.isNotEmpty()) {
                val idLength = 6
                var latestOrderCount = 0
                latestOrderCount = Integer.parseInt(splitInstallation) + (installationList.size + 1)

                val postfix = String.format("%0" + idLength + "d", latestOrderCount)
                val installNo = prefixForInstallation + postfix
                //tvOrderNo.setText(orderNo);
                tvInstallationNoInfo.setText(installNo)
            }

        }
    }

    private fun setMultiSpinnerForCustomer() {
        spCustomerName.setDataForCustomer(keyPairForCustomerList, cusName, -1) { items ->
            selectCustomerID = SettingPreference.getDataFromSharedPref(Constants.keyCustomerID, this).toString()
            Log.d(TAG, "selectCustomerID $selectCustomerID")
        }
    }

    private fun setMultiSpinnerForEngineer() {
        spEngineerName.setDataForEngineer(keyPairForEngineerList, engineerName, -1) { items ->
            selectEngineerID = SettingPreference.getDataFromSharedPref(Constants.keyEngineerID, this).toString()
            Log.d(TAG, "selectEngineerID $selectEngineerID")

        }
    }

    private fun setMultiSpinnerForProductGroup() {
        doAsync {
            val database = AppDatabase.getInstance(context = this@InstallationDetailActivity)
            val productList = database.getProductDAO().getProductById(productID)
            if (productList.isNotEmpty()) {
                val productGroupList = database.getProductGroupDAO().getProductGroupById(productList[0].ProductGroupID)
                for (i in 0 until productGroupList.size) {
                    prodGroupName = productGroupList[i].ProductGroupName
                    productGroupID = productGroupList[i].ProductGroupID

                   Log.e(TAG , "ProductGroup id : ${productGroupList[i].ProductGroupID}")
                   Log.e(TAG , "ProductGroup Name : ${productGroupList[i].ProductGroupName}")
                }
                val showProductList = database.getProductDAO().getProductByGroupId(productGroupID)
                for (i in 0 until showProductList.size) {
                    val keyPairForProduct = KeyPairForProduct()
                    keyPairForProduct.id = showProductList[i].ProductID
                    keyPairForProduct.name = showProductList[i].ProductName
                    keyPairForProductList.add(keyPairForProduct)
                }
            }


            setMultiSpinnerForProduct()
            spProductGroup.setDataForProductGroup(keyPairForProductGroupList, prodGroupName, -1) { items ->
                selectProductGroupID = SettingPreference.getDataFromSharedPref(Constants.keyProductGroupID,
                    this@InstallationDetailActivity).toString()
                Log.d(TAG, "selectProductGroupID $selectProductGroupID")

            runOnUiThread {
                    doAsync {
                        val database = AppDatabase.getInstance(context = this@InstallationDetailActivity)
                        val productList = database.getProductDAO().getProductByGroupId(selectProductGroupID)
                        Log.i(TAG, "productList = ${productList.size}")
                        keyPairForProductList.clear()
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
                                    this@InstallationDetailActivity
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

        doAsync {
            val database = AppDatabase.getInstance(context = this@InstallationDetailActivity)
            val productList = database.getProductDAO().getProductById(productID)
            for (i in 0 until productList.size) {
                prodName = productList[i].ProductName

                uiThread {
                    etInstallationFee.setText(productList[i].InstallationFees)
                    etCost.setText(productList[i].Cost)
                }

                val productGroupList = database.getProductGroupDAO().getProductGroupById(productList[i].ProductGroupID)
                for (i in 0 until productGroupList.size) {
                    prodGroupName = productGroupList[i].ProductGroupName
                }
            }


        }
        spProduct.setDataForProduct(keyPairForProductList, prodName, -1) { items ->
            selectProductID = SettingPreference.getDataFromSharedPref(Constants.keyProductID, this).toString()
            Log.d(TAG, "selectProductID __ : $selectProductID")

        }
        if (selectProductID == ""){
            selectProductID = productID
        }

    }

    private fun bindDataFromSpinnerForInstallStatus() {
        statusListString = resources.getStringArray(R.array.status_arr)

        val dataAdapter = ArrayAdapter<String>(
            this,
            R.layout.spinner_item, statusListString
        )
        dataAdapter.setDropDownViewResource(R.layout.spinner_item)

        spInstallStatus.adapter = dataAdapter

        spInstallStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, l: Long) {
                // selectBankName = bankNameList.get(position);
                selectInstallationStatus = statusListString[position]!!
                Log.d(TAG, "selectStatus : $selectInstallationStatus")
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }
    }

}
