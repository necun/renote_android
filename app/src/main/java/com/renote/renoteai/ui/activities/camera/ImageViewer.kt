package com.renote.renoteai.ui.activities.camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
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
import com.google.gson.Gson
import com.renote.renoteai.database.tables.FileEntity
import com.renote.renoteai.databinding.ActivityImageViewerBinding
import com.renote.renoteai.ui.activities.camera.libs.CVLib
import com.renote.renoteai.ui.activities.camera.libs.DocLib
import com.renote.renoteai.ui.activities.camera.scanutil.DocumentBorders
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImageViewer : AppCompatActivity() {
  private lateinit var viewBinding: ActivityImageViewerBinding
  private var original : Mat? = null
  private var pictureType : String? = ""

  @SuppressLint("WrongThread")
  @RequiresApi(Build.VERSION_CODES.P)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = ActivityImageViewerBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)
//
    window.setFlags(
      WindowManager.LayoutParams.FLAG_SECURE,
      WindowManager.LayoutParams.FLAG_SECURE
    )
    supportActionBar?.hide()
//
      viewBinding.warpButton.setOnClickListener {
        deleteInternalStorageDirectoryy()
        warpImage()
      }
    viewBinding.retakeButton.setOnClickListener{
      deleteInternalStorageDirectoryy()
      val intent = Intent(this, CameraActivity::class.java)
      startActivity(intent)
    }
//
    val uri = Uri.parse(intent.getStringExtra(EXTRA_PICTURE_URI))
    pictureType = intent.getStringExtra(EXTRA_PICTURE_TYPE);
//
    val imageDecoder = ImageDecoder.createSource(contentResolver, uri)
    val bitmap = ImageDecoder.decodeBitmap(imageDecoder)
//
    val bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//
    contentResolver.delete(uri, null, null)
//
    val mat = Mat()
//
    Utils.bitmapToMat(bmp32, mat)
//
//
//    // get current camera frame as OpenCV Mat object
    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB)
//
    original = mat.clone()
//
    viewBinding.borderOverlay.post(Runnable {
      val approxCnt = DocLib.detect(mat)
      if(approxCnt != null) {
        val documentBorders = DocumentBorders(approxCnt)
        viewBinding.borderOverlay.setDocumentBorders(documentBorders, mat.cols(), mat.rows())
      }
    })
//
    val result = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(mat, result)
    viewBinding.imageView.setImageBitmap(result)
  }

  @SuppressLint("SuspiciousIndentation")
  private fun warpImage()
  {
    val border = viewBinding.borderOverlay.getDocumentBorders()
    val mat = original
    if(mat != null && border != null) {
      border.rescaleSpecial(
          viewBinding.borderOverlay.getWidth().toFloat(),
          viewBinding.borderOverlay.getHeight().toFloat(),
          mat.cols().toFloat(),
          mat.rows().toFloat(), mat.rows() > mat.cols() )
      val cnt = border.toMat()
      val warped = Mat()
      CVLib.getDocumentWarped(mat.nativeObjAddr, warped.nativeObjAddr, cnt.nativeObjAddr)
//
      val result = Bitmap.createBitmap(warped.cols(), warped.rows(), Bitmap.Config.ARGB_8888)
      Utils.matToBitmap(warped, result)
//
      val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
      .format(System.currentTimeMillis())
      val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "captured_image")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
          put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ReNoteAI-Image")
        }
      }
//
      val uri = contentResolver.insert(
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
          contentValues
      ) ?: throw IOException("Could not open uri")
//
      val stream = contentResolver.openOutputStream(uri) ?: throw IOException("Could not open output stream")
//
      result.compress(Bitmap.CompressFormat.JPEG, 50, stream)
      stream.close()
//
      if(pictureType == "book") {
        val intent = Intent(this, BookViewer::class.java).apply {
         // putExtra(EXTRA_PICTURE_URI, uri.toString())
        }
        startActivity(intent)
//
      } else if(pictureType == "idcard") {
       val intent = Intent(this, IDCardViewer::class.java).apply {
         // putExtra(EXTRA_PICTURE_URI, uri.toString())
        }
        startActivity(intent)
//
      } else {
        val output_path =
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .toString() + "/ReNoteAI-Image/"
        val file = File(output_path, "captured_image.jpg")
             var fileEntities = mutableListOf<FileEntity>()
        val currentTmStmp=convertTimestampToDateAndTime(timestamp = currentTimestamp)
        val fileName = "RenoteAI_${currentTmStmp}"

        val directory =
          File(this.filesDir, "ReNoteAI") // Directory path within app's internal storage
        if (!directory.exists()) {
          directory.mkdirs() // Create the directory if it doesn't exist
        }

        val newFile = File(directory, fileName)
        val fileUri:Uri = Uri.fromFile(newFile)
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




        //fileEntities.add(FileEntity("file_$currentTimestamp",fileName,currentTimestamp,0L,"",false,false,false,fileUri.toString(),0,"","","gDrive","jpg"))
        val fileEntity = FileEntity("file_$currentTimestamp",fileName,currentTimestamp,0L,"",false,false,false,fileUri.toString(),0,"","","gDrive","jpg")
        saveFileEntities(this@ImageViewer,fileEntity)
       val intent = Intent(this, CameraActivity::class.java)
         startActivity(intent)


      }
    }
  }

  fun deleteInternalStorageDirectoryy() {
    if (ContextCompat.checkSelfPermission(
        this@ImageViewer,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
      ) == PackageManager.PERMISSION_DENIED
    ) {
      val input_pathh = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
          .toString() + "/ReNoteAI-Image/"
      )


      val input_path = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
          .toString() + "/ReNoteAI-Image-Input/"
      )
      val output_pathh = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
          .toString() + "/ReNoteAI-Image-Output/"
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
        this@ImageViewer,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      ActivityCompat.requestPermissions(
        this@ImageViewer,
        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
        14
      )
      return false
    }
    return true
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requestCode) {
      14 -> {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          Toast.makeText(this@ImageViewer, "Permission Granted", Toast.LENGTH_LONG)
            .show()
        } else {
          ActivityCompat.requestPermissions(
            this@ImageViewer,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            14
          )
        }
      }
    }
  }

  fun saveFileEntities(context: Context, newFileEntity: FileEntity) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val gson = Gson()

    // Retrieve the existing list of file entities
    val existingFileEntitiesJson = prefs.getString(FILE_ENTITIES_KEY, null)
    val existingFileEntities: MutableList<FileEntity> = if (existingFileEntitiesJson != null) {
      gson.fromJson(existingFileEntitiesJson, Array<FileEntity>::class.java).toMutableList()
    } else {
      mutableListOf()
    }

    // Add the new file entity to the list
    existingFileEntities.add(newFileEntity)

    // Save the updated list back to shared preferences
    val editor = prefs.edit()
    val updatedFileEntitiesJson = gson.toJson(existingFileEntities)
    editor.putString(FILE_ENTITIES_KEY, updatedFileEntitiesJson)
    editor.apply()
  }

  // Modify the part of the code where you're calling saveFileEntities
// Instead of creating a list and passing it, you will now create a single FileEntity and pass it.


  override fun onDestroy() {
    super.onDestroy()
    deleteInternalStorageDirectoryy()
  }

  companion object {
    private const val TAG = "ReNoteAIApp"
    private const val FILENAME_FORMAT = "yyyy_MM_dd_HH_mm_ss_SSS"
    private const val PREFS_NAME = "MyAppPrefs"
    private const val FILE_ENTITIES_KEY = "fileEntities"

  }
  fun convertTimestampToDateAndTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.getDefault())
    val date = Date(timestamp)
    return sdf.format(date)
  }
  val currentTimestamp: Long = System.currentTimeMillis()
}

