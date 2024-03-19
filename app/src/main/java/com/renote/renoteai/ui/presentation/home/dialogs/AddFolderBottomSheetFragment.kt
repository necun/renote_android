package com.renote.renoteai.ui.presentation.home.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.renote.renoteai.R
import com.renote.renoteai.database.tables.FolderEntity
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.renote.renoteai.databinding.CreateFolderDataBinding
import com.renote.renoteai.ui.presentation.home.viewmodel.AddFolderViewModel
import com.renote.renoteai.utils.CommonUtils
import org.koin.android.ext.android.inject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AddFolderBottomSheetFragment : BottomSheetDialogFragment() {
    lateinit var binding: CreateFolderDataBinding
    val viewModel: AddFolderViewModel by inject()
    var mContext: Context? = null

    private var folderTitle = ""
    val folderEntities = mutableListOf<FolderEntity>()

    @RequiresApi(Build.VERSION_CODES.O)
    val current = LocalDateTime.now()

    @RequiresApi(Build.VERSION_CODES.O)
    val formatter = DateTimeFormatter.ofPattern("ddHHmmss")

    @RequiresApi(Build.VERSION_CODES.O)
    val formattedTimestamp = current.format(formatter).toLong()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContext = activity
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_folder, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.create_button)
        drawable?.setBounds(0, 0, 50, 0) // Set the desired width and height
        binding.etFolderName.setCompoundDrawables(drawable, null, null, null)
        observeData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun observeData() {
        binding.apply {
            viewModel?.resourseClick?.observe(viewLifecycleOwner) { integer ->
                when (integer) {
                    R.id.close_iv -> {
                        dismiss()
                    }

                    R.id.btnCreateFolder -> {
                        viewModel?.createFolder()
                    }
                }
            }

            viewModel?.message?.observe(viewLifecycleOwner) { message ->
                if (message != null && message.isNotEmpty()) {
                    CommonUtils.showToast(mContext!!, message)
                }
            }

            viewModel?.isRefresh?.observe(viewLifecycleOwner) { isRefresh ->
                if (isRefresh) {
                    viewModel?.message?.observe(viewLifecycleOwner) { message ->
                        CommonUtils.showToast(mContext!!, message)
                    }
                    dismiss()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createFolderInInternalStorage(folderTitle: String) {
        val directory = File(requireContext().filesDir, "ReNoteAI/$folderTitle")
        if (!directory.exists()) {
            directory.mkdirs() // Create the directory if it doesn't exist
        }
        println("directory:$directory")
        saveToRoom(folderTitle)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveToRoom(folderTitle: String) {
        folderEntities.add(
            FolderEntity(
                id = "folder_${formattedTimestamp}",
                name = folderTitle,
                createdDate = formattedTimestamp,
                updatedDate = 10005003,
                emailOrPhone = "raju@gmail.com",
                isSynced = false,
                isPin = false,
                isFavourite = false,
                fileCount = 100,
                driveType = "gDrive"
            )
        )
//        viewModel.saveFolderDetails(folderEntities)
        Toast.makeText(requireContext(), "Folder Created", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return dialog
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

}