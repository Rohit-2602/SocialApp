package com.example.socailapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socailapp.R
import com.example.socailapp.data.User
import com.example.socailapp.databinding.ItemUserBinding
import com.example.socailapp.viewModel.SearchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchAdapter(private val listener: OnConnectionClick) : ListAdapter<User, SearchAdapter.SearchViewHolder>(DiffUtilCallback()) {

    class DiffUtilCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.name == newItem.name
        }
    }

    class SearchViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            CoroutineScope(Dispatchers.IO).launch {
                val currentUser = SearchViewModel().getCurrentUser()
                withContext(Dispatchers.Main) {
                    binding.apply {
                        if (user.id == currentUser.id || currentUser.connections.contains(user.id)) {
                            connectBTN.visibility = View.GONE
                        } else {
                            connectBTN.visibility = View.VISIBLE
                        }
                        if (currentUser.connectionRequests.contains(user.id)) {
                            connectBTN.setImageResource(R.drawable.ic_right_arrow)
                        }
                        Glide.with(binding.root).load(user.imageURL).circleCrop().into(profileIV)
                        profilenameTV.text = user.name
                        descriptionTV.text = user.description
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = SearchViewHolder(binding)
        binding.apply {
            connectBTN.setOnClickListener {
                listener.connect(getItem(viewHolder.adapterPosition).id)
            }
            constraintLayout.setOnClickListener {
                listener.seeUserProfile(getItem(viewHolder.adapterPosition).id)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val currentUser = getItem(position)
        holder.bind(currentUser)
    }

}

interface OnConnectionClick {
    fun connect(userId: String)
    fun seeUserProfile(userId: String)
}