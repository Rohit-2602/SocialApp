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
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socailapp.R
import com.example.socailapp.adapter.ManageConnectionAdapter
import com.example.socailapp.adapter.OnManageConnectionClick
import com.example.socailapp.databinding.FragmentManageConnectionBinding
import com.example.socailapp.viewModel.ManageConnectionViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.toolbar_manage_connection.*
import kotlinx.coroutines.*

class ManageConnectionFragment : Fragment(R.layout.fragment_manage_connection), OnManageConnectionClick {

    private lateinit var binding: FragmentManageConnectionBinding
    private var manageConnectionAdapter = ManageConnectionAdapter(this)
    private lateinit var bottomSheetBehavior : BottomSheetBehavior<NestedScrollView>
    private val manageConnectionViewModel = ManageConnectionViewModel()

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentManageConnectionBinding.bind(view)
        bottomSheetBehavior = BottomSheetBehavior.from(binding.nestedScrollView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        hideMainToolbar()
        setupRecyclerview()

        binding.apply {
            back_button.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        manageConnectionViewModel.connections.observe(viewLifecycleOwner) {
            manageConnectionAdapter.submitList(it)
            manageConnectionAdapter.notifyDataSetChanged()
        }
    }

    private fun setupRecyclerview() {
        binding.apply {
            manageConnectionRV.apply {
                adapter = manageConnectionAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                addItemDecoration(SimpleDividerItemDecoration(requireContext(), R.drawable.line_divider))
            }
        }
    }

    fun hideMainToolbar() {
        val mainActivityLayout = requireActivity().findViewById<ConstraintLayout>(R.id.main_constraint_layout)
        val mainToolbar = mainActivityLayout.findViewById<Toolbar>(R.id.toolbar)
        mainToolbar.visibility = View.GONE
    }

    override fun seeProfile(userId: String) {
        val action = ManageConnectionFragmentDirections.actionManageConnectionFragmentToUserProfileFragment(userId)
        findNavController().navigate(action)
    }

    override fun showBottomSheet(userId: String) {
        binding.blackScreen.visibility = View.VISIBLE
        binding.blackScreen.setOnClickListener {
            binding.blackScreen.visibility = View.GONE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        binding.removeConnectionBTN.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                manageConnectionViewModel.removeConnection(userId)
                withContext(Dispatchers.Main) {
                    binding.blackScreen.visibility = View.GONE
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    class SimpleDividerItemDecoration(context: Context, @DrawableRes dividerRes: Int) : RecyclerView.ItemDecoration() {
        private val mDivider: Drawable = ContextCompat.getDrawable(context, dividerRes)!!
        override fun onDrawOver(c: Canvas, parent: RecyclerView) {
            val left = 160
            val right = parent.width - parent.paddingRight
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