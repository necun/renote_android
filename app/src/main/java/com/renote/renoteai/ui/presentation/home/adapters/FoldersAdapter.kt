package com.renote.renoteai.ui.presentation.home.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.renote.renoteai.R
import com.renote.renoteai.database.tables.FolderEntity
import com.renote.renoteai.databinding.FoldersItemBinding


class FoldersAdapter(private val context:Context,private val userEmailId: String?) :  ListAdapter<FolderEntity, FoldersAdapter.FoldersHolder>(
    diffUtil
)  {
    val arrayList = ArrayList<FolderEntity>()
    val selectList = ArrayList<FolderEntity>()


    var isEnable = false
    var isSelectAll = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoldersHolder {
        val binding = DataBindingUtil.inflate<FoldersItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.folder_recycler_view,
            parent,
            false
        )
        return FoldersHolder(binding,-1)
    }

    override fun onBindViewHolder(holder: FoldersHolder, position: Int) {
        holder.onBind(getItem(position),position)

    }



    inner class FoldersHolder(private val binding: FoldersItemBinding, private val pos:Int) : RecyclerView.ViewHolder(binding.root) {


        fun onBind(data: FolderEntity?, position:Int) {
            if(data != null){
                binding.folder = data

                binding.executePendingBindings()
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


