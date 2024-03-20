package com.renote.renoteai.ui.fragments.folders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.renote.renoteai.R
import com.renote.renoteai.databinding.MultipleFilesFragmentDataBinding
import com.renote.renoteai.ui.main.MainActivity
import com.renote.renoteai.ui.presentation.home.adapters.DocumentsDetailsAdapter
import com.renote.renoteai.ui.fragments.folders.viewmodel.MultipleFilesViewModel
import org.koin.android.ext.android.inject


class MultipleFilesFragment : Fragment() {

    private var binding: MultipleFilesFragmentDataBinding? = null
    private val viewModel: MultipleFilesViewModel by inject()
    private var mContext: Context? = null
    private var folderId: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       mContext = activity
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_multiple_files, container, false)
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
        folderId = sharedPreference.getString("folderId", "100")
        val folderName =sharedPreference.getString("folderName", "ReNoteAI")
        println("folderId:$folderId")
        println("folderName:$folderName")

        val tvFolderName = view.findViewById<TextView>(R.id.tvFolderName) // Use 'requireView().findViewById' if you're in a Fragment
        tvFolderName.setText(folderName)

        binding!!.tvBreadcrumbs.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }
//        initMultipleFilesRecyclerview()
//        folderDocumentsObserveData()



    }

//    private fun showEmpty(isEmpty: Boolean) {
//        if (isEmpty) {
//            binding?.multipleFilesRecyclerview?.visibility = View.GONE
//            binding?.emptyIV?.visibility = View.VISIBLE
//            binding?.emptyTv?.visibility = View.VISIBLE
//            binding?.empty2Tv?.visibility = View.VISIBLE
//        } else {
//            binding?.multipleFilesRecyclerview?.visibility = View.VISIBLE
//            binding?.emptyIV?.visibility = View.GONE
//            binding?.emptyTv?.visibility = View.GONE
//            binding?.empty2Tv?.visibility = View.GONE
//        }
//    }

//    private fun folderDocumentsObserveData() {
//        viewModel.getFolderDocumentsDetails(folderId.toString())
//        viewModel.folderDocumentsDetailsList.observe(viewLifecycleOwner) { documents ->
//            if (documents.isNotEmpty()) {
//                Emptyshow(false)
//                viewModel.documentsDetailsAdapter?.submitList(documents)
//                viewModel.documentsDetailsAdapter?.notifyDataSetChanged()
//            } else {
//                Emptyshow(true)
//            }
//        }
//    }

//    private fun Emptyshow(isEmpty: Boolean) {
//        if (isEmpty) {
//            binding?.multipleFilesRecyclerview?.visibility = View.GONE
//            binding?.emptyIV?.visibility = View.VISIBLE
//            binding?.emptyTv?.visibility = View.VISIBLE
//            binding?.empty2Tv?.visibility = View.VISIBLE
//        } else {
//            binding?.multipleFilesRecyclerview?.visibility = View.VISIBLE
//            binding?.emptyIV?.visibility = View.GONE
//            binding?.emptyTv?.visibility = View.GONE
//            binding?.empty2Tv?.visibility = View.GONE
//        }
//    }


//    private fun initMultipleFilesRecyclerview() {
//        viewModel.documentsDetailsAdapter = DocumentsDetailsAdapter(requireContext())
//        val layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
//        binding?.multipleFilesRecyclerview?.layoutManager = layoutManager
//        binding?.multipleFilesRecyclerview?.isNestedScrollingEnabled = false
//        binding?.multipleFilesRecyclerview?.adapter = viewModel.documentsDetailsAdapter
//    }

}