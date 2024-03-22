package com.renote.renoteai.ui.activities.edit.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.database.tables.FileEntity
import com.renote.renoteai.repository.DocumentsRepository
import com.renote.renoteai.repository.FilesRepository
import com.renote.renoteai.repository.FoldersRepository
import com.renote.renoteai.repository.TagsRepository
import kotlinx.coroutines.launch

class EditViewModel(private val documentsRepository: DocumentsRepository, private  val foldersRepository: FoldersRepository, private val filesRepository: FilesRepository): ViewModel() {

    val showLoading = MutableLiveData<Boolean?>()
    val resourseClick = MutableLiveData<Int>()

    fun onresorceClick(viewId: Int) {
        resourseClick.postValue(viewId)
    }

    private val _recentFileDetails = MutableLiveData<List<FileEntity>>()
    val recentFileDetails: LiveData<List<FileEntity>>
        get() = _recentFileDetails
    fun getRecentFileDetailsByDocumentId(recentDocumentId:String) = viewModelScope.launch {
        filesRepository.getRecentFileDetailsByRecentDocumentId(recentDocumentId).collect() {
            _recentFileDetails.postValue(it)
        }
    }

    fun saveFilesDetails(file: List<FileEntity>) = viewModelScope.launch {
        filesRepository.saveFilesDetails(file)
    }


    fun saveDocumentDetail(document: DocumentEntity) = viewModelScope.launch {
        documentsRepository.saveDocumentDetail(document)
    }

}