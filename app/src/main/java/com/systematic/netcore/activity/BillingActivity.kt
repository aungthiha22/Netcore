package com.systematic.netcore.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.systematic.netcore.R
import com.systematic.netcore.api.UploadInstallationInfo
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.database.model.BillingInfo
import com.systematic.netcore.database.model.Customer
import com.systematic.netcore.objects.KeyPairForCustomer
import com.systematic.netcore.objects.KeyPairForTowerType
import com.systematic.netcore.objects.ModelTesting
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import kotlinx.android.synthetic.main.activity_billing_information.*
import kotlinx.android.synthetic.main.activity_installation_info.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.ArrayList


class BillingActivity : AppCompatActivity(),View.OnClickListener {


    var TAG = "BillingActivity"
    var strBillingTitle = ""
    var strBillingNo = ""
    var strBillingFor = ""
    var strStatus = "Please Select"
    var strBillingDate = ""
    var strBillingType = "Please Select"
    var strRepeatType = "Please Select"
    var strEndDate = ""
    var strSaleOrder = "Please Select"
    var strContactInfo = ""
    var strAmount = ""
    var strCurrency = "Please Select"
    var strRemainderOne = ""
    var strRemainderTwo = ""
    var strNextBillDate = ""
    var strNextTargetBillDate = ""
    var strImportantLevel = ""
    var strRemainderEmail = ""
    var strLatePolicy = ""
    var strNotifyUser1 = "Please Select"
    var strNotifyUser2 = "Please Select"

    var billArrayList: MutableList<BillingInfo>? = null
    var selectCustomerID = ""
   // var cusName = "select status"
    private var keyPairForCustomerList = ArrayList<KeyPairForCustomer>()
    private var modelTestingArr = ArrayList<ModelTesting>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_billing_information)

        getDataFromDb()
       // setMultiSpinnerForCustomer()
       // setTestingSpinner()

        btnSaveBill.setOnClickListener(this)
        btnSeeTableData.setOnClickListener(this)

    }
    override fun onClick(view: View?) {
        when(view){
            btnSaveBill -> {

                saveBillingInfo()

            }
            btnSeeTableData -> {

                //todo This is Testing
                doAsync {
                    val database = AppDatabase.getInstance(context = this@BillingActivity)
                    //database.getBillingInfoDAO().getBillingInfo()
                    // final List<Person> persons = appDatabase.personDao().loadAllPersons();
                    val billingList = database.getBillingInfoDAO().getBillingInfo()
                    Log.e(TAG , "ArrayList ${billingList.size}")
                    Log.d(TAG , "ArrayList ${billingList.size}")

                    for (i in 0 until billingList.size) {
                        Log.d(TAG , "Name ${billingList.get(i).BillingTitle}")
                    }

                    uiThread {
                        Log.d(TAG, "bill data are ${billingList.size}")


                        val uploadInstallationInfo = UploadInstallationInfo(this@BillingActivity, context = this@BillingActivity)
                        //uploadInstallationInfo.uploadInstallationInfo()
                        Log.d(TAG, "${uploadInstallationInfo}")
                    }

                }
            }

        }

    }



    private fun getText(){
        strBillingTitle = etBillingTitle.text.toString()
        strBillingFor = etBillingFor.text.toString()
        strContactInfo = etContactInfo.text.toString()
        strRemainderOne = etRemainderOne.text.toString()
        strRemainderTwo = etRemainderTwo.text.toString()
        strImportantLevel = etImportantLevel.text.toString()
        strRemainderEmail = etReminderEmail.text.toString()
        strLatePolicy = etLatePolicy.text.toString()


    }
    private fun saveBillingInfo(){
        getText()

        var billingInfo = BillingInfo()
        billingInfo.BillingTitle = strBillingTitle
        billingInfo.BillingFor = strBillingFor
        billingInfo.ContactInfo = strContactInfo
        billingInfo.RemainderOne = strRemainderOne
        billingInfo.RemainderTwo = strRemainderTwo
        billingInfo.ImportantLevel = strImportantLevel
        billingInfo.RemainderEmail = strRemainderEmail
        billingInfo.LatePolicy = strLatePolicy

        doAsync {
            //val database = AppDatabase.getInstance(context = this@InstallationInfoActivity)
            //database.getInstallationInfoDAO().insert(installationInfo)
            val database = AppDatabase.getInstance(context = this@BillingActivity)
            database.getBillingInfoDAO().insert(billingInfo)

            uiThread {
               /* val uploadInstallationInfo =
                    UploadInstallationInfo(this@InstallationInfoActivity, context = this@InstallationInfoActivity)
                uploadInstallationInfo.uploadInstallationInfo()*/
                Toast.makeText(applicationContext, "Save success.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /*private fun setMultiSpinnerForCustomer() {
        spStatus.setDataForCustomer(keyPairForCustomerList, strStatus, -1) { items ->
            selectCustomerID = SettingPreference.getDataFromSharedPref(Constants.keyCustomerID, this).toString()
            Log.d(TAG, "selectCustomerID $selectCustomerID")
        }
    }*/
   /* private fun setTestingSpinner(){
        spBillType.setDataForCustomer(modelTestingArr,strBillingType,-1){ items ->
            selectCustomerID = SettingPreference.getDataFromSharedPref(Constants.keyCustomerID,this).toString()
        }

    }*/

    private fun getDataFromDb() {
        doAsync {
            /*val database = AppDatabase.getInstance(context = this@BillingActivity)
            val customerList = database.getCustomerDAO().getCustomer()*/


        }
    }

}