package com.example.socailapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socailapp.R
import com.example.socailapp.Utils
import com.example.socailapp.data.Post
import com.example.socailapp.data.User
import com.example.socailapp.databinding.ItemPostBinding
import com.example.socailapp.viewModel.HomeViewModel

class UserProfileAdapter(private val listener: UserProfileItemClick, private val currentUser: User) : ListAdapter<Post, UserProfileAdapter.UserProfileViewHolder>(DiffUtilCallBack()) {

    class DiffUtilCallBack : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.text == newItem.text
        }
    }

    inner class UserProfileViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                postBodyTV.text = post.text
                Glide.with(binding.root).load(post.image).into(postIV)
                Glide.with(binding.root).load(currentUser.imageURL).circleCrop().into(postProfileIV)
                usernameTV.text = currentUser.name
                descriptionTV.text = currentUser.description
                timeTV.text = Utils.getTimeAgo(post.createdAt)
                options.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserProfileViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = UserProfileViewHolder(binding)
        viewHolder.binding.likeButton.setOnClickListener {
            listener.onLikeClicked(getItem(viewHolder.adapterPosition).id)
        }
        viewHolder.binding.commentButton.setOnClickListener {
            listener.commentOnPost(getItem(viewHolder.adapterPosition))
        }
        return viewHolder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserProfileViewHolder, position: Int) {
        val currentPost = getItem(position)
        holder.bind(currentPost)

        val currentUserId = HomeViewModel().currentUserId
        val isLiked = currentPost.likedBy.contains(currentUserId)
        val likeCount = currentPost.likedBy.size
        val commentCount = currentPost.commentedBy.size
        if (isLiked) {
            holder.binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(
                0,
                R.drawable.ic_liked,
                0,
                0
            )
        } else {
            holder.binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(
                0,
                R.drawable.ic_not_liked,
                0,
                0
            )
        }

        when (likeCount) {
            0 -> holder.binding.likeCountTV.text = ""
            1 -> holder.binding.likeCountTV.text = "$likeCount Like"
            else -> holder.binding.likeCountTV.text = "$likeCount Likes"
        }
        when (commentCount) {
            0 -> holder.binding.commentCountTV.text = ""
            1 -> holder.binding.commentCountTV.text = "$commentCount comment"
            else -> holder.binding.commentCountTV.text = "$commentCount comments"
        }
    }
}

interface UserProfileItemClick {
    fun onLikeClicked(postId: String)
    fun commentOnPost(post: Post)
}