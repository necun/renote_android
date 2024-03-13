package com.renote.renoteai.database.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders_table")
data class FolderEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val createdDate: Long,
    val updatedDate: Long,
    val  emailOrPhone: String,
    val isSynced: Boolean,
    val isPin: Boolean,
    val isFavourite: Boolean,
    val fileCount: Int,
    val driveType: String

)