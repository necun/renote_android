package com.renote.renoteai.ui.fragments.folders.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renote.renoteai.database.tables.FileEntity
import com.renote.renoteai.repository.FilesRepository
import com.renote.renoteai.ui.fragments.files.adapters.FileDetailsAdapter
import kotlinx.coroutines.launch

class MultipleFilesViewModel(
    private val filesRepository: FilesRepository
) : ViewModel() {

//    lateinit var filesDetailsAdapter: FilesAdapter
    lateinit var filesDetailsAdapter: FileDetailsAdapter

    private val _documentFilesDetailsList = MutableLiveData<List<FileEntity>>()
    val documentFilesDetailsList: LiveData<List<FileEntity>>
        get() = _documentFilesDetailsList

    fun getDocumentFilesDetails(documentId:String) = viewModelScope.launch {
        filesRepository.getFiles(documentId).collect(){
            _documentFilesDetailsList.postValue(it)
        }
    }

}