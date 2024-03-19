package com.renote.renoteai.ui.presentation.home.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.renote.base.BaseViewModel
import com.renote.renoteai.R
import com.renote.renoteai.database.tables.FolderEntity
import com.renote.renoteai.repository.DocumentsRepository
import com.renote.renoteai.repository.FoldersRepository
import com.renote.renoteai.repository.TagsRepository
import kotlinx.coroutines.launch

class AddFolderViewModel(private val documentsRepository: DocumentsRepository, private  val foldersRepository: FoldersRepository, private val tagsRepository: TagsRepository, private val application: Application): BaseViewModel(documentsRepository,foldersRepository,tagsRepository,application) {

    var id = MutableLiveData(0L)
    val folderName = MutableLiveData<String>("")
    val resourseClick = MutableLiveData<Int>()
    val message = MutableLiveData<String>()
    val isRefresh = MutableLiveData<Boolean>()
    fun onresorceClick(viewId: Int) {
        resourseClick.postValue(viewId)
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

                    val folder= FolderEntity("folder_${getCurrentTimestamp()}", name = folderName.value!!,createdDate=getCurrentTimestamp(),updatedDate=0L, emailOrPhone = "aaa@gmail.com", isSynced=false, isPin = false, isFavourite = false, fileCount = 0, driveType = "gDrive")
                    if(id.value==0L){
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