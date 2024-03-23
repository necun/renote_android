package com.renote.renoteai.ui.presentation.home.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.renote.base.BaseViewModel
import com.renote.renoteai.R
import com.renote.renoteai.database.tables.FolderEntity
import com.renote.renoteai.repository.DocumentsRepository
import com.renote.renoteai.repository.FoldersRepository
import com.renote.renoteai.repository.TagsRepository
import kotlinx.coroutines.launch

class AddFolderViewModel(
    private val documentsRepository: DocumentsRepository,
    private val foldersRepository: FoldersRepository,
    private val tagsRepository: TagsRepository,
    private val application: Application
) : BaseViewModel(documentsRepository, foldersRepository, tagsRepository, application) {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    var loginUserGoogleId: String? = null
    var id = MutableLiveData(0L)
    val folderName = MutableLiveData<String>("")
    val resourseClick = MutableLiveData<Int>()
    val message = MutableLiveData<String>()
    val isRefresh = MutableLiveData<Boolean>()
    fun onresorceClick(viewId: Int) {
        resourseClick.postValue(viewId)
    }

    fun userLoginEmailId(): String {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.resources.getString(R.string.default_web_client_id))
            .requestEmail().build()

        mGoogleSignInClient = GoogleSignIn.getClient(application, gso)

        val auth = Firebase.auth
        val user = auth.currentUser

        loginUserGoogleId = user?.email

        return if (loginUserGoogleId!=null){
            loginUserGoogleId.toString()
        }else{
           return  "youremail@gmail.com"
        }
    }


    fun createFolder() {
        if (folderName.value == null || folderName.value!!.isEmpty()) {
            message.postValue(application.resources.getString(R.string.enter_folder_name_message))
        } else {
            ioScope.launch {
                val c = foldersRepository.getFoldersWithName(folderName.value!!)
                if (c.size == 1) {
                    message.postValue(folderName.value + " folder is already exists")
                } else {

                    val folder = FolderEntity(
                        "folder_${getCurrentTimestamp()}",
                        name = folderName.value!!,
                        createdDate = getCurrentTimestamp(),
                        updatedDate = 0L,
                        emailOrPhone = userLoginEmailId(),
                        isSynced = false,
                        isPin = false,
                        isFavourite = false,
                        fileCount = 0,
                        driveType = "gDrive"
                    )
                    if (id.value == 0L) {
                        ioScope.launch {
                            foldersRepository.saveFolderDetails(folder)
                            isRefresh.postValue(true)
                            message.postValue(folderName.value + " folder created")
                        }
                    }
                }
            }
        }
    }

    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
}