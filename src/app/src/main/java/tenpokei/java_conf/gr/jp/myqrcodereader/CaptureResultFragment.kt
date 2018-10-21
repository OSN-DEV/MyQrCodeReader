package tenpokei.java_conf.gr.jp.myqrcodereader

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe

/**
 * show result of qr code capture
 */
class CaptureResultFragment : Fragment() {

    private var _value: String = ""
    private lateinit var _displayValue: TextView

    //==============================================================================================
    // Fragment
    //==============================================================================================
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_capture_result, container, false)
        _displayValue = view.findViewById(R.id.display_value)
        _displayValue.text = _value
        return view
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


    //==============================================================================================
    // Public Method
    //==============================================================================================
    fun showResult(displayValue: String) {
        _value = displayValue
        _displayValue.text = _value
    }


    //==============================================================================================
    // Event Bus
    //==============================================================================================
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: BarcodeDetectEvent) {
        _displayValue.text = event.displayValue
    }


    //==============================================================================================
    // Static
    //==============================================================================================
    companion object {
        const val TAG = "CaptureResultFragment"

        fun newInstance(): Fragment {
            val fragment = CaptureResultFragment()
            return fragment
        }
    }
}
