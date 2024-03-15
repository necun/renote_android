package com.renote.renoteai.repository

import com.renote.renoteai.database.dao.DocumentDao
import com.renote.renoteai.database.tables.DocumentEntity


class DocumentsRepository(private val dao: DocumentDao) {

    suspend fun saveDocumentDetails(document: List<DocumentEntity>) = dao.saveDocumentsDetails(document)
    fun getAllDocumentsDetails() = dao.getAllDocumentDetails()
//    suspend fun deleteNotes(note: List<NoteEntity>) = // populate this list with the IDs of notes you want to delete
//        dao.deleteNotes(note)
fun documentsIdsFromDB() = dao.getAllDocumentIds()

    fun getAllMostViewedDocuments()=dao.getAllMostViewedDocuments()

    fun getAllStarredDocuments()=dao.getAllStarredDocuments()

}