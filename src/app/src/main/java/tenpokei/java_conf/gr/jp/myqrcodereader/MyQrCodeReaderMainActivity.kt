package tenpokei.java_conf.gr.jp.myqrcodereader

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import tenpokei.java_conf.gr.jp.myqrcodereader.barcode.*
import java.io.IOException


class MyQrCodeReaderMainActivity : Activity(), CommonDialogFragment.OnCommonDialogFragmentListener, BarcodeGraphicTracker.BarcodeUpdateListener {

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

    private lateinit var _drawerLayout: DrawerLayout
    private lateinit var _drawerToggle: ActionBarDrawerToggle
    private var _cameraSource: CameraSource? = null
    private lateinit var _preview: CameraSourcePreview
    private lateinit var _overlay: GraphicOverlay<BarcodeGraphic>

    private val _permissionRequestCamera = 1

    private enum class DialogId(val rawValue: Int) {
        UnavailableCamera(1),
        PermissionDenied(2)
    }

    //==============================================================================================
    // Activity
    //==============================================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_qr_code_reader_main)

        // setup side menu
        val sideMenu = SideMenuFragment.newInstance()
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.side_menu, sideMenu)
        transaction.commit()

        // setup action bar
        _drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout).apply {
            // Set a simple drawable used for the left or right shadow.
            setDrawerShadow(R.drawable.drawer_shadow, Gravity.START)
        }
        actionBar.run {
            actionBar?.setDisplayHomeAsUpEnabled(true)
            actionBar?.setHomeButtonEnabled(true)
        }
        _drawerToggle = object : ActionBarDrawerToggle(this, _drawerLayout, R.string.drawer_open, R.string.drawer_close) {}
        _drawerLayout.addDrawerListener(_drawerToggle)

        // set up preview
        _preview = findViewById(R.id.preview)
        _overlay = findViewById(R.id.graphic_overlay)

        // setup permission(only once)
        if (null == savedInstanceState) {
            this.setupPermission()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after has occurred.
        _drawerToggle.syncState()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (_drawerToggle.onOptionsItemSelected(item)) {
            // home is selected
            return true
        }
        when (item?.itemId) {
            SideMenuFragment.MenuItemType.Recent.rawValue -> {
                Toast.makeText(this, "Recent", Toast.LENGTH_SHORT).show()
                _drawerLayout.closeDrawer(Gravity.START)
                return true
            }
            SideMenuFragment.MenuItemType.Favorite.rawValue -> {
                Toast.makeText(this, "Favorite", Toast.LENGTH_SHORT).show()
                _drawerLayout.closeDrawer(Gravity.START)
                return true
            }
            SideMenuFragment.MenuItemType.License.rawValue -> {
                Toast.makeText(this, "License", Toast.LENGTH_SHORT).show()
                _drawerLayout.closeDrawer(Gravity.START)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Pass any configuration change to the drawer toggle.
        _drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (_permissionRequestCamera == requestCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted
                this.setupCameraSource()
            } else {
                // show error
                CommonDialogFragment.show(fragmentManager, DialogId.PermissionDenied.rawValue,
                        R.string.error_message_permission_denied, CommonDialogFragment.DialogType.Error)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onPause() {
        super.onPause()
        _preview?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _preview?.release()
    }


    //==============================================================================================
    // OnCommonDialogFragmentListener
    //==============================================================================================
    override fun onOkButtonClick(dialogId: Int) {
        if (dialogId == DialogId.PermissionDenied.rawValue) {
            this.finish()
        }
    }

    override fun onYesButtonClick(dialogId: Int) {
        if (dialogId == DialogId.UnavailableCamera.rawValue) {
            // show system app setting
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + packageName))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.startActivity(intent)
            this.finish()
        }
    }

    override fun onNoButtonClick(dialogId: Int) {
        if (dialogId == DialogId.UnavailableCamera.rawValue) {
            this.finish()
        }
    }


    override fun onBarcodeDetected(barcode: Barcode?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        runOnUiThread(Runnable {
            Toast.makeText(this, "detect!!!!", Toast.LENGTH_SHORT).show()
        })
    }



    //==============================================================================================
    // Private method
    //==============================================================================================
    /*
     * check camera permission. and if no permission, request permission or induction to system app setting
     */
    private fun setupPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                // permission denied and user check never show dialog again.
                CommonDialogFragment.show(fragmentManager, DialogId.UnavailableCamera.rawValue,
                        R.string.error_message_unavailable_camera, CommonDialogFragment.DialogType.Confirm)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), _permissionRequestCamera)
            }
        } else {
            // permission granted
            this.setupCameraSource()
        }
    }

    /*
     * set up camera
     */
    private fun setupCameraSource() {
        // BarcodeDetector : Recognizes barcodes (in a variety of 1D and 2D formats) in a supplied Frame
        val barcodeDetector = BarcodeDetector.Builder(applicationContext).build()
        val barcodeFactory = BarcodeTrackerFactory(_overlay, this)
        barcodeDetector.setProcessor(MultiProcessor.Builder(barcodeFactory).build())

        if (!barcodeDetector.isOperational()) {
            // 初めてバーコードやface APIなどを利用する場合、GMSは必要なライブラリをダウンロードするらしい。
            // 通常はアプリ起動時にダウンロードは完了しているはずらしい。もし完了してなければバーコード等の読取りが出来ない。
            // ※ネットで調べてもOfflineで動く・動かないの意見が別れているっぽい。おそらく端末内にあるかどうか、という話なんだろうなと
            // → Google Play開発者サービスが最新であれば、おそらく問題なく動く気がする。

            // 以下は端末の空き容量の判定処理。このif文に入るケースは気にしなくても良い気がするのでエラーハンドリングはしない
//            if (cacheDir.usableSpace * 100 / cacheDir.totalSpace <= 10) { // Alternatively, use cacheDir.freeSpace
//                // Handle storage low state
//            } else {
//                // Handle storage ok state
//            }
        }

        val builder = CameraSource.Builder(applicationContext, barcodeDetector).apply {
            setFacing(CameraSource.CAMERA_FACING_BACK)
            setRequestedPreviewSize(1600, 1024)
            setRequestedFps(15.0f)
            if (Build.VERSION_CODES.ICE_CREAM_SANDWICH <= Build.VERSION.SDK_INT) {
                setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
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
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(applicationContext)
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, 9999)
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








}
