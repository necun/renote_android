package com.renote.renoteai.ui.presentation.home.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.renote.renoteai.R
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.database.tables.FileEntity
import com.renote.renoteai.databinding.DocumentsItemBinding
import com.renote.renoteai.di.provideDocumentDatabase
import com.renote.renoteai.ui.fragments.folders.MultipleFilesFragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentsDetailsAdapter(private val context: Context) :
    ListAdapter<DocumentEntity, DocumentsDetailsAdapter.DocumentsHolder>(diffUtil) {
    val arrayList = ArrayList<DocumentEntity>()
    val selectList = ArrayList<DocumentEntity>()
    private lateinit var binding: DocumentsItemBinding
    var isEnable = false
    var isSelectAll = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentsHolder {
        val binding = DataBindingUtil.inflate<DocumentsItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.docs_recycler_view,
            parent,
            false
        )
        return DocumentsHolder(binding, -1)
    }

    override fun onBindViewHolder(holder: DocumentsHolder, position: Int) {
        holder.onBind(getItem(position), position)

    }

    inner class DocumentsHolder(private val binding: DocumentsItemBinding, private val pos: Int) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(data: DocumentEntity, position: Int) {
            if (data != null) {
                binding.document = data
                binding.dateAndTime.text = convertTimestampToDateAndTime(data.createdDate)
                binding.executePendingBindings()

                binding.docRelativeLayout.setOnClickListener {
//
                    val documentId = data.id
                    val documentName = data.name

                    val sharedPreference =

                        context.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
                    var editor = sharedPreference.edit()
                    editor.putString("documentId",documentId)
                    editor.putString("documentName",documentName)
                    editor.apply()
                    editor.commit()

                    val database = provideDocumentDatabase(context)
                    val fileDao = database.fileDao()

                    fileDao.getFiles(data.id)

                    val context = binding.root.context
                    if (context is AppCompatActivity) {
                        val fragmentManager = context.supportFragmentManager
                        val fragmentTransaction = fragmentManager.beginTransaction()
                        val fragment = MultipleFilesFragment()
                        fragmentTransaction.replace(R.id.frameLayout, fragment)
                        fragmentTransaction.addToBackStack(null)  // Optional: Add to back stack to enable back navigation
                        fragmentTransaction.commit()
                    }

                }

            }
        }
    }

    fun convertTimestampToDateAndTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }

    private fun openFileFromUri(uri: Uri) {
        try {
            val file = File(uri.path ?: return)
            val fileUri = FileProvider.getUriForFile(context, "com.renote.renoteai.provider", file)

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(fileUri, context.contentResolver.getType(fileUri))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
            }
        } catch (exception: Exception) {
            Toast.makeText(context, "Error opening file: ${exception.message}", Toast.LENGTH_SHORT)
                .show()
            println("Error opening file: ${exception.message}")
            exception.printStackTrace()
        }
    }


    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<DocumentEntity>() {
            override fun areItemsTheSame(
                oldItem: DocumentEntity,
                newItem: DocumentEntity
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: DocumentEntity,
                newItem: DocumentEntity
            ): Boolean {
                return oldItem == newItem
            }

        }

    }
}