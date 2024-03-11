package com.renote.renoteai.ui.activities.camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.renote.renoteai.databinding.ActivityImageViewerBinding
import com.renote.renoteai.ui.activities.camera.libs.CVLib
import com.renote.renoteai.ui.activities.camera.libs.DocLib
import com.renote.renoteai.ui.activities.camera.scanutil.DocumentBorders
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File
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

    window.setFlags(
      WindowManager.LayoutParams.FLAG_SECURE,
      WindowManager.LayoutParams.FLAG_SECURE
    )
    supportActionBar?.hide()

      viewBinding.warpButton.setOnClickListener {
        deleteInternalStorageDirectoryy()
        warpImage()
      }

    val uri = Uri.parse(intent.getStringExtra(EXTRA_PICTURE_URI))
    pictureType = intent.getStringExtra(EXTRA_PICTURE_TYPE);

    val imageDecoder = ImageDecoder.createSource(contentResolver, uri)
    val bitmap = ImageDecoder.decodeBitmap(imageDecoder)

    val bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);

    contentResolver.delete(uri, null, null)

    val mat = Mat()
    
    Utils.bitmapToMat(bmp32, mat)


    // get current camera frame as OpenCV Mat object
    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB)

    original = mat.clone()

    viewBinding.borderOverlay.post(Runnable {
      val approxCnt = DocLib.detect(mat)
      if(approxCnt != null) {
        val documentBorders = DocumentBorders(approxCnt)
        viewBinding.borderOverlay.setDocumentBorders(documentBorders, mat.cols(), mat.rows())
      }
    })

    val result = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(mat, result)
    viewBinding.imageView.setImageBitmap(result)
  }

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

      val result = Bitmap.createBitmap(warped.cols(), warped.rows(), Bitmap.Config.ARGB_8888)
      Utils.matToBitmap(warped, result)

      val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
      .format(System.currentTimeMillis())
      val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
          put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
        }
      }

      val uri = contentResolver.insert(
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
          contentValues
      ) ?: throw IOException("Could not open uri")

      val stream = contentResolver.openOutputStream(uri) ?: throw IOException("Could not open output stream")

      result.compress(Bitmap.CompressFormat.JPEG, 50, stream)
      stream.close()

      if(pictureType == "book") {
        val intent = Intent(this, BookViewer::class.java).apply {
          putExtra(EXTRA_PICTURE_URI, uri.toString())
        }
        startActivity(intent)

      } else if(pictureType == "idcard") {
        val intent = Intent(this, IDCardViewer::class.java).apply {
          putExtra(EXTRA_PICTURE_URI, uri.toString())
        }
        startActivity(intent)

      } else {
        val intent = Intent(this, ImageFilter::class.java).apply {
          putExtra(EXTRA_PICTURE_URI, uri.toString())
        }
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


  override fun onDestroy() {
    super.onDestroy()
    deleteInternalStorageDirectoryy()
  }

  companion object {
    private const val TAG = "CameraXApp"
    private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

  }
}

