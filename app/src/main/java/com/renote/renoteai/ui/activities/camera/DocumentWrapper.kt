package com.renote.renoteai.ui.activities.camera

import com.renote.renoteai.database.tables.DocumentEntity

data class DocumentsWrapper(
    val documents: List<DocumentEntity>
)