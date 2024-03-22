package com.renote.renoteai.ui.activities.filteredit

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
import android.provider.MediaStore
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.gson.Gson
import com.renote.renoteai.R
import com.renote.renoteai.database.tables.FileEntity
import com.renote.renoteai.databinding.FilterEditActivityDataBinding
import com.renote.renoteai.ui.activities.edit.EditActivity
import org.koin.android.ext.android.inject
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FilterEditActivity : AppCompatActivity() {

    private lateinit var binding: FilterEditActivityDataBinding
    private val viewModel: FilterEditViewModel by inject()
    private var original: Mat? = null
    private var result: Mat? = null

    var enhancedImageType: String = ""
    private lateinit var uri: Uri
    private lateinit var imageUri: String
    private lateinit var fileName: String
    private lateinit var fileId: String
    var originalImage: String = ""

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this@FilterEditActivity, R.layout.activity_filter_edit
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

//        uri = Uri.parse(intent.getStringExtra("URI"))
        //      println("uri:$uri")

        imageUri = intent.getStringExtra("imageUri").toString()
        fileName = intent.getStringExtra("fileName").toString()
        fileId = intent.getStringExtra("fileId").toString()
//        Glide.with(this)
//            .load(Uri.parse(imageUri))
//            .into(findViewById(R.id.imageView2))
        initImageDisplay(this@FilterEditActivity, fileName)
        //  saveImageToPicturesDirectory(this@FilterEditActivity, imageUri.toUri(), "captured_image.jpg")

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

        // binding.imageView2.setImageURI(imageUri)
        //  aiFilter()
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
//        doNoFilter()
//        }

    }

    private fun initImageDisplay(context: Context, fileName: String) {
        originalImage = "OriginalImage"
        val remoteAIDirectory = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "ReNoteAI-Image-Original"
        )
        println(remoteAIDirectory)
        val f = File(remoteAIDirectory, fileName)
        System.out.println("122334465=" + f)
        val b = BitmapFactory.decodeStream(FileInputStream(f))
        binding.imageView2.setImageBitmap(b)
    }


    fun updateFileUriBasedOnFileId(context: Context, fileId: String, newFileUri: String) {
        // Retrieve the current list of FileEntities
        val fileEntities = getFileEntities(context)

        // Update the fileUri for the matching FileEntity
        val updatedFileEntities = fileEntities.map { fileEntity ->
            if (fileEntity.id == fileId) {
                println("dsdfdfsdff:${fileEntity.id} and ${fileId} dfdfsdff:$newFileUri")
                fileEntity.copy(fileData = newFileUri)

            } else {
                fileEntity
            }
        }

        // Save the updated list back to SharedPreferences
        saveFileEntities(context, updatedFileEntities)
    }

    fun saveFileEntities(context: Context, fileEntities: List<FileEntity>) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val jsonString = gson.toJson(fileEntities)
        editor.putString(FILE_ENTITIES_KEY, jsonString)
        editor.apply()
    }

    fun saveImageToPicturesDirectory(context: Context, imageUri: Uri, fileName: String) {
        // Open an InputStream to read the image data from the URI
        val inputStream = context.contentResolver.openInputStream(imageUri)

        // Define the path for the "RemoteAI" folder within the app-specific Pictures directory
        val remoteAIDirectory = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "ReNoteAI-Image-Input"
        )
        if (!remoteAIDirectory.exists()) {
            remoteAIDirectory.mkdirs() // Create the "RemoteAI" directory if it doesn't exist
        }

        // Create a File instance for the new image file within the "RemoteAI" folder
        val imageFile = File(remoteAIDirectory, fileName)

        // Copy the InputStream to the output file in the "RemoteAI" directory
        inputStream?.use { input ->
            FileOutputStream(imageFile).use { output ->
                input.copyTo(output)
            }
        }
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
                    doNoFilter(this@FilterEditActivity)
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

                R.id.imgRight -> {
                    saveFilteredImageToInternalStorage()
                }
            }
        }
    }

    private fun doNoFilter(context: Context) {
        val input_path =
            this@FilterEditActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() +
                    "/ReNoteAI-Image-Original/"
        val input = input_path + fileName

        val remoteAIDirectory = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "ReNoteAI-Image-Output"
        )
        if (!remoteAIDirectory.exists()) {
            remoteAIDirectory.mkdirs() // Create the directory if it doesn't exist
        }
        val outputFile = File(remoteAIDirectory, "filter_image.jpg")

        var inputChannel: FileChannel? = null
        var outputChannel: FileChannel? = null

        try {
            inputChannel = FileInputStream(input).channel
            outputChannel = FileOutputStream(outputFile).channel
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size())
        } finally {
            inputChannel?.close()
            outputChannel?.close()
        }
    }

    val currentTimestamp: Long = System.currentTimeMillis()
    fun convertTimestampToDateAndTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }

    private fun saveFilteredImageToInternalStorage() {
        val output_path =
            this@FilterEditActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .toString() + "/ReNoteAI-Image-Output/"
        val file = File(output_path, "filter_image.jpg")
        val currentDateAndTime = convertTimestampToDateAndTime(currentTimestamp)
        val filteredFileName = "ReNoteAI_$currentDateAndTime.jpg"
        val directory =
            File(this.filesDir, "ReNoteAI") // Directory path within app's internal storage
        if (!directory.exists()) {
            directory.mkdirs() // Create the directory if it doesn't exist
        }

        val newFile = File(directory, filteredFileName)
        val fileUri: Uri = Uri.fromFile(newFile)
        try {
            val inputStream = FileInputStream(file)
            val outputStream = FileOutputStream(newFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle error
        }
        updateFileUriBasedOnFileId(this@FilterEditActivity, fileId, fileUri.toString())
        startActivity(Intent(this@FilterEditActivity, EditActivity::class.java))
    }

    private fun blackAndWhiteFilter() {
        val input_path =
            this@FilterEditActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() +
                    "/ReNoteAI-Image-Original/"

        val output_path =
            this@FilterEditActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .toString() + "/ReNoteAI-Image-Output/"
        val file = File(
            this@FilterEditActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .toString() + "/ReNoteAI-Image-Output/"
        )

        if (!file.exists()) {
            file.mkdirs()
        }
        val input = input_path + fileName
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val module = py.getModule("script")

        val fact = module["black_and_white_filter"]
        fact?.call(input, output_path)
        val f = File(output_path, "filter_image.jpg")
        System.out.println("122334465=")
        val b = BitmapFactory.decodeStream(FileInputStream(f))
        if (b != null) {
            enhancedImageType = "filter_image"
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
            this@FilterEditActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() +
                    "/ReNoteAI-Image-Original/"

        val output_path =
            this@FilterEditActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .toString() + "/ReNoteAI-Image-Output/"
        val file = File(
            this@FilterEditActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .toString() + "/ReNoteAI-Image-Output/"
        )
        if (!file.exists()) {
            file.mkdirs()
        }
        val input = input_path + fileName
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val module = py.getModule("script")

        val fact = module["grey_filter"]
        fact?.call(input, output_path)
        val f = File(output_path, "filter_image.jpg")
        System.out.println("122334465=")
        val b = BitmapFactory.decodeStream(FileInputStream(f))
        if (b != null) {
            enhancedImageType = "filter_image"
            binding.imageView2.setImageBitmap(b)


            //viewBinding.greyFilterImageView.setImageBitmap(b)
            //binding.greyFilterProgressbar.visibility = View.GONE


        } else {
            //binding.greyFilterProgressbar.visibility = View.VISIBLE
            //viewBinding.greyFilterImageView.setImageResource(R.drawable.ic_no_picture)
        }

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

    private fun softFilter() {
        val input_path =
            this@FilterEditActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .toString() + "/ReNoteAI-Image-Input/"
        val output_path =
            this@FilterEditActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .toString() + "/ReNoteAI-Image-Output/"
        val file = File(
            this@FilterEditActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .toString() + "/ReNoteAI-Image-Output/"
        )
        if (!file.exists()) {
            file.mkdirs()
        }
        val input = input_path + "captured_image" + ".jpg"

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val module = py.getModule("script")

        val fact = module["soft_filter"]
        fact?.call(input, output_path)
        val f = File(output_path, "filter_image.jpg")
        System.out.println("122334465=")
        val b = BitmapFactory.decodeStream(FileInputStream(f))
        if (b != null) {
            enhancedImageType = "filter_image"
            binding.imageView2.setImageBitmap(b)
            // viewBinding.softFilterImageView.setImageBitmap(b)
            // binding.softFilterProgressbar.visibility = View.GONE
        } else {
            // viewBinding.softFilterImageView.setImageResource(R.drawable.ic_no_picture);
            //binding.softFilterProgressbar.visibility = View.VISIBLE

        }
    }


//    private fun softFilter(context: Context, imageUri: Uri) {
//        val imagePath = getPathFromUri(context, imageUri) ?: return
//        println("imagePath:$imagePath")
//
//        // Start Python if not already started
//        if (!Python.isStarted()) {
//            Python.start(AndroidPlatform(context))
//        }
//
//        val outputPath = imagePath
//        // Execute the Python script
//        val py = Python.getInstance()
//        val pyModule = py.getModule("script")
//        val fact = pyModule["soft_filter"]
//        println("fact:$fact")
//        fact?.call(imagePath,outputPath)
//        // Load the modified image back into ImageView
//        val modifiedImageUri = getFileContentUri(context, File(outputPath))
//        println("outputPath:$outputPath")
//        val imageView = binding.imageView2 // Assuming you have an ImageView named imageView2 in your layout
//
//        // Invalidate Glide cache and load the modified image
//        Glide.with(context)
//            .load(modifiedImageUri)
//            .skipMemoryCache(true) // Skip memory cache
//            .diskCacheStrategy(DiskCacheStrategy.NONE) // Skip disk cache
//            .into(imageView)
//    }

    fun getFileContentUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "com.renote.renoteai.provider",
            file
        )
    }

    fun getPathFromUri(context: Context, uri: Uri): String? {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
        return null
    }

//    private fun doNoFilter() {
//
//        val output_path =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//                .toString() + "/CameraX-Image-Input/"
//        val f = File(output_path, "cropped_image.jpg")
//        val b = BitmapFactory.decodeStream(FileInputStream(f))
//        binding.imageView2.setImageBitmap(b)
//    }

    @SuppressLint("SuspiciousIndentation")
    private fun aiFilter() {

        val input_path =
            this@FilterEditActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() +
                    "/ReNoteAI-Image-Original/"
        val output_path =
            this@FilterEditActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .toString() + "/ReNoteAI-Image-Output/"
        val file = File(
            this@FilterEditActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .toString() + "/ReNoteAI-Image-Output/"
        )
        if (!file.exists()) {
            file.mkdirs()
        }
        val input = input_path + fileName
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val module = py.getModule("script")

        val fact = module["ai_filter"]
        fact?.call(input, output_path)
        val f = File(output_path, "filter_image.jpg")
        System.out.println("122334465=")
        val b = BitmapFactory.decodeStream(FileInputStream(f))
        if (b != null) {
            enhancedImageType = "filter_image"
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

    companion object {
        private const val PREFS_NAME = "MyAppPrefs"
        private const val FILE_ENTITIES_KEY = "fileEntities"
    }

}