package com.renote.renoteai.di


import com.renote.renoteai.repository.DocumentsRepository
import com.renote.renoteai.repository.FoldersRepository
import com.renote.renoteai.repository.TagsRepository
import com.renote.renoteai.ui.presentation.home.viewmodel.HomeFragmentViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    viewModel() { HomeFragmentViewModel(get(), get(), get()) }


    //Document

    //Folders

    single { provideDocumentDatabase(androidContext()) }
    single { provideDocumentDao(get()) }
    single { provideFolderDao(get()) }
    single { provideTagDao(get()) }
    factory { DocumentsRepository(get()) }
    factory { FoldersRepository(get()) }
    factory { TagsRepository(get()) }
}