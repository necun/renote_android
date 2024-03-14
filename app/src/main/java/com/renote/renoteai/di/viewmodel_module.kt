package com.renote.renoteai.di



import com.renote.renoteai.ui.activities.camera.viewmodel.EmailViewModel
import com.renote.renoteai.ui.fragments.folders.viewmodel.FolderFilesViewModel
import com.renote.renoteai.ui.presentation.home.viewmodel.HomeFragmentViewModel
import com.renote.renoteai.ui.registration.RegistrationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel() { HomeFragmentViewModel(get(), get(), get()) }
    viewModel{ RegistrationViewModel() }

    viewModel { EmailViewModel() }
    viewModel{FolderFilesViewModel(get(),get(),get())}
}