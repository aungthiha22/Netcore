package com.systematic.netcore.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.google.android.material.tabs.TabLayout
import com.systematic.netcore.R
import com.systematic.netcore.adapter.CustomerDetailsPagerAdapter
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.fragment.CustomerListFragment
import com.systematic.netcore.fragment.InstallationListFragment
import com.systematic.netcore.fragment.SaleOrderFragment
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import kotlinx.android.synthetic.main.activity_customer_details.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class CustomerDetailsActivity : AppCompatActivity() {

    var TAG = "CustomerDetailsActivity"
    var customerID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_details)

        if (supportActionBar!=null){
            supportActionBar!!.title = "CustomerDetails"
            supportActionBar!!.setHomeAsUpIndicator(resources.getDrawable(R.drawable.ic_arrow_back))
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        customerID = SettingPreference.getDataFromSharedPref(Constants.keyCustomerID,this@CustomerDetailsActivity).toString()
        Log.d(TAG, "CustomerID $customerID")
        doAsync {
            val database = AppDatabase.getInstance(this@CustomerDetailsActivity)
            val customerList = database.getCustomerDAO().getCustomerById(customerID)
            Log.d(TAG, "customerList ${customerList.size}")
            if (customerList.isNotEmpty()){
                uiThread {
                    tvCustomerNameInfo.text = customerList[0].CustomerNameEng
                    tvPhoneNoInfo.text = customerList[0].PhoneNo
                    tvAddressInfo.text = customerList[0].Address
                }
            }
        }

        setUpTabLayout()
        setUpViewPager()
    }

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        val menuItem = menu!!.getItem(R.id.action_add) as MenuItem
        menuItem.isVisible = false
        return true
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            SettingPreference.putDataToSharefPref(Constants.keyCustomerID,"",this)
            goToMainActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("WrongConstant")
    fun setUpTabLayout() {

        tabLayout.setupWithViewPager(vpCustomerDetails)
        tabLayout.tabGravity = TabLayout.INDICATOR_GRAVITY_CENTER
        tabLayout.setTabTextColors(Color.WHITE, Color.GREEN)
    }

    fun setUpViewPager() {
        val adapter = CustomerDetailsPagerAdapter(supportFragmentManager)
            adapter.addFragment(SaleOrderFragment(), "Sale Order")
            adapter.addFragment(SaleOrderFragment(), "Billing")
            adapter.addFragment(SaleOrderFragment(), "Invoicing")

        vpCustomerDetails.offscreenPageLimit = 3
        vpCustomerDetails.adapter = adapter
        vpCustomerDetails.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
    }

    override fun onBackPressed() {
        SettingPreference.putDataToSharefPref(Constants.keyCustomerID,"",this)
        goToMainActivity()
    }

    fun goToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Constants.key_go_to, Constants.key_customer_list)
        startActivity(intent)
        finish()
    }
}
