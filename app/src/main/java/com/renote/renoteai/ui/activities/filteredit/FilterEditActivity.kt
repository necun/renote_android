package com.renote.renoteai.ui.activities.filteredit

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.renote.renoteai.R
import com.renote.renoteai.databinding.FilterEditActivityDataBinding
import com.renote.renoteai.ui.activities.camera.EXTRA_PICTURE_URI
import org.koin.android.ext.android.inject
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class FilterEditActivity : AppCompatActivity() {

    private lateinit var binding: FilterEditActivityDataBinding
    private val viewModel: FilterEditViewModel by inject()
    private var original: Mat? = null
    private var result: Mat? = null

    var enhancedImageType: String = ""
    private lateinit var uri: Uri

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this@FilterEditActivity, R.layout.activity_filter_edit
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        uri = Uri.parse(intent.getStringExtra("URI"))
        println("uri:$uri")

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE
        )

//        val originalLayout = findViewById<LinearLayout>(R.id.originalImg)
//        val originalTextView = findViewById<AppCompatTextView>(R.id.original)
//        val magicImgBtn = findViewById<ConstraintLayout>(R.id.magicImgBtn)
//        val magicTextView = findViewById<AppCompatTextView>(R.id.enhanced)
//        val softImgBtn = findViewById<ConstraintLayout>(R.id.softImgBtn)
//        val softTextView = findViewById<AppCompatTextView>(R.id.soft)
//        val grayImgBtn = findViewById<ConstraintLayout>(R.id.grayImgBtn)
//        val greyTextView = findViewById<AppCompatTextView>(R.id.grey)
//        val blackWhiteImgBtn = findViewById<ConstraintLayout>(R.id.blackWhiteImgBtn)
//        val blackWhiteTextView = findViewById<AppCompatTextView>(R.id.blackWhite)
//
//        val textViewList = listOf(originalTextView, magicTextView, softTextView, greyTextView, blackWhiteTextView)

//        val clickListeners = listOf(
//            originalLayout to originalTextView,
//            magicImgBtn to magicTextView,
//            softImgBtn to softTextView,
//            grayImgBtn to greyTextView,
//            blackWhiteImgBtn to blackWhiteTextView
//        )

//        clickListeners.forEach { (layout, textView) ->
//            layout.setOnClickListener {
//                textViewList.forEach { it.setTextColor(ContextCompat.getColor(this, R.color.black)) }
//                textView.setTextColor(ContextCompat.getColor(this, R.color.green))
//            }
//        }

        // doSaveGetSave()

        aiFilter()
        observeData()

//        binding.originalImg.setOnClickListener {
//            deleteInternalStorageDirectoryy()
//
//            val original = original ?: return@setOnClickListener
//            result = original.clone()
//            val bitmap =
//                Bitmap.createBitmap(original.cols(), original.rows(), Bitmap.Config.ARGB_8888)
//            Utils.matToBitmap(original, bitmap)
//
//            val name = "no_filter_image"
//            val contentValues = ContentValues().apply {
//                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image-Output")
//                }
//            }
//
////            val uri = contentResolver.insert(
////                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
////            ) ?: throw IOException("Could not open uri")
////            val stream = contentResolver.openOutputStream(uri)
////                ?: throw IOException("Could not open output stream")
////            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
////            stream.close()
        doNoFilter()
//        }

    }

    fun observeData() {
        viewModel.resourseClick.observe(this) { integer ->
            when (integer) {
                R.id.softImgBtn -> {
                    // deleteInternalStorageDirectoryy()
                    //doSaveGetSave()
                    softFilter()
                    Toast.makeText(this@FilterEditActivity, "Soft Filter", Toast.LENGTH_SHORT)
                        .show()
                }

                R.id.originalImg -> {
                    val name = "no_filter_image"
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                            put(
                                MediaStore.Images.Media.RELATIVE_PATH,
                                "Pictures/CameraX-Image-Output"
                            )
                        }
                    }

                    val uri = contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
                    ) ?: throw IOException("Could not open uri")

//                    val stream = contentResolver.openOutputStream(uri)
//                        ?: throw IOException("Could not open output stream")
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//                    stream.close()
                    //doSaveGetSave()
                    doNoFilter()
                    Toast.makeText(this@FilterEditActivity, "Original Filter", Toast.LENGTH_SHORT)
                        .show()
                }

                R.id.magicImgBtn -> {
                    //doSaveGetSave()
                    aiFilter()
                    Toast.makeText(this@FilterEditActivity, "AI Filter", Toast.LENGTH_SHORT)
                        .show()
                }

                R.id.grayImgBtn -> {
                    greyFilter()
                    Toast.makeText(this@FilterEditActivity, "Grey Filter", Toast.LENGTH_SHORT)
                        .show()
                }

                R.id.blackWhiteImgBtn -> {
                    blackAndWhiteFilter()
                    Toast.makeText(this@FilterEditActivity, "B&W Filter", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun blackAndWhiteFilter() {

        val input_path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Input/"
        val output_path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Output/"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Output/"
        )
        if (!file.exists()) {
            file.mkdirs()
        }
        val input = input_path + "cropped_image" + ".jpg"
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val module = py.getModule("script")

        val fact = module["black_and_white_filter"]
        fact?.call(input, output_path)
        val f = File(output_path, "black_and_white_filter_image.jpg")
        System.out.println("122334465=")
        val b = BitmapFactory.decodeStream(FileInputStream(f))
        if (b != null) {
            enhancedImageType = "black_and_white_filter_image"
            binding.imageView2.setImageBitmap(b)
            //viewBinding.blackAndWhiteFilterImageView.setImageBitmap(b)
            // binding.blackAndWhiteFilterProgressbar.visibility = View.GONE


        } else {
            // binding.blackAndWhiteFilterProgressbar.visibility = View.VISIBLE
            //viewBinding.blackAndWhiteFilterImageView.setImageResource(R.drawable.ic_no_picture)
        }

    }


    private fun greyFilter() {

        val input_path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Input/"
        val output_path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Output/"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Output/"
        )
        if (!file.exists()) {
            file.mkdirs()
        }
        val input = input_path + "cropped_image" + ".jpg"
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val module = py.getModule("script")

        val fact = module["grey_filter"]
        fact?.call(input, output_path)
        val f = File(output_path, "grey_filter_image.jpg")
        System.out.println("122334465=")
        val b = BitmapFactory.decodeStream(FileInputStream(f))
        if (b != null) {
            enhancedImageType = "grey_filter_image"
            binding.imageView2.setImageBitmap(b)
            //viewBinding.greyFilterImageView.setImageBitmap(b)
            //binding.greyFilterProgressbar.visibility = View.GONE


        } else {
            //binding.greyFilterProgressbar.visibility = View.VISIBLE
            //viewBinding.greyFilterImageView.setImageResource(R.drawable.ic_no_picture)
        }

    }

    private fun softFilter() {
        val input_path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Input/"
        val output_path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Output/"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Output/"
        )
        if (!file.exists()) {
            file.mkdirs()
        }
        val input = input_path + "cropped_image" + ".jpg"
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val module = py.getModule("script")

        val fact = module["soft_filter"]
        fact?.call(input, output_path)
        val f = File(output_path, "soft_filter_image.jpg")
        System.out.println("122334465=")
        val b = BitmapFactory.decodeStream(FileInputStream(f))
        if (b != null) {
            enhancedImageType = "soft_filter_image"
            binding.imageView2.setImageBitmap(b)
            // viewBinding.softFilterImageView.setImageBitmap(b)
            // binding.softFilterProgressbar.visibility = View.GONE
        } else {
            // viewBinding.softFilterImageView.setImageResource(R.drawable.ic_no_picture);
            //binding.softFilterProgressbar.visibility = View.VISIBLE

        }
    }

    private fun doNoFilter() {

        val output_path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Input/"
        val f = File(output_path, "cropped_image.jpg")
        val b = BitmapFactory.decodeStream(FileInputStream(f))
        binding.imageView2.setImageBitmap(b)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun aiFilter() {

        val input_path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Input/"
        val output_path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Output/"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Output/"
        )
        if (!file.exists()) {
            file.mkdirs()
        }
        val input = input_path + "cropped_image" + ".jpg"
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val module = py.getModule("script")

        val fact = module["ai_filter"]
        fact?.call(input, output_path)
        val f = File(output_path, "ai_filter_image.jpg")
        System.out.println("122334465=")
        val b = BitmapFactory.decodeStream(FileInputStream(f))
        if (b != null) {
            enhancedImageType = "ai_filter_image"
            // binding.aiFilterProgressbar.visibility = View.GONE
            //viewBinding.aiFilterImageView.setImageBitmap(b)
            binding.imageView2.setImageBitmap(b)
        } else {
            // binding.aiFilterProgressbar.visibility = View.VISIBLE
            // viewBinding.aiFilterImageView.setImageResource(R.drawable.ic_no_picture)
        }

    }

    private fun doSaveGetSave() {

        val original = original ?: return
        result = original.clone()
        val bitmap = Bitmap.createBitmap(original.cols(), original.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(original, bitmap)

        val name = "cropped_image"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image-Input")
            }
        }

        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ) ?: throw IOException("Could not open uri")

        val stream = contentResolver.openOutputStream(uri)
            ?: throw IOException("Could not open output stream")
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        stream.close()
    }

    private fun deleteInternalStorageDirectoryy() {
        if (ContextCompat.checkSelfPermission(
                this@FilterEditActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            val input_pathh = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/CameraX-Image/"
            )

            val input_path = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/CameraX-Image-Input/"
            )
            val output_pathh = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/CameraX-Image-Output/"
            )
            if (input_path.exists()) {
                input_path.deleteRecursively()
            }
            if (input_pathh.exists()) {
                input_pathh.deleteRecursively()
            }
            if (output_pathh.exists()) {
                output_pathh.deleteRecursively()
            }
        } else {
            requestRuntimePermissionn()
        }
    }

    private fun requestRuntimePermissionn(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this@FilterEditActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@FilterEditActivity,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                14
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            14 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this@FilterEditActivity, "Permission Granted", Toast.LENGTH_LONG)
                        .show()
                } else {
                    ActivityCompat.requestPermissions(
                        this@FilterEditActivity,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        14
                    )
                }
            }
        }
    }

}