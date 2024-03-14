package com.renote.renoteai.database.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag_table")
data class TagEntity(
    @PrimaryKey
    val id: String,
    val tagName: String,

)