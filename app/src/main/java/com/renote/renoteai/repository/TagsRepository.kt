package com.renote.renoteai.repository

import com.renote.renoteai.database.dao.TagDao
import com.renote.renoteai.database.tables.TagEntity


class TagsRepository(private val dao: TagDao) {

    suspend fun saveTagDetails(tag: TagEntity) = dao.saveTagsDetails(tag)
    fun getAllTagsDetails() = dao.getAllTagDetails()
//    suspend fun deleteNotes(note: List<NoteEntity>) = // populate this list with the IDs of notes you want to delete
//        dao.deleteNotes(note)
fun tagIdsFromDB() = dao.getAllTagIds()
    fun getTagsWithName(searchWith:String): List<TagEntity> = dao.getTagsWithName(searchWith)
}