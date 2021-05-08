package com.example.socailapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socailapp.FirebaseService
import com.example.socailapp.R
import com.example.socailapp.data.Post
import com.example.socailapp.databinding.ItemPostBinding
import com.example.socailapp.viewModel.UserViewModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeAdapter(options: FirestoreRecyclerOptions<Post>, private val listener: OnClickListener) :
    FirestoreRecyclerAdapter<Post, HomeAdapter.HomeViewHolder>(options) {

    class HomeViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                CoroutineScope(Dispatchers.IO).launch {
                    val postCreator = UserViewModel().getUserById(post.creatorId)!!
                    withContext(Dispatchers.Main) {
                        postBodyTV.text = post.text
                        Glide.with(binding.root).load(post.image).into(postIV)
                        Glide.with(binding.root).load(postCreator.imageURL).circleCrop().into(postProfileIV)
                        usernameTV.text = postCreator.name
                        descriptionTV.text = postCreator.description
                        timeTV.text = post.createdAt
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = HomeViewHolder(binding)
        viewHolder.binding.likeButton.setOnClickListener {
            listener.onLikeClicked(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }
        viewHolder.binding.constraint.setOnClickListener {
            listener.seeUserProfile(getItem(viewHolder.adapterPosition).creatorId)
        }
        return viewHolder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int, model: Post) {
        holder.bind(model)

        val currentUserId = FirebaseService().currentUser!!.uid
        val isLiked = model.likedBy.contains(currentUserId)
        val likeCount = model.likedBy.size
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