package com.renote.renoteai.ui.presentation.home.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.RawRes
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.Drive
import com.renote.renoteai.R
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.di.provideDocumentDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

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
            val folderDao = database.folderDao()

            // Fetch unsynced documents. Assuming getUnsyncedDocuments() returns a Flow, use .first() to get the current list.
            val unsyncedDocuments = documentDao.getAllUnsyncedDocumentIds().first()
            println("adasfsdfs:$unsyncedDocuments")
            unsyncedDocuments.forEach { document ->
                // Simulate a task like uploading to Google Drive.
//                if (uploadDocument(document)) {
                val (uploadSuccess, fileId) = uploadDocument(document)
                if (uploadSuccess && fileId != null) {
                    // Now also passing the fileId to update the record accordingly
                    documentDao.updateDocumentWithDriveId(document.id, fileId)
                    documentDao.markDocumentAsSynced(document.id)

                    val resourceName =
                        getResourceName(applicationContext, R.raw.schema) + ".json"
                    val jsonFileContent = loadJSONFromRaw(applicationContext, R.raw.schema)

                    val uploadedFile =
                        uploadJsonFileToDrive(resourceName, jsonFileContent!!)

                    if (uploadedFile != null) {
                        Log.d(
                            "Json File uploaded in Google Drive",
                            "File ID: ${uploadedFile.id}"
                        )
                        // Continue with your logic here
                    } else {
                        Log.e(
                            "Json File upload in Google Drive Error",
                            "Failed to upload file."
                        )
                    }
//                     else {
//                        Log.e(
//                            " ReNote AI Folder Creation in Google Drive Error",
//                            "Failed to create ReNoteAi  folder in Google Drive."
//                        )
//                    }

                    val documents =
                        documentDao.getDocumentDetailsForJson()// Fetch your actual data from Room database
                    println("32334343445:$documents")
                    val documentsArray = JSONArray()
                    documents.forEach { document ->
                        val jsonDocument = JSONObject().apply {
                            put("id", document.id)
                            put("name", document.name)
                            put("createdDate", document.createdDate)
                            put("updatedDate", document.updatedDate)
                            put("folderId", document.folderId)
                            put("isSynced", document.isSynced)
                            put("isPin", document.isPin)
                            put("isFavourite", document.isFavourite)
                            put("fileData", document.fileData)
                            put("fileDriveId", document.fileDriveId)
                            put("openCount", document.openCount)
                            put("localFilePathAndroid", document.localFilePathAndroid)
                            put("tagId", document.tagId)
                            put("driveType", document.driveType)
                            put("fileExtension", document.fileExtension)
                            // Include other fields as needed
                        }
                        documentsArray.put(jsonDocument)
                    }

//                    val documentContainer = JSONObject().apply {
//                        put("documents", documentsArray)
//                    }

//                    val docArray = documentContainer.toString()
//                    updateFolderAndDocToJson(docArray)

                    ///adding folders to json......
                    val folders =
                        folderDao.getAllFoldersForJson()
                    println("323343434455555:$folders")
                    val foldersArray = JSONArray()
                    folders.forEach { folder ->
                        val jsonFolder = JSONObject().apply {
                            put("id", folder.id)
                            put("name", folder.name)
                            put("createdDate", folder.createdDate)
                            put("updatedDate", folder.updatedDate)
                            put("emailOrPhone", folder.emailOrPhone)
                            put("isSynced", folder.isSynced)
                            put("isPin", folder.isPin)
                            put("isFavourite", folder.isFavourite)
                            put("fileCount", folder.fileCount)
                            put("driveType", folder.driveType)
                            // Include other fields as needed
                        }
                        foldersArray.put(jsonFolder)
                    }

//                    val combinedJson = JSONObject().apply {
//                        put("documents", documentsArray)
//                        if (foldersArray!= null){
//                            put("folders", foldersArray)
//                        }
//                    }

                    val combinedJson = JSONObject().apply {
                        put("documents", documentsArray)

                        // Include foldersArray only if it's not empty or documentsArray has content
                        if (foldersArray.length()>0) {
                            put("folders", foldersArray)
                        }
                    }

                    val jsonContent = combinedJson.toString()
                    updateFolderAndDocToJson(jsonContent)

//                    val foldersContainer = JSONObject().apply {
//                        put("folders", foldersArray)
//                    }
//
//                    val folderArray = foldersContainer.toString()

                   // updateFolderAndDocToJson(folderArray)
                }

                // Mark document as synced if upload is successful.
                // documentDao.updateDocumentWithDriveId(document.id,)
                //}
            }
            if (unsyncedDocuments.isNotEmpty()) {

            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    private fun updateFolderAndDocToJson(array: String) {
        val fileQueryResult = driveService.files().list()
            .setQ("name = 'schema.json' and mimeType = 'application/json'")
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        val files = fileQueryResult.files
        if (files == null || files.isEmpty()) {
            println("File not found.")

        } else {

            // Assuming the first search result is the file we want to update
            val fileId = files[0].id

            // Step 2: Update the file with new JSON data
            val fileMetadata = com.google.api.services.drive.model.File()
            val contentStream =
                ByteArrayContent.fromString("application/json", array)

            driveService.files().update(fileId, fileMetadata, contentStream).execute()
            println("File updated successfully.")
        }
    }

//    private fun updateFolderAndDocToJson(array: String) {
//        val fileQueryResult = driveService.files().list()
//            .setQ("name = 'schema.json' and mimeType = 'application/json'")
//            .setSpaces("drive")
//            .setFields("files(id, name)")
//            .execute()
//
//        val files = fileQueryResult.files
//        if (files == null || files.isEmpty()) {
//            println("File not found.")
//        } else {
//            // Assuming the first search result is the file we want to update
//            val fileId = files[0].id
//
//            // Step 1: Retrieve existing JSON content from the file
//            val file = driveService.files().get(fileId).execute()
//            val inputStream = ByteArrayOutputStream()
//            driveService.files().get(fileId).executeMediaAndDownloadTo(inputStream)
//            val existingContent = inputStream.toString()
//
//            // Step 2: Combine existing JSON content with new JSON array
//            val combinedContent = combineJsonContent(existingContent, array)
//
//            // Step 3: Update the file with the combined JSON data
//            val fileMetadata = com.google.api.services.drive.model.File()
//            val contentStream = ByteArrayContent.fromString("application/json", combinedContent)
//
//            driveService.files().update(fileId, fileMetadata, contentStream).execute()
//            println("File updated successfully.")
//        }
//    }

    // Function to combine existing JSON content with new JSON array
//    private fun combineJsonContent(existingContent: String, newArray: String): String {
//        val existingJson = JSONObject(existingContent)
//        val newJson = JSONObject(newArray)
//
//        // Merge the new array with existing JSON content
//        existingJson.putAll(newJson)
//
//        return existingJson.toString()
//    }

//    private fun combineJsonContent(existingContent: String, newArray: String): String {
//        val existingJson = JSONObject(existingContent)
//        val newJson = JSONObject(newArray)
//
//        // Iterate through the keys of the new JSON object and add them to the existing JSON object
//        val keys = newJson.keys()
//        while (keys.hasNext()) {
//            val key = keys.next() as String
//            existingJson.put(key, newJson[key])
//        }
//
//        return existingJson.toString()
//    }



    fun getResourceName(context: Context, @RawRes resId: Int): String {
        return context.resources.getResourceEntryName(resId)
    }

    fun loadJSONFromRaw(context: Context, @RawRes resourceId: Int): String? {
        return try {
            val inputStream = context.resources.openRawResource(resourceId)
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

//    suspend fun uploadJsonFileToDrive(
//        folderId: String, fileName: String, fileContent: String
//    ): com.google.api.services.drive.model.File? {
//        return withContext(Dispatchers.IO) {
//            try {
//                val fileCheck = driveService?.files()!!.list()
//                    .setQ("name = '$fileName' and '$folderId' in parents and trashed = false")
//                    .setSpaces("drive").setFields("files(id, name)").execute()
//
//                if (fileCheck.files.isNotEmpty()) {
//                    // A file with the same name exists, update its content
//                    val file = fileCheck.files[0]
//                    Log.d("Google Drive", "File already exists: ${file.name} (${file.id})")
//
//                    val updatedFileMetadata = com.google.api.services.drive.model.File()
//                    val mediaContent = ByteArrayContent.fromString("application/json", fileContent)
//                    val updatedFile =
//                        driveService!!.files().update(file.id, updatedFileMetadata, mediaContent)
//                            .execute()
//
//                    Log.d("Google Drive", "File updated: ${updatedFile.name} (${updatedFile.id})")
//                    return@withContext updatedFile
//                } else {
//                    val fileMetadata = com.google.api.services.drive.model.File()
//                    fileMetadata.name = fileName
//                    fileMetadata.parents = listOf(folderId)
//
//                    val mediaContent = ByteArrayContent.fromString("application/json", fileContent)
//                    val uploadedFile =
//                        driveService!!.files().create(fileMetadata, mediaContent).setFields("id, parents")
//                            .execute()
//
//                    Log.d(
//                        "Google Drive", "File uploaded: ${uploadedFile.name} (${uploadedFile.id})"
//                    )
//                    return@withContext uploadedFile
//                }
//            } catch (e: IOException) {
//                Log.e("Google Drive", "Error uploading or updating json file", e)
//                return@withContext null
//            }
//        }
//    }

    suspend fun uploadJsonFileToDrive(
        fileName: String,
        fileContent: String
    ): com.google.api.services.drive.model.File? {
        return withContext(Dispatchers.IO) {
            try {
                // Search for the folder named "ReNoteAI"
                val folderQuery = driveService.files().list()
                    .setQ("name = 'ReNoteAI' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                    .setSpaces("drive")
                    .setFields("files(id, name)")
                    .execute()

                if (folderQuery.files.isNotEmpty()) {
                    val folderId = folderQuery.files[0].id

                    // Check if the file already exists in the "ReNoteAI" folder
                    val fileCheck = driveService.files().list()
                        .setQ("name = '$fileName' and '$folderId' in parents and trashed = false")
                        .setSpaces("drive")
                        .setFields("files(id, name)")
                        .execute()

                    if (fileCheck.files.isNotEmpty()) {
                        // A file with the same name exists, log and return
                        val file = fileCheck.files[0]
                        Log.d("Google Drive", "File already exists: ${file.name} (${file.id})")
                        return@withContext file
                    } else {
                        // Create a new file in the "ReNoteAI" folder
                        val fileMetadata = com.google.api.services.drive.model.File()
                        fileMetadata.name = fileName
                        fileMetadata.parents = listOf(folderId)

                        val mediaContent =
                            ByteArrayContent.fromString("application/json", fileContent)
                        val uploadedFile =
                            driveService.files().create(fileMetadata, mediaContent)
                                .setFields("id, parents")
                                .execute()

                        Log.d(
                            "Google Drive",
                            "File uploaded: ${uploadedFile.name} (${uploadedFile.id})"
                        )
                        return@withContext uploadedFile
                    }
                } else {
                    Log.e("Google Drive", "Folder 'ReNoteAI' not found")
                    return@withContext null
                }
            } catch (e: IOException) {
                Log.e("Google Drive", "Error uploading or updating json file", e)
                return@withContext null
            }
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
            ).setApplicationName(context.getString(R.string.app_name)).build()
        }
        var tempDrive: Drive
        return tempDrive
    }

//    private suspend fun uploadDocument(
//        document: DocumentEntity
//    ): Pair<Boolean, String?> {
//        return try {
//            // Get the file path from the document entity
//            val filePath = document.fileData
//
//            println("filePath:$filePath")
//            // Get the file content from the file path
//            val fileContent = getFileContentFromPath(filePath, applicationContext)
//            println("fileContent:$fileContent")
//
//            if (fileContent != null) {
//                // The MIME type of the folder
//                val folderMimeType = "application/vnd.google-apps.folder"
//                // Search for the ReNoteAI folder
//                val folderSearchQuery =
//                    "name = 'ReNoteAI' and mimeType = '$folderMimeType' and trashed = false"
//                val folderSearch = driveService.files().list().setQ(folderSearchQuery).execute()
//                var folderId: String? = folderSearch.files.firstOrNull()?.id
//
//                // If the ReNoteAI folder doesn't exist, create it
//                if (folderId == null) {
//                    val folderMetadata = com.google.api.services.drive.model.File().apply {
//                        name = "ReNoteAI"
//                        mimeType = folderMimeType
//                    }
//                    folderId = driveService.files().create(folderMetadata).execute().id
//                }
//
//                // Prepare the file to be uploaded
//                val fileMetadata = com.google.api.services.drive.model.File().apply {
//                    name = document.name
//                    parents = listOf(folderId)
//                }
//
//                val mediaContent = InputStreamContent(null, ByteArrayInputStream(fileContent))
//
//                // Upload the file
//                val uploadFile = driveService.files().create(fileMetadata, mediaContent).execute()
//                Pair(true, uploadFile.id)
//
//                //true // Return true if upload succeeds
//            } else {
//                Pair(false, null)
//                // false // Return false if file content is null or cannot be obtained
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            // false // Return false if upload fails
//            Pair(false, null)
//
//        }
//    }

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

                // If the ReNoteAI folder doesn't exist, log an error and return
                if (folderId == null) {
                    println("ReNoteAI folder not found in Google Drive")
                    return Pair(false, null)
                }

                // Prepare the file to be uploaded
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = document.name
                    parents = listOf(folderId)
                }

                val mediaContent = InputStreamContent(null, ByteArrayInputStream(fileContent))

                // Upload the file
                val uploadFile = driveService.files().create(fileMetadata, mediaContent).execute()
                Pair(true, uploadFile.id)

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
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
