package com.example.mytvapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LoaderBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var mActivity: FragmentActivity


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            R.layout.torrent_select_load_action_item,
            container,
            false
        )

        return view
    }

    override fun getTheme(): Int = R.style.LoaderBottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.viewTreeObserver.addOnGlobalLayoutListener {

            view.viewTreeObserver.removeOnGlobalLayoutListener { }

            val bottomSheetDialog = dialog?.window?.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )

            bottomSheetDialog?.let {

                bottomSheetDialog.setBackgroundColor(Color.TRANSPARENT)

                val behavior = BottomSheetBehavior.from(bottomSheetDialog)

                with(behavior) {

                    //isDraggable = false
                    isHideable = false
                    //isCancelable = false
                    state = BottomSheetBehavior.STATE_HALF_EXPANDED
                }
            }
        }
    }


    //#region Public methods

    fun show(): LoaderBottomSheetDialog {

        try {

            show(mActivity.supportFragmentManager, TAG)

        } catch (ex: Exception) {

            println(":: ERROR ::")
            ex.printStackTrace()
        }

        return this
    }

    //#endregion


    companion object {

        const val TAG = "@LoaderBottomSheetDialog"

        fun create(activity: FragmentActivity): LoaderBottomSheetDialog {

            return LoaderBottomSheetDialog().apply {
                mActivity = activity
            }
        }
    }
}