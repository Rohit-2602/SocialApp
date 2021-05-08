package com.example.socailapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socailapp.databinding.ItemConnectionRequestBinding
import com.example.socailapp.viewModel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConnectionAdapter(private val listener: ConnectionInterface) : ListAdapter<String, ConnectionAdapter.ConnectionViewHolder>(DiffUtilCallback()) {

    class ConnectionViewHolder(val binding: ItemConnectionRequestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uid: String) {
            binding.apply {
                CoroutineScope(Dispatchers.IO).launch {
                    val user = UserViewModel().getUserById(uid)!!
                    withContext(Dispatchers.Main) {
                        Glide.with(binding.root).load(user.imageURL).into(profileIV)
                        profilenameTV.text = user.name
                        descriptionTV.text = user.description
                    }
                }
            }
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionViewHolder {
        val binding = ItemConnectionRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ConnectionViewHolder(binding)

//        val currentItem = getItem(viewHolder.adapterPosition)
//
//        viewHolder.binding.acceptBTN.setOnClickListener {
//            listener.acceptRequest(currentItem)
//        }
//
//        viewHolder.binding.declineBTN.setOnClickListener {
//            listener.declineRequest(currentItem)
//        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ConnectionViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)

        holder.binding.acceptBTN.setOnClickListener {
            listener.acceptRequest(currentItem)
        }

        holder.binding.declineBTN.setOnClickListener {
            listener.declineRequest(currentItem)
        }

    }
}

interface ConnectionInterface {
    fun acceptRequest(uid: String)
    fun declineRequest(uid: String)
}