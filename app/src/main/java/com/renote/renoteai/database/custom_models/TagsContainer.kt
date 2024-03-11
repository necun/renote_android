package com.renote.renoteai.database.custom_models

data class Tag(
    val id: String,
    val tagName: String,
    var isSelected: Boolean?
)


data class TagsContainer(
    val tags: Map<String, Tag>
)