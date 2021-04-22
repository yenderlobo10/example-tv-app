package com.example.mytvapp.watch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentActivity
import com.example.mytvapp.R

class LoaderDialogFragment : AppCompatDialogFragment() {

    private lateinit var mActivity: FragmentActivity


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_TITLE, R.style.LoaderDialogFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(
            R.layout.loader_dialog_fragment,
            container,
            false
        )
    }


    //#region Public methods

    fun show(): LoaderDialogFragment {

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

        const val TAG = "@LoaderDialogFragment"

        fun create(
            activity: FragmentActivity,
            isCancelable: Boolean = false
        ): LoaderDialogFragment {

            return LoaderDialogFragment().also {

                it.mActivity = activity
                it.isCancelable = isCancelable
            }
        }
    }
}