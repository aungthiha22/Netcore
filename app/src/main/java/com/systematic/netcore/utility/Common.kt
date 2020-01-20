package com.systematic.netcore.utility

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.systematic.netcore.R
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class Common {
    companion object {

        @SuppressLint("NewApi")
        fun getSHAKey(context: Context): String {
            var keyHash = ""
            try {
                val info = context.packageManager.getPackageInfo(
                    context.packageName, PackageManager.GET_SIGNATURES
                )

                Log.i("packageName", context.packageName)

                for (signature in info.signatures) {
                    val md = MessageDigest.getInstance("MD5")
                    md.update(signature.toByteArray())
                    Log.i("md", md.digest().toString())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) { // android 2.2 and above
                        keyHash = android.util.Base64.encodeToString(
                            md.digest(),
                            android.util.Base64.DEFAULT
                        )
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                keyHash = e.message.toString()
            } catch (e: NoSuchAlgorithmException) {
                keyHash = e.message.toString()
            } catch (e: Exception) {
                keyHash = e.message.toString()
            }

            return keyHash.trim { it <= ' ' }
        }

        fun isConnected(context: Context): Boolean {
            val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo

            return activeNetwork != null && activeNetwork.isConnectedOrConnecting
        }

        fun showPoorConnectionToast(context: Context) {
            Toast.makeText(context, context.getString(R.string.poor_connection), Toast.LENGTH_SHORT).show()
        }

        fun getCurrentDate(withTime: Boolean): String {
            if (withTime) {
                return Constants.defaultDateFormat.format(Calendar.getInstance().time)
            }
            else {
                return Constants.defaultDateFormatWithoutTime.format(Calendar.getInstance().time)
            }
        }

        fun isValidEmail(email: String): Boolean {
           // return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }
}