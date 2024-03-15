package com.renote.renoteai.ui.presentation.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.renote.renoteai.R
import com.renote.renoteai.ui.presentation.home.adapters.DocumentsDetailsAdapter
import com.renote.renoteai.ui.presentation.home.adapters.FoldersAdapter
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.database.custom_models.DocumentsContainer
import com.renote.renoteai.database.tables.FolderEntity
import com.renote.renoteai.database.custom_models.FoldersContainer
import com.renote.renoteai.database.tables.TagEntity
import com.renote.renoteai.ui.presentation.home.viewmodel.HomeFragmentViewModel
import org.koin.android.ext.android.inject
import java.io.File
import com.renote.renoteai.ui.registration.RegistrationActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.renote.renoteai.ui.presentation.home.adapters.TagsAdapter
import com.renote.renoteai.database.custom_models.TagsContainer
import com.renote.renoteai.databinding.HomeFragmentDataBinding
import com.renote.renoteai.ui.base.listeners.TagsItemListener
import com.renote.renoteai.ui.presentation.home.dialogs.AddFolderBottomSheetFragment
import com.renote.renoteai.ui.presentation.home.dialogs.CreateTagBottomSheetFragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.api.client.http.InputStreamContent
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.renote.renoteai.ui.presentation.home.workers.DocumentSyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {
    var mContext: Context? = null
    var mDrive: Drive? = null
    private lateinit var auth: FirebaseAuth
    val authh = FirebaseAuth.getInstance()
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    var binding: HomeFragmentDataBinding? = null

    private val viewModel: HomeFragmentViewModel by inject()

    var folderId: String? = null
    var actualFolderName: String? = null
    var loginUserGoogleId: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mContext = activity
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding?.lifecycleOwner = this
        binding?.viewModel = viewModel
        return binding!!.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val directory = File(requireContext().filesDir, "ReNoteAI")
        if (!directory.exists()) {
            directory.mkdirs() // Create the directory if it doesn't exist
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val auth = Firebase.auth
        val user = auth.currentUser


        loginUserGoogleId = user?.email

        createReNoteAiFolderInGoogleDrive()

        binding?.adFolder?.setOnClickListener {
            AddFolderBottomSheetFragment().show(
                childFragmentManager,
                AddFolderBottomSheetFragment().tag
            )
        }

        binding?.adTag?.setOnClickListener {
            CreateTagBottomSheetFragment().show(childFragmentManager, CreateTagBottomSheetFragment().tag)
        }

            syncDocuments()

//
//        val workRequest = OneTimeWorkRequest.Builder(DocumentSyncWorker::class.java).build()
//
//// Enqueue the WorkRequest
//        WorkManager.getInstance(requireContext()).enqueue(workRequest)


        val jsonString = loadJSONFromRaw(
            requireActivity(), R.raw.schema
        )
        //tags

        viewModel.saveTagDetails(TagEntity("1000","All"))
        viewModel.saveTagDetails(TagEntity("2000","Starred"))
        viewModel.saveTagDetails(TagEntity("3000","Most Viewed"))


        binding?.profileIcon?.setOnClickListener {
            if (loginUserGoogleId != null) {
                lifecycleScope.launch {
                    updateDocumentInJsonFile("ReNoteAI", "schema.json", newDocumentData)
                }
            }
            signOutFromGoogle()
            val i = Intent(requireActivity(), RegistrationActivity::class.java)
            startActivity(i)
        }

//        binding?.imgSync?.setOnClickListener {
//            signOutFromGoogle()
//        }

        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.img_search)
        drawable?.setBounds(0, 0, 40, 40) // Set the desired width and height
        binding?.etSearch?.setCompoundDrawables(drawable, null, null, null)


        binding?.relativeCross?.setOnClickListener {
            binding?.registerCard?.visibility = View.GONE
        }

        initTagsRecyclerview()
       tagsObserveData()
        initFoldersRecyclerView()
        foldersObserveData()
        initDocumentsRecyclerView()
        documentsObserveData()

    }

    private fun syncDocuments() {
        val syncWorkRequest = OneTimeWorkRequestBuilder<DocumentSyncWorker>()
            .setInputData(Data.Builder().build())
            .build()

        // Enqueue the work request
        WorkManager.getInstance(requireContext()).enqueue(syncWorkRequest)
    }

    fun createReNoteAiFolderInGoogleDrive() {
        try {
            if (loginUserGoogleId != null) {
                Toast.makeText(requireContext(), "$loginUserGoogleId", Toast.LENGTH_LONG).show()
                mDrive = getDriveService(requireActivity())
                lifecycleScope.launch {
                    actualFolderName = "ReNoteAI"
                    actualFolderName?.let {

                        folderId = createFolderInGoogleDrive(actualFolderName!!)
                        val resourceName =
                            getResourceName(requireActivity(), R.raw.schema) + ".json"
                        val jsonFileContent = loadJSONFromRaw(requireActivity(), R.raw.schema)
                        if (folderId != null) {
                            val uploadedFile =
                                uploadJsonFileToDrive(folderId!!, resourceName, jsonFileContent!!)

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
                        } else {
                            Log.e(
                                " ReNote AI Folder Creation in Google Drive Error",
                                "Failed to create ReNoteAi  folder in Google Drive."
                            )
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "User not SignedIn!!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(
                "Google Drive ReNote Ai Folder Creation and Json File Upload Error",
                "Failed to upload file.${e}"
            )
        }
    }


//    fun savingFoldersDataFromJSONFileToRoomDatabase() {
////        if (loginUserGoogleId != null) {
//        val jsonString = loadJSONFromRaw(
//            requireActivity(), R.raw.schema
//        )
//        val foldersContainer = parseFolders(jsonString!!)
//        val folders = foldersContainer.folders // Your Map<String, Folder>
//        // Convert the Map<String, Folder> to JSONObject
//        val jsonFolderObject = JSONObject(folders)
//        viewModel.getAllFolderIds()
//
//        viewModel.allFolderIdsList.observe(requireActivity()) { folderIds ->
//
//            println("folderIdsfolderIds" + folderIds)
//            if (folderIds.isNotEmpty()) {
//                val folderEntities = mutableListOf<FolderEntity>()
//                val keys = jsonFolderObject.keys()
//                println("keys:" + keys)
//                while (keys.hasNext()) {
//                    val key = keys.next() as String
//                    println("key:" + key)
//                    if (key !in folderIds) {
//                        val folderData = jsonFolderObject.getJSONObject(key)
//                        println("folderData:" + folderData)
//                        val id = folderData.getString("id")
//                        val name = folderData.getString("name")
//                        val createdDate = folderData.getLong("createdDate")
//                        val updatedDate = folderData.getLong("updatedDate")
//                        val emailOrPhone = folderData.getString("emailOrPhone")
//                        val isSynced = folderData.getBoolean("isSynced")
//                        val isPin = folderData.getBoolean("isPin")
//                        val isFavourite = folderData.getBoolean("isFavourite")
//                        val fileCount = folderData.getInt("fileCount")
//                        val driveType = folderData.getString("driveType")
//
//                        folderEntities.add(
//                            FolderEntity(
//                                id = id,
//                                name = name,
//                                createdDate = createdDate,
//                                updatedDate = updatedDate,
//                                emailOrPhone = emailOrPhone,
//                                isSynced = isSynced,
//                                isPin = isPin,
//                                isFavourite = isFavourite,
//                                fileCount = fileCount,
//                                driveType = driveType
//                            )
//                        )
//                    }
//                }
//
//                viewModel.saveFolderFilesDetails(folderEntities)
//            } else {
//                val folderEntities = folders.map { (_, folder) ->
//                    FolderEntity(
//                        id = folder.id,
//                        name = folder.name,
//                        createdDate = folder.createdDate,
//                        updatedDate = folder.updatedDate,
//                        emailOrPhone = folder.emailOrPhone,
//                        isSynced = folder.isSynced,
//                        isPin = folder.isPin,
//                        isFavourite = folder.isFavourite,
//                        fileCount = folder.fileCount,
//                        driveType = folder.driveType
//                    )
//                }
//                viewModel.saveFolderFilesDetails(folderEntities)
//            }
//        }
//        //}
//
//    }

//    fun savingDocumentsDataFromJSONFileToRoomDatabase() {
//        // if (loginUserGoogleId != null) {
//        val jsonString = loadJSONFromRaw(
//            requireActivity(), R.raw.schema
//        ) // Replace 'example' with your file's name without the extension
//        val documentsContainer = parseDocuments(jsonString!!)
//        val documents = documentsContainer.documents // Your Map<String, Folder>
//        // Convert the Map<String, Folder> to JSONObject
//        val jsonObject = JSONObject(documents)
//        viewModel.getAllDocumentsIds()
//        viewModel.allDocumentsIdsList.observe(requireActivity()) { documentIds ->
//
//            println("documentIds documentIds " + documentIds)
//            if (documentIds.isNotEmpty()) {
//                val documentEntities = mutableListOf<DocumentEntity>()
//                val keys = jsonObject.keys()
//                println("keys:" + keys)
//                while (keys.hasNext()) {
//                    val key = keys.next() as String
//                    println("key:" + key)
//                    if (key !in documentIds) {
//                        val documentData = jsonObject.getJSONObject(key)
//                        println("documentData:" + documentData)
//
//                        val id = documentData.getString("id")
//                        val name = documentData.getString("name")
//                        val createdDate = documentData.getLong("createdDate")
//                        val updatedDate = documentData.getLong("updatedDate")
//                        val fileData = documentData.getString("fileData")
//                        val isSynced = documentData.getBoolean("isSynced")
//                        val isPin = documentData.getBoolean("isPin")
//                        val isFavourite = documentData.getBoolean("isFavourite")
//                        val folderId = documentData.getString("folderId")
//                        val openCount = documentData.getInt("openCount")
//                        val localFilePathIos = documentData.getString("localFilePathIos")
//                        val localFilePathAndroid = documentData.getString("localFilePathIos")
//                        val tagId = documentData.getString("tagId")
//                        val driveType = documentData.getString("driveType")
//                        val fileExtension = documentData.getString("fileExtension")
//                        documentEntities.add(
//                            DocumentEntity(
//                                id = id,
//                                name = name,
//                                createdDate = createdDate,
//                                updatedDate = updatedDate,
//                                fileData = fileData,
//                                isSynced = isSynced,
//                                isPin = isPin,
//                                isFavourite = isFavourite,
//                                folderId = folderId,
//                                openCount = openCount,
//                                localFilePathAndroid = localFilePathAndroid,
//                                tagId = tagId,
//                                driveType = driveType,
//                                fileExtension = fileExtension
//                            )
//                        )
//                    }
//                }
//
//                viewModel.saveDocumentsDetails(documentEntities)
//            } else {
//                val documentEntities = documents.map { (_, document) ->
//                    DocumentEntity(
//                        id = document.id,
//                        name = document.name,
//                        createdDate = document.createdDate,
//                        updatedDate = document.updatedDate,
//                        fileData = document.fileData,
//                        isSynced = document.isSynced,
//                        isPin = document.isPin,
//                        isFavourite = document.isFavourite,
//                        folderId = document.folderId,
//                        openCount = document.openCount,
//                        localFilePathAndroid = document.localFilePathAndroid,
//                        tagId = document.tagId,
//                        driveType = document.driveType,
//                        fileExtension = document.fileExtension
//                    )
//                }
//                viewModel.saveDocumentsDetails(documentEntities)
//            }
//        }
//
//        //}
//    }

//    fun savingTagsDataFromJSONFileToRoomDatabase() {
//        // if (loginUserGoogleId != null) {
//        val jsonString = loadJSONFromRaw(
//            requireActivity(), R.raw.schema
//        )
//        val tagsContainer = parseTags(jsonString!!)
//        val tags = tagsContainer.tags // Your Map<String, Folder>
//        // Convert the Map<String, Folder> to JSONObject
//        val jsonTagObject = JSONObject(tags)
//        viewModel.getAllTagIds()
//        viewModel.allTagIdsList.observe(requireActivity()) { tagIds ->
//
//            println("tagIds tagIds " + tagIds)
//            if (tagIds.isNotEmpty()) {
//                val tagEntities = mutableListOf<TagEntity>()
//                val keys = jsonTagObject.keys()
//                println("keys:" + keys)
//                while (keys.hasNext()) {
//                    val key = keys.next() as String
//                    println("key:" + key)
//                    if (key !in tagIds) {
//                        val tagData = jsonTagObject.getJSONObject(key)
//                        println("tagData:" + tagData)
//
//                        val id = tagData.getString("id")
//                        val tagName = tagData.getString("name")
//
//                        tagEntities.add(
//                            TagEntity(
//                                id = id, tagName = tagName, isSelected = false
//                            )
//                        )
//                    }
//                }
//
//                viewModel.saveTagDetails(tagEntities)
//            } else {
//                val tagEntities = tags.map { (_, tag) ->
//                    TagEntity(
//                        id = tag.id, tagName = tag.tagName, isSelected = false
//                    )
//                }
//                viewModel.saveTagDetails(tagEntities)
//            }
//        }
//        // }
//    }
//
//    //}
//
//    }

//    fun savingDocumentsDataFromJSONFileToRoomDatabase() {
//       // if (loginUserGoogleId != null) {
//            val jsonString = loadJSONFromRaw(
//                requireActivity(), R.raw.schema
//            ) // Replace 'example' with your file's name without the extension
//            val documentsContainer = parseDocuments(jsonString!!)
//            val documents = documentsContainer.documents // Your Map<String, Folder>
//            // Convert the Map<String, Folder> to JSONObject
//            val jsonObject = JSONObject(documents)
//            viewModel.getAllDocumentsIds()
//            viewModel.allDocumentsIdsList.observe(requireActivity()) { documentIds ->
//
//                println("documentIds documentIds " + documentIds)
//                if (documentIds.isNotEmpty()) {
//                    val documentEntities = mutableListOf<DocumentEntity>()
//                    val keys = jsonObject.keys()
//                    println("keys:" + keys)
//                    while (keys.hasNext()) {
//                        val key = keys.next() as String
//                        println("key:" + key)
//                        if (key !in documentIds) {
//                            val documentData = jsonObject.getJSONObject(key)
//                            println("documentData:" + documentData)
//
//                            val id = documentData.getString("id")
//                            val name = documentData.getString("name")
//                            val createdDate = documentData.getLong("createdDate")
//                            val updatedDate = documentData.getLong("updatedDate")
//                            val fileData = documentData.getString("fileData")
//                            val isSynced = documentData.getBoolean("isSynced")
//                            val isPin = documentData.getBoolean("isPin")
//                            val isFavourite = documentData.getBoolean("isFavourite")
//                            val folderId = documentData.getString("folderId")
//                            val openCount = documentData.getInt("openCount")
//                            val localFilePathIos = documentData.getString("localFilePathIos")
//                            val localFilePathAndroid = documentData.getString("localFilePathIos")
//                            val tagId = documentData.getString("tagId")
//                            val driveType = documentData.getString("driveType")
//                            val fileExtension = documentData.getString("fileExtension")
//                            documentEntities.add(
//                                DocumentEntity(
//                                    id = id,
//                                    name = name,
//                                    createdDate = createdDate,
//                                    updatedDate = updatedDate,
//                                    fileData = fileData,
//                                    isSynced = isSynced,
//                                    isPin = isPin,
//                                    isFavourite = isFavourite,
//                                    folderId = folderId,
//                                    openCount = openCount,
//                                    localFilePathAndroid = localFilePathAndroid,
//                                    tagId = tagId,
//                                    driveType = driveType,
//                                    fileExtension = fileExtension
//                                )
//                            )
//                        }
//                    }
//
//                    viewModel.saveDocumentsDetails(documentEntities)
//                } else {
//                    val documentEntities = documents.map { (_, document) ->
//                        DocumentEntity(
//                            id = document.id,
//                            name = document.name,
//                            createdDate = document.createdDate,
//                            updatedDate = document.updatedDate,
//                            fileData = document.fileData,
//                            isSynced = document.isSynced,
//                            isPin = document.isPin,
//                            isFavourite = document.isFavourite,
//                            folderId = document.folderId,
//                            openCount = document.openCount,
//                            localFilePathAndroid = document.localFilePathAndroid,
//                            tagId = document.tagId,
//                            driveType = document.driveType,
//                            fileExtension = document.fileExtension
//                        )
//                    }
//                    viewModel.saveDocumentsDetails(documentEntities)
//                }
//            }
//        //}
//    }

//    fun savingTagsDataFromJSONFileToRoomDatabase() {
//       // if (loginUserGoogleId != null) {
//            val jsonString = loadJSONFromRaw(
//                requireActivity(), R.raw.schema
//            )
//            val tagsContainer = parseTags(jsonString!!)
//            val tags = tagsContainer.tags // Your Map<String, Folder>
//            // Convert the Map<String, Folder> to JSONObject
//            val jsonTagObject = JSONObject(tags)
//            viewModel.getAllTagIds()
//            viewModel.allTagIdsList.observe(requireActivity()) { tagIds ->
//
//                println("tagIds tagIds " + tagIds)
//                if (tagIds.isNotEmpty()) {
//                    val tagEntities = mutableListOf<TagEntity>()
//                    val keys = jsonTagObject.keys()
//                    println("keys:" + keys)
//                    while (keys.hasNext()) {
//                        val key = keys.next() as String
//                        println("key:" + key)
//                        if (key !in tagIds) {
//                            val tagData = jsonTagObject.getJSONObject(key)
//                            println("tagData:" + tagData)
//
//                            val id = tagData.getString("id")
//                            val tagName = tagData.getString("name")
//
//                            tagEntities.add(
//                                TagEntity(
//                                    id = id, tagName = tagName
//                                )
//                            )
//                        }
//                    }
//
//                    viewModel.saveTagDetails(tagEntities)
//                } else {
//                    val tagEntities = tags.map { (_, tag) ->
//                        TagEntity(
//                            id = tag.id, tagName = tag.tagName
//                        )
//                    }
//                    viewModel.saveTagDetails(tagEntities)
//                }
//            }
//       // }
//    }

    fun initTagsRecyclerview() {
        viewModel.tagsAdapter = TagsAdapter(mContext!!)
        viewModel.tagsAdapter.itemClickListener = itemClickListener
        val linearLayoutmanger =
            LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        binding?.tagRecyclerView?.layoutManager = linearLayoutmanger
        binding?.tagRecyclerView?.isNestedScrollingEnabled = false
        binding?.tagRecyclerView?.adapter = viewModel.tagsAdapter
    }

    fun tagsObserveData() {
        //if (loginUserGoogleId != null) {
        viewModel.getAllTagDetails()

        viewModel.tagDetailsList.observe(requireActivity()) {
            if (it.isNotEmpty()) {
                showTagEmpty(false)
                println("232133243243244545:" + viewModel.tagDetailsList)

                if (viewModel.tagsAdapter != null) {
                    viewModel.tagsAdapter.submitList(it)
                    viewModel.tagsAdapter.notifyDataSetChanged()

                }
            } else {
                showTagEmpty(true)
            }
        }
        //  }
    }

    fun initFoldersRecyclerView() {

        viewModel.foldersAdapter = FoldersAdapter(mContext!!, authh.currentUser?.email)
        val linearLayoutmanger =
            LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        binding?.folderRecyclerView?.layoutManager = linearLayoutmanger
        binding?.folderRecyclerView?.isNestedScrollingEnabled = false
        binding?.folderRecyclerView?.adapter = viewModel.foldersAdapter

    }

    fun foldersObserveData() {
        //  if (loginUserGoogleId != null) {
        viewModel.getAllFolderFileDetails()

        viewModel.folderFileDetailsList.observe(requireActivity()) {
            if (it.isNotEmpty()) {
                showFolderEmpty(false)
                println("23213324324324:" + viewModel.folderFileDetailsList)
                if (viewModel.foldersAdapter != null) {
                    viewModel.foldersAdapter.submitList(it)
                    viewModel.foldersAdapter.notifyDataSetChanged()
                }

            } else {
                showFolderEmpty(true)
            }
        }
        //  }
    }

    fun initDocumentsRecyclerView() {
        viewModel.documentsAdapter = DocumentsDetailsAdapter(mContext!!)
        val linearLayoutmanger = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        binding?.recycleview?.layoutManager = linearLayoutmanger
        binding?.recycleview?.isNestedScrollingEnabled = false
        binding?.recycleview?.adapter = viewModel.documentsAdapter

    }

    fun documentsObserveData() {
        // if (loginUserGoogleId != null) {
        viewModel.getAllDocumentsDetails()

        viewModel.documentsDetailsList.observe(requireActivity()) {
            if (it.isNotEmpty()) {
                showEmpty(false)
                println("23213324324324:" + viewModel.documentsDetailsList)
                if (viewModel.documentsAdapter != null) {
                    viewModel.documentsAdapter.submitList(it)
                    viewModel.documentsAdapter.notifyDataSetChanged()
                }

            } else {
                showEmpty(true)
            }
        }
        //  }
    }

    suspend fun createFolderInGoogleDrive(folderName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Check if a folder with the same name already exists
                val existingFolders = mDrive!!.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder' and name='$folderName'")
                    .execute().files

                if (existingFolders.isNotEmpty()) {
//                    requireActivity().runOnUiThread {
//                        Toast.makeText(
//                            requireActivity(),
//                            "${existingFolders[0].name} folder already exists",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
                    Log.d(
                        "Google Drive",
                        "Folder already exists: ${existingFolders[0].name} (${existingFolders[0].id})"
                    )
                    return@withContext existingFolders[0].id
                }

                // Create a file metadata for the folder
                val folderMetadata = com.google.api.services.drive.model.File().setName(folderName)
                    .setMimeType("application/vnd.google-apps.folder")

                // Create the folder
                val folder = mDrive?.files()?.create(folderMetadata)?.execute()
//                requireActivity().runOnUiThread {
//                    Toast.makeText(
//                        requireActivity(),
//                        "${folder!!.name} folder created",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
                Log.d("Google Drive", "Folder created: ${folder!!.name} (${folder!!.id})")
                return@withContext folder.id
            } catch (e: IOException) {
//                runOnUiThread {
//                    Toast.makeText(
//                        this@MainActivity,
//                        "some error occured in creating ${folderName} folder, please try again after sometime",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
                Log.e("Google Drive", "Error creating folder", e)
                return@withContext null
            }
        }
    }

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


    fun parseDocuments(jsonString: String): DocumentsContainer {
        val gson = Gson()
        return gson.fromJson(jsonString, DocumentsContainer::class.java)
    }

    fun parseFolders(jsonString: String): FoldersContainer {
        val gson = Gson()
        return gson.fromJson(jsonString, FoldersContainer::class.java)
    }

    fun parseTags(jsonString: String): TagsContainer {
        val gson = Gson()
        return gson.fromJson(jsonString, TagsContainer::class.java)
    }


    suspend fun uploadJsonFileToDrive(
        folderId: String, fileName: String, fileContent: String
    ): com.google.api.services.drive.model.File? {
        return withContext(Dispatchers.IO) {
            try {
                val fileCheck = mDrive?.files()!!.list()
                    .setQ("name = '$fileName' and '$folderId' in parents and trashed = false")
                    .setSpaces("drive").setFields("files(id, name)").execute()

                if (fileCheck.files.isNotEmpty()) {
                    // A file with the same name exists, update its content
                    val file = fileCheck.files[0]
                    Log.d("Google Drive", "File already exists: ${file.name} (${file.id})")

                    val updatedFileMetadata = com.google.api.services.drive.model.File()
                    val mediaContent = ByteArrayContent.fromString("application/json", fileContent)
                    val updatedFile =
                        mDrive!!.files().update(file.id, updatedFileMetadata, mediaContent)
                            .execute()

                    Log.d("Google Drive", "File updated: ${updatedFile.name} (${updatedFile.id})")
                    return@withContext updatedFile
                } else {
                    val fileMetadata = com.google.api.services.drive.model.File()
                    fileMetadata.name = fileName
                    fileMetadata.parents = listOf(folderId)

                    val mediaContent = ByteArrayContent.fromString("application/json", fileContent)
                    val uploadedFile =
                        mDrive!!.files().create(fileMetadata, mediaContent).setFields("id, parents")
                            .execute()

                    Log.d(
                        "Google Drive", "File uploaded: ${uploadedFile.name} (${uploadedFile.id})"
                    )
                    return@withContext uploadedFile
                }
            } catch (e: IOException) {
                Log.e("Google Drive", "Error uploading or updating json file", e)
                return@withContext null
            }
        }
    }


    private fun showEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding?.recycleview?.visibility = View.GONE
            binding?.imgEmptyRecycle?.visibility = View.VISIBLE
        } else {
            binding?.recycleview?.visibility = View.VISIBLE
            binding?.imgEmptyRecycle?.visibility = View.GONE
        }
    }

    private fun showFolderEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding?.folderRecyclerView?.visibility = View.GONE

        } else {
            binding?.folderRecyclerView?.visibility = View.VISIBLE

        }
    }

    private fun showTagEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding?.tagRecyclerView?.visibility = View.GONE

        } else {
            binding?.tagRecyclerView?.visibility = View.VISIBLE

        }
    }

    val itemClickListener: TagsItemListener = object : TagsItemListener {
        override fun onItemClicked(view: View, position: Int) {


        }
    }

    fun getDriveService(context: Context): Drive? {
        try {
            GoogleSignIn.getLastSignedInAccount(context).let { googleAccount ->
                val credential = GoogleAccountCredential.usingOAuth2(
                    requireActivity(), setOf(DriveScopes.DRIVE_FILE)
                )
                credential.selectedAccount = googleAccount!!.account!!
                return Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
                ).setApplicationName(getString(R.string.app_name)).build()
            }
            var tempDrive: Drive
            return tempDrive
        } catch (e: Exception) {

            Toast.makeText(
                requireActivity(),
                "some error occured, please try again after some time",
                Toast.LENGTH_LONG
            ).show()

            return null

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    private fun uriToFile(uri: Uri): File? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireContext().contentResolver.query(uri, filePathColumn, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
        val filePath = cursor?.getString(columnIndex ?: -1)
        cursor?.close()
        return filePath?.let { File(it) }
    }

    fun onShareContent(content: String) {
        val file = uriToFile(content.toUri())
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, file)
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    private fun openFileFromUri(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)

            // Use the document URI directly
            intent.setDataAndType(uri, requireContext().contentResolver.getType(uri))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            }
        } catch (exception: Exception) {
            Toast.makeText(requireContext(), "uri:$uri", Toast.LENGTH_SHORT).show()
        }

    }

    private fun getMimeType(file: File): String {
        val extension = file.extension.toLowerCase()

        // Determine the MIME type based on the file extension
        return when (extension) {
            "pdf" -> "application/pdf"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "ppt", "pptx" -> "application/vnd.ms-powerpoint"
            "txt" -> "text/plain"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            else -> "*/*" // fallback MIME type
        }
    }

    private fun signOutFromGoogle() {

        auth.signOut()

        mGoogleSignInClient.signOut().addOnCompleteListener {
            // Optional: Update UI or show a message to the user
            val intent = Intent(requireActivity(), RegistrationActivity::class.java)
            startActivity(intent)
            Toast.makeText(requireContext(), "User Logged out", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    @SuppressLint("SuspiciousIndentation")
    suspend fun updateJsonFileInFolder(
        folderName: String, fileName: String, newJsonContent: JSONObject
    ) {
        println("folderName:$folderName")
        println("fileName:$fileName")
        println("newJsonContent:$newJsonContent")
        withContext(Dispatchers.IO) {
//            val driveService = Drive.Builder(
//                AndroidHttp.newCompatibleTransport(),
//                GsonFactory(),
//                googleSignInAccount.credential
//            ).setApplicationName("Your Application Name").build()

            try {
                // Find the folder ID
                val folderResult = mDrive?.files()!!.list()
                    .setQ("mimeType = 'application/vnd.google-apps.folder' and name = '$folderName'")
                    .setSpaces("drive").setFields("files(id, name)").execute()

                val folderId = folderResult.files.firstOrNull()?.id
                    ?: throw FileNotFoundException("Folder '$folderName' not found")
                println("folderId:$folderId")
                // Search for the file in the folder
                val fileResult = mDrive!!.files().list()
                    .setQ("'$folderId' in parents and name = '$fileName' and mimeType = 'application/json'")
                    .setSpaces("drive").setFields("files(id, name)").execute()

                val file = fileResult.files.firstOrNull()
                    ?: throw FileNotFoundException("File '$fileName' not found in folder '$folderName'")
                println("file:$folderId")
                // Download the file content
                val outputStream = ByteArrayOutputStream()
                mDrive!!.files().get(file.id).executeMediaAndDownloadTo(outputStream)
                val originalContent = outputStream.toString()
                val originalJson = JSONObject(originalContent)
                println("original json:$newJsonContent")
                // Update the JSON content
//                for (key in newJsonContent.keys()) {
//                    originalJson.put(key, newJsonContent.get(key))
//                }
                for (key in newJsonContent.keys()) {
                    // Check if originalJson already contains the key
                    if (originalJson.has(key)) {
                        // If so, you might want to update, merge, or skip based on your application logic
                        // For simplicity, let's assume we're updating the value with the new one
                        originalJson.put(key, newJsonContent.get(key))
                    } else {
                        // If the key doesn't exist, simply add it to the original JSON
                        originalJson.put(key, newJsonContent.get(key))
                    }
                }

                // Upload the updated content
                val updatedContent = ByteArrayInputStream(originalJson.toString().toByteArray())
                System.out.println("updatedContent:$updatedContent")

                val fileMetadata = com.google.api.services.drive.model.File()
                System.out.println("fileMetadata:$fileMetadata")
                System.out.println("file:$file")

                mDrive!!.files().update(
                    file.id, fileMetadata, InputStreamContent("application/json", updatedContent)
                ).execute()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace()
                    // Handle exceptions, e.g., show a message to the user
                }
            }
        }
    }


    suspend fun updateDocumentInJsonFile(
        folderName: String, fileName: String, newDocument: Map<String, Any>
    ) = withContext(Dispatchers.IO) {
        try {
            // Step 0: Find the folder by name
            val folderResult = mDrive?.files()!!.list()
                .setQ("mimeType = 'application/vnd.google-apps.folder' and name = '$folderName'")
                .setSpaces("drive").setFields("files(id, name)").execute()

            val folderId = folderResult.files.firstOrNull()?.id
                ?: throw FileNotFoundException("Folder '$folderName' not found")

            // Step 1: Search for the file in the folder
            val fileResult = mDrive!!.files().list()
                .setQ("'$folderId' in parents and name = '$fileName' and mimeType = 'application/json'")
                .setSpaces("drive").setFields("files(id, name)").execute()

            val file = fileResult.files.firstOrNull()
                ?: throw FileNotFoundException("File '$fileName' not found in folder '$folderName'")

            // Step 2: Download the existing file content
            val outputStream = ByteArrayOutputStream()
            mDrive!!.files().get(file.id).executeMediaAndDownloadTo(outputStream)
            val currentJson = JSONObject(String(outputStream.toByteArray()))

            // Assume "documents" is a JSON object with unique document IDs as keys
            val documents = currentJson.optJSONObject("documents") ?: JSONObject()
            val newDocJson = JSONObject(newDocument)
            // Here ensure newDocJson.getString("id") provides a unique ID for each document
            documents.put(newDocJson.getString("id"), newDocJson)

            // If documents was not originally part of currentJson, add it back
            currentJson.put("documents", documents)

            // Step 3: Update the file with new content
            val updatedContent =
                ByteArrayContent.fromString("application/json", currentJson.toString())
            val fileMetadata = com.google.api.services.drive.model.File()

            mDrive!!.files().update(file.id, fileMetadata, updatedContent).execute()
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error
        }
    }


    val newDocumentData = mapOf(
        "id" to "aaaaaaaaaaaaa",
        "name" to "Document5555",
        "createdDate" to 3434356545677,
        "updatedDate" to 4344454545455,
        "fileData" to "",
        "isSynced" to true,
        "isPin" to false,
        "isFavourite" to false,
        "folderId" to "aewfsasdfdfsasfs", // Assuming it's being added to the same folder
        "openCount" to 50,
        "localFilePathIos" to "",
        "localFilePathAndroid" to "",
        "tagId" to "oa39weoiafk", // Example tag ID, assuming you're tagging it with an existing tag
        "driveType" to "Google",
        "fileExtension" to "pdf"
    )

    fun createJsonDocument(): JSONObject {
        // Document details as a JSONObject
        val documentDetails = JSONObject().apply {
            put("id", "ererdfdfddftyrtgu")
            put("name", "Document3")
            put("createdDate", 1709509777943)
            put("updatedDate", 1709809777943)
            put("fileData", "")
            put("isSynced", true)
            put("isPin", false)
            put("isFavourite", true)
            put("folderId", "aewfsasdfdfsfgh")
            put("openCount", 100)
            put("localFilePathIos", "")
            put("localFilePathAndroid", "")
            put("tagId", "")
            put("driveType", "Google")
            put("fileExtension", "png")
        }

        // Wrapping the document details in a "documents" JSONObject
        val documentsObject = JSONObject().apply {
            put("ererdfdfddftyrtgu", documentDetails)
        }

        // The entire JSON structure
        val jsonDocument = JSONObject().apply {
            put("documents", documentsObject)
        }

        return jsonDocument
    }

//    override fun onResume() {
//        super.onResume()
//
//        binding!!.imgSync.setOnClickListener {
////            val constraints = Constraints.Builder()
////                .setRequiredNetworkType(NetworkType.CONNECTED) // Example constraint: require network connectivity
////                .build()
//
//// Create WorkRequest
//            val workRequest = OneTimeWorkRequest.Builder(DocumentSyncWorker::class.java)
//                //.setConstraints(constraints) // Optional: apply constraints
//                .build()
//
//// Enqueue WorkRequest
//            WorkManager.getInstance(requireContext()).enqueue(workRequest)
//        }
//
//    }
}