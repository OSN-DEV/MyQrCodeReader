package tenpokei.java_conf.gr.jp.myqrcodereader

import android.app.Fragment
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import tenpokei.java_conf.gr.jp.myqrcodereader.barcode.*
import java.io.IOException

/**
 * capture barcode
 */
class BarcodeCaptureFragment : Fragment(), BarcodeGraphicTracker.BarcodeUpdateListener {

    // Barcode reader sample(Github)
    // https://github.com/googlesamples/android-vision/tree/master/visionSamples/barcode-reader

    // アイコンのサイズ
    // https://backport.net/blog/2018/02/17/adaptive_icon/

    // Adaptive Iconの作成
    // https://akira-watson.com/android/adaptive-icons.html

    // 少し親切なランタイムパーミッション対応
    // https://qiita.com/caad1229/items/35bab757217b204711df

    // AndroidでQRコードをリーダーを作る（超シンプル版）
    // https://dev.eyewhale.com/archives/1372

    //==============================================================================================
    // Declaration
    //==============================================================================================
    private var _cameraSource: CameraSource? = null
    private lateinit var _preview: CameraSourcePreview
    private lateinit var _overlay: GraphicOverlay<BarcodeGraphic>
    private var _detected: Boolean = false


    interface OnBarcodeDetectedListener {
        fun onBarcodeDetected(value: String?)
    }

    private var _barcodeDetectedListener: OnBarcodeDetectedListener? = null

    //==============================================================================================
    // Fragment
    //==============================================================================================
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val parent = inflater!!.inflate(R.layout.fragment_barcode_capture, container, false)

        // set up preview
        _preview = parent.findViewById(R.id.preview)
        _overlay = parent.findViewById(R.id.graphic_overlay)
        this.setupCameraSource()

        // Inflate the layout for this fragment
        return parent
    }

    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onPause() {
        super.onPause()
        _preview.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _preview.release()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnBarcodeDetectedListener) {
            this._barcodeDetectedListener = context
        } else {
//            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        this._barcodeDetectedListener = null
    }

    //==============================================================================================
    // BarcodeGraphicTracker.BarcodeUpdateListener
    //==============================================================================================
    override fun onBarcodeDetected(barcode: Barcode?) {
        // この時点で _preview を破棄(release or stop)すると次回の起動時にstartCameraSourceが応答なしになるので
        // 複数回イベントを送信しないよう、フラグで管理する
        if (!_detected) {
            activity.runOnUiThread({
                _barcodeDetectedListener?.onBarcodeDetected(barcode?.displayValue)
            })
        }
        _detected = true
    }


    //==============================================================================================
    // Private method
    //==============================================================================================
    /*
     * set up camera
     */
    private fun setupCameraSource() {
        // BarcodeDetector : Recognizes barcodes (in a variety of 1D and 2D formats) in a supplied Frame
        val barcodeDetector = BarcodeDetector.Builder(activity.applicationContext).build()
        val barcodeFactory = BarcodeTrackerFactory(_overlay, this)
        barcodeDetector.setProcessor(MultiProcessor.Builder(barcodeFactory).build())

//        if (!barcodeDetector.isOperational()) {
//            // 初めてバーコードやface APIなどを利用する場合、GMSは必要なライブラリをダウンロードするらしい。
//            // 通常はアプリ起動時にダウンロードは完了しているはずらしい。もし完了してなければバーコード等の読取りが出来ない。
//            // ※ネットで調べてもOfflineで動く・動かないの意見が別れているっぽい。おそらく端末内にあるかどうか、という話なんだろうなと
//            // → Google Play開発者サービスが最新であれば、おそらく問題なく動く気がする。
//
//            // 以下は端末の空き容量の判定処理。このif文に入るケースは気にしなくても良い気がするのでエラーハンドリングはしない
//            if (cacheDir.usableSpace * 100 / cacheDir.totalSpace <= 10) { // Alternatively, use cacheDir.freeSpace
//                // Handle storage low state
//            } else {
//                // Handle storage ok state
//            }
//        }

        val builder = CameraSource.Builder(activity.applicationContext, barcodeDetector).apply {
            setFacing(CameraSource.CAMERA_FACING_BACK)
            setRequestedPreviewSize(1600, 1024)
            setRequestedFps(15.0f)
            if (Build.VERSION_CODES.ICE_CREAM_SANDWICH <= Build.VERSION.SDK_INT) {
                setFocusMode(CameraSource.FOCUS_MODE_AUTO)
            }
        }
        _cameraSource = builder.build()
    }


    /*
     * start tracking
     */
    @Throws(SecurityException::class)
    private fun startCameraSource() {
        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity.applicationContext)
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(activity, code, 9999)
            dlg.show()
        }

        if (_cameraSource != null) {
            try {
                _preview.start(_cameraSource, _overlay)
            } catch (e: IOException) {
                _cameraSource?.release()
                _cameraSource = null
            }
        }
    }


    companion object {
        /*
         * create a fragment instance.
         */
        fun newInstance(): BarcodeCaptureFragment {
            return BarcodeCaptureFragment()
        }
    }
}
