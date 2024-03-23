package com.renote.renoteai.ui.fragments.folders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.renote.renoteai.R
import com.renote.renoteai.databinding.MultipleFilesFragmentDataBinding
import com.renote.renoteai.ui.fragments.files.adapters.FileDetailsAdapter
import com.renote.renoteai.ui.fragments.folders.viewmodel.MultipleFilesViewModel
import com.renote.renoteai.ui.main.MainActivity
import com.renote.renoteai.ui.presentation.home.adapters.DocumentsDetailsAdapter
import com.renote.renoteai.ui.presentation.home.viewmodel.HomeFragmentViewModel
import org.koin.android.ext.android.inject


class MultipleFilesFragment : Fragment() {

    private var binding: MultipleFilesFragmentDataBinding? = null
    private var mContext: Context? = null
    private var documentId: String? = null
    private val viewModel: MultipleFilesViewModel by inject()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContext = activity
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_multiple_files, container, false)
        binding?.lifecycleOwner = this
        binding?.viewModel = viewModel
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.img_search)
        drawable?.setBounds(0, 0, 40, 40) // Set the desired width and height
        binding?.etSearch?.setCompoundDrawables(drawable, null, null, null)

        val sharedPreference = requireContext().getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        documentId = sharedPreference.getString("documentId", null)
        val documentName =sharedPreference.getString("documentName", null)
        println("folderId:$documentId")
        println("folderName:$documentName")

        initDocumentFilesRecyclerView()
        documentFilesObserveData()

        handleBackPress()

        binding!!.tvFolderName.text = documentName

        binding!!.tvBreadcrumbs.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            val sharedPreferences = requireContext().getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString("documentId", null)
                putString("documentName", null)
                apply() // apply() is asynchronous, use commit() if you need synchronous operation
            }
            startActivity(intent)
        }
    }

    private fun documentFilesObserveData() {
        viewModel.getDocumentFilesDetails(documentId.toString())
        viewModel.documentFilesDetailsList.observe(viewLifecycleOwner){files->
            if (files.isNotEmpty()){
                showEmpty(false)
                viewModel.filesDetailsAdapter?.submitList(files)
                viewModel.filesDetailsAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun showEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding?.multipleFilesRecyclerview?.visibility = View.GONE
            binding?.emptyIV?.visibility = View.VISIBLE
            binding?.emptyTv?.visibility = View.VISIBLE
            binding?.empty2Tv?.visibility = View.VISIBLE
        } else {
            binding?.multipleFilesRecyclerview?.visibility = View.VISIBLE
            binding?.emptyIV?.visibility = View.GONE
            binding?.emptyTv?.visibility = View.GONE
            binding?.empty2Tv?.visibility = View.GONE
        }
    }

    private fun initDocumentFilesRecyclerView() {
        viewModel.filesDetailsAdapter = FileDetailsAdapter(requireContext())
        val layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        binding?.multipleFilesRecyclerview?.layoutManager = layoutManager
        binding?.multipleFilesRecyclerview?.isNestedScrollingEnabled = false
        binding?.multipleFilesRecyclerview?.adapter = viewModel.filesDetailsAdapter
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Update shared preferences before navigating back or to any specific logic
                val sharedPreference = requireContext().getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
                with(sharedPreference.edit()) {
                    putString("documentId", null)
                    putString("documentName", null)
                    apply() // apply() is asynchronous, use commit() if you need synchronous operation
                }

                // Option 1: Navigate back by popping the back stack
                isEnabled = false
                requireActivity().onBackPressed()
            }
        })
    }

}