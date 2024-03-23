package com.renote.renoteai.ui.activities.camera.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CropViewModel(): ViewModel() {
    val resourseClick = MutableLiveData<Int>()

    fun onresorceClick(viewId: Int) {
        resourseClick.postValue(viewId)
    }

}