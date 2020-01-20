package com.systematic.netcore.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.systematic.netcore.R
import com.systematic.netcore.objects.InstallationObject
import com.systematic.netcore.utility.Constants
import kotlinx.android.synthetic.main.fragment_list_installation.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TestingActivity : AppCompatActivity(),View.OnClickListener, DatePickerDialog.OnDateSetListener {

    var TAG = "InstallationListFragment"
    var selectDate = ""
    var statusForCheckAllDate = ""

    private lateinit var dpd: DatePickerDialog
    private val calendar = Calendar.getInstance()
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_list_installation)

        dpd = DatePickerDialog(
            this@TestingActivity!!,
            this,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        showDatePickerDialog()

        btnPreviousDate.setOnClickListener(this)
        btnNextDate.setOnClickListener(this)

        rvInstallationList.layoutManager = LinearLayoutManager(this@TestingActivity!!, RecyclerView.VERTICAL, false) as RecyclerView.LayoutManager?

    }

    override fun onClick(view: View?) {
        when (view){
            btnPreviousDate -> {
                statusForCheckAllDate = ""
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                tvDate.text = dateTimeFormat.format(calendar.time)
            }

            btnNextDate -> {
                statusForCheckAllDate = ""
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                tvDate.text = dateTimeFormat.format(calendar.time)
            }
        }
    }

    override fun onDateSet(p0: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        calendar.set(year, monthOfYear, dayOfMonth)
        tvDate.text = dateTimeFormat.format(calendar.time)
    }

    private fun showDatePickerDialog() {
        val c = Calendar.getInstance(Locale.ENGLISH)
        val dateSetListener = DatePickerDialog.OnDateSetListener { it, year, monthOfYear, dayOfMonth ->
            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, monthOfYear)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            tvDate.text = Constants.dateTimeFormatForDataShow.format(c.time)
            selectDate = Constants.dateTimeFormatForDataShow.format(c.time)
            Log.i("selectedDate", "$selectDate check")
        }

        val dpd = DatePickerDialog(
            this@TestingActivity!!,
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
}