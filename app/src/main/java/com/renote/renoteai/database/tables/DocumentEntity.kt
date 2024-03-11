package com.renote.renoteai.database.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Document_table")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val  idd:Int = 0,
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