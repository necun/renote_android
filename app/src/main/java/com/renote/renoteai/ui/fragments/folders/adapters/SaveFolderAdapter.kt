package com.renote.renoteai.ui.fragments.folders.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.renote.renoteai.database.tables.DocumentEntity
import com.renote.renoteai.databinding.FoldersRecyclerBinding
import com.renote.renoteai.database.tables.FolderEntity
import com.renote.renoteai.ui.presentation.home.viewmodel.HomeFragmentViewModel

class SaveFolderAdapter(private val viewModel: HomeFragmentViewModel) :
    RecyclerView.Adapter<SaveFolderAdapter.ViewHolder>() {

    //private val viewModelHome: HomeFragmentViewModel by inject()
    val docEntities = mutableListOf<DocumentEntity>()
    val arrayList = ArrayList<FolderEntity>()
    val selectList = ArrayList<FolderEntity>()
    private lateinit var binding: FoldersRecyclerBinding
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = FoldersRecyclerBinding.inflate(inflater, parent, false)

        context = parent.context
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
        holder.setIsRecyclable(false)
    }

    inner class ViewHolder(private val binding: FoldersRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FolderEntity) {
            binding.apply {
                name.text = item.name
            }

            binding.folder.setOnClickListener {
               Toast.makeText(context,"${item.name}",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<FolderEntity>() {
        override fun areItemsTheSame(oldItem: FolderEntity, newItem: FolderEntity): Boolean {
            return oldItem.id.equals(newItem.id)
        }

        override fun areContentsTheSame(oldItem: FolderEntity, newItem: FolderEntity): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

}