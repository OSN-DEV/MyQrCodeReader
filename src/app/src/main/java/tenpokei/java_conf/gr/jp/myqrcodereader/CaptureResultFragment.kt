package tenpokei.java_conf.gr.jp.myqrcodereader

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.github.kittinunf.fuel.httpGet
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe
import com.github.kittinunf.result.Result
import tenpokei.java_conf.gr.jp.myqrcodereader.data.AppDatabase
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.Button
import tenpokei.java_conf.gr.jp.myqrcodereader.event.BarcodeDetectEvent
import tenpokei.java_conf.gr.jp.myqrcodereader.event.ScanBarcodeEvent
import java.util.regex.Pattern


/**
 * show result of qr code capture
 */
class CaptureResultFragment : Fragment() {
    // KotlinでHTTP通信（FuelとHttpURLConnection）
    // https://qiita.com/naoi/items/8df1409ad48ad8f3c632

    // AndroidでのBitmap/JPEG/byte配列の相互変換
    // https://qiita.com/aymikmts/items/7139fa6c4da3b57cb4fc

    // How to convert image to byte array and byte array to image in Java
    // http://mrbool.com/how-to-convert-image-to-byte-array-and-byte-array-to-image-in-java/25136

    private val FaviconUrl = "http://www.google.com/s2/favicons?domain="
    private lateinit var _database: AppDatabase
    private var _id: Long = -1
    private val LogTag = "MyQuCodeReader"

    private lateinit var _displayValue: TextView
    private lateinit var _siteIcon: ImageView
    private lateinit var _siteName: TextView

    //==============================================================================================
    // Fragment
    //==============================================================================================
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_capture_result, container, false)

        _displayValue = view.findViewById(R.id.display_value)
        _siteName = view.findViewById(R.id.site_name)
        _siteIcon = view.findViewById(R.id.site_icon)

        view.findViewById<Button>(R.id.scan_barcode).setOnClickListener { EventBus.getDefault().post(ScanBarcodeEvent()) }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity != null) {
            _database = AppDatabase(activity!!.applicationContext)
        }
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
    // Event Bus
    //==============================================================================================
    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: BarcodeDetectEvent) {
        showResult(event.displayValue)
    }


    //==============================================================================================
    // Private Method
    //==============================================================================================
    /**
     * show detected result
     */
    private fun showResult(displayValue: String) {
        _displayValue.text = displayValue
        _id = _database.createHistory(displayValue)

        // get favicon and site namte
        try {
            if (displayValue.startsWith("http://") || displayValue.startsWith("https://")) {
                var uri = Uri.parse(displayValue)

                // site name
                displayValue.httpGet().response { request, response, result ->
                    when (result) {
                        is Result.Success -> {
                            var body = String(response.data)
                            val pattern = Pattern.compile("<title>(?<value>.*)</title>")
                            val matcher = pattern.matcher(body)
                            if (matcher.matches()) {
//                                _siteName.text = matcher.group("value").toString()
                                _siteName.text = matcher.group(0).toString()
                            }

                        }
                        is Result.Failure -> {
                            Log.d(LogTag, "Fail to get favicon")
                        }
                    }
                }

                // favicon
                (FaviconUrl + uri.authority).httpGet().response { request, response, result ->
                    when (result) {
                        is Result.Success -> {
                            _database.updateSiteIcon(_id, response.data)
                            val bmp = BitmapFactory.decodeByteArray(response.data, 0, response.data.size)
                            Handler(Looper.getMainLooper()).post(Runnable {
                                _siteIcon.setImageBitmap(BitmapFactory.decodeByteArray(response.data, 0, response.data.size))
                            })
                        }
                        is Result.Failure -> {
                            Log.d(LogTag, "Fail to get favicon")
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e(LogTag, ex.message, ex)
        }
    }


    //==============================================================================================
    // Static
    //==============================================================================================
    companion object {
        const val TAG = "CaptureResultFragment"

        fun newInstance(): Fragment {
            return CaptureResultFragment()
        }
    }
}
