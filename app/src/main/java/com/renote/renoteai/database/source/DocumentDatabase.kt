package com.renote.renoteai.database.source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.renote.renoteai.database.dao.DocumentDao
import com.renote.renoteai.database.dao.FileDao
import com.renote.renoteai.database.dao.FolderDao
import com.renote.renoteai.database.dao.TagDao
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.database.tables.FolderEntity
import com.renote.renoteai.database.tables.TagEntity
import com.renote.renoteai.database.tables.FileEntity

@Database(entities = [FolderEntity::class, DocumentEntity::class, TagEntity::class, FileEntity::class], version = 1, exportSchema = false)
abstract class DocumentDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun folderDao(): FolderDao

    abstract fun tagDao(): TagDao
    abstract fun fileDao(): FileDao
}

