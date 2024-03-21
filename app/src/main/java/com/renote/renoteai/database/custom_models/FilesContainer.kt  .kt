package com.renote.renoteai.database.custom_models

data class File(
    val id: String,
    val name: String,
    val createdDate: Long,
    val updatedDate: Long,
    val documentId:String="",
    val isSynced: Boolean,
    val isPin: Boolean,
    val isFavourite: Boolean,
    val fileData: String,
    val openCount:Int,
    val localFilePathAndroid:String,
    val tagId:String,
    val driveType:String,
    val fileExtension:String
)


data class FilesContainer(
    val files: Map<String, File>
)