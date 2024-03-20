package com.renote.renoteai.ui.activities.filteredit

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.renote.renoteai.R

class FilterEditActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_edit)

        val originalLayout = findViewById<LinearLayout>(R.id.originalImg)
        val originalTextView = findViewById<AppCompatTextView>(R.id.original)
        val magicImgBtn = findViewById<ConstraintLayout>(R.id.magicImgBtn)
        val magicTextView = findViewById<AppCompatTextView>(R.id.enhanced)
        val softImgBtn = findViewById<ConstraintLayout>(R.id.softImgBtn)
        val softTextView = findViewById<AppCompatTextView>(R.id.soft)
        val grayImgBtn = findViewById<ConstraintLayout>(R.id.grayImgBtn)
        val greyTextView = findViewById<AppCompatTextView>(R.id.grey)
        val blackWhiteImgBtn = findViewById<ConstraintLayout>(R.id.blackWhiteImgBtn)
        val blackWhiteTextView = findViewById<AppCompatTextView>(R.id.blackWhite)

        val textViewList = listOf(originalTextView, magicTextView, softTextView, greyTextView, blackWhiteTextView)

        val clickListeners = listOf(
            originalLayout to originalTextView,
            magicImgBtn to magicTextView,
            softImgBtn to softTextView,
            grayImgBtn to greyTextView,
            blackWhiteImgBtn to blackWhiteTextView
        )

        clickListeners.forEach { (layout, textView) ->
            layout.setOnClickListener {
                textViewList.forEach { it.setTextColor(ContextCompat.getColor(this, R.color.black)) }
                textView.setTextColor(ContextCompat.getColor(this, R.color.green))
            }
        }
    }

}