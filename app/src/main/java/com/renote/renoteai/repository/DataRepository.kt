package com.renote.renoteai.repository

import com.renote.renoteai.database.source.pref.AccountPreference

class DataRepository(
    private val pref: AccountPreference
    ) {

    fun getIsVerfied() = pref.getIsVerified()
    fun getLoggedInUserId() = pref.getUserId()
    fun getLanguage()= pref.getLanguage()
    fun getLatitude()= pref.getLatitude()
    fun getLongitude()= pref.getLongitude()



    /* private var user: User? = null
     init {
         user = pref.readUser()
     }*/

    fun clear() = pref.clear()







}