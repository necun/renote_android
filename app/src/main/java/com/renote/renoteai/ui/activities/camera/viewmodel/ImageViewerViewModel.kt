package com.renote.renoteai.ui.activities.camera.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renote.renoteai.repository.DocumentsRepository
import com.renote.renoteai.repository.FilesRepository

class ImageViewerViewModel(private val documentsRepository: DocumentsRepository, private val filesRepository: FilesRepository): ViewModel() {
    val resourseClick = MutableLiveData<Int>()

    fun onresorceClick(viewId: Int) {
        resourseClick.postValue(viewId)
    }
}