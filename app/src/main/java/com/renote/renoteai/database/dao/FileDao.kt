package com.renote.renoteai.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.database.tables.FileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {

    @Query("UPDATE File_table SET documentId = :documentId WHERE id = :fileId")
    suspend fun updateDocumentIdForFileEntity(fileId: String, documentId: String)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFilesDetails(fileEntity: List<FileEntity>)

    @Query("SELECT * FROM file_table WHERE documentId = :recentDocumentId")
    fun getRecentFileDetailsByRecentDocumentId(recentDocumentId: String): Flow<MutableList<FileEntity>>

    @Query("SELECT * FROM file_table WHERE documentId= :documentId")
    fun getFiles(documentId: String): Flow<MutableList<FileEntity>>
}