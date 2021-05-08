package com.example.socailapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socailapp.data.User
import com.example.socailapp.databinding.ItemUserBinding

class SearchAdapter(private val listener: OnConnectionClick) : ListAdapter<User, SearchAdapter.SearchViewHolder>(DiffUtilCallback()) {

    class DiffUtilCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.name == newItem.name
        }
    }

    inner class SearchViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.apply {
                Glide.with(binding.root).load(user.imageURL).circleCrop().into(profileIV)
                profilenameTV.text = user.name
                descriptionTV.text = user.description
                Log.i("FIREBASE SERVICE", user.id)
                connectBTN.setOnClickListener {
                    listener.connect(user.id)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            ItemUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val currentUser = getItem(position)
        holder.bind(currentUser)
    }

}

interface OnConnectionClick {
    fun connect(userId: String)
}