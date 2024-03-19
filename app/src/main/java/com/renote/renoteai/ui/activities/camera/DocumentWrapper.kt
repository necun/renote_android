package com.renote.renoteai.ui.activities.camera

import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.database.tables.FolderEntity

data class DocumentsWrapper(
    val documents: List<DocumentEntity>,
    val folders: List<FolderEntity>
)