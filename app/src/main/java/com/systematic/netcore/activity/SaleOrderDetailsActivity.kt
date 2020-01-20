package com.systematic.netcore.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.systematic.netcore.R
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import kotlinx.android.synthetic.main.activity_sale_order_details.*
import kotlinx.android.synthetic.main.fragment_sale_order.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class SaleOrderDetailsActivity : AppCompatActivity(), View.OnClickListener {

    var saleOrderId = ""
    var customerId = ""
    var productId = ""
    var customerName = ""

    internal var nf = NumberFormat.getNumberInstance(Locale.US)
    var formatter : DecimalFormat = DecimalFormat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sale_order_details)

        if (supportActionBar != null) {
            supportActionBar!!.title = "Sale Order Details"
            supportActionBar!!.setHomeAsUpIndicator(resources.getDrawable(R.drawable.ic_arrow_back))
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        formatter = nf as DecimalFormat
        formatter.applyPattern("#,###,###")

        saleOrderId = intent.getStringExtra(Constants.keySaleOrderID)

        if (intent.getStringExtra(Constants.keyCustomerName) != null) {
            customerName = intent.getStringExtra(Constants.keyCustomerName)
        }

        ShowSaleOrderInfo()

        btnAddToInstallation.setOnClickListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            goToMainActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        when (view) {
            btnAddToInstallation -> goToInstallationActivity()
        }
    }

    override fun onBackPressed() {
        goToMainActivity()
    }

    private fun goToInstallationActivity() {
        val intent = Intent(this, InstallationDetailActivity::class.java)
        intent.putExtra(Constants.key_go_to, Constants.key_sale_order_details)
        intent.putExtra(Constants.keySaleOrderID, saleOrderId)
        intent.putExtra(Constants.keyCustomerName, customerName)
        intent.putExtra(Constants.keyCustomerID, customerId)
        intent.putExtra(Constants.keyProductID, productId)
        startActivity(intent)
        finish()
    }

    private fun goToMainActivity() {
        SettingPreference.putDataToSharefPref(Constants.keyCustomerID,"",this)
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Constants.key_go_to, Constants.key_sale_order_list)
        startActivity(intent)
        finish()
    }

    private fun ShowSaleOrderInfo() {
        doAsync {

            val database = AppDatabase.getInstance(context = this@SaleOrderDetailsActivity)

            val saleOrderList = database.getSaleOrderDAO().getSaleOrderById(saleOrderId)
            if (saleOrderList.isNotEmpty()) {
                val productList = database.getProductDAO().getProductById(saleOrderList[0].ProductID)

                customerId = saleOrderList[0].CustomerID
                productId = saleOrderList[0].ProductID

                uiThread {
                    tvSaleNoInfo.text = saleOrderList[0].OrderNo
                    tvStatusInfo.text = saleOrderList[0].OrderStatus
                    tvCustomerInfo.text = customerName

                    if (productList.isNotEmpty()) {
                        tvProductNameInfo.text = productList[0].ProductName
                        tvInstallationFeeInfo.text = productList[0].InstallationFees

                        if (productList[0].Cost.isNotEmpty()){
                           // val costFormat = formatter.format(productList[0].Cost)
                            tvProductCostInfo.text = productList[0].Cost
                        }
                    }

                    if (saleOrderList[0].Tax.isNotEmpty()){
                        //val taxFormat = formatter.format(saleOrderList[0].Tax)
                        tvTaxInfo.text = saleOrderList[0].Tax
                    }

                    if (saleOrderList[0].TotalAmount.isNotEmpty()){
                        //val totalAmtFormat = formatter.format(saleOrderList[0].TotalAmount)
                        tvTotalAmtInfo.text = saleOrderList[0].TotalAmount
                    }
                }
            }
        }
    }
}
