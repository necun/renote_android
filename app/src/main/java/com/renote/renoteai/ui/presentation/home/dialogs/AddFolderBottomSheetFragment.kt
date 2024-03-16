package com.renote.renoteai.ui.presentation.home.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.renote.renoteai.R
import com.renote.renoteai.database.tables.FolderEntity
import com.renote.renoteai.databinding.FragmentAddFolderBinding
import com.renote.renoteai.ui.presentation.home.viewmodel.HomeFragmentViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AddFolderBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentAddFolderBinding? = null
    private val binding get() = _binding
    private var folderTitle = ""
    val folderEntities = mutableListOf<FolderEntity>()
    @RequiresApi(Build.VERSION_CODES.O)
    val current = LocalDateTime.now()
    @RequiresApi(Build.VERSION_CODES.O)
    val formatter = DateTimeFormatter.ofPattern("ddHHmmss")
    @RequiresApi(Build.VERSION_CODES.O)
    val formattedTimestamp = current.format(formatter).toLong()
    private val viewModel: HomeFragmentViewModel by inject()
  //  private val folderEntity by lazy { FolderEntity() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddFolderBinding.inflate(layoutInflater)
        return binding!!.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.create_button)
        drawable?.setBounds(0, 0, 50, 0) // Set the desired width and height
//        binding!!.tag_et.setCompoundDrawables(drawable, null, null, null)
        binding!!.etFolderName.setCompoundDrawables(drawable, null, null, null)

        binding!!.apply {
            closeIv.setOnClickListener {
                dismiss()
            }

            btnCreateFolder.setOnClickListener {
                folderTitle = binding!!.etFolderName.text.toString().trim()

                if (folderTitle.isEmpty()){
                    Snackbar.make(it, "File name cannot be empty", Snackbar.LENGTH_SHORT).show()
                }else{
                   // createFolderInInternalStorage(folderTitle)
                    saveToRoom(folderTitle)
                }
            }
        }

        //to display the dropdown menu for selection of accounts
        val accounts = resources.getStringArray(R.array.sync_options)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, accounts)
        val autoTextView = binding!!.autoCompleteTextView
        autoTextView.setAdapter(arrayAdapter)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createFolderInInternalStorage(folderTitle:String) {
        val directory = File(requireContext().filesDir, "ReNoteAI/$folderTitle")
        if (!directory.exists()) {
            directory.mkdirs() // Create the directory if it doesn't exist
        }
        println("directory:$directory")
        //val folderUri = FileProvider.getUriForFile(requireContext(),"com.example.googledriveupload.provider",directory)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveToRoom(folderTitle: String){

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
        viewModel.saveFolderFilesDetails(folderEntities)

        Toast.makeText(requireContext(),"Folder Created",Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = STATE_EXPANDED
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Optional: to make the corners outside the dialog transparent
        dialog.setContentView(R.layout.fragment_home) // Set your custom layout here
        return dialog
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

}