package com.example.notesapp.util

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.notesapp.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom.*

class BottomSheet : BottomSheetDialogFragment() {
    var selectedColor = "#202020"


    companion object {
        var noteId = -1L
        fun newInstance(id: Long): BottomSheet {
            val args = Bundle()
            val fragment = BottomSheet()
            fragment.arguments = args
            noteId = id
            return fragment
        }
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val view = LayoutInflater.from(context).inflate(R.layout.bottom, null)
        dialog.setContentView(view)
        val param = (view.parent as View).layoutParams as CoordinatorLayout.LayoutParams

        val behavior = param.behavior
        if (behavior is BottomSheetBehavior<*>) {
            behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    TODO("Not yet implemented")
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    var state = ""
                    when (newState) {
                        BottomSheetBehavior.STATE_DRAGGING -> {
                            state = "DRAGGING"
                        }
                        BottomSheetBehavior.STATE_SETTLING -> {
                            state = "SETTLING"
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            state = "EXPANDED"
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            state = "COLLAPSED"
                        }

                        BottomSheetBehavior.STATE_HIDDEN -> {
                            state = "HIDDEN"
                            dismiss()
                            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        }

                    }
                }

            })


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListener()
    }

    private fun setListener() {
        fNote1.setOnClickListener {
            resetImageResources()
            imgNote1.setImageResource(R.drawable.ic_done)
            selectedColor = "#4e33ff"
            broadcastChangeColor(selectedColor)
        }

        fNote2.setOnClickListener {
            resetImageResources()
            imgNote2.setImageResource(R.drawable.ic_done)
            selectedColor = "#ffd633"
            broadcastChangeColor(selectedColor)
        }

        fNote3.setOnClickListener {
            resetImageResources()
            imgNote3.setImageResource(R.drawable.ic_done)
            selectedColor = "#5C4033"
            broadcastChangeColor(selectedColor)
        }

        fNote4.setOnClickListener {
            resetImageResources()
            imgNote4.setImageResource(R.drawable.ic_done)
            selectedColor = "#0aebaf"
            broadcastChangeColor(selectedColor)
        }

        fNote5.setOnClickListener {
            resetImageResources()
            imgNote5.setImageResource(R.drawable.ic_done)
            selectedColor = "#ae3b76"
            broadcastChangeColor(selectedColor)
        }

        fNote6.setOnClickListener {
            resetImageResources()
            imgNote6.setImageResource(R.drawable.ic_done)
            selectedColor = "#ff7746"
            broadcastChangeColor(selectedColor)
        }

        fNote7.setOnClickListener {
            resetImageResources()
            imgNote7.setImageResource(R.drawable.ic_done)
            selectedColor = "#FF0000"
            broadcastChangeColor(selectedColor)
        }
    }

    private fun resetImageResources() {
        imgNote1.setImageResource(0)
        imgNote2.setImageResource(0)
        imgNote3.setImageResource(0)
        imgNote4.setImageResource(0)
        imgNote5.setImageResource(0)
        imgNote6.setImageResource(0)
        imgNote7.setImageResource(0)
    }

    private fun broadcastChangeColor(selectedColor: String) {
        val intent = Intent("bottom_sheet_action")
        intent.putExtra("action", "ChangeColor")
        intent.putExtra("selectedColor", selectedColor)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }
}
