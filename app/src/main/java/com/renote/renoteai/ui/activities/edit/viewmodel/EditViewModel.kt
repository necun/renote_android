package com.renote.renoteai.ui.activities.edit.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renote.renoteai.repository.DocumentsRepository
import com.renote.renoteai.repository.FoldersRepository
import com.renote.renoteai.repository.TagsRepository

class EditViewModel(private val documentsRepository: DocumentsRepository, private  val foldersRepository: FoldersRepository, private val tagsRepository: TagsRepository): ViewModel() {


    val resourseClick = MutableLiveData<Int>()

    fun onresorceClick(viewId: Int) {
        resourseClick.postValue(viewId)
    }

}