package com.renote.renoteai.utils

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object FileUtil {
    fun saveFile(rootPath: String, fileName:String, bitmap: Bitmap?) : String?{
        bitmap?:return null

        val file = File(rootPath,fileName)
        file.createNewFile()

        try {
            // make a new bitmap from your file
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream) // YOU can also save it in JPEG
            stream.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("created file path ${file.path}")
        return file.path
    }
}