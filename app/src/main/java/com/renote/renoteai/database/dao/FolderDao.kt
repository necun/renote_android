package com.renote.renoteai.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.renote.renoteai.database.tables.FolderEntity
import com.renote.renoteai.database.tables.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFolderDetails(folderEntity: FolderEntity)

    @Query("SELECT  * FROM folders_table")
    fun getAllFolders(): Flow<MutableList<FolderEntity>>

    @Query("SELECT id FROM folders_table")
    fun getAllFolderIds(): Flow<MutableList<String>>

    @Query("SELECT * FROM folders_table WHERE  name LIKE  :searchWith ")
    fun getFoldersWithName(searchWith:String): List<FolderEntity>
}