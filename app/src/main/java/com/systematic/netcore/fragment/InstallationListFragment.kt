package com.systematic.netcore.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.systematic.netcore.R
import com.systematic.netcore.activity.CreateNewScoutActivity
import com.systematic.netcore.activity.InstallationDetailActivity
import com.systematic.netcore.adapter.InstallationListAdapter
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.objects.InstallationObject
import com.systematic.netcore.utility.Common
import com.systematic.netcore.utility.Constants
import kotlinx.android.synthetic.main.activity_installation_detail.*
import kotlinx.android.synthetic.main.fragment_list_installation.*
import org.jetbrains.anko.doAsync
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class InstallationListFragment : Fragment(),View.OnClickListener, DatePickerDialog.OnDateSetListener {

    var TAG = "InstallationListFragment"
    var selectDate = ""
    var statusForCheckAllDate = ""

    private lateinit var dpd: DatePickerDialog
    private val calendar = Calendar.getInstance()
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    private val installationList : ArrayList<InstallationObject> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_installation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        dpd = DatePickerDialog(
            context!!,
            this,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        showDatePickerDialog()
        getDataFromDb()

        btnPreviousDate.setOnClickListener(this)
        btnNextDate.setOnClickListener(this)

        rvInstallationList.layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false) as RecyclerView.LayoutManager?
        rvInstallationList.adapter = InstallationListAdapter(activity!!,context!!,installationList){
            val intent = Intent(this.context, InstallationDetailActivity::class.java)
            intent.putExtra(Constants.key_go_to,Constants.key_installation_details )
            intent.putExtra(Constants.keyInstallationID, installationList[it].id)
            intent.putExtra(Constants.keyProductID, installationList[it].productID)
            intent.putExtra(Constants.keyCustomerID, installationList[it].customerID)
            intent.putExtra(Constants.keyCustomerName, installationList[it].customerName)
            startActivity(intent)
            activity!!.overridePendingTransition(0, 0)
            activity!!.finish()
        }
        rvInstallationList.adapter?.notifyDataSetChanged()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onClick(view: View?) {
        when (view){
            btnPreviousDate -> {
                statusForCheckAllDate = ""
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                tvDate.text = dateTimeFormat.format(calendar.time)

                //callReceiptListAndGetData(btnDate.text.toString())
                Toast.makeText(context,"Date is : "+dateTimeFormat.format(calendar.time),Toast.LENGTH_SHORT).show()
            }

            btnNextDate -> {
                statusForCheckAllDate = ""
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                tvDate.text = dateTimeFormat.format(calendar.time)

                //callReceiptListAndGetData(btnDate.text.toString())
                Toast.makeText(context,"Date is : "+dateTimeFormat.format(calendar.time),Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity!!.menuInflater.inflate(R.menu.menu_add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_add) {
            val intent = Intent(activity!!, InstallationDetailActivity::class.java)
            activity!!.startActivity(intent)
            activity!!.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDateSet(p0: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        calendar.set(year, monthOfYear, dayOfMonth)
        tvDate.text = dateTimeFormat.format(calendar.time)
    }

    private fun showDatePickerDialog() {
        val c = Calendar.getInstance(Locale.ENGLISH)
        val dateSetListener = DatePickerDialog.OnDateSetListener { it, year, monthOfYear, dayOfMonth ->
            //            val cal = Calendar.getInstance()

            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, monthOfYear)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            tvDate.text = Constants.dateTimeFormatForDataShow.format(c.time)

            selectDate = Constants.dateTimeFormatForDataShow.format(c.time)
            Log.i("selectedDate", "$selectDate check")
        }

        val dpd = DatePickerDialog(
            activity!!,
            dateSetListener,
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )

        tvDate.text = Constants.dateTimeFormatForDataShow.format(c.time)
        tvDate.setOnClickListener {
            dpd.show()
        }
        selectDate = Constants.dateTimeFormatForDataShow.format(c.time)
        Log.i("selectedDate1", selectDate)
    }

    private fun getDataFromDb(){
        doAsync {
            val database = AppDatabase.getInstance(context = activity!!)
            val installList = database.getInstallationDAO().getInstallation()
            Log.d(TAG, "installList ${installList.size}")

            for (i in 0 until installList.size){
                val installObject = InstallationObject()
                installObject.id = installList[i].InstallationID
                installObject.customerID = installList[i].CustomerID
                val customerList = database.getCustomerDAO().getCustomerById(installList[i].CustomerID)
                if (customerList.isNotEmpty()){
                    installObject.customerName = customerList[0].CustomerNameEng
                }

                installObject.productID = installList[i].ProductID
                val productList = database.getProductDAO().getProductById(installList[i].ProductID)
                if (productList.isNotEmpty()){
                    installObject.productName = productList[0].ProductName
                    installObject.productPrice = productList[0].Cost
                }

                installationList.add(installObject)
            }
        }
    }

    private fun callReceiptListAndGetData(strDate: String) {
        if (Common.isConnected(context!!)){
            //requestDataByDate(strDate)
        }else{
            //showServiceList(strDate)
        }
    }

}