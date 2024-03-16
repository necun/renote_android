package com.renote.renoteai.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.database.tables.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFileFolderDetails(folderEntity: List<FolderEntity>)

    @Query("SELECT  * FROM folders_table")
    fun getAllFolders(): Flow<MutableList<FolderEntity>>

    @Query("SELECT * FROM folders_table")
    fun getAllFoldersForJson(): List<FolderEntity>

    @Query("SELECT id FROM folders_table")
    fun getAllFolderIds(): Flow<MutableList<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoldersFromJson(folderEntities: List<FolderEntity>)
}