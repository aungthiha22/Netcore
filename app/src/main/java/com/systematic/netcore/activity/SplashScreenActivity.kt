package com.systematic.netcore.activity

import android.animation.ValueAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.systematic.netcore.R
import com.systematic.netcore.utility.Constants
import com.systematic.netcore.utility.SettingPreference
import kotlinx.android.synthetic.main.activity_splash_screen.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SplashScreenActivity : AppCompatActivity() {

    var TAG = "SplashScreenActivity"

    internal var isShowingRubberEffect = false
    var loginSign = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        loginSign = SettingPreference.getDataFromSharedPref(Constants.key_login_state, this).toString()
        Log.d(TAG,"LoginState $loginSign")
        initAnimation()

    }

    private fun initAnimation() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.anim_top_in)
        logo_outer_iv.startAnimation(animation)

        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 1000
        valueAnimator.addUpdateListener { animation ->
            val fraction = animation.animatedFraction
            Log.i("fraction", fraction.toString())
            if (fraction >= 0.8 && !isShowingRubberEffect) {
                isShowingRubberEffect = true
                startLogoOuter()
                startShowAppName()
                finishActivity()
            } else if (fraction >= 0.95) {
                valueAnimator.cancel()
                startLogoInner2()
            }
        }
        valueAnimator.start()

    }

    private fun startLogoOuter() {
        YoYo.with(Techniques.RubberBand).duration(1000).playOn(logo_outer_iv)
    }

    private fun startShowAppName() {
        YoYo.with(Techniques.FadeIn).duration(1000).playOn(textViewWelcome)
        YoYo.with(Techniques.FadeIn).duration(1000).playOn(textViewUserName)
    }

    private fun startLogoInner2() {
        YoYo.with(Techniques.Bounce).duration(1000).playOn(logo_inner_iv)
    }

    private fun finishActivity() {
        Observable.timer(1000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if(loginSign != "1"){
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0,0)
                    finish()
                }else{
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0,0)
                    finish()
                }

            }
    }
}
