package com.renote.renoteai.repository

import com.renote.renoteai.database.dao.DocumentDao
import com.renote.renoteai.database.dao.FileDao
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.database.tables.FileEntity


class FilesRepository(private val dao: FileDao) {
  suspend fun updateDocumentIdInFileEntity(fileId:String,documentId:String) = dao.updateDocumentIdForFileEntity(fileId,documentId)
    suspend fun saveFilesDetails(file: List<FileEntity>) =
        dao.saveFilesDetails(file)
    fun getRecentFileDetailsByRecentDocumentId(recentDocumentId:String) = dao.getRecentFileDetailsByRecentDocumentId(recentDocumentId)

}