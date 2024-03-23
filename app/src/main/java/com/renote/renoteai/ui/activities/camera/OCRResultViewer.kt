package com.renote.renoteai.ui.activities.camera

import com.renote.renoteai.databinding.ActivityOcrresultViewerBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.renote.renoteai.ui.activities.edit.EXTRA_OCR_TEXT

class OCRResultViewer : AppCompatActivity() {

  private lateinit var viewBinding: ActivityOcrresultViewerBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = ActivityOcrresultViewerBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)

    viewBinding.textView.text = intent.getStringExtra(EXTRA_OCR_TEXT)
  }
}

