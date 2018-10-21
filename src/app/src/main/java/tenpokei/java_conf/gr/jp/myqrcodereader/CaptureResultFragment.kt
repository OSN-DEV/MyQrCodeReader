package tenpokei.java_conf.gr.jp.myqrcodereader

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * show result of qr code capture
 */
class CaptureResultFragment : Fragment() {

    private var _displayValue: String = ""

    //==============================================================================================
    // Fragment
    //==============================================================================================
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_capture_result, container, false)
        if (_displayValue.isNotEmpty()) {
            this.setResult(view)
        }
        return view
    }


    //==============================================================================================
    // Public Method
    //==============================================================================================
    fun showResult(displayValue: String) {
        _displayValue = displayValue
        if (null != view) {
            setResult(view!!)
        }
    }


    //==============================================================================================
    // Private method
    //==============================================================================================
    fun setResult(view: View) {
        view.findViewById<TextView>(R.id.display_value).setText(_displayValue)
    }



    //==============================================================================================
    // Static
    //==============================================================================================
    companion object {
        public val TAG = "CaptureResultFragment"

        fun newInstance(): Fragment {
            val fragment = CaptureResultFragment()
            return fragment
        }
    }
}
