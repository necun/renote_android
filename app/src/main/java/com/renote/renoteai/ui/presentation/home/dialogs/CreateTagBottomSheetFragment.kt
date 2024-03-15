package com.renote.renoteai.ui.presentation.home.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.renote.renoteai.R

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.renote.renoteai.databinding.CreateTagDataBinding
import com.renote.renoteai.utils.CommonUtils
import org.koin.android.ext.android.inject


class CreateTagBottomSheetFragment : BottomSheetDialogFragment() {
    lateinit var binding: CreateTagDataBinding
    val viewModel: CreateTagViewModel by inject()
    var mContext: Context? = null





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContext = activity
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tag, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return dialog
    }
    fun observeData() {
        viewModel.resourseClick.observe(viewLifecycleOwner) { integer ->
            when (integer) {
                R.id.close_iv -> {
                    dismiss()
                }

                R.id.create_tag_ll -> {

                    viewModel.createTag()
                }


            }
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message != null && message.isNotEmpty()) {
                CommonUtils.showToast(mContext!!, message)
            }
        }

        viewModel.isRefresh.observe(viewLifecycleOwner) { isRefresh ->
            if (isRefresh) {
                viewModel.message.observe(viewLifecycleOwner) { message ->
                    CommonUtils.showToast(mContext!!, message)
                }
                dismiss()
            }
        }
    }

}