package com.renote.renoteai.di



import com.renote.renoteai.ui.activities.camera.viewmodel.EmailViewModel
import com.renote.renoteai.ui.presentation.home.dialogs.CreateTagViewModel
import com.renote.renoteai.ui.registration.RegistrationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel{ RegistrationViewModel() }

    viewModel { EmailViewModel() }
    viewModel{CreateTagViewModel(get(),get(),get(),get())}
}