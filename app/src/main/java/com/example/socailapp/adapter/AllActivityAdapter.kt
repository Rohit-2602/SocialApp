package com.example.socailapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socailapp.data.Post
import com.example.socailapp.data.User
import com.example.socailapp.databinding.ItemPostBinding

class AllActivityAdapter(private val postCreator: User) :
    ListAdapter<Post, AllActivityAdapter.AllActivityViewHolder>(DiffUtilCallback()) {

    class DiffUtilCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.text == newItem.text
        }
    }

    inner class AllActivityViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                Glide.with(binding.root).load(postCreator.imageURL).circleCrop().into(postProfileIV)
                postBodyTV.text = post.text
                Glide.with(binding.root).load(post.image).into(postIV)
                usernameTV.text = postCreator.name
                descriptionTV.text = postCreator.description
                timeTV.text = post.createdAt
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllActivityViewHolder {
        return AllActivityViewHolder(
            ItemPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AllActivityViewHolder, position: Int) {
        val currentPost = getItem(position)
        holder.bind(currentPost)
    }
}