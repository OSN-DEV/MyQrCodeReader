package tenpokei.java_conf.gr.jp.myqrcodereader

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.FragmentManager
import android.content.Context
import android.os.Bundle

/**
 * common dialog fragment
 */
@Suppress("unused")
class CommonDialogFragment : DialogFragment() {

    // 呼び出し
    // CommonDialogFragment dialog = CommonDialogFragment.newInstance(xxx,xxx,xxx);
    // dialog.show(fragmentManager, "xxx");

    //==============================================================================================
    // Declaration
    //==============================================================================================
    enum class DialogType(val rawValue: Int) {
        Error(1),       // OK only
        Confirm(2);     // Yes, No

        companion object {
            fun fromInt(intValue: Int): DialogType {
                return values().first { it.rawValue == intValue }
            }
        }
    }

    private var _dialogId: Int = 0
    private var _title: Int = 0
    private var _message: Int = 0
    private lateinit var _dialogType: DialogType

    interface OnCommonDialogFragmentListener {
        fun onOkButtonClick(dialogId: Int)
        fun onYesButtonClick(dialogId: Int)
        fun onNoButtonClick(dialogId: Int)
    }

    private lateinit var _listener: OnCommonDialogFragmentListener


    //==============================================================================================
    // Fragment
    //==============================================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            _dialogId = arguments.getInt(ARG_ID)
            _title = arguments.getInt(ARG_TITLE)
            _message = arguments.getInt(ARG_MESSAGE)
            _dialogType = DialogType.fromInt(arguments.getInt(ARG_DIALOG_TYPE))
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnCommonDialogFragmentListener) {
            _listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnCommonDialogFragmentListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity).apply {
            setMessage(_message)
            when (_dialogType) {
                DialogType.Error -> {
                    setPositiveButton(R.string.dialog_button_ok, { _, _ ->
                        setTitle(R.string.dialog_title_error)
                        _listener.onOkButtonClick(_dialogId)
                    })
                }
                DialogType.Confirm -> {
                    setTitle(R.string.dialog_title_error)
                    setPositiveButton(R.string.dialog_button_yes, { _, _ ->
                        _listener.onYesButtonClick(_dialogId)
                    })
                    setNegativeButton(R.string.dialog_button_no, { _, _ ->
                        _listener.onNoButtonClick(_dialogId)
                    })

                    // この書き方だと冗長だと言われる
//                    setNegativeButton(R.string.dialog_button_no,
//                            DialogInterface.OnClickListener { _, _ ->
//                                _listener.onNoButtonClick()
//                            })
                }
            }
        }
        return builder.create()
    }


    //==============================================================================================
    // Private Method
    //==============================================================================================
    companion object {
        private val ARG_ID = "id"
        private val ARG_TITLE = "title"
        private val ARG_MESSAGE = "message"
        private val ARG_DIALOG_TYPE = "dialog_type"

        /*
         * show dialog
         */
        fun show(manager: FragmentManager, dialogId: Int, message: Int, dialogType: DialogType, tag: String = "") {
            newInstance(dialogId, message, dialogType).show(manager, tag)
        }

        /*
         * create instance
         */
        @JvmStatic
        private fun newInstance(dialogId: Int, message: Int, dialogType: DialogType): CommonDialogFragment {
            val args = Bundle()
            args.putInt(ARG_ID, dialogId)
            args.putInt(ARG_MESSAGE, message)
            args.putInt(ARG_DIALOG_TYPE, dialogType.rawValue)
            val fragment = CommonDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}