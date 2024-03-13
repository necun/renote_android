package com.renote.renoteai.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.renote.renoteai.database.tables.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDocumentsDetails(documentEntity: List<DocumentEntity>)

    @Query("SELECT  * FROM document_table")
     fun getAllDocumentDetails(): Flow<MutableList<DocumentEntity>>

    @Query("SELECT id FROM document_table")
     fun getAllDocumentIds(): Flow<MutableList<String>>

     //query if isSync is false then all the un synced files will retrieve
    @Query("SELECT * FROM document_table WHERE isSynced = 0")
    fun getAllUnsyncedDocumentIds(): Flow<MutableList<DocumentEntity>>

    //after successful uploading the file in drive it will update isSync to true
    @Query("UPDATE document_table SET isSynced = 1 WHERE id = :documentId")
    suspend fun markDocumentAsSynced(documentId: String)

}