package com.renote.renoteai.database.custom_models

data class Folder(
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


data class FoldersContainer(
    val folders: Map<String, Folder>
)