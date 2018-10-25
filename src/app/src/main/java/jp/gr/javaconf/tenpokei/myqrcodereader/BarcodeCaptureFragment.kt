package jp.gr.javaconf.tenpokei.myqrcodereader

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import jp.gr.javaconf.tenpokei.myqrcodereader.event.BarcodeDetectEvent
import org.greenrobot.eventbus.EventBus
import java.io.IOException

/**
 * capture barcode
 */
class BarcodeCaptureFragment : Fragment() {
    // Barcode reader sample(GitHub)
    // https://github.com/googlesamples/android-vision/tree/master/visionSamples/barcode-reader

    // Mobile Vision Barcode APIを用いたバーコード機能の実装方法
    // http://rozkey.hatenablog.com/entry/2018/03/26/225014

    //==============================================================================================
    // Declaration
    //==============================================================================================
    private var _cameraSource: CameraSource? = null
    private lateinit var _preview: SurfaceView
    private var _detected: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val parent = inflater.inflate(R.layout.fragment_barcode_capture, container, false)
        _preview= parent.findViewById(R.id.preview)
        return parent
    }


    //==============================================================================================
    // Fragment
    //==============================================================================================
    override fun onResume() {
        super.onResume()
        if (null == _cameraSource) {
            this.setupCameraSource()
        }
        this.startCameraSource()
    }


    //==============================================================================================
    // Private method
    //==============================================================================================
    /*
     * set up camera source
     */
    private fun setupCameraSource() {
        val barcodeDetector = BarcodeDetector.Builder(activity?.applicationContext)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build()
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode>  {
            override fun release() {
            }
            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                if (detections.detectedItems.size() != 0)
                    if (!_detected) {
                        EventBus.getDefault().post(BarcodeDetectEvent(detections.detectedItems.valueAt(0).rawValue))
                        _detected = true
                    }
            }
        })
        _cameraSource = CameraSource.Builder(activity?.applicationContext, barcodeDetector).apply {
            setFacing(CameraSource.CAMERA_FACING_BACK)
            setRequestedPreviewSize(1600, 1024)
            setRequestedFps(15.0f)
            setAutoFocusEnabled(true)
        }.build()
    }

    /*
     * start tracking
     */
    @Throws(SecurityException::class)
    private fun startCameraSource() {
        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity?.applicationContext)
        if (code != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(activity, code, 9999).show()
            return
        }

        _preview.holder.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                try {
                    // app check permission when app launch, but need to check agaimn because of compile error
                    if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) }
                            == PackageManager.PERMISSION_GRANTED) {
                        _cameraSource?.start(holder)
                    } else {
                        activity?.runOnUiThread {
                            Toast.makeText(activity, R.string.error_message_permission_denied, Toast.LENGTH_SHORT).show()
                            activity?.finish()
                        }
                    }
                } catch (e: IOException) {
                    _cameraSource?.release()
                    _cameraSource = null
                }
            }
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            }
            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                if (_cameraSource != null) {
                    _cameraSource?.release()
                    _cameraSource = null
                }
            }
        })
    }


    //==============================================================================================
    // Static
    //==============================================================================================
    companion object {
        /*
         * create a fragment instance.
         */
        fun newInstance(): BarcodeCaptureFragment {
            return BarcodeCaptureFragment()
        }
    }
}
