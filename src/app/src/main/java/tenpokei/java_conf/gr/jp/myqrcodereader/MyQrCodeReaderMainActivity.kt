package tenpokei.java_conf.gr.jp.myqrcodereader

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast


class MyQrCodeReaderMainActivity : Activity(), CommonDialogFragment.OnCommonDialogFragmentListener, BarcodeCaptureFragment.OnBarcodeDetectedListener {

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
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.side_menu, SideMenuFragment.newInstance())
        transaction.replace(R.id.container, CaptureResultFragment.newInstance(), CaptureResultFragment.TAG)
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
                this.setupCaptureFragment()
            } else {
                // show error
                CommonDialogFragment.show(fragmentManager, DialogId.PermissionDenied.rawValue,
                        R.string.error_message_permission_denied, CommonDialogFragment.DialogType.Error)
            }
        }
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


    //==============================================================================================
    // BarcodeCaptureFragment.OnBarcodeDetectedListener
    //==============================================================================================
    override fun onBarcodeDetected(displayValue: String?) {
        fragmentManager.popBackStack()
        if (null == displayValue) {
            Toast.makeText(this, R.string.error_capture, Toast.LENGTH_SHORT).show()
        } else {
            if (fragmentManager.findFragmentByTag(CaptureResultFragment.TAG) != null &&
                    fragmentManager.findFragmentByTag(CaptureResultFragment.TAG) is CaptureResultFragment) {
                (fragmentManager.findFragmentByTag(CaptureResultFragment.TAG) as CaptureResultFragment).showResult(displayValue)
            } else {
                Toast.makeText(this, R.string.error_capture, Toast.LENGTH_SHORT).show()
            }
        }
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
            this.setupCaptureFragment()
        }
    }

    /*
     * set up capture fragment
     */
    private fun setupCaptureFragment() {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, BarcodeCaptureFragment.newInstance())
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
