package com.renote.renoteai.ui.fragments.imports

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.renote.renoteai.R
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.databinding.FragmentImportBinding
import com.renote.renoteai.ui.presentation.home.viewmodel.HomeFragmentViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

class ImportFragment : Fragment() {
    private var currentFragment: Fragment? = null

    private var _binding: FragmentImportBinding? = null
    private val binding get() = _binding!!
    private val viewModel:HomeFragmentViewModel by inject()
    val docEntities = mutableListOf<DocumentEntity>()

    private var fileUri: Uri? = null
    private var noteTitle = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//      return inflater.inflate(R.layout.fragment_import, container, false)
        _binding = FragmentImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentFragment = ImportFragment()

        binding.apply {
            browse.setOnClickListener {
                showFilePicker()
            }

            btnFolder.setOnClickListener {
                val folderFragment = parentFragmentManager.beginTransaction()
                folderFragment.replace(R.id.frameLayout, FoldersFragment())
                folderFragment.addToBackStack(null)
                folderFragment.commit()
            }

       //     importBackBtn.setOnClickListener {
//                val homeFragment = parentFragmentManager.beginTransaction()
//                homeFragment.replace(R.id.frameLayout,HomeFragment())
//               // homeFragment.addToBackStack(null)
//                homeFragment.commit()
//                if (currentFragment !is HomeFragment) {
//                    val homeFragment = HomeFragment()
//                    childFragmentManager.beginTransaction()
//                        .replace(
//                            R.id.frameLayout,
//                            homeFragment
//                        ) // Ensure you have a FrameLayout in your layout where the fragment should be placed
//                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                        //.addToBackStack(null) // Optional: Add transaction to the back stack
//                        .commit()
//
//                    currentFragment = homeFragment
//                }
            //}

            btnSave.setOnClickListener {
                noteTitle = binding.etFileName.text.toString().trim { it <= ' ' }

                if (noteTitle.isEmpty()) {
                    Snackbar.make(it, "File name cannot be empty", Snackbar.LENGTH_SHORT).show()
                } else {
                    val fileUri:Uri = Uri.parse(fileUri.toString())
                    val fileType = getMimeType(requireContext(),fileUri)

                    println("importUri:$fileUri")

                    docEntities.add(
                        DocumentEntity(
                            id = "10",
                            name = noteTitle,
                            createdDate = 10052003,
                            updatedDate = 10052003,
                            fileData = fileUri.toString(),
                            isSynced = false,
                            isPin = false,
                            isFavourite = false,
                            folderId = "12",
                            fileDriveId = "",
                            openCount = 20,
                            localFilePathAndroid = "sssssssssssssss",
                            tagId = "1234",
                            driveType = "Gdrive",
                            fileExtension = fileType.toString()
                        )
                    )

                    viewModel.saveDocumentsDetails(docEntities)

//                    .noteId = 0
//                    noteEntity.noteTitle = noteTitle
//
//                    val currentTimeMillis = System.currentTimeMillis()
//                    val fileName = getFileName(fileUri) ?: "unKnownFile"
//                    val fileUri:Uri = Uri.parse(fileUri.toString())
//                    val fileType = getMimeType(requireContext(),fileUri)
//
//                    noteEntity.fileUri = fileUri.toString()
//                    noteEntity.fileName = fileName
//                    noteEntity.timeStamp = currentTimeMillis
//                    noteEntity.fileType = fileType.toString()
//
//                    viewmodel.saveNote(noteEntity)
//
//                    binding.etFileName.setText("")
//                    Toast.makeText(
//                        requireContext(), noteEntity.noteTitle + " saved", Toast.LENGTH_SHORT
//                    ).show()
                    binding.imgSelected.setImageResource(0)
                }
            }
        }

    }

//    private fun showFilePicker() {
//
//        // Check for camera permission
//        if (ContextCompat.checkSelfPermission(
//                requireContext(), android.Manifest.permission.CAMERA
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                requireActivity(), arrayOf(android.Manifest.permission.CAMERA), 100
//            )
//            return
//        }
//
//        // Intent for picking a file from the gallery
//        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
//            type = "*/*"
//        }
//
//        val chooserIntent = Intent.createChooser(galleryIntent, "Select a file")
//        fileUri = FileProvider.getUriForFile(
//            requireContext(), "${requireContext().packageName}.provider", createImageFile()
//        )
//        //chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
//
//        // Intent for capturing an image from the camera
//        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        fileUri = FileProvider.getUriForFile(
//            requireContext(), "${requireContext().packageName}.provider", createImageFile()
//        )
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
//
//        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
//        startActivityForResult(chooserIntent, 111)
//
////        startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
//    }
//
//    private fun createImageFile(): File {
//        // Create an image file name
//        val timeStamp: String =
//            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//        val storageDir: File =
//            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
//        return File.createTempFile(
//            "JPEG_${timeStamp}_", /* prefix */
//            ".jpg", /* suffix */
//            storageDir /* directory */
//        )
//    }

    private fun showFilePicker() {
        // Check for gallery permission


        // Intent for picking an image from the gallery
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*" // Limit to image files only
        }

        val chooserIntent = Intent.createChooser(galleryIntent, "Select a file")

        // Start the file picker activity
        startActivityForResult(chooserIntent, 111)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            // Handle result from gallery/file picker
            val selectedImageUri: Uri? = data?.data

            selectedImageUri?.let { uri ->
                // Create a file object to store the image in internal storage
                // Get the URI for the file in internal storage
               val savedFileUri =  saveImageToInternalStorage(uri)

                savedFileUri?.let {fileUri->
                    binding.apply {
                        imgSelected.setPadding(0, 0, 0, 0)
                        imgSelected.setImageURI(savedFileUri)
                    }
                    this.fileUri = fileUri
                }

            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): Uri? {
        return try {
            // Open an input stream from the selected image URI
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                // Create a file in the app's internal storage directory
                val outputFile = File(requireContext().filesDir, "imports/image.jpg")

                // Open an output stream to the created file
                FileOutputStream(outputFile).use { outputStream ->
                    // Copy the contents of the input stream to the output stream
                    inputStream.copyTo(outputStream)
                }

                // Create a file URI for the saved image
                FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.googledriveupload.provider",
                    outputFile
                )
            }
        } catch (e: IOException) {
            // Handle any errors that occur during the save process
            e.printStackTrace()
            null
        }
    }



//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
//            val uri: Uri? = data?.data
//
//            data?.data?.let { uri ->
//                fileUri = uri
//            }
////
//        }
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
//            // Handle result from both camera and gallery/file picker
//            val uri: Uri? =
//                data?.data ?: fileUri // Use fileUri if data is null, indicating camera usage
//
//            uri?.let {
//                // Here you can update your UI or handle the selected file URI
//                fileUri = uri
//
//                binding.apply {
//                    imgSelected.setPadding(0, 0, 0, 0)
//                    imgSelected.setImageURI(fileUri)
//                }
//
//                // For demonstration, show the URI in a Toast
//                Toast.makeText(requireContext(), "File Selected: $uri", Toast.LENGTH_SHORT).show()
//                println("importedURI:$uri")
//
//                // Assume you have a method in your ViewModel to save the URI
////                noteEntity.fileUri = uri.toString()
////                viewmodel.saveNote(noteEntity)
//            }
//        }
//    }

    private fun getFileName(fileUri: Uri?): String? {
        fileUri ?: return null// If fileUri is null, return null
        var fileName: String? = null
        val contentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(fileUri, null, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                fileName = c.getString(c.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }
        return fileName
    }

   private fun getMimeType(context: Context, uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            // If the URI is a content URI
            val contentResolver = context.contentResolver
            contentResolver.getType(uri)
        } else {
            // If the URI is a file URI
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase(Locale.ROOT))
        }
    }

}