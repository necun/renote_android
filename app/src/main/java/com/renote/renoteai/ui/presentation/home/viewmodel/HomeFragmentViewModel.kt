package com.renote.renoteai.ui.presentation.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.renote.renoteai.ui.presentation.home.adapters.TagsAdapter
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.database.tables.FolderEntity

import com.renote.renoteai.repository.DocumentsRepository
import com.renote.renoteai.repository.FoldersRepository
import com.renote.renoteai.database.tables.TagEntity
import com.renote.renoteai.repository.TagsRepository
import com.renote.renoteai.ui.presentation.home.adapters.DocumentsDetailsAdapter
import com.renote.renoteai.ui.presentation.home.adapters.FoldersAdapter
import kotlinx.coroutines.launch

class HomeFragmentViewModel(private val documentsRepository: DocumentsRepository,private  val foldersRepository: FoldersRepository,private val tagsRepository:TagsRepository): ViewModel() {
    lateinit var tagsAdapter: TagsAdapter
    lateinit var foldersAdapter: FoldersAdapter
    lateinit var documentsAdapter: DocumentsDetailsAdapter

//documents

    private val _allDocumentsIdsList = MutableLiveData<List<String>>()
    val allDocumentsIdsList: LiveData<List<String>>
        get() = _allDocumentsIdsList


    private val _documentsDetailsList = MutableLiveData<List<DocumentEntity>>()
    val documentsDetailsList: LiveData<List<DocumentEntity>>
        get() = _documentsDetailsList

    private val _unSyncedDocumentDetailsList = MutableLiveData<List<DocumentEntity>>()
    val unSyncedDocumentDetailsList:LiveData<List<DocumentEntity>>
        get() = _unSyncedDocumentDetailsList

    fun getAllDocumentsDetails() = viewModelScope.launch {
        documentsRepository.getAllDocumentsDetails().collect() {
            _documentsDetailsList.postValue(it)
        }
    }

    fun getAllDocumentsIds() = viewModelScope.launch {
        documentsRepository.documentsIdsFromDB().collect(){
            _allDocumentsIdsList.postValue(it)
        }
    }

    fun getAllUnSyncedDocumentIds() = viewModelScope.launch {
        documentsRepository.getUnSyncedDocuments().collect(){
            _unSyncedDocumentDetailsList.postValue(it)
        }
    }


//    fun saveFolderFilesDetails(note: List<FolderEntity>) = viewModelScope.launch {
//        folderRepository.saveFolderFiles(note)
//    }

    fun saveDocumentsDetails(document: List<DocumentEntity>) = viewModelScope.launch {
        documentsRepository.saveDocumentDetails(document)
    }


    //folder details

    private val _allFolderIdsList = MutableLiveData<List<String>>()
    val allFolderIdsList: LiveData<List<String>>
        get() = _allFolderIdsList


    private val _folderFileDetailsList = MutableLiveData<List<FolderEntity>>()
    val folderFileDetailsList: LiveData<List<FolderEntity>>
        get() = _folderFileDetailsList

    fun getAllFolderFileDetails() = viewModelScope.launch {
        foldersRepository.getAllFoldersFileDetails().collect() {
            _folderFileDetailsList.postValue(it)
        }
    }

    fun getAllFolderIds() = viewModelScope.launch {
        foldersRepository.folderIdsFromDB().collect(){
            _allFolderIdsList.postValue(it)
        }
    }



//
//    fun saveFolderFilesDetails(folder: List<FolderEntity>) = viewModelScope.launch {
//        foldersRepository.saveFolderFiles(folder)
//    }



    //tag details
    var tagsList: MutableList<TagEntity> = java.util.ArrayList<TagEntity>()
    private val _allTagIdsList = MutableLiveData<List<String>>()
    val allTagIdsList: LiveData<List<String>>
        get() = _allTagIdsList


    private val _tagDetailsList = MutableLiveData<List<TagEntity>>()
    val tagDetailsList: LiveData<List<TagEntity>>
        get() = _tagDetailsList

    fun getAllTagDetails() = viewModelScope.launch {
        tagsRepository.getAllTagsDetails().collect() {
            _tagDetailsList.postValue(it)
        }
    }

    fun getAllTagIds() = viewModelScope.launch {
        tagsRepository.tagIdsFromDB().collect(){
            _allTagIdsList.postValue(it)
        }
    }





    fun saveTagDetails(tag: TagEntity) = viewModelScope.launch {
        tagsRepository.saveTagDetails(tag)
    }


//    private val _currentFolderId = MutableLiveData<Int>()
//    val currentFolderId: LiveData<Int> = _currentFolderId
//
//    // Flow of notes in the current folder
//    val notesInCurrentFolder: LiveData<List<NoteEntity>> = _currentFolderId.switchMap { folderId ->
//        repository.getNotesByFolder(folderId).asLiveData()
//    }
//
//    // Function to be called when a new folder is selected
//    fun setCurrentFolder(folderId: Int) {
//        _currentFolderId.value = folderId
//    }
//
//    // Function to insert a new folder
//    fun insertFolder(folderName: String) {
//        // Create a new FolderEntity and insert it
//        viewModelScope.launch {
//            val newFolder = FolderEntity(folderName = folderName)
//            val newFolderId = repository.insertFolder(newFolder)
//            // Optionally update the current folder
//            _currentFolderId.value = newFolderId.toInt()
//        }
//    }
//
//    // Add additional ViewModel operations for updating and deleting folders if needed.
//    // ...
    companion object {
    }


}



