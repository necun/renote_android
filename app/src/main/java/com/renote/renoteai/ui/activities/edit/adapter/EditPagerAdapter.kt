package com.renote.renoteai.ui.activities.edit.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.renote.renoteai.R
import com.renote.renoteai.database.tables.FileEntity
import com.renote.renoteai.databinding.DocumentsItemBinding
import com.renote.renoteai.databinding.EditPagerItemBinding


typealias OnTextChangeListener<T> = (position: Int, data: T) -> Unit
class EditPagerAdapter() :
    ListAdapter<FileEntity, EditPagerAdapter.PreviewHolder>(MediaDiffUtill) {
    lateinit var onTextChanged: OnTextChangeListener<String>
    var showEditTitle = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewHolder {
        val binding = DataBindingUtil.inflate<EditPagerItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.edit_pager_item_layout,
            parent,
            false
        )
        return PreviewHolder(binding, -1)
    }

    override fun onBindViewHolder(holder: PreviewHolder, position: Int) {
        holder.onBind(getItem(position),position)
    }


    inner class PreviewHolder(val binding:EditPagerItemBinding, private val pos: Int) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(media: FileEntity, position: Int) {
            binding.item = media
            if(showEditTitle){
                binding.toolbarLay.visibility = View.VISIBLE
            }

            val absoluteFilePath = media.fileData.removePrefix("file://")

// Decode the file path into a Bitmap
            val bitmap = BitmapFactory.decodeFile(absoluteFilePath)

// Set the Bitmap on the ImageView
            binding.previewImg.setImageBitmap(bitmap)
            println("loading path ${media.fileData.toUri()}")
            binding.countTxt.text = buildString {
                append(adapterPosition+1)
                append(" / ")
                append(itemCount)
            }
            binding.executePendingBindings()
            binding.fileNameEdit.doOnTextChanged { text, start, count, after ->
                onTextChanged(adapterPosition,text.toString())
            }
        }

    }
    override fun getItemViewType(position: Int) = R.layout.edit_pager_item_layout

    companion object {
        val MediaDiffUtill = object : DiffUtil.ItemCallback<FileEntity>()
        {
            override fun areItemsTheSame(oldItem: FileEntity, newItem: FileEntity): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: FileEntity, newItem: FileEntity): Boolean =
                oldItem == newItem

        }

    }
}