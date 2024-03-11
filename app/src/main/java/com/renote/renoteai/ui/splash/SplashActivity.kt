package com.renote.renoteai.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import com.renote.renoteai.R
import com.renote.renoteai.ui.main.MainActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var viewSplash: View
    private lateinit var txtCompany: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        viewSplash = findViewById(R.id.viewSplash)
        txtCompany = findViewById(R.id.textView)
        viewSplash.alpha = 0.94f
        val spannable = SpannableStringBuilder(getString(R.string.company_name))
        spannable.setSpan(
            ForegroundColorSpan(getColor(R.color.green)),
            0,
            2,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        txtCompany.text = spannable
        Handler().postDelayed(Runnable { display() }, 2000)

    }

    private fun display(){
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }
}