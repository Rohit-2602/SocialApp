package com.example.socailapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socailapp.FirebaseService
import com.example.socailapp.R
import com.example.socailapp.data.Post
import com.example.socailapp.databinding.ItemPostBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivityAdapter(private val listener: OnClickListener) :
    ListAdapter<Post, ProfileActivityAdapter.ProfileActivityViewHolder>(DiffUtilCallback()) {

    class ProfileActivityViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                CoroutineScope(Dispatchers.IO).launch {
                    val postCreator = FirebaseService().getUserById(post.creatorId)!!
                    withContext(Dispatchers.Main) {
                        postBodyTV.text = post.text
                        Glide.with(binding.root).load(post.image).into(postIV)
                        Glide.with(binding.root).load(postCreator.imageURL).circleCrop()
                            .into(postProfileIV)
                        usernameTV.text = postCreator.name
                        descriptionTV.text = postCreator.description
                        timeTV.text = post.createdAt
                    }
                }
            }
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.text == newItem.text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileActivityViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ProfileActivityViewHolder(binding)

        viewHolder.binding.likeButton.setOnClickListener {
            listener.onLikeClicked(getItem(viewHolder.adapterPosition).id)
        }
        return viewHolder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ProfileActivityViewHolder, position: Int) {
        val currentPost = getItem(position)
        holder.bind(currentPost)

        val currentUserId = FirebaseService().currentUser!!.uid
        val isLiked = currentPost.likedBy.contains(currentUserId)
        val likeCount = currentPost.likedBy.size
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
    }
}

//interface OnClickListener {
//    fun onLikeClicked(postId: String)
//}