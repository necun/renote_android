package com.renote.renoteai.database.custom_models

data class Document(
    val id: String,
    val name: String,
    val createdDate: Long,
    val updatedDate: Long,
    val fileData: String,
    val isSynced: Boolean,
    val isPin: Boolean,
    val isFavourite: Boolean,
    val folderId:String,
    val openCount:Int,
    val localFilePathIos:String,
    val localFilePathAndroid:String,
    val tagId:String,
    val driveType:String,
    val fileExtension:String

)



data class DocumentsContainer(
    val documents: Map<String, Document>
)