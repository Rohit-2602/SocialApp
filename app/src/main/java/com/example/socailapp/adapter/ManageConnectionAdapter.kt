package com.example.socailapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socailapp.databinding.ItemManageconnectionBinding
import com.example.socailapp.viewModel.ManageConnectionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageConnectionAdapter(private val listener: OnManageConnectionClick) :
    ListAdapter<String, ManageConnectionAdapter.ManageConnectionViewHolder>(DiffUtilCallback()) {

    class DiffUtilCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return false
        }
    }

    class ManageConnectionViewHolder(val binding: ItemManageconnectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(userId: String) {
            binding.apply {
                CoroutineScope(Dispatchers.IO).launch {
                    val user = ManageConnectionViewModel().getUserById(userId)
                    withContext(Dispatchers.Main) {
                        Glide.with(binding.root).load(user.imageURL).circleCrop().into(profileIV)
                        profilenameTV.text = user.name
                        descriptionTV.text = user.description
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageConnectionViewHolder {
        val binding =
            ItemManageconnectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ManageConnectionViewHolder(binding)

        binding.constraintLayout.setOnClickListener {
            listener.seeProfile(getItem(viewHolder.adapterPosition))
        }

        binding.removeBTN.setOnClickListener {
            listener.showBottomSheet(getItem(viewHolder.adapterPosition))
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ManageConnectionViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }
}

interface OnManageConnectionClick {
    fun seeProfile(userId: String)
    fun showBottomSheet(userId: String)
}