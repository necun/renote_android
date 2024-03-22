package com.renote.renoteai.ui.activities.edit

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.renote.renoteai.R
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.database.tables.FileEntity
import com.renote.renoteai.databinding.EditActivityDataBinding
import com.renote.renoteai.ui.activities.camera.CameraActivity
import com.renote.renoteai.ui.activities.camera.EXTRA_PICTURE_TYPE
import com.renote.renoteai.ui.activities.camera.EmailActivity
import com.renote.renoteai.ui.activities.camera.OCRResultViewer
import com.renote.renoteai.ui.activities.camera.libs.CVLib
import com.renote.renoteai.ui.activities.cropedit.CropEditActivity
import com.renote.renoteai.ui.activities.edit.adapter.EditPagerAdapter
import com.renote.renoteai.ui.activities.edit.viewmodel.EditViewModel
import com.renote.renoteai.ui.activities.filteredit.FilterEditActivity
import com.renote.renoteai.ui.main.MainActivity
import org.koin.android.ext.android.inject
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


const val EXTRA_OCR_TEXT = "com.example.cameraxapp.OCR_TEXT"
const val EXTRA_PICTURE_URI = "com.example.cameraxapp.PICTURE_URI"


class EditActivity : AppCompatActivity() {
    private lateinit var binding: EditActivityDataBinding
    private val viewModel: EditViewModel by inject()
    var mContext: Context? = null
    private var original: Mat? = null
    private var result: Mat? = null

    private lateinit var org: View
    private lateinit var ai: View
    private lateinit var bw: View
    private lateinit var grey: View
    private lateinit var soft: View
    private var pictureType: String? = ""

    var enhancedImageType: String = ""

    private var currentRotation = 0f
    private lateinit var uri: Uri

    lateinit var editPagerAdapter: EditPagerAdapter
    var recentDocumentId: String = ""

    @SuppressLint("WrongThread")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this@EditActivity, R.layout.activity_edit
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        supportActionBar?.hide()

//        uri = Uri.parse(intent.getStringExtra(EXTRA_PICTURE_URI))
//        println("uri in EditActivity:$uri")
//        pictureType = intent.getStringExtra(EXTRA_PICTURE_TYPE)

        editPagerAdapter = EditPagerAdapter(this@EditActivity)
        editPagerAdapter.showEditTitle = true
        recentDocumentId = intent.getStringExtra("recentdocumentid").toString()
        println("343refewr5:$recentDocumentId")
        binding.imageView2.apply {
            adapter = editPagerAdapter
        }
//        val uri = Uri.parse(intent.getStringExtra(EXTRA_PICTURE_URI))
//        pictureType = intent.getStringExtra(EXTRA_PICTURE_TYPE)


        org = findViewById(R.id.viewOrg)
        ai = findViewById(R.id.viewAI)
        bw = findViewById(R.id.viewBW)
        grey = findViewById(R.id.viewGrey)
        soft = findViewById(R.id.viewSoft)

        bw.visibility = View.VISIBLE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE
        )

        //deleteInternalStorageDirectoryy()
        binding.aiFilterImgBtn.setOnClickListener {
            ai.visibility = View.VISIBLE

            bw.visibility = View.GONE
            org.visibility = View.GONE
            grey.visibility = View.GONE
            soft.visibility = View.GONE
            binding.aiFilterProgressbar.visibility = View.VISIBLE
            deleteInternalStorageDirectoryy()


//      viewBinding.originalFilterImageView.setImageResource(R.drawable.ic_no_picture)
//      viewBinding.blackAndWhiteFilterImageView.setImageResource(R.drawable.ic_no_picture)
//      viewBinding.greyFilterImageView.setImageResource(R.drawable.ic_no_picture)
//      viewBinding.softFilterImageView.setImageResource(R.drawable.ic_no_picture)

            doSaveGetSave()
            aiFilter()
        }
        binding.greyFilterImgBtn.setOnClickListener {
            grey.visibility = View.VISIBLE

            bw.visibility = View.GONE
            org.visibility = View.GONE
            ai.visibility = View.GONE
            soft.visibility = View.GONE
            //viewBinding.greyFilterProgressbar.visibility=View.VISIBLE
            deleteInternalStorageDirectoryy()

//      viewBinding.originalFilterImageView.setImageResource(R.drawable.ic_no_picture)
//      viewBinding.blackAndWhiteFilterImageView.setImageResource(R.drawable.ic_no_picture)
//      viewBinding.aiFilterImageView.setImageResource(R.drawable.ic_no_picture)
//      viewBinding.softFilterImageView.setImageResource(R.drawable.ic_no_picture)
            doSaveGetSave()
            greyFilter()
        }
        binding.softFilterImgBtn.setOnClickListener {
            soft.visibility = View.VISIBLE

            bw.visibility = View.GONE
            org.visibility = View.GONE
            grey.visibility = View.GONE
            ai.visibility = View.GONE
            //viewBinding.softFilterProgressbar.visibility=View.VISIBLE
            deleteInternalStorageDirectoryy()

//      viewBinding.originalFilterImageView.setImageResource(R.drawable.ic_no_picture)
//      viewBinding.blackAndWhiteFilterImageView.setImageResource(R.drawable.ic_no_picture)
//      viewBinding.aiFilterImageView.setImageResource(R.drawable.ic_no_picture)
//      viewBinding.greyFilterImageView.setImageResource(R.drawable.ic_no_picture)
            doSaveGetSave()
            softFilter()
        }
        binding.blackAndWhiteFilterImgBtn.setOnClickListener {
            bw.visibility = View.VISIBLE

            ai.visibility = View.GONE
            org.visibility = View.GONE
            grey.visibility = View.GONE
            soft.visibility = View.GONE
            binding.blackAndWhiteFilterProgressbar.visibility = View.VISIBLE
            deleteInternalStorageDirectoryy()

//      viewBinding.originalFilterImageView.setImageResource(R.drawable.ic_no_picture)
//      viewBinding.aiFilterImageView.setImageResource(R.drawable.ic_no_picture)
//      viewBinding.greyFilterImageView.setImageResource(R.drawable.ic_no_picture)
//      viewBinding.softFilterImageView.setImageResource(R.drawable.ic_no_picture)
            doSaveGetSave()
            blackAndWhiteFilter()
        }
//    viewBinding.BWFilterButton.setOnClickListener {
//      doBWFilter()
//
//    viewBinding.GrayscaleFilterButton.setOnClickListener {
//      doGrayscaleFilter()
//    }
//    viewBinding.EnhanceFilterButton.setOnClickListener {
//      deleteInternalStorageDirectoryy()
//      doSaveGetSave()
//      val input_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/CameraX-Image-Input/"
//      val output_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/CameraX-Image-Output/"
//      val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/CameraX-Image-Output/")
//      if(!file.exists()){
//        file.mkdirs()
//      }
//      val input = input_path + "cropped_image" + ".jpg"
//      if (!Python.isStarted()) {
//        Python.start(AndroidPlatform(this))
//      }
//      val py = Python.getInstance()
//      val module = py.getModule("script")
//
//      val fact = module["process_and_enhance_image"]
//      fact?.call(input,output_path)
//      val f=File(output_path,"enhanced_image.jpg")
//      System.out.println("122334465=")
//      val b=BitmapFactory.decodeStream(FileInputStream(f))
//
//      viewBinding.imageView2.setImageBitmap(b)
//    }
//    viewBinding.SaveButton.setOnClickListener {
//      doSave()
//    }
        binding.tickMarkImageView.setOnClickListener {
            doSave()
        }

        binding.scanMoreBtn.setOnClickListener {
            val intent = Intent(this@EditActivity, CameraActivity::class.java)
            startActivity(intent)
            deleteInternalStorageDirectoryy()
            finishAffinity()
        }
//    viewBinding.SoftFilterButton.setOnClickListener {
//      doSoftFilter()
//    }
        binding.originalFilterImgBtn.setOnClickListener {
            org.visibility = View.VISIBLE

            bw.visibility = View.GONE
            ai.visibility = View.GONE
            grey.visibility = View.GONE
            soft.visibility = View.GONE
            // viewBinding.originalFilterProgressbar.visibility=View.VISIBLE
            deleteInternalStorageDirectoryy()

            // org.isVisible = true
//      viewBinding.greyFilterImageView.setImageResource(R.drawable.ic_no_picture)
//      viewBinding.blackAndWhiteFilterImageView.setImageResource(R.drawable.ic_no_picture)
//      viewBinding.aiFilterImageView.setImageResource(R.drawable.ic_no_picture)
//      viewBinding.softFilterImageView.setImageResource(R.drawable.ic_no_picture)
            val original = original ?: return@setOnClickListener
            result = original.clone()
            val bitmap =
                Bitmap.createBitmap(original.cols(), original.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(original, bitmap)

            val name = "no_filter_image"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image-Output")
                }
            }


            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
            ) ?: throw IOException("Could not open uri")
//    val stream = contentResolver.openOutputStream(uri) ?: throw IOException("Could not open output stream")
//
//// Create a buffer to hold the bitmap's pixels
//    val byteBuffer = ByteBuffer.allocate(bitmap.byteCount)
//    bitmap.copyPixelsToBuffer(byteBuffer)
//    byteBuffer.rewind()
//
//// Write the buffer's contents to the output stream
//    stream.write(byteBuffer.array())
//
//    stream.close()
            val stream = contentResolver.openOutputStream(uri)
                ?: throw IOException("Could not open output stream")
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.close()
//val msg = "Save succeeded: ${uri.getPath()}"
//Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            doNoFilter()

        }
//    viewBinding.OCRButton.setOnClickListener {
//      doOCR()
//    }


        //  val uri = Uri.parse(intent.getStringExtra(EXTRA_PICTURE_URI))
//        val imageDecoder = ImageDecoder.createSource(contentResolver, uri)
//        val bitmap = ImageDecoder.decodeBitmap(imageDecoder)
//        val bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//        contentResolver.delete(uri, null, null)


//        val mat = Mat()
//        Utils.bitmapToMat(bmp32, mat)
//// get current camera frame as OpenCV Mat object
//        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB)
//        original = mat.clone()
//        result = mat.clone()
//        val resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(mat, resultBitmap)


//        doSaveGetSave()
//        blackAndWhiteFilter()
        observeData()

        //doSaveGetSave()
        // doNoFilter()
        //aiFilter()
        //blackAndWhiteFilter()
        //greyFilter()
        //softFilter()
        // viewBinding.imageView2.setImageBitmap(resultBitmap)
        progressBarObserveData()
        recentFileDetailsByRecentDocumentIdObserveData()
        editPagerAdapter.onTextChanged = { position, data ->
            //cropScanList[position].fileName = data
        }
    }

    private fun recentFileDetailsByRecentDocumentIdObserveData() {
        println("43rgty6uj:$recentDocumentId")
        //viewModel.getRecentFileDetailsByDocumentId(recentDocumentId)
        val getEntities = getFileEntities(this@EditActivity)
        //viewModel.recentFileDetails.observe(this@EditActivity) {
        println("343refe4:$getEntities")
        editPagerAdapter.submitList(getEntities)
        //}
    }

    fun getFileEntities(context: Context): List<FileEntity> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        // Retrieve the JSON string of file entities from shared preferences
        val fileEntitiesJson = prefs.getString(FILE_ENTITIES_KEY, null)
        // Check if the string is null. If it's not, convert the JSON string back into a list of FileEntity objects
        return if (fileEntitiesJson != null) {
            // Convert the JSON string back into an Array of FileEntity objects and then to a list
            gson.fromJson(fileEntitiesJson, Array<FileEntity>::class.java).toList()
        } else {
            // Return an empty list if there are no saved file entities
            emptyList()
        }
    }

    private fun progressBarObserveData() {
        viewModel.showLoading.observe(this) {
            if (it == true) {
                binding.progressbar.visibility = View.VISIBLE
            } else {
                binding.progressbar.visibility = View.GONE
            }
        }
    }

    fun observeData() {
        viewModel.resourseClick.observe(this) { integer ->
            when (integer) {
                R.id.cropBtn -> {
                    val intent = Intent(this@EditActivity, CropEditActivity::class.java)
                    intent.putExtra("enhancedImageType", enhancedImageType)
                    startActivity(intent)
                }

                R.id.filterBtn -> {
                    // navigateToNextScreenWithImage()
                    val currentPosition = binding.imageView2.currentItem
                    val currentFileEntity = editPagerAdapter.currentList[currentPosition]
                    val imageUri = currentFileEntity.fileData
                    val fileName = currentFileEntity.name
                    val fileId = currentFileEntity.id
                    val intent = Intent(this, FilterEditActivity::class.java).apply {
                        putExtra("imageUri", imageUri)
                        putExtra("fileName", fileName)
                        putExtra("fileId", fileId)
                    }
                    startActivity(intent)
                }

                R.id.rotateBtn -> {
                    rotateImageView()
                }

                R.id.tickMarkImageView -> {
                    val docEntity = getDocumentEntity(this@EditActivity)
                    val fileEntities = getFileEntities(this@EditActivity)
                    if (docEntity != null) {
                        viewModel.saveDocumentDetail(docEntity)
                        if (fileEntities != null) {
                            viewModel.saveFilesDetails(fileEntities)
                        }
                    }
                    clearAllPreferences(this@EditActivity)
                    clearDocPreferences(this@EditActivity)
                    startActivity(Intent(this@EditActivity, MainActivity::class.java))
                    finishAffinity()
                }
            }
        }
    }

    private fun clearDocPreferences(context: Context) {
        val prefs = context.getSharedPreferences("DocumentEntityPreference", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear() // Clear all data
        editor.apply()
    }

    fun getDocumentEntity(context: Context): DocumentEntity? {
        val sharedPreferences =
            context.getSharedPreferences("DocumentEntityPreference", Context.MODE_PRIVATE)
        val jsonString = sharedPreferences.getString("DocumentEntityKey", null)

        // Convert JSON String back to DocumentEntity
        return if (jsonString != null) {
            val gson = Gson()
            gson.fromJson(jsonString, DocumentEntity::class.java)
        } else {
            null
        }
    }

    private fun rotateImageView() {
        currentRotation += 90f
        if (currentRotation >= 360f) currentRotation = 0f
        val imageView = binding.imageView2

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        // Set the height of the imageView to match the width of the screen
        imageView.layoutParams.height = screenWidth

        val rotationAnimator =
            ObjectAnimator.ofFloat(imageView, "rotation", imageView.rotation, currentRotation)
        rotationAnimator.duration = 500 // 500 milliseconds
        rotationAnimator.start()

//        currentRotation += 90f
//        if (currentRotation >= 360f) currentRotation = 0f // Reset rotation if full circle
//
//        val imageView = binding.imageView2
//
//        val displayMetrics = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(displayMetrics)
//        val screenWidth = displayMetrics.widthPixels
//        val screenHeight = displayMetrics.heightPixels
//
//        // Assuming the original dimensions of the imageView represent its aspect ratio
//        val originalWidth = imageView.width
//        val originalHeight = imageView.height
//
//        // Determine new dimensions based on current rotation
//        val isLandscape = currentRotation % 180 != 0f
//        val newWidth: Int
//        val newHeight: Int
//
//        if (isLandscape) {
//            // When the image is rotated "landscape", match screen height to imageView's height and adjust width to maintain aspect ratio
//            newHeight = screenHeight
//            newWidth = (originalWidth.toFloat() / originalHeight.toFloat() * newHeight).toInt()
//        } else {
//            // When the image is back to "portrait", match screen width to imageView's width and adjust height to maintain aspect ratio
//            newWidth = screenWidth
//            newHeight = (originalHeight.toFloat() / originalWidth.toFloat() * newWidth).toInt()
//        }
//
//        // Adjust imageView's layoutParams
//        val layoutParams = imageView.layoutParams
//        layoutParams.width = newWidth
//        layoutParams.height = newHeight
//        imageView.layoutParams = layoutParams
//
//        // Rotate the imageView with animation
//        val rotationAnimator = ObjectAnimator.ofFloat(imageView, "rotation", imageView.rotation, currentRotation)
//        rotationAnimator.duration = 500 // 500 milliseconds
//        rotationAnimator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteInternalStorageDirectoryy()
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
            binding.aiFilterProgressbar.visibility = View.GONE
            //viewBinding.aiFilterImageView.setImageBitmap(b)
            // binding.imageView2.setImageBitmap(b)
        } else {
            binding.aiFilterProgressbar.visibility = View.VISIBLE
            // viewBinding.aiFilterImageView.setImageResource(R.drawable.ic_no_picture)
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
            //binding.imageView2.setImageBitmap(b)
            //viewBinding.greyFilterImageView.setImageBitmap(b)
            binding.greyFilterProgressbar.visibility = View.GONE


        } else {
            binding.greyFilterProgressbar.visibility = View.VISIBLE
            //viewBinding.greyFilterImageView.setImageResource(R.drawable.ic_no_picture)
        }

    }

    @SuppressLint("SuspiciousIndentation")
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
            ////binding.imageView2.setImageBitmap(b)
            // viewBinding.softFilterImageView.setImageBitmap(b)
            binding.softFilterProgressbar.visibility = View.GONE
        } else {
            // viewBinding.softFilterImageView.setImageResource(R.drawable.ic_no_picture);
            binding.softFilterProgressbar.visibility = View.VISIBLE

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
            ////binding.imageView2.setImageBitmap(b)
            //viewBinding.blackAndWhiteFilterImageView.setImageBitmap(b)
            binding.blackAndWhiteFilterProgressbar.visibility = View.GONE


        } else {
            binding.blackAndWhiteFilterProgressbar.visibility = View.VISIBLE
            //viewBinding.blackAndWhiteFilterImageView.setImageResource(R.drawable.ic_no_picture)
        }

    }

    private fun doEnhancePythonFilter() {

//deleteInternalStorageDirectoryy()
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

        val fact = module["process_and_enhance_image"]
        fact?.call(input, output_path)
        val f = File(output_path, "enhanced_image.jpg")
        System.out.println("122334465=")
        val b = BitmapFactory.decodeStream(FileInputStream(f))

        //// binding.imageView2.setImageBitmap(b)
    }

    private fun doBWFilter() {
        val original = original ?: return
        val result = result ?: return
        CVLib.doFilterBW(original.nativeObjAddr, result.nativeObjAddr)
        val resultBitmap =
            Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(result, resultBitmap)
        //// binding.imageView2.setImageBitmap(resultBitmap)
    }

    //
    private fun doGrayscaleFilter() {
        val original = original ?: return
        val result = result ?: return
        CVLib.doFilterGrayscale(original.nativeObjAddr, result.nativeObjAddr)
        val resultBitmap =
            Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(result, resultBitmap)
        ////binding.imageView2.setImageBitmap(resultBitmap)
    }

    private fun doEnhanceFilter() {
        val original = original ?: return
        val result = result ?: return
        CVLib.doFilterEnhance(original.nativeObjAddr, result.nativeObjAddr)
        val resultBitmap =
            Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(result, resultBitmap)
        //// binding.imageView2.setImageBitmap(resultBitmap)
    }

    private fun doSoftFilter() {
        val original = original ?: return
        val result = result ?: return
        CVLib.doFilterSoft(original.nativeObjAddr, result.nativeObjAddr)
        val resultBitmap =
            Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(result, resultBitmap)
        ////binding.imageView2.setImageBitmap(resultBitmap)
    }

    private fun doNoFilter() {
        val original = original ?: return
        result = original.clone()
        val resultBitmap =
            Bitmap.createBitmap(original.cols(), original.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(original, resultBitmap)
        if (resultBitmap != null) {
            enhancedImageType = "no_filter_image"
            //// binding.imageView2.setImageBitmap(resultBitmap)
            //  viewBinding.originalFilterImageView.setImageBitmap(resultBitmap)
            binding.originalFilterProgressbar.visibility = View.GONE


        } else {
            binding.originalFilterProgressbar.visibility = View.VISIBLE
//      viewBinding.originalFilterImageView.setImageResource(R.drawable.ic_no_picture)
        }

    }

    private fun doOCR() {
        val result = result ?: return
        val resultBitmap =
            Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(result, resultBitmap)
// https://developers.google.com/ml-kit/vision/text-recognition/android
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(resultBitmap, 0)
        recognizer.process(image).addOnSuccessListener { visionText ->
// Task completed successfully
// ...
            val msg = "Recognition success!"
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            val intent = Intent(this, OCRResultViewer::class.java).apply {
                putExtra(EXTRA_OCR_TEXT, visionText.getText())
            }
            startActivity(intent)
        }.addOnFailureListener { e ->
// Task failed with an exception
// ...
            val msg = "Recognition failed: ${e}"
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun doSave() {
        val result = result ?: return
        val bitmap = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(result, bitmap)
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ) ?: throw IOException("Could not open uri")
        val stream = contentResolver.openOutputStream(uri)
            ?: throw IOException("Could not open output stream")
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)


        stream.close()
        //val msg = "Save succeeded: ${uri.getPath()}"
        //val msg = "Save succeeded"
        // Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        println("enhancedImageTypenhancedImageTyp:$enhancedImageType")

        val intent = Intent(this@EditActivity, EmailActivity::class.java)
        intent.putExtra("enhancedImageType", enhancedImageType)
        startActivity(intent)
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
//    val stream = contentResolver.openOutputStream(uri) ?: throw IOException("Could not open output stream")
//
//// Create a buffer to hold the bitmap's pixels
//    val byteBuffer = ByteBuffer.allocate(bitmap.byteCount)
//    bitmap.copyPixelsToBuffer(byteBuffer)
//    byteBuffer.rewind()
//
//// Write the buffer's contents to the output stream
//    stream.write(byteBuffer.array())
//
//    stream.close()
        val stream = contentResolver.openOutputStream(uri)
            ?: throw IOException("Could not open output stream")
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        //bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        stream.close()
//val msg = "Save succeeded: ${uri.getPath()}"
//Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
    }

    fun deleteInternalStorageDirectoryy() {
        if (ContextCompat.checkSelfPermission(
                this@EditActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE
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
                this@EditActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@EditActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 14
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
                    Toast.makeText(this@EditActivity, "Permission Granted", Toast.LENGTH_LONG)
                        .show()
                } else {
                    ActivityCompat.requestPermissions(
                        this@EditActivity,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        14
                    )
                }
            }
        }
    }

    fun clearAllPreferences(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear() // Clear all data
        editor.apply() // Apply the changes
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Start MainActivity
        clearAllPreferences(this@EditActivity)
        deleteInternalStorageDirectoryy()
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)

        // Optional: if you want to finish the current activity
        finishAffinity()
    }

    companion object {
        private const val PREFS_NAME = "MyAppPrefs"
        private const val FILE_ENTITIES_KEY = "fileEntities"
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    override fun onResume() {
        super.onResume()
        // deleteInternalStorageDirectoryy()
    }
}