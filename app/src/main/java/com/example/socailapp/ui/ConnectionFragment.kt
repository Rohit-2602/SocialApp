package com.example.socailapp.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socailapp.R
import com.example.socailapp.adapter.ConnectionAdapter
import com.example.socailapp.adapter.ConnectionInterface
import com.example.socailapp.databinding.FragmentConnectionBinding
import com.example.socailapp.viewModel.ConnectionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

class ConnectionFragment : Fragment(R.layout.fragment_connection), ConnectionInterface {

    private lateinit var binding: FragmentConnectionBinding
    private val connectionAdapter = ConnectionAdapter(this)
    private val connectionViewModel = ConnectionViewModel()

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentConnectionBinding.bind(view)
        visibleMainToolbar()
        setUpRecyclerview()

        binding.apply {
            mutualConnectionBTN.setOnClickListener {
                val action = ConnectionFragmentDirections.actionConnectionFragmentToManageConnectionFragment()
                findNavController().navigate(action)
            }
        }

        connectionViewModel.connectionRequest.observe(viewLifecycleOwner) {
            connectionAdapter.submitList(it)
            connectionAdapter.notifyDataSetChanged()
        }
    }

    override fun acceptRequest(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            connectionViewModel.acceptConnectionRequest(uid)
        }
    }

    override fun declineRequest(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            connectionViewModel.declineConnectionRequest(uid)
        }
    }

    override fun seeUserProfile(uid: String) {
        val action = ConnectionFragmentDirections.actionConnectionFragmentToUserProfileFragment(uid)
        findNavController().navigate(action)
    }

    private fun setUpRecyclerview() {
        binding.apply {
            connectionRV.apply {
                adapter = connectionAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                addItemDecoration(SimpleDividerItemDecoration(requireContext(), R.drawable.line_divider)
                )
            }
        }
    }

    private fun visibleMainToolbar() {
        val mainActivityLayout = requireActivity().findViewById<ConstraintLayout>(R.id.main_constraint_layout)

        val mainToolbar = mainActivityLayout.findViewById<Toolbar>(R.id.toolbar)
        mainToolbar.visibility = View.VISIBLE
    }

    class SimpleDividerItemDecoration(context: Context, @DrawableRes dividerRes: Int) : RecyclerView.ItemDecoration() {

        private val mDivider: Drawable = ContextCompat.getDrawable(context, dividerRes)!!

        override fun onDrawOver(c: Canvas, parent: RecyclerView) {
            val left = 30
            val right = parent.width - 30
            val childCount = parent.childCount
            for (i in 0 until childCount) {
                val child: View = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams
                val top: Int = child.bottom + params.bottomMargin
                val bottom = top + mDivider.intrinsicHeight
                mDivider.setBounds(left, top, right, bottom)
                mDivider.draw(c)
            }
        }
    }

}