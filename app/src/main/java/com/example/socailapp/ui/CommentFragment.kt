package com.example.socailapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.socailapp.R
import com.example.socailapp.Utils
import com.example.socailapp.adapter.CommentAdapter
import com.example.socailapp.adapter.CommentClickListener
import com.example.socailapp.data.Comment
import com.example.socailapp.data.Post
import com.example.socailapp.databinding.FragmentCommentBinding
import com.example.socailapp.viewModel.CommentViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.item_post.*
import kotlinx.android.synthetic.main.toolbar_comment.*
import kotlinx.coroutines.*

class CommentFragment: Fragment(R.layout.fragment_comment), CommentClickListener {

    private val navArgs: CommentFragmentArgs by navArgs()
    private lateinit var binding: FragmentCommentBinding
    private var commentAdapter = CommentAdapter(this)
    private val commentViewModel = CommentViewModel()
    private var likeCount: Int = 0
    private var commentCount: Int = 0
    private lateinit var post: Post
    private val currentUserId = commentViewModel.currentUserId

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCommentBinding.bind(view)
        hideMainToolbar()

        post = navArgs.Post
        likeCount = post.likedBy.size
        commentCount = post.commentedBy.size
        updateLikeTV()

        commentViewModel.getPostComments(post.id).observe(viewLifecycleOwner) {
            commentAdapter.submitList(it)
            commentCount = it.size
            updateCommentTV()
            commentAdapter.notifyDataSetChanged()
        }

        binding.apply {
            commentRV.apply {
                adapter = commentAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }

        binding.apply {
            options.visibility = View.GONE
            CoroutineScope(Dispatchers.IO).launch {
                val postCreator = commentViewModel.getUserById(post.creatorId)!!
                withContext(Dispatchers.Main) {
                    Glide.with(requireContext()).load(postCreator.imageURL).circleCrop().into(postProfileIV)
                    usernameTV.text = postCreator.name
                    descriptionTV.text = postCreator.description
                    timeTV.text = Utils.getTimeAgo(post.createdAt)
                    postBodyTV.text = post.text
                    if (post.likedBy.contains(currentUserId)) {
                        likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_liked, 0, 0)
                    }
                    else {
                        likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_not_liked, 0, 0)
                    }

                    backBTN.setOnClickListener {
                        findNavController().navigateUp()
                    }
                    postBTN.setOnClickListener {
                        if (commentET.text.toString().trim() == "") {
                            Snackbar.make(commentET, "Comment Can't Be Empty", Snackbar.LENGTH_SHORT).setAnchorView(commentET).show()
                            return@setOnClickListener
                        }
                        val comment = Comment(creatorId = currentUserId, text = commentET.text.toString().trim(), creationTime = System.currentTimeMillis())
                        commentViewModel.addComment(comment, post.id, post.creatorId)
                        commentET.setText("")
                    }

                    likeButton.setOnClickListener {
                        commentViewModel.updateLikes(post.id)
                        updateLikes()
                        updateLikeTV()
                    }
                }
            }
        }
    }

    override fun deleteComment(commentId: String) {
        commentViewModel.deleteComment(commentId, post.id)
    }

    private fun updateLikes() {
        if (post.likedBy.contains(currentUserId)) {
            post.likedBy.remove(currentUserId)
            likeCount = post.likedBy.size
            likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_not_liked, 0, 0)
        }
        else {
            post.likedBy.add(currentUserId)
            likeCount = post.likedBy.size
            likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_liked, 0, 0)
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateLikeTV() {
        when (likeCount) {
            0 -> likeCountTV.text = ""
            1 -> likeCountTV.text = "$likeCount Like"
            else -> likeCountTV.text = "$likeCount Likes"
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateCommentTV() {
        when (commentCount) {
            0 -> commentCountTV.text = ""
            1 -> commentCountTV.text = "$commentCount comment"
            else -> commentCountTV.text = "$commentCount comments"
        }
    }

    private fun hideMainToolbar() {
        val mainActivityLayout =
            requireActivity().findViewById<ConstraintLayout>(R.id.main_constraint_layout)

        val mainToolbar = mainActivityLayout.findViewById<Toolbar>(R.id.toolbar)
        mainToolbar.visibility = View.GONE
    }

}