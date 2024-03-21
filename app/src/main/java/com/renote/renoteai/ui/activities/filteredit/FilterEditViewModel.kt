package com.renote.renoteai.ui.activities.filteredit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FilterEditViewModel(): ViewModel() {
    val resourseClick = MutableLiveData<Int>()

    fun onresorceClick(viewId: Int) {
        resourseClick.postValue(viewId)
    }
}