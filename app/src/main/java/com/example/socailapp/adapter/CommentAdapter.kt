package com.example.socailapp.adapter

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
import com.example.socailapp.data.Comment
import com.example.socailapp.databinding.ItemCommentBinding
import com.example.socailapp.viewModel.CommentViewModel
import com.example.socailapp.viewModel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentAdapter(private val listener: CommentClickListener) : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(DiffUtilCallback()) {

    class DiffUtilCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.creatorId == newItem.creatorId && oldItem.text == newItem.text
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.text == newItem.text
        }
    }

    class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            CoroutineScope(Dispatchers.IO).launch {
                val commentUser = CommentViewModel().getUserById(comment.creatorId)!!
                withContext(Dispatchers.Main) {
                    binding.apply {
                        Glide.with(binding.root).load(commentUser.imageURL).circleCrop().into(postProfileIV)
                        usernameTV.text = commentUser.name
                        descriptionTV.text = commentUser.description
                        commentBody.text = comment.text
                        timeTV.text = Utils.getTimeAgo(comment.creationTime)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = CommentViewHolder(binding)
        viewHolder.binding.options.setOnClickListener {
            val wrapper = ContextThemeWrapper(parent.context, R.style.Widget_AppCompat_PopupMenu)
            val popup = PopupMenu(wrapper, it, Gravity.END)
            popup.inflate(R.menu.post_menu)
            popup.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.deletePost -> {
                        listener.deleteComment(getItem(viewHolder.adapterPosition).id)
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

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val currentItem = getItem(position)
        val currentUserId = HomeViewModel().currentUserId
        if (currentItem.creatorId != currentUserId) {
            holder.binding.options.visibility = View.GONE
        }
        holder.bind(currentItem)
    }
}

interface CommentClickListener {
    fun deleteComment(commentId: String)
}