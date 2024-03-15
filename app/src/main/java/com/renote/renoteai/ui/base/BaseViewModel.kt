package com.renote.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.renote.renoteai.repository.DocumentsRepository
import com.renote.renoteai.repository.FoldersRepository
import com.renote.renoteai.repository.TagsRepository

import com.renote.renoteai.utils.helper.Event


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

open class BaseViewModel(private val documentsRepository: DocumentsRepository, private  val foldersRepository: FoldersRepository, private val tagsRepository: TagsRepository, application:Application):AndroidViewModel(application) {

    private val viewModelJob = Job()
    protected val messageLiveData = MutableLiveData<String>()
    private val navigateToScreen = MutableLiveData<Event<Int>>()

    val uiScope = CoroutineScope(Dispatchers.Main)
    val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    /**
     * Cancel all coroutines when the ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        uiScope.coroutineContext.cancel()
        ioScope.coroutineContext.cancel()
        viewModelJob.cancel()
    }


}