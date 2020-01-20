package com.systematic.netcore.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.systematic.netcore.R
import com.systematic.netcore.api.DoLogin
import com.systematic.netcore.database.AppDatabase
import com.systematic.netcore.database.model.User
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.doAsync

class LoginActivity : AppCompatActivity(),View.OnClickListener{
    var TAG = "LoginActivity"


    var userList: List<User> = ArrayList()
    private var userCode =""
    private var password=""
    private var userIDFromdb=""
    private var userNameFromdb=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val window = this.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this,R.color.colorPrimary)

        etUserCode.setText("wpp")
        etPassword.setText("yumon")

        btnLogin.setOnClickListener(this)
        tvVersion.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        when (view) {

            btnLogin -> onTapLogin()

            tvVersion -> onTapVersion()
        }
    }

    private fun onTapLogin() {
        userCode = etUserCode.text.toString()
        password = etPassword.text.toString()

        if (TextUtils.isEmpty(userCode)) {
            etUserCode.error = "Please Enter User Code"
            etUserCode.requestFocus()
        } else if (TextUtils.isEmpty(password)) {
            etPassword.error = "Please Enter Password"
            etPassword.requestFocus()

        } else {
            if (SettingPreference.getDataFromSharedPref(Constants.key_web_service_url, this).toString().isEmpty()) {
                SettingPreference.putDataToSharefPref(
                    Constants.key_web_service_url,
                    "http://ncservice.bandaryae.com:82/Webservice/",
                    this
                )

            }

            doAsync {

                val database = AppDatabase.getInstance(this@LoginActivity)
                userList = database.getUserDAO().getUserLogin(userCode, password)

                runOnUiThread {
                    if (userList.isNotEmpty()) {
                         Toast.makeText(this@LoginActivity,"not empty",Toast.LENGTH_LONG).show()
                        userIDFromdb = userList[0].UserID
                        userNameFromdb = userList[0].UserName
                        Log.d("Login", "UserName $userNameFromdb")

                        SettingPreference.putDataToSharefPref(Constants.keyUserId, userIDFromdb, this@LoginActivity)
                        SettingPreference.putDataToSharefPref(Constants.keyUserName, userNameFromdb, this@LoginActivity)
                        SettingPreference.putDataToSharefPref(Constants.key_login_state, "1", this@LoginActivity)

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                        finish()

                    } else {
                        val doLogin = DoLogin(this@LoginActivity, applicationContext, userCode, password)
                        doLogin.login()
                    }

                }


            }
        }
    }

    private fun onTapVersion() {
        val dialog = MaterialDialog.Builder(this)
            .customView(R.layout.setting_dialog, false)
            .show()

        val etPassword = dialog.findViewById(R.id.etSettingPassword) as EditText

        val btnOk = dialog.findViewById(R.id.btnSettingOk) as Button
        btnOk.setOnClickListener {
            val strPassword = etPassword.text.toString()
            if (strPassword == "nc") {
                val intent = Intent(this@LoginActivity, ApplicationSettingActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Wrong Password!!", Toast.LENGTH_SHORT).show()
            }
        }

        val btnSettingCancel = dialog.findViewById(R.id.btnSettingCancel) as Button
        btnSettingCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()

    }
}
