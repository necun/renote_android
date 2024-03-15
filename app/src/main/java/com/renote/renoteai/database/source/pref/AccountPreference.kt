package com.renote.renoteai.database.source.pref

import android.content.Context
import android.content.SharedPreferences

class AccountPreference (private val context: Context){

    private fun get(): SharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)



    fun clear(){
        get().edit().clear().apply()
    }



    companion object {
        private val TAG = AccountPreference::class.java.simpleName
        private const val SP_NAME: String = "AccountPrefs"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER = "user"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_IS_VERIFIED = "is_verified"

        private const val KEY_FCM_TOKEN = "fcm token"
    }





    fun saveIsVerified(value:Boolean){
        get().edit().putBoolean(KEY_IS_VERIFIED, value).apply()
    }

    fun getIsVerified():Boolean{
        return  get().getBoolean(KEY_IS_VERIFIED, false)
    }


    fun saveUserId(value:String?){
        get().edit().putString(KEY_USER_ID, value).apply()
    }

    fun getUserId():String{
        return  get().getString(KEY_USER_ID, "0")?:"0"
    }

    fun saveLanguage(lan: String) {
        get().edit().putString(KEY_LANGUAGE, lan).apply()
    }
    fun getLanguage():String{
        return  get().getString(KEY_LANGUAGE, "")?:""
    }
    fun saveLatitude(latitude: String) {
        get().edit().putString(KEY_LATITUDE, latitude).apply()
    }
    fun getLatitude():String{
        return  get().getString(KEY_LATITUDE, "")?:""
    }
    fun saveLongitude(latitude: String) {
        get().edit().putString(KEY_LONGITUDE, latitude).apply()
    }
    fun getLongitude():String{
        return  get().getString(KEY_LONGITUDE, "")?:""
    }



}