package com.systematic.netcore.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.systematic.netcore.R
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import kotlinx.android.synthetic.main.activity_application_setting.*

class ApplicationSettingActivity : AppCompatActivity(),View.OnClickListener {

    var TAG = "ApplicationSettingActivity"

    var webServiceUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application_setting)

        webServiceUrl = SettingPreference.getDataFromSharedPref(Constants.key_web_service_url, this).toString()

        if (webServiceUrl.isNotEmpty()){
            etWebServicUrl.setText(webServiceUrl)
        }

        btnSave.setOnClickListener(this)
        btnClearData.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view){
            btnSave -> saveUrl()

            btnClearData -> {
                etWebServicUrl.setText("")
            }
        }
    }

    private fun saveUrl(){
        val strUrl = etWebServicUrl.text.toString()

        if (TextUtils.isEmpty(strUrl)){
            etWebServicUrl.setText("Please Enter Web Service Url")
            etWebServicUrl.requestFocus()
        }else {
            //if (strUrl.contains("/Webservice/")) {
            if (strUrl.endsWith("/")) {
                SettingPreference.putDataToSharefPref(Constants.key_web_service_url, strUrl, this)
                onBackPressed()
            } else {
                Toast.makeText(this, "must end with /", Toast.LENGTH_SHORT).show()
            }
//            } else {
//                Toast.makeText(this, "You forget /Webservice/", Toast.LENGTH_SHORT).show()
//            }
        }
    }
}
