package com.renote.renoteai

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface api {

//    curl --location 'http://13.200.238.163:5001/send-email' \
//    --form 'to_email="snehal.trapsiya@gmail.com"' \
//    --form 'subject="This is test"' \
//    --form 'message="Hello please check you image"' \
//    --form 'image=@"/C:/Users/Administrator/Downloads/Necun/DocumentScanner/Sample_Images/2.jpeg"'

    @Multipart
    @POST("send-email")
     fun sendEmail(
        @Part image: MultipartBody.Part,
        @Part("to_email") toEmail: String,
        @Part("subject") subject: String,
        @Part("message") message: String
    ): Call<UploadResponse>


}
