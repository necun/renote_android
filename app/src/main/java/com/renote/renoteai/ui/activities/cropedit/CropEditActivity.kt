package com.renote.renoteai.ui.activities.cropedit

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.renote.renoteai.R
import com.renote.renoteai.databinding.CropDataBinding
import com.renote.renoteai.ui.activities.camera.viewmodel.CropViewModel
import com.renote.renoteai.ui.activities.edit.EditActivity
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileInputStream

class CropEditActivity : AppCompatActivity() {

    val viewModel: CropViewModel by inject()
    var enhancedImageType: String = ""
    private lateinit var fileName: String
    private lateinit var viewBinding: CropDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(
            this@CropEditActivity,
            R.layout.activity_crop_edit
        )
        viewBinding.lifecycleOwner = this
        viewBinding.viewmodel = viewModel

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        enhancedImageType = intent.getStringExtra("enhancedImageType").toString()
        println("enhancedImageType:$enhancedImageType")

        fileName = intent.getStringExtra("fileName").toString()

        initImageDisplay(this@CropEditActivity,fileName)
        observeData()
    }

    fun observeData() {
        viewModel.resourseClick.observe(this) { integer ->
            when (integer) {
                R.id.continueBtn -> {
                    val intent = Intent(this@CropEditActivity, EditActivity::class.java)
                    intent.putExtra("enhancedImageType", enhancedImageType)
                    startActivity(intent)
                }
            }
        }
    }

    private fun initImageDisplay(context: Context, fileName: String) {

        val remoteAIDirectory = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "ReNoteAI-Image-Original"
        )
        println(remoteAIDirectory)
        val f = File(remoteAIDirectory, fileName)
        System.out.println("122334465=" + f)
        val b = BitmapFactory.decodeStream(FileInputStream(f))
        viewBinding.cropImage.setImageBitmap(b)
    }
}