package com.renote.renoteai.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.renote.renoteai.database.tables.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTagsDetails(tagEntity: TagEntity)

    @Query("SELECT  * FROM tag_table ORDER BY id ASC")
    fun getAllTagDetails(): Flow<MutableList<TagEntity>>

    @Query("SELECT id FROM tag_table")
    fun getAllTagIds(): Flow<MutableList<String>>

    @Query("SELECT * FROM tag_table WHERE  tagName   LIKE  :searchWith ")
    fun getFoldersWithName(searchWith:String): List<TagEntity>

}