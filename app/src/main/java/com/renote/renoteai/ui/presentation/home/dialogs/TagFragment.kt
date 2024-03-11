package com.renote.renoteai.ui.presentation.home.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.renote.renoteai.R
import com.renote.renoteai.databinding.FragmentTagBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class TagFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentTagBinding? = null
    private val binding get() = _binding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTagBinding.inflate(layoutInflater)
        return binding!!.root
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.create_button)
        drawable?.setBounds(0, 0, 50, 0) // Set the desired width and height
//        binding!!.tag_et.setCompoundDrawables(drawable, null, null, null)
        binding!!.textEditing.setCompoundDrawables(drawable, null, null, null)

        binding!!.apply {
            closeIv.setOnClickListener {
                dismiss()
            }
        }

    }



}