package com.renote.renoteai.ui.presentation.home.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.renote.renoteai.R
import com.renote.renoteai.database.tables.FolderEntity
import com.renote.renoteai.databinding.FoldersItemBinding
import com.renote.renoteai.di.provideDocumentDatabase
import com.renote.renoteai.ui.base.listeners.TagsItemListener
import com.renote.renoteai.ui.fragments.folders.FolderFilesFragment


class FoldersAdapter(private val context: Context, private val userEmailId: String?) :
    ListAdapter<FolderEntity, FoldersAdapter.FoldersHolder>(
        diffUtil
    ) {
    val arrayList = ArrayList<FolderEntity>()
    val selectList = ArrayList<FolderEntity>()
    var selectedPosition = 0

    var itemClickListener: TagsItemListener? = null

    var isEnable = false
    var isSelectAll = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoldersHolder {
        val binding = DataBindingUtil.inflate<FoldersItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.folder_recycler_view,
            parent,
            false
        )
        return FoldersHolder(binding, -1)
    }

    override fun onBindViewHolder(holder: FoldersHolder, position: Int) {
        holder.onBind(getItem(position), position)

    }

    inner class FoldersHolder(private val binding: FoldersItemBinding, private val pos: Int) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(data: FolderEntity, position: Int) {
            if (data != null) {
                binding.folder = data
                binding.executePendingBindings()

                binding.folderRecycle.setOnClickListener {
//                selectedPosition = adapterPosition
//                itemClickListener?.onItemClicked(data, adapterPosition)
                    val folderId = data.id
                    val folderName = data.name
                    val sharedPreference =
                        context.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
                    var editor = sharedPreference.edit()
                    editor.putString("folderId", folderId)
                    editor.putString("folderName",folderName)
                    editor.apply()
                    editor.commit()

                    val database = provideDocumentDatabase(context)
                    val documentDao = database.documentDao()

                    documentDao.getDocuments(data.id)
                    // Assuming you have access to a context or an activity reference
                    val context = binding.root.context
                    if (context is AppCompatActivity) {
                        val fragmentManager = context.supportFragmentManager
                        val fragmentTransaction = fragmentManager.beginTransaction()

                        // Replace "YourFragmentToNavigateTo" with the name of your fragment class
                        val fragment = FolderFilesFragment()

                        // Add any necessary arguments to the fragment
                        // fragment.arguments = Bundle()

                        // Replace the current fragment with the new fragment
                        fragmentTransaction.replace(R.id.frameLayout, fragment)

                        // Add the transaction to the back stack (optional)
                        fragmentTransaction.addToBackStack(null)

                        // Commit the transaction
                        fragmentTransaction.commit()
                    }
                }
            }

        }
    }


    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<FolderEntity>() {
            override fun areItemsTheSame(oldItem: FolderEntity, newItem: FolderEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: FolderEntity, newItem: FolderEntity): Boolean {
                return oldItem == newItem
            }

        }

    }


}


