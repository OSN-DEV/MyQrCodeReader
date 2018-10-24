package jp.gr.javaconf.tenpokei.myqrcodereader

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import jp.gr.javaconf.tenpokei.myqrcodereader.data.AppDatabase
import jp.gr.javaconf.tenpokei.myqrcodereader.event.RefreshScanResultEvent
import jp.gr.javaconf.tenpokei.myqrcodereader.event.ScanBarcodeEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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

    private lateinit var _database: AppDatabase
    private var _id: Long = -1

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
        view.findViewById<TextView>(R.id.scan_barcode).setOnClickListener { EventBus.getDefault().post(ScanBarcodeEvent()) }
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
    @Suppress("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: RefreshScanResultEvent) {
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
        view?.findViewById<TextView>(R.id.launch)?.visibility = View.VISIBLE

        // get favicon and site namte
        try {
            if (displayValue.startsWith("http://") || displayValue.startsWith("https://")) {
                view?.findViewById<LinearLayout>(R.id.web_page_container)?.visibility = View.VISIBLE
                val uri = Uri.parse(displayValue)

                // site name
                displayValue.httpGet().response { _, response, result ->
                    when (result) {
                        is Result.Success -> {
                            val pattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE)
                            val matcher = pattern.matcher(String(response.data))
                            if (matcher.find()) {
                                Handler(Looper.getMainLooper()).post({
                                    // _siteName.text = matcher.group("value").toString() // this method requires minimum SDK API 26
                                    val siteName = matcher.group(1).toString()
                                    _database.updateSiteName(_id, siteName)
                                    _siteName.text = siteName
                                })
                            }
                        }
                        is Result.Failure -> {
                            Log.d(TAG, "Fail to get favicon")
                        }
                    }
                }

                // favicon
                (FAVICON_URL + uri.authority).httpGet().response { _, response, result ->
                    when (result) {
                        is Result.Success -> {
                            _database.updateSiteIcon(_id, response.data)
                            Handler(Looper.getMainLooper()).post({
                                _database.updateSiteIcon(_id, response.data)
                                _siteIcon.setImageBitmap(BitmapFactory.decodeByteArray(response.data, 0, response.data.size))
                            })
                        }
                        is Result.Failure -> {
                            Log.d(TAG, "Fail to get favicon")
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.message, ex)
        }
    }


    //==============================================================================================
    // Static
    //==============================================================================================
    companion object {
        const val TAG = "CaptureResultFragment"
        const val FAVICON_URL = "http://www.google.com/s2/favicons?domain="

        fun newInstance(): Fragment {
            return CaptureResultFragment()
        }
    }
}
