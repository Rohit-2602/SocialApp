package com.example.socailapp.adapter

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socailapp.R
import com.example.socailapp.Utils
import com.example.socailapp.data.Post
import com.example.socailapp.databinding.ItemPostBinding
import com.example.socailapp.viewModel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeAdapter(private val listener: HomeItemClickListener) :
    ListAdapter<Post, HomeAdapter.HomeViewHolder>(DiffUtilCallBack()) {

    class DiffUtilCallBack: DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.text == newItem.text
        }
    }

    class HomeViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                CoroutineScope(Dispatchers.IO).launch {
                    val postCreator = HomeViewModel().getUserById(post.creatorId)
                    withContext(Dispatchers.Main) {
                        postBodyTV.text = post.text
                        Glide.with(binding.root).load(post.image).into(postIV)
                        Glide.with(binding.root).load(postCreator.imageURL).circleCrop().into(postProfileIV)
                        usernameTV.text = postCreator.name
                        descriptionTV.text = postCreator.description
                        timeTV.text = Utils.getTimeAgo(post.createdAt)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = HomeViewHolder(binding)
        viewHolder.binding.likeButton.setOnClickListener {
            listener.onLikeClicked(getItem(viewHolder.adapterPosition).id)
        }
        viewHolder.binding.constraint.setOnClickListener {
            listener.seeUserProfile(getItem(viewHolder.adapterPosition).creatorId)
        }
        viewHolder.binding.commentButton.setOnClickListener {
            listener.commentOnPost(getItem(viewHolder.adapterPosition))
        }
        viewHolder.binding.options.setOnClickListener {
            val wrapper = ContextThemeWrapper(parent.context, R.style.Widget_AppCompat_PopupMenu)
            val popup = PopupMenu(wrapper, it, Gravity.END)
            popup.inflate(R.menu.post_menu)
            popup.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.deletePost -> {
                        listener.deletePost(getItem(viewHolder.adapterPosition).id)
                        Toast.makeText(parent.context, "Post Deleted", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                return@setOnMenuItemClickListener true
            }
            popup.show()
        }
        return viewHolder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val currentPost = getItem(position)
        val currentUserId = HomeViewModel().currentUserId
        if (currentPost.creatorId != currentUserId) {
            holder.binding.options.visibility = View.GONE
        }
        holder.bind(currentPost)

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

interface HomeItemClickListener {
    fun onLikeClicked(postId: String)
    fun seeUserProfile(userUid: String)
    fun commentOnPost(post: Post)
    fun deletePost(postId: String)
}