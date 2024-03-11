package com.renote.renoteai.ui.activities.signupotp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.renote.renoteai.ui.main.MainActivity
import com.renote.renoteai.R
import com.renote.renoteai.ui.activities.signup.SignUpActivity

class SignUpOTPActivity : AppCompatActivity() {

    private lateinit var txtCompanyName: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnVerify:CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_otpactivity)

        txtCompanyName = findViewById(R.id.textView)
        btnBack = findViewById(R.id.btnBack)
        btnVerify = findViewById(R.id.btnVerify)

        val spannable = SpannableStringBuilder(getString(R.string.company_name))
        spannable.setSpan(
            ForegroundColorSpan(getColor(R.color.green)),
            0,
            2,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        txtCompanyName.text = spannable

        btnBack.setOnClickListener {
            startActivity(Intent(this@SignUpOTPActivity, SignUpActivity::class.java))
            finishAffinity()
        }

        btnVerify.setOnClickListener {
            startActivity(Intent(this@SignUpOTPActivity, MainActivity::class.java))
        }
    }
}