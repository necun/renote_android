package com.renote.renoteai.database.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag_table")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val idd: Int = 0,
    val id: String,
    val tagName: String,
    var isSelected: Boolean? = false
)