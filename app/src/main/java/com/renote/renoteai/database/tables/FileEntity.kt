package com.renote.renoteai.database.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "File_table")
data class FileEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val createdDate: Long,
    val updatedDate: Long,
    val documentId:String,
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