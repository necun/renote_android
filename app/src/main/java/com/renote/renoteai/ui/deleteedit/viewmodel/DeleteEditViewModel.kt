package com.renote.renoteai.ui.deleteedit.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.renote.renoteai.repository.FilesRepository
import kotlinx.coroutines.launch

class DeleteEditViewModel(private val filesRepository: FilesRepository): ViewModel() {
    val resourseClick = MutableLiveData<Int>()

    fun onresorceClick(viewId: Int) {
        resourseClick.postValue(viewId)
    }

    fun deleteFileByFileId(fileId: String) = viewModelScope.launch {
        filesRepository.deleteFileByFileId(fileId)
    }
}