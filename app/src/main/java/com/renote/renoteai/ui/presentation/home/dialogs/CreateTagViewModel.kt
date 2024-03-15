package com.renote.renoteai.ui.presentation.home.dialogs

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.renote.base.BaseViewModel
import com.renote.renoteai.R
import com.renote.renoteai.database.tables.TagEntity
import com.renote.renoteai.repository.DocumentsRepository
import com.renote.renoteai.repository.FoldersRepository
import com.renote.renoteai.repository.TagsRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class CreateTagViewModel(private val documentsRepository: DocumentsRepository, private  val foldersRepository: FoldersRepository, private val tagsRepository: TagsRepository, private val application: Application): BaseViewModel(documentsRepository,foldersRepository,tagsRepository,application) {

    var id = MutableLiveData(0L)
    val tagName = MutableLiveData<String>("")
    val resourseClick = MutableLiveData<Int>()
    val message = MutableLiveData<String>()
    val isRefresh = MutableLiveData<Boolean>()
    fun onresorceClick(viewId: Int) {
        resourseClick.postValue(viewId)
    }

    fun createTag() {
        if (tagName.value == null || tagName.value!!.isEmpty()) {
            message.postValue(application.resources.getString(R.string.enter_tag_name_message))
        } else {
            ioScope.launch {
                val c = tagsRepository.getFoldersWithName(tagName.value!!)
                if (c.size == 1) {
                    message.postValue(tagName.value + " tag is already exists")
                } else {
                    val tag= TagEntity("tag_${getCurrentTimestamp()}", tagName = tagName.value!!)
                    if(id.value==0L){
                        ioScope.launch {
                            tagsRepository.saveTagDetails(tag)
                            isRefresh.postValue(true)
                            message.postValue(tagName.value + " tag created")
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