package com.renote.renoteai.ui.fragments.folders.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.repository.DocumentsRepository
import com.renote.renoteai.repository.FoldersRepository
import com.renote.renoteai.repository.TagsRepository
import com.renote.renoteai.ui.presentation.home.adapters.DocumentsDetailsAdapter
import kotlinx.coroutines.launch

class MultipleFilesViewModel (
    private val documentsRepository: DocumentsRepository,
    private val foldersRepository: FoldersRepository,
    private val tagsRepository: TagsRepository): ViewModel()
{
//    lateinit var documentsDetailsAdapter: DocumentsDetailsAdapter
//    private val _folderDocumentsDetailsList = MutableLiveData<List<DocumentEntity>>()
//    val folderDocumentsDetailsList: LiveData<List<DocumentEntity>>
//        get() = _folderDocumentsDetailsList
//
//    fun getFolderDocumentsDetails(folderId: String) = viewModelScope.launch {
//        documentsRepository.getDocuments(folderId).collect() {
//            _folderDocumentsDetailsList.postValue(it)
//        }
//    }
}