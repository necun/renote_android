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

    @Query("SELECT * FROM document_table ORDER BY openCount DESC ")
    fun getAllMostViewedDocuments(): Flow<MutableList<DocumentEntity>>

    @Query("SELECT * FROM document_table where isFavourite = 1 ORDER BY isFavourite DESC ")
    fun getAllStarredDocuments(): Flow<MutableList<DocumentEntity>>
}