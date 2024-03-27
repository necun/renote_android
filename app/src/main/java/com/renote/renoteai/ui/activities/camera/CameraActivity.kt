package com.renote.renoteai.ui.activities.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.renote.renoteai.R
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.database.tables.FileEntity
import com.renote.renoteai.databinding.CameraDataBinding
import com.renote.renoteai.ui.activities.camera.extension.circularClose
import com.renote.renoteai.ui.activities.camera.extension.circularReveal
import com.renote.renoteai.ui.activities.camera.extension.toggleButton
import com.renote.renoteai.ui.activities.camera.libs.CVLib
import com.renote.renoteai.ui.activities.camera.scanutil.DocumentBorders
import com.renote.renoteai.ui.activities.camera.scanutil.ScanType
import com.renote.renoteai.ui.activities.camera.viewmodel.CameraViewModel
import com.renote.renoteai.ui.activities.edit.EditActivity
import com.renote.renoteai.ui.main.MainActivity
import org.koin.android.ext.android.inject
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.properties.Delegates


typealias CVAnalyzerListener = () -> Unit

const val EXTRA_PICTURE_URI = "com.example.renoteaiapp.PICTURE_URI"
const val EXTRA_PICTURE_TYPE = "com.example.renoteaiapp.PICTURE_TYPE"

class CameraActivity : AppCompatActivity() {
    var pictureType = ""
    var mContext: Context? = null

    private var original: Mat? = null
    val viewModel: CameraViewModel by inject()
    val fileEntities = mutableListOf<FileEntity>()
    private lateinit var viewBinding: CameraDataBinding

    private var imageCapture: ImageCapture? = null

    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private var bitmap: Bitmap? = null
    private var mask: Mat? = null

    private var preview: Preview? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null

    private lateinit var cameraExecutor: ExecutorService

    private var documentBorders: DocumentBorders? = null
    private var documentImageWidth: Int = 0
    private var documentImageHeight: Int = 0

    private lateinit var txtLogout: TextView

    private var activeScanType = ""

    private lateinit var folderId:String
    private lateinit var folderName:String

    //new variables
    private var hasGrid = false

    val currentTimestamp: Long = System.currentTimeMillis()

    private var flashMode by Delegates.observable(ImageCapture.FLASH_MODE_OFF) { _, _, new ->
        viewBinding.flashBtn.setImageResource(
            when (new) {
                ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
                ImageCapture.FLASH_MODE_AUTO -> R.drawable.ic_flash_auto
                else -> R.drawable.ic_flash_off
            }
        )
    }

//    var fileUri:String? = null
//    var fileName:String? = null
//    var fileId:String? = null
//    var documentId:String? = null
//    var editActivityIntent:String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(
            this@CameraActivity, R.layout.activity_camera
        )
        viewBinding.lifecycleOwner = this
        viewBinding.viewmodel = viewModel

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE
        )
        //clearAllPreferences(this@CameraActivity)
        // val userEmailId = intent.getStringExtra("userEmailId")

        // Request camera permissions
//         documentId = intent.getStringExtra("currentDocumentId")
//        fileUri = intent.getStringExtra("fileUri")
//        fileName=intent.getStringExtra("fileName")
//        fileId = intent.getStringExtra("fileId")
//        editActivityIntent = intent.getStringExtra("fromeditactivity")
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        val sharedPreference =
            this.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        folderId = sharedPreference.getString("folderId", "100").toString()
        folderName = sharedPreference.getString("folderName", "ReNoteAI").toString()
        println("folderId:$folderId")
        println("folderName:$folderName")

        observeData()

        viewBinding.scanTypeLay.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    ScanType.OCR ->{activeScanType = "ocr" }
                    ScanType.DOCUMENT_TYPE->{activeScanType = "document"}
                    ScanType.BOOK_TYPE-> {activeScanType = "book"}
                    ScanType.QR_CODE -> {activeScanType = "qrcode"}
                }


//                if (viewModel.getPreviousScans().value?.isNotEmpty() == true) {
//                    basicAlert()
//                } else {
//                    chang+eScanType(tab?.position)
//                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    ScanType.BOOK_TYPE -> viewBinding.dashedLine.visibility = View.GONE
                    ScanType.QR_CODE -> {
                        viewBinding.overlay.visibility = View.GONE
                        imageAnalyzer?.clearAnalyzer()
                        startCamera()
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        cameraExecutor = Executors.newSingleThreadExecutor()
        val getFileEntities = getFileEntities(this@CameraActivity)
        println("getFileEntitiessss:$getFileEntities")
//        if(editActivityIntent != null) {
//            viewBinding.conformScanBtn.visibility=View.GONE
//            viewBinding.previewLay.visibility=View.GONE
//        }
//         else if(intent.getStringExtra("123").equals("123")){
//            viewBinding.conformScanBtn.visibility=View.VISIBLE
//            viewBinding.previewLay.visibility=View.VISIBLE
//        }
//        else
            if(getFileEntities.isNotEmpty()){
            viewBinding.conformScanBtn.visibility=View.VISIBLE
            viewBinding.previewLay.visibility=View.VISIBLE
        }
        else{
            viewBinding.conformScanBtn.visibility=View.GONE
            viewBinding.previewLay.visibility=View.GONE
        }
    }

    private fun observeData() {
        viewModel.resourseClick.observe(this) { integer ->
            when (integer) {
                R.id.image_capture_button -> {

                    takePhoto()
                    hasGrid = false
                }

                R.id.scanBackBtn -> {
                    onBackPressed()

                }

                R.id.gridBtn -> {
                    toggleGrid()
                }

                R.id.flashBtn -> {
                    selectFlash()
                }

                R.id.btnFlashOff -> {
                    closeFlashAndSelect(ImageCapture.FLASH_MODE_OFF)
                }

                R.id.btnFlashOn -> {
                    closeFlashAndSelect(ImageCapture.FLASH_MODE_ON)
                }

                R.id.btnFlashAuto -> {
                    closeFlashAndSelect(ImageCapture.FLASH_MODE_AUTO)
                }
                R.id.conformScanBtn->{
                    saveCapturedImagesToRoomAndNavigateToEditScreen()
                }

            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun saveCapturedImagesToRoomAndNavigateToEditScreen() {
       val getFileEntities = getFileEntities(this@CameraActivity)
        println("getFileEntities:$getFileEntities")

       // viewModel.saveFilesDetails(getFileEntities)

      // val currentDateAndTime=convertTimestampToDateAndTime(currentTimestamp)
        val documentEntity= DocumentEntity("document_$currentTimestamp","ReNoteAI_$currentTimestamp",currentTimestamp,0L,folderId,false,false,false,"","",0,"","","gDrive","")
       // viewModel.saveDocumentDetail(documentEntity)
                 saveDocumentEntity(this@CameraActivity,documentEntity)
        documentObserveData()
        }
    fun updateDocumentIdInFileEntities(context: Context, newDocumentId: String) {
        val fileEntities = getFileEntities(context)

        // Update the document ID for each FileEntity as needed
        val updatedFileEntities = fileEntities.map { fileEntity ->
            if (fileEntity.documentId.isEmpty()) { // Check any condition you need
                fileEntity.copy(documentId = newDocumentId) // Assuming FileEntity is a data class
            } else {
                fileEntity
            }
        }

        // Save the updated list back to SharedPreferences
        saveFileEntities(context, updatedFileEntities)
    }

    fun saveDocumentEntity(context: Context, documentEntity: DocumentEntity) {
        val sharedPreferences = context.getSharedPreferences("DocumentEntityPreference", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Convert DocumentEntity to JSON String
        val gson = Gson()
        val jsonString = gson.toJson(documentEntity)

        editor.putString("DocumentEntityKey", jsonString)
        editor.apply()
    }
    fun getDocumentEntity(context: Context): DocumentEntity? {
        val sharedPreferences = context.getSharedPreferences("DocumentEntityPreference", Context.MODE_PRIVATE)
        val jsonString = sharedPreferences.getString("DocumentEntityKey", null)

        // Convert JSON String back to DocumentEntity
        return if (jsonString != null) {
            val gson = Gson()
            gson.fromJson(jsonString, DocumentEntity::class.java)
        } else {
            null
        }
    }

    fun saveFileEntities(context: Context, fileEntities: List<FileEntity>) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val jsonString = gson.toJson(fileEntities)
        editor.putString(FILE_ENTITIES_KEY, jsonString)
        editor.apply()
    }
    @SuppressLint("SuspiciousIndentation")
    fun documentObserveData() {
//        //  if (loginUserGoogleId != null) {
//
           // viewModel.getRecentDocumentId()
        val documentEntity=getDocumentEntity(this@CameraActivity)
//
//
       //viewModel.recentDocumentId.observe(this@CameraActivity) {

            if (documentEntity?.id != null) {
//
               println("23213324324324dsd:" + documentEntity?.id )


                val getFileEntities = getFileEntities(this@CameraActivity)
                getFileEntities.forEach { fileEntity ->
                    // Perform operations on each FileEntity object
                    println("File ID: ${fileEntity.id}")
                   println("File Name: ${fileEntity.name}")
                    println("Timestamp: ${fileEntity.createdDate}")
                    println("file uri: ${fileEntity.fileData}")
//                    // Add more properties as needed
//
//                    // Access the document ID if it's set
                    if (fileEntity.documentId.isNotEmpty()) { //
                        println("document ID associated with this file")
                        val recentDocumentId = documentEntity.id
                        println("Recent Document IDDD: $recentDocumentId")
                        val intent =Intent(this@CameraActivity,EditActivity::class.java)
                        intent.putExtra("recentdocumentid",recentDocumentId)
                        startActivity(intent)
       2             }
//                    else if(fileEntity.documentId.isEmpty() && intent.getStringExtra("123").equals("123"))  {
//                        println("No document ID associated with this file and edit activity intent:$editActivityIntent")
//                                  val recentDocumentId=intent.getStringExtra("documentId")
//                        println("Recent Document ID32ewee: $recentDocumentId")
//                        recentDocumentId?.let{
//                            updateDocumentIdInFileEntities(this@CameraActivity, recentDocumentId)
//                            val intent =Intent(this@CameraActivity,EditActivity::class.java)
//                            intent.putExtra("recentdocumentid",recentDocumentId)
//                            intent.putExtra("fromeditActivity","fromeditactivity")
//                            intent.putExtra("123","123")
//                            startActivity(intent)
//                        }
//
//                        //clearAllPreferences(this@CameraActivity)
//
//       }
                    else{
                        println("No document ID associated with this file")
                        val recentDocumentId = documentEntity.id
                        println("Recent Document ID: $recentDocumentId")
                            updateDocumentIdInFileEntities(this@CameraActivity, recentDocumentId)

                        //clearAllPreferences(this@CameraActivity)
                        val intent =Intent(this@CameraActivity,EditActivity::class.java)
                        intent.putExtra("recentdocumentid",recentDocumentId)
                        startActivity(intent)
                    }

//                    // Add any other operations you want to perform on each FileEntity
                }
            } else {
//
            }
        //}
    }


    fun clearAllPreferences(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear() // Clear all data
        editor.apply() // Apply the changes
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
    //function to toggle grid view
    private fun toggleGrid() {
        viewBinding.gridBtn.toggleButton(
            flag = hasGrid,
            rotationAngle = 180f,
            firstIcon = R.drawable.ic_grid_off,
            secondIcon = R.drawable.ic_grid,
        ) { flag ->
            hasGrid = flag
            // prefs.putBoolean(KEY_GRID, flag)
            viewBinding.groupGridLines.visibility = if (flag) View.VISIBLE else View.GONE
        }
    }


    //function that will opens menu to select flashLight options to the user
    private fun selectFlash() = viewBinding.llFlashOptions.circularReveal(viewBinding.flashBtn)

    private fun closeFlashAndSelect(@ImageCapture.FlashMode flash: Int) =
        viewBinding.llFlashOptions.circularClose(viewBinding.flashBtn) {
            flashMode = flash
            viewBinding.flashBtn.setImageResource(
                when (flash) {
                    ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
                    ImageCapture.FLASH_MODE_OFF -> R.drawable.ic_flash_off
                    else -> R.drawable.ic_flash_auto
                }
            )
            imageCapture?.flashMode = flashMode
            // prefs.putInt(KEY_FLASH, flashMode)
        }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "captured_image")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ReNoteAI-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                @RequiresApi(Build.VERSION_CODES.P)
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    // Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    // Log.d(TAG, msg)
                    val uri = output.savedUri ?: return
                    startViewer(uri)
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun startViewer(uri: Uri) {
//        var pictureType = ""
//        if (viewBinding.radioDocument.isChecked) {
//            pictureType = "document"
//        }
//
//        if (viewBinding.radioIDCard.isChecked) {
//            pictureType = "idcard"
//        }
//
//        if (viewBinding.radioBook.isChecked) {
//            pictureType = "book"
//        }

         val intent = Intent(this@CameraActivity, ImageViewer::class.java).apply {

//             if(editActivityIntent != null) {
//                 putExtra("fileId", fileId)
//                 putExtra("fileName", fileName)
//                 putExtra("fileUri", fileUri)
//                 putExtra("currentDocumentId",documentId)
//
//                 putExtra("fromeditactivity","fromeditactivity")
//                 putExtra(EXTRA_PICTURE_URI, uri.toString())
//                 putExtra(EXTRA_PICTURE_TYPE, activeScanType)
//             }else{
                 putExtra(EXTRA_PICTURE_URI, uri.toString())
                 putExtra(EXTRA_PICTURE_TYPE, activeScanType)
             //}
        }
        startActivity(intent)

    }


    private fun captureVideo() {}

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
            }

            imageCapture =
                ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

            // Analyzer
            val analyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor, CVAnalyzer {
                    runOnUiThread(Runnable {
                        if (documentBorders != null) {
                            viewBinding.rectOverlay.setDocumentBorders(
                                documentBorders, documentImageWidth, documentImageHeight
                            )
                        } else {
                            viewBinding.rectOverlay.clear();
                        }

                        if (bitmap != null) {
                            viewBinding.rectOverlay.setBitmap(bitmap)
                        }
                    })
                })
            }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, analyzer, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
//                Toast.makeText(
//                    this,
//                    "Permissions not granted by the user.",
//                    Toast.LENGTH_SHORT
//                ).show()
                showPermissionDialog()
                //finish()
            }
        }
    }

    private fun showPermissionDialog() {
        val permissionDialog = Dialog(this@CameraActivity)
        permissionDialog.setContentView(com.renote.renoteai.R.layout.permission_diaog)
        permissionDialog.setCancelable(false)
        permissionDialog.setCanceledOnTouchOutside(false)
        permissionDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        permissionDialog.findViewById<CardView>(com.renote.renoteai.R.id.btnCancel)
            .setOnClickListener {
                permissionDialog.dismiss()
            }

        permissionDialog.findViewById<CardView>(com.renote.renoteai.R.id.btnOk).setOnClickListener {
            openAppPermissionSettings()
            permissionDialog.dismiss()
        }

        permissionDialog.show()
    }

    fun openAppPermissionSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            val uri = Uri.fromParts("package", packageName, null)
            data = uri
        }
        startActivity(intent)
        recreate()
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // moveTaskToBack(true)
        clearAllPreferences(this@CameraActivity)
        val intent = Intent(this@CameraActivity, MainActivity::class.java)
        startActivity(intent)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onResume() {
        super.onResume()
        val getFileEntities = getFileEntities(this@CameraActivity)
//        if(editActivityIntent != null )
//        {
//            viewBinding.conformScanBtn.visibility=View.GONE
//            viewBinding.previewLay.visibility=View.GONE
//        }
//        else if(intent.getStringExtra("123").equals("123"))
//        {
//            viewBinding.conformScanBtn.visibility=View.VISIBLE
//            viewBinding.previewLay.visibility=View.VISIBLE
////            Glide.with(this@CameraActivity)
////                .load(Uri.parse(getFileEntities[getFileEntities.size-1].fileData))
////                .into(viewBinding.previewImage)
////            viewBinding.previewCount.text = getFileEntities.size.toString()
//        }
//        else
                   if(getFileEntities.isNotEmpty()){
            viewBinding.conformScanBtn.visibility=View.VISIBLE
            viewBinding.previewLay.visibility=View.VISIBLE
            Glide.with(this@CameraActivity)
                .load(Uri.parse(getFileEntities[getFileEntities.size-1].fileData))
                .into(viewBinding.previewImage)
            viewBinding.previewCount.text = getFileEntities.size.toString()
                    }

        else{
            viewBinding.conformScanBtn.visibility=View.GONE
            viewBinding.previewLay.visibility=View.GONE
        }
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "ERROR OpenCV library not found.")
        } else {
            CVLib.initLib()
            Log.d(TAG, "OpenCV library found inside package. Using it!")
        }
    }


    private inner class CVAnalyzer(private val listener: CVAnalyzerListener) :
        ImageAnalysis.Analyzer {

        private var pcnt: Mat? = null

        private var acnt: Mat? = null

        private var success_counter: Int = 0
        private var frame_counter: Int = 0


        // [Reference] https://stackoverflow.com/questions/58102717/android-camerax-analyzer-image-with-format-yuv-420-888-to-opencv-mat
        // Ported from opencv private class JavaCamera2Frame
        private fun Image.yuvToRgba(): Mat {
            val rgbaMat = Mat()

            if (format == ImageFormat.YUV_420_888 && planes.size == 3) {

                val chromaPixelStride = planes[1].pixelStride

                if (chromaPixelStride == 2) { // Chroma channels are interleaved
                    assert(planes[0].pixelStride == 1)
                    assert(planes[2].pixelStride == 2)
                    val yPlane = planes[0].buffer
                    val uvPlane1 = planes[1].buffer
                    val uvPlane2 = planes[2].buffer
                    val yMat = Mat(height, width, CvType.CV_8UC1, yPlane)
                    val uvMat1 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane1)
                    val uvMat2 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane2)
                    val addrDiff = uvMat2.dataAddr() - uvMat1.dataAddr()
                    if (addrDiff > 0) {
                        assert(addrDiff == 1L)
                        Imgproc.cvtColorTwoPlane(yMat, uvMat1, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV12)
                    } else {
                        assert(addrDiff == -1L)
                        Imgproc.cvtColorTwoPlane(yMat, uvMat2, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21)
                    }
                } else { // Chroma channels are not interleaved
                    val yuvBytes = ByteArray(width * (height + height / 2))
                    val yPlane = planes[0].buffer
                    val uPlane = planes[1].buffer
                    val vPlane = planes[2].buffer

                    yPlane.get(yuvBytes, 0, width * height)

                    val chromaRowStride = planes[1].rowStride
                    val chromaRowPadding = chromaRowStride - width / 2

                    var offset = width * height
                    if (chromaRowPadding == 0) {
                        // When the row stride of the chroma channels equals their width, we can copy
                        // the entire channels in one go
                        uPlane.get(yuvBytes, offset, width * height / 4)
                        offset += width * height / 4
                        vPlane.get(yuvBytes, offset, width * height / 4)
                    } else {
                        // When not equal, we need to copy the channels row by row
                        for (i in 0 until height / 2) {
                            uPlane.get(yuvBytes, offset, width / 2)
                            offset += width / 2
                            if (i < height / 2 - 1) {
                                uPlane.position(uPlane.position() + chromaRowPadding)
                            }
                        }
                        for (i in 0 until height / 2) {
                            vPlane.get(yuvBytes, offset, width / 2)
                            offset += width / 2
                            if (i < height / 2 - 1) {
                                vPlane.position(vPlane.position() + chromaRowPadding)
                            }
                        }
                    }

                    val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
                    yuvMat.put(0, 0, yuvBytes)
                    Imgproc.cvtColor(yuvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_I420, 4)
                }
            }

            return rgbaMat
        }

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mat = imageProxy.image?.yuvToRgba();
            val rotation = imageProxy.imageInfo.rotationDegrees
            imageProxy.close()
            mat?.let {
                val result = processImg(mat, rotation)
                Log.d(TAG, "cv width ${result.cols()} height ${result.rows()}")

                if (bitmap == null) {
                    bitmap =
                        Bitmap.createBitmap(mask!!.cols(), mask!!.rows(), Bitmap.Config.ARGB_8888)
                }
                Utils.matToBitmap(mask, bitmap)
                listener()
            }
        }

        fun processImg(mat: Mat, rotation: Int): Mat {
            // get current camera frame as OpenCV Mat object
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB)

            val resized = Mat()

            CVLib.getReducedImage(mat.nativeObjAddr, resized.nativeObjAddr)

            mask = Mat()
            CVLib.getDocumentMask(resized.nativeObjAddr, mask!!.nativeObjAddr)

            val cnt = Mat()
            CVLib.getDocumentContour(mask!!.nativeObjAddr, cnt.nativeObjAddr)

            val approxCnt = Mat()
            val found = CVLib.getDocumentApproxContour(cnt.nativeObjAddr, approxCnt.nativeObjAddr)


            if (found) {
                if (pcnt == null) {
                    pcnt = Mat()
                }

                val is_valid = CVLib.isContourValid(approxCnt.nativeObjAddr, pcnt!!.nativeObjAddr)
                pcnt = approxCnt;

                if (is_valid) {
                    if (acnt == null) {
                        acnt = Mat()
                    }

                    CVLib.addContour(approxCnt.nativeObjAddr, acnt!!.nativeObjAddr)
                    success_counter += 1
                }

            }

            if (frame_counter >= 30) {
                frame_counter = 0;
                if (success_counter > 20) {
                    val bcnt = Mat()
                    CVLib.divContour(acnt!!.nativeObjAddr, bcnt.nativeObjAddr, success_counter)

                    val resizedCnt = Mat()
                    CVLib.resizeContour(
                        mat.nativeObjAddr,
                        resized.nativeObjAddr,
                        bcnt.nativeObjAddr,
                        resizedCnt.nativeObjAddr
                    )

                    // This is ugly but what can you do
                    documentBorders = DocumentBorders(resizedCnt)
                    when (rotation) {
                        0 -> {
                            documentImageWidth = mat.cols()
                            documentImageHeight = mat.rows()
                        }

                        90 -> {
                            documentBorders?.rotate(
                                DocumentBorders.Rotation.ROTATE_90, mat.cols(), mat.rows()
                            )

                            documentImageWidth = mat.rows()
                            documentImageHeight = mat.cols()
                        }

                        180 -> {
                            documentImageWidth = mat.cols()
                            documentImageHeight = mat.rows()
                        }

                        270 -> {
                            documentBorders?.rotate(
                                DocumentBorders.Rotation.ROTATE_180, mat.cols(), mat.rows()
                            )

                            documentImageWidth = mat.rows()
                            documentImageHeight = mat.cols()
                        }
                    }

                } else {
                    documentBorders = null
                }
                acnt = Mat()

                success_counter = 0;
            }
            frame_counter += 1

            System.gc()
            return mat
        }
    }

    companion object {
        const val PREFS_NAME = "MyAppPrefs"
         const val FILE_ENTITIES_KEY = "fileEntities"
        private const val TAG = "ReNoteAI"
        private const val FILENAME_FORMAT = "yyyy_MM_dd_HH_mm_ss_SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    fun generateBase64EncodedSHA1(input: String): String {
        try {
            val info = packageManager.getPackageInfo(
                packageName, PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hash = Base64.encodeToString(
                    md.digest(), Base64.DEFAULT
                )
                Log.d("KeyHash", "KeyHash:$hash")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("KeyHash", e.toString())
        } catch (e: NoSuchAlgorithmException) {
        }
        // Convert the input string to bytes
        val bytes = input.toByteArray()

        // Compute the SHA1 hash
        val sha1Digest = MessageDigest.getInstance("SHA-1").digest(bytes)

        // Base64 encode the SHA1 hash
        return Base64.encodeToString(sha1Digest, Base64.NO_WRAP)
    }
   // val currentTimestamp: Long = System.currentTimeMillis()
    fun convertTimestampToDateAndTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }
}

