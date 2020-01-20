package com.systematic.netcore.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.systematic.netcore.R
import com.systematic.netcore.fragment.*
import com.systematic.netcore.fragment.TestingActivity
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var TAG = "MainActivity"

    var fragment: Fragment? = null
    var fragmentClass: Class<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        val name = SettingPreference.getDataFromSharedPref(Constants.keyUserName, this).toString()
        Log.d(TAG,"UserName : $name")
        val headerView = navView.inflateHeaderView(R.layout.nav_header_main)
        val tvUserName = headerView.findViewById(R.id.tvUserName) as TextView
        tvUserName.text = name

        if (intent.getStringExtra(Constants.key_go_to) !=null){
            Log.d(TAG,"key to go if: ${intent.getStringExtra(Constants.key_go_to)}")
            if(intent.getStringExtra(Constants.key_go_to) == Constants.key_new_sacout){
                fragmentClass = ListScoutFragment()::class.java
            }else  if(intent.getStringExtra(Constants.key_go_to) == Constants.key_installation){
                fragmentClass = InstallationListFragment()::class.java
            } else  if(intent.getStringExtra(Constants.key_go_to) == Constants.key_customer_list){
                fragmentClass = CustomerListFragment()::class.java
            }else  if(intent.getStringExtra(Constants.key_go_to) == Constants.key_sale_order_list){
                fragmentClass = SaleOrderFragment()::class.java
            }
        }else{
            Log.d(TAG,"key to go else : ${intent.getStringExtra(Constants.key_go_to)}")
            fragmentClass = CustomerListFragment()::class.java
        }
        //todo this must delete
        fragmentClass = TestingActivity()::class.java
        callFragment()
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

            R.id.navCreateSaleOrder ->{
                fragmentClass = CreateSaleOrderFragment::class.java
                callFragment()
            }

            R.id.navSaleOrder -> {
                SettingPreference.putDataToSharefPref(Constants.keyCustomerID,"",this)
                fragmentClass = SaleOrderFragment::class.java
                callFragment()
            }

            R.id.navCustomer -> {
                fragmentClass = CustomerListFragment::class.java
                callFragment()
            }

            R.id.navInstallation -> {
                fragmentClass = InstallationListFragment::class.java
                callFragment()
            }

            R.id.navBilling -> {
                fragmentClass = BillingFragment::class.java
                callFragment()

            }
            R.id.navInvoicing -> {

            }
            R.id.navPromotion -> {
            }

            R.id.navSacout ->{
                fragmentClass = ListScoutFragment::class.java
                callFragment()
            }

            R.id.navLogout -> {
                SettingPreference.putDataToSharefPref(Constants.key_login_state, "", this)
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun callFragment() {
        try {
            fragment = fragmentClass?.newInstance() as Fragment
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val fragmentManager = supportFragmentManager
        fragment?.let { fragmentManager.beginTransaction().replace(R.id.frameLayout, it).commit() }
    }
}
