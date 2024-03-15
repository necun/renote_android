package com.renote.renoteai.ui.presentation.home.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.Drive
import com.renote.renoteai.R
import com.renote.renoteai.database.dao.DocumentDao
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.di.provideDocumentDatabase
import kotlinx.coroutines.flow.first
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class DocumentSyncWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    //private val driveService: Drive = getDriveService(context)
    private val driveService: Drive = getDriveService(context)

    override suspend fun doWork(): Result {
        try {
            //  val documentDao = DatabaseBuilder.getInstance(applicationContext).documentDao()
            val database = provideDocumentDatabase(applicationContext)
            val documentDao = database.documentDao()
            // Fetch unsynced documents. Assuming getUnsyncedDocuments() returns a Flow, use .first() to get the current list.
            val unsyncedDocuments = documentDao.getAllUnsyncedDocumentIds().first()

            unsyncedDocuments.forEach { document ->
                // Simulate a task like uploading to Google Drive.
//                if (uploadDocument(document)) {
                    val (uploadSuccess, fileId) = uploadDocument(document)
                if (uploadSuccess && fileId != null) {
                    // Now also passing the fileId to update the record accordingly
                    documentDao.updateDocumentWithDriveId(document.id, fileId)
                }

                    // Mark document as synced if upload is successful.
                   // documentDao.updateDocumentWithDriveId(document.id,)
                //}
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    fun getDriveService(context: Context): Drive {
        GoogleSignIn.getLastSignedInAccount(context).let { googleAccount ->
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = googleAccount!!.account!!
            return Drive.Builder(
                //AndroidHttp().newCompatibleTransport(),
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(context.getString(R.string.app_name))
                .build()
        }
        var tempDrive: Drive
        return tempDrive
    }

    private suspend fun uploadDocument(
        document: DocumentEntity
    ): Pair<Boolean, String?> {
        return try {
            // Get the file path from the document entity
            val filePath = document.fileData

            println("filePath:$filePath")
            // Get the file content from the file path
            val fileContent = getFileContentFromPath(filePath, applicationContext)
            println("fileContent:$fileContent")

            if (fileContent != null) {
                // The MIME type of the folder
                val folderMimeType = "application/vnd.google-apps.folder"
                // Search for the ReNoteAI folder
                val folderSearchQuery =
                    "name = 'ReNoteAI' and mimeType = '$folderMimeType' and trashed = false"
                val folderSearch = driveService.files().list().setQ(folderSearchQuery).execute()
                var folderId: String? = folderSearch.files.firstOrNull()?.id

                // If the ReNoteAI folder doesn't exist, create it
                if (folderId == null) {
                    val folderMetadata = com.google.api.services.drive.model.File().apply {
                        name = "ReNoteAI"
                        mimeType = folderMimeType
                    }
                    folderId = driveService.files().create(folderMetadata).execute().id
                }

                // Prepare the file to be uploaded
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = document.name
                    parents = listOf(folderId)
                }

                val mediaContent = InputStreamContent(null, ByteArrayInputStream(fileContent))

                // Upload the file
               val uploadFile =  driveService.files().create(fileMetadata, mediaContent).execute()
                Pair(true,uploadFile.id)

                //true // Return true if upload succeeds
            } else {
                Pair(false, null)
               // false // Return false if file content is null or cannot be obtained
            }
        } catch (e: Exception) {
            e.printStackTrace()
           // false // Return false if upload fails
            Pair(false, null)

        }
    }

    // Function to get file content from path
    private fun getFileContentFromPath(filePath: String, context: Context): ByteArray? {
        return try {

            val uri = Uri.parse(filePath)
            val fileDestin = uri.path

            val file = File(fileDestin.toString())
            Log.d("FilePath", "File path: $filePath")
            Log.d("FileExistence", "File exists: ${file.exists()}")
            FileInputStream(file).use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }catch (e:Exception){
            e.printStackTrace()
            null
        }
    }

}
