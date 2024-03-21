package com.renote.renoteai.ui.activities.camera.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.database.tables.FileEntity
import com.renote.renoteai.repository.DocumentsRepository
import com.renote.renoteai.repository.FilesRepository
import kotlinx.coroutines.launch

class CameraViewModel(private val documentsRepository: DocumentsRepository,private val filesRepository:FilesRepository): ViewModel() {

    private val _recentDocumentId = MutableLiveData<String>()
    val recentDocumentId: LiveData<String>
        get() = _recentDocumentId

    val resourseClick = MutableLiveData<Int>()

    fun onresorceClick(viewId: Int) {
        resourseClick.postValue(viewId)
    }
    fun saveDocumentDetail(document: DocumentEntity) = viewModelScope.launch {
        documentsRepository.saveDocumentDetail(document)
    }

    fun getRecentDocumentId() = viewModelScope.launch {
        documentsRepository.getRecentDocumentId().collect() {
            _recentDocumentId.postValue(it)
        }
    }


        fun updateDocumentIdInFileEntity(fileId: String, documentId: String) =
            viewModelScope.launch {
                filesRepository.updateDocumentIdInFileEntity(fileId, documentId)
            }


    fun saveFilesDetails(file: List<FileEntity>) = viewModelScope.launch {
        filesRepository.saveFilesDetails(file)
    }
}