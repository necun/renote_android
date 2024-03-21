package com.renote.renoteai.di

import android.content.Context
import androidx.room.Room
import com.renote.renoteai.database.source.DocumentDatabase
import com.renote.renoteai.utils.Constants


fun provideDocumentDatabase(context: Context) =
    Room.databaseBuilder(context, DocumentDatabase::class.java, Constants.DOCUMENT_DATABASE)
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build()

fun provideDocumentDao(db: DocumentDatabase) = db.documentDao()
fun provideFolderDao(db: DocumentDatabase) = db.folderDao()

fun provideTagDao(db: DocumentDatabase) = db.tagDao()
fun provideFileDao(db: DocumentDatabase) = db.fileDao()