package com.renote.renoteai.ui.activities.camera

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.renote.renoteai.R
import com.renote.renoteai.UploadResponse
import com.renote.renoteai.api
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.databinding.EmailDataBinding
import com.renote.renoteai.ui.activities.camera.viewmodel.EmailViewModel
import com.renote.renoteai.ui.main.MainActivity
import com.renote.renoteai.ui.presentation.home.viewmodel.HomeFragmentViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit

import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.android.inject
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class EmailActivity : AppCompatActivity() {
    //    curl --location 'http://13.200.238.163:5001/send-email' \
//    --form 'to_email="snehal.trapsiya@gmail.com"' \
//    --form 'subject="This is test"' \
//    --form 'message="Hello please check you image"' \
//    --form 'image=@"/C:/Users/Administrator/Downloads/Necun/DocumentScanner/Sample_Images/2.jpeg"'
    val viewModel: EmailViewModel by inject()
    var enhancedImageType: String = ""
    private lateinit var viewBinding: EmailDataBinding

    private val viewModelHome: HomeFragmentViewModel by inject()
    val docEntities = mutableListOf<DocumentEntity>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        viewBinding = DataBindingUtil.setContentView(
            this@EmailActivity,
            R.layout.activity_email
        )
        viewBinding.lifecycleOwner = this
        viewBinding.viewmodel = viewModel

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

     //  val uri = Uri.parse(intent.getStringExtra(EXTRA_PICTURE_URI))


        enhancedImageType = intent.getStringExtra("enhancedImageType").toString()
        println("enhancedImageType:$enhancedImageType")
        initImageDisplay()

        observeData()

    }

    private fun observeData() {
        viewModel.resourseClick.observe(this) { integer ->
            when (integer) {
                R.id.emailImageView -> {
                    //enhancedImageType = intent.getStringExtra("enhancedImageType").toString()
                    saveImage()
                }

                R.id.cameraRetakeImgVw->{
                    val intent = Intent(this@EmailActivity, CameraActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteInternalStorageDirectoryy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        deleteInternalStorageDirectoryy()
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)

        // Optional: if you want to finish the current activity
        finishAffinity()
    }

    fun deleteInternalStorageDirectoryy() {
        if (ContextCompat.checkSelfPermission(
                this@EmailActivity,
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
                this@EmailActivity,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@EmailActivity,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                14
            )
            return false
        }
        return true
    }

    //function to save the filtered image into the app internal storage.....
    private fun saveImage() {
        val enhancedImageType = intent.getStringExtra("enhancedImageType")
        println("454354353346546"+enhancedImageType)
        val output_path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Output/"
        val file = File(output_path, "$enhancedImageType.jpg")

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "$enhancedImageType.jpg"


        val directory = File(this.filesDir, "ReNoteAI") // Directory path within app's internal storage
        if (!directory.exists()) {
            directory.mkdirs() // Create the directory if it doesn't exist
        }

//        val storageDir: File =
//            this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

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
            // Now 'newFile' contains the copied image file in the internal storage of your app
            Toast.makeText(this@EmailActivity,"Image Saved",Toast.LENGTH_SHORT).show()
            saveToRoom(fileUri,fileName)
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle error
        }

    }

    //function to save the file details to the room database after storing it in the internal storage........
    private fun saveToRoom(fileUri: Uri, fileName: String) {
//        val uri:Uri = Uri.parse(fileUri.toString())
        val fileType = "jpg"

       // println("")
        docEntities.add(
            DocumentEntity(
                id = "100",
                name = fileName,
                createdDate = 10005000,
                updatedDate = 10005000,
                fileData = fileUri.toString(),
                isSynced = false,
                isPin = false,
                isFavourite = false,
                folderId = "12",
                openCount = 30,
                localFilePathIos = "aaaaaaaaaaaaa",
                localFilePathAndroid = "ssssssssssssss",
                tagId = "12345",
                driveType = "gDrive",
                fileExtension = fileType
            )
        )

        println("savedFileUri:$fileUri")

        viewModelHome.saveDocumentsDetails(docEntities)
        startActivity(Intent(this@EmailActivity, MainActivity::class.java))
        finishAffinity()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun initSendEmail() {
        // viewBinding.progressBar.visibility = View.VISIBLE
        startActivity(Intent(this@EmailActivity, EmailPopUpActivity::class.java))
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val userEmailId = sharedPreference.getString("userEmailId", "defaultName")
        println("userEmailId:$userEmailId")
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

                  val enhancedImageType = intent.getStringExtra("enhancedImageType")
                     println("454354353346546"+enhancedImageType)
        val output_path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Output/"
        val file = File(output_path, "$enhancedImageType.jpg")
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("image", file.name, requestBody)
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(6, TimeUnit.MINUTES)
            .writeTimeout(6, TimeUnit.MINUTES)
            .readTimeout(6, TimeUnit.MINUTES)
            .build()
        val retrofit = Retrofit.Builder().baseUrl("http://13.200.238.163:5001/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(api::class.java)


//            val response =  retrofit.sendEmail(part,"agarwalkomal2030@gmail.com","filtered image","please the image in your mail")
//                   Log.d("response retrofit",response)
        val listCall: Call<UploadResponse> = retrofit.sendEmail(
            part,
            userEmailId.toString(),
            "Your Document is ready",
            "[PFA] Please find attached"
        )

        listCall.enqueue(object : Callback<UploadResponse> {
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {

                try {
                    val response = response.body()
                    if (response != null) {

                        //viewBinding.progressBar.visibility = View.GONE
                        //  val intent = Intent(this@EmailActivity, EmailPopUpActivity::class.java)
                        //startActivity(intent)
                        deleteInternalStorageDirectoryy()
                        //showSuccessDialog()
//                            Toast.makeText(
//                                this@EmailActivity,
//                                "" + response.message,
//                                Toast.LENGTH_LONG
//                            ).show()
                    } else {

//                        Toast.makeText(
//                            this@EmailActivity,
//                            "please try again after sometime",
//                            Toast.LENGTH_LONG
//                        ).show()
                    }
                } catch (e: Exception) {
//                    Toast.makeText(
//                        this@EmailActivity,
//                        "please try again after sometime",
//                        Toast.LENGTH_LONG
//                    ).show()
                }
            }


            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
//                Toast.makeText(
//                    this@EmailActivity,
//                    "please try again after sometime",
//                    Toast.LENGTH_LONG
//                ).show()

            }
        })


    }

    private fun showSuccessDialog() {
        val successDialog = Dialog(this@EmailActivity)
        successDialog.setContentView(R.layout.mail_sent_dialog)
        successDialog.setCancelable(false)
        successDialog.setCanceledOnTouchOutside(true)
        successDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        successDialog.show()
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
                    Toast.makeText(this@EmailActivity, "Permission Granted", Toast.LENGTH_LONG)
                        .show()
                } else {
                    ActivityCompat.requestPermissions(
                        this@EmailActivity,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        14
                    )
                }
            }
        }
    }

    private fun initImageDisplay() {


        val output_path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/CameraX-Image-Output/"

        println(output_path)
        val f = File(output_path, "$enhancedImageType.jpg")
        System.out.println("122334465=" + f)
        val b = BitmapFactory.decodeStream(FileInputStream(f))
        viewBinding.imageView.setImageBitmap(b)

    }
}