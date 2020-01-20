package com.systematic.netcore.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.systematic.netcore.R
import com.systematic.netcore.api.UploadInstallationInfo
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.database.model.InstallationInfo
import com.systematic.netcore.objects.KeyPairForCustomer
import com.systematic.netcore.objects.KeyPairForTowerType
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import kotlinx.android.synthetic.main.activity_installation_info.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*

class InstallationInfoActivity : AppCompatActivity(),View.OnClickListener {

    var TAG = "InstallationInfoActivity"

    private var strInstallationInfoNoInfo = ""
    private var strPPOEName = ""
    private var strPPOEPassword = ""
    private var strWifiName = ""
    private var strWifiPassword = ""
    private var strVLAN = ""
    private var strManageMAC = ""
    private var strUserMAC = ""
    private var strSerialNo = ""
    private var strPortNo = ""

    private var userID = ""
    private var installationNo = ""
    private var installationId = ""
    private var customerId = ""
    private var customerNo = ""

    private var cusName = "Please Select"
    private var towerName = "Please Select"
    private var selectCustomerID = ""
    private var selectTowerTypeID = ""
    private var strNode = ""

    private val dateTimeFormatForDb = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss", Locale.US)

    private var keyPairForCustomerList = ArrayList<KeyPairForCustomer>()
    private var keyPairForTowerTypeList = ArrayList<KeyPairForTowerType>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installation_info)

        if (supportActionBar != null) {
            supportActionBar!!.title = "Installation Info"
            supportActionBar!!.setHomeAsUpIndicator(resources.getDrawable(R.drawable.ic_arrow_back))
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        userID = SettingPreference.getDataFromSharedPref(Constants.keyUserId, this).toString()

        if (intent.getStringExtra(Constants.keyInstallationNo) != null && intent.getStringExtra(Constants.keyCustomerID) != null) {
            installationId = intent.getStringExtra(Constants.keyInstallationID)
            installationNo = intent.getStringExtra(Constants.keyInstallationNo)
            customerId = intent.getStringExtra(Constants.keyCustomerID)

            tvInstallationInfoNoInfo.text = installationNo

            doAsync {
                val database = AppDatabase.getInstance(context = this@InstallationInfoActivity)
                val customerlist = database.getCustomerDAO().getCustomerById(customerId)
                Log.d(TAG, "customerlist ${customerlist.size}")
                if (customerlist.isNotEmpty()) {
                    customerNo = customerlist[0].CustomerCode
                    cusName = customerlist[0].CustomerNameEng
                    Log.d(TAG, "CustomerNo $customerNo")
                    Log.d(TAG, "CustomerName $cusName")
                }
            }
        }

        getDataFromDb()
        setMultiSpinnerForCustomer()
        setMultiSpinnerForTowerType()

        btnSave.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {

        } else if (id == R.id.action_save) {
           // saveInstallationInfo()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        when (view){
          //  btnSave -> saveInstallationInfo()
        }
    }

    private fun saveInstallationInfo() {
        getText()

        val installationInfoID = UUID.randomUUID().toString()

        val installationInfo = InstallationInfo()
        installationInfo.InstallationInfoID = installationInfoID
        installationInfo.CustomerID = selectCustomerID
        installationInfo.PPOEName = strPPOEName
        installationInfo.PPOEPassword = strPPOEPassword
        installationInfo.WifiName = strWifiName
        installationInfo.WifiPassword = strWifiPassword
        installationInfo.VLAN = strVLAN
        installationInfo.ManagementMAC = strManageMAC
        installationInfo.UserMAC = strUserMAC
        installationInfo.SearialNo = strSerialNo
        installationInfo.PortNo = strPortNo
        installationInfo.Node = strNode
        installationInfo.TowerTypeID = selectTowerTypeID
        installationInfo.Active = "1"
        installationInfo.CreatedBy = userID
        installationInfo.CreatedOn = dateTimeFormatForDb.format(Calendar.getInstance().time)
        installationInfo.ModifiedBy = userID
        installationInfo.ModifiedOn = dateTimeFormatForDb.format(Calendar.getInstance().time)
        installationInfo.CustomerNo = customerNo
        installationInfo.InstallationID = installationId

        doAsync {
            val database = AppDatabase.getInstance(context = this@InstallationInfoActivity)
            database.getInstallationInfoDAO().insert(installationInfo)

            uiThread {
               /* val uploadInstallationInfo =
                    UploadInstallationInfo(this@InstallationInfoActivity, context = this@InstallationInfoActivity)
                uploadInstallationInfo.uploadInstallationInfo()*/
                val InstsllationInfo = database.getInstallationDAO().getInstallation()
                Log.d("Testing" ,"Testing _______ $installationInfo")
            }
        }
    }

    private fun getDataFromDb() {
        doAsync {
            val database = AppDatabase.getInstance(context = this@InstallationInfoActivity)
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

            val towerTypeList = database.getTowerTypeDAO().getTowerType()
            for (i in 0 until towerTypeList.size) {
                val keyPairForTowerType = KeyPairForTowerType()
                keyPairForTowerType.id = towerTypeList[i].TowerTypeID
                keyPairForTowerType.name = towerTypeList[i].TowerTypeName

                keyPairForTowerTypeList.add(keyPairForTowerType)
            }
        }
    }

    private fun setMultiSpinnerForCustomer() {
        spCustomerNameForInfo.setDataForCustomer(keyPairForCustomerList, cusName, -1) { items ->
            selectCustomerID = SettingPreference.getDataFromSharedPref(Constants.keyCustomerID, this).toString()
            Log.d(TAG, "selectCustomerID $selectCustomerID")
        }
    }

    private fun setMultiSpinnerForTowerType() {
        spTowerType.setDataForTowerType(keyPairForTowerTypeList, towerName, -1) { items ->
            selectTowerTypeID = SettingPreference.getDataFromSharedPref(Constants.keyTowerTypeID, this).toString()
            Log.d(TAG, "selectTowerTypeID $selectTowerTypeID")
        }
    }

    private fun getText() {
        strInstallationInfoNoInfo = tvInstallationInfoNoInfo.text.toString()
        strPPOEName = etPPOEName.text.toString()
        strPPOEPassword = etPPOEPassword.text.toString()
        strWifiName = etWifiName.text.toString()
        strWifiPassword = etWifiPassword.text.toString()
        strVLAN = etVLAN.text.toString()
        strManageMAC = etManageMAC.text.toString()
        strUserMAC = etUserMAC.text.toString()
        strSerialNo = etSerialNo.text.toString()
        strPortNo = etPortNo.text.toString()
        strNode = etNode.text.toString()
    }

}
