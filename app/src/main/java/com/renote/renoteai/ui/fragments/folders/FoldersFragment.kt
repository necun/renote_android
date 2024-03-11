package com.renote.renoteai.ui.fragments.folders

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RawRes
import androidx.recyclerview.widget.LinearLayoutManager
import com.renote.renoteai.ui.presentation.home.dialogs.AddFolderBottomSheetFragment
import com.renote.renoteai.R
import com.renote.renoteai.ui.fragments.folders.adapters.SaveFolderAdapter
import com.renote.renoteai.databinding.FragmentFoldersBinding
import com.renote.renoteai.database.tables.FolderEntity
import com.renote.renoteai.database.custom_models.FoldersContainer
import com.renote.renoteai.ui.presentation.home.viewmodel.HomeFragmentViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.gson.Gson
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.io.IOException


class FoldersFragment : Fragment() {
    var mContext: Context? = null
    private var _binding: FragmentFoldersBinding? = null
    private val binding get() = _binding!!
    val authh = FirebaseAuth.getInstance()

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private val viewModel: HomeFragmentViewModel by inject()
    private val foldersAdapter by lazy { SaveFolderAdapter(viewModel) }

    var folderId: String? = null
    var actualFolderName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContext = activity

        val fileName = arguments?.getString("folderName")
      //  val adapter = SaveFolderAdapter(fileName)


        Log.d("FoldersFragment", "Received folder name: $fileName")

        _binding = FragmentFoldersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val auth = Firebase.auth
        val user = auth.currentUser

        val loginUserGoogleId = user?.email

        val jsonString = loadJSONFromRaw(
            requireActivity(),
            R.raw.schema
        )
        val foldersContainer = parseFolders(jsonString!!)
        val folders = foldersContainer.folders // Your Map<String, Folder>
        // Convert the Map<String, Folder> to JSONObject
        val jsonFolderObject = JSONObject(folders)

        if (loginUserGoogleId != null) {
            viewModel.getAllFolderIds()

            viewModel.allFolderIdsList.observe(requireActivity()) { folderIds ->

                println("folderIdsfolderIds" + folderIds)
                if (folderIds.isNotEmpty()) {
                    val folderEntities = mutableListOf<FolderEntity>()
                    val keys = jsonFolderObject.keys()
                    println("keys:" + keys)
                    while (keys.hasNext()) {
                        val key = keys.next() as String
                        println("key:" + key)
                        if (key !in folderIds) {
                            val folderData = jsonFolderObject.getJSONObject(key)
                            println("folderData:" + folderData)
                            val id = folderData.getString("id")
                            val name = folderData.getString("name")
                            val createdDate = folderData.getLong("createdDate")
                            val updatedDate = folderData.getLong("updatedDate")
                            val emailOrPhone = folderData.getString("emailOrPhone")
                            val isSynced = folderData.getBoolean("isSynced")
                            val isPin = folderData.getBoolean("isPin")
                            val isFavourite = folderData.getBoolean("isFavourite")
                            val fileCount = folderData.getInt("fileCount")
                            val driveType = folderData.getString("driveType")

                            folderEntities.add(
                                FolderEntity(
                                    id = id,
                                    name = name,
                                    createdDate = createdDate,
                                    updatedDate = updatedDate,
                                    emailOrPhone = emailOrPhone,
                                    isSynced = isSynced,
                                    isPin = isPin,
                                    isFavourite = isFavourite,
                                    fileCount = fileCount,
                                    driveType = driveType
                                )
                            )
                        }
                    }

                    viewModel.saveFolderFilesDetails(folderEntities)
                } else {
                    val folderEntities = folders.map { (_, folder) ->
                        FolderEntity(
                            id = folder.id,
                            name = folder.name,
                            createdDate = folder.createdDate,
                            updatedDate = folder.updatedDate,
                            emailOrPhone = folder.emailOrPhone,
                            isSynced = folder.isSynced,
                            isPin = folder.isPin,
                            isFavourite = folder.isFavourite,
                            fileCount = folder.fileCount,
                            driveType = folder.driveType
                        )
                    }
                    viewModel.saveFolderFilesDetails(folderEntities)
                }
            }
        }

       // if (loginUserGoogleId != null) {
            viewModel.getAllFolderFileDetails()

            viewModel.folderFileDetailsList.observe(requireActivity()) {
                if (it.isNotEmpty()) {
                    //showFolderEmpty(false)
                    println("23213324324324:" + viewModel.folderFileDetailsList)
                    foldersAdapter.differ.submitList(it)
                    binding.recycleView.apply {
                        layoutManager = LinearLayoutManager(
                            requireActivity()
                        )
                        adapter = foldersAdapter
                    }
                } else {
                    //  showFolderEmpty(true)
                }
            }
       // }

        binding.addIcon.setOnClickListener {
            AddFolderBottomSheetFragment().show(childFragmentManager, AddFolderBottomSheetFragment().tag)
        }

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

    fun parseFolders(jsonString: String): FoldersContainer {
        val gson = Gson()
        return gson.fromJson(jsonString, FoldersContainer::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}