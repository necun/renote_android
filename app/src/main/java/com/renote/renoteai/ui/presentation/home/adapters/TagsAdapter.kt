package com.renote.renoteai.ui.presentation.home.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.renote.renoteai.R
import com.renote.renoteai.ui.base.listeners.TagsItemListener
import com.renote.renoteai.databinding.TagsItemBinding
import com.renote.renoteai.database.tables.TagEntity


class TagsAdapter(var context: Context) : ListAdapter<TagEntity, TagsAdapter.TagsHolder>(diffUtil) {

    var selectedPosition = 0

    var itemClickListener: TagsItemListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsHolder {
        val binding = DataBindingUtil.inflate<TagsItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.tag_recycler_view,
            parent,
            false
        )
        return TagsHolder(binding, -1)
    }

    override fun onBindViewHolder(holder: TagsHolder, position: Int) {
        holder.onBind(getItem(position), position)


    }


    inner class TagsHolder(private val binding: TagsItemBinding, private val pos: Int) :
        RecyclerView.ViewHolder(binding.root) {


        fun onBind(data: TagEntity?, position: Int) {
            if (data != null) {
                binding.tag = data
                binding.executePendingBindings()

                if (selectedPosition == position) {
                    binding.tagNameTV.setBackgroundResource(R.drawable.green_gradient)
                    binding.tagNameTV.setTextColor(context.getColor(R.color.white))
                } else {
                    binding.tagNameTV.setTextColor(context.getColor(R.color.text_color))
                    binding.tagNameTV.setBackgroundResource(R.drawable.white_gradient)
                }
//                 if(data.isSelected == true){
//
//                 }
//                 else if(data.isSelected == false){
//                     binding.tagNameTV.setBackgroundResource(R.drawable.white_gradient)
//                 }
                binding.tagNameTV.setOnClickListener {
                    selectedPosition = adapterPosition
                    itemClickListener?.onItemClicked(it, adapterPosition)
                    notifyDataSetChanged()
                }
            }
        }
    }


    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<TagEntity>() {
            override fun areItemsTheSame(oldItem: TagEntity, newItem: TagEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: TagEntity, newItem: TagEntity): Boolean {
                return oldItem == newItem
            }

        }

    }
}