package com.renote.renoteai.repository

import com.renote.renoteai.database.dao.FolderDao
import com.renote.renoteai.database.tables.FolderEntity
import com.renote.renoteai.database.tables.TagEntity


class FoldersRepository(private val dao: FolderDao) {

    suspend fun saveFolderDetails(folder: FolderEntity) = dao.saveFolderDetails(folder)
    fun getAllFoldersFileDetails() = dao.getAllFolders()
//    suspend fun deleteNotes(note: List<NoteEntity>) = // populate this list with the IDs of notes you want to delete
//        dao.deleteNotes(note)
fun folderIdsFromDB() = dao.getAllFolderIds()
    fun getFoldersWithName(searchWith:String): List<FolderEntity> = dao.getFoldersWithName(searchWith)
}