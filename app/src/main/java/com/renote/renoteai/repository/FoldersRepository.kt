package com.renote.renoteai.repository

import com.renote.renoteai.database.dao.FolderDao
import com.renote.renoteai.database.tables.FolderEntity


class FoldersRepository(private val dao: FolderDao) {

    suspend fun saveFolderFiles(note: List<FolderEntity>) = dao.saveFileFolderDetails(note)
    fun getAllFoldersFileDetails() = dao.getAllFolders()
//    suspend fun deleteNotes(note: List<NoteEntity>) = // populate this list with the IDs of notes you want to delete
//        dao.deleteNotes(note)
fun folderIdsFromDB() = dao.getAllFolderIds()
}