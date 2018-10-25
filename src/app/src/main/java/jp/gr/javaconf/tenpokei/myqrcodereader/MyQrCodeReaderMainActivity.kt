package jp.gr.javaconf.tenpokei.myqrcodereader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MenuItem
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import jp.gr.javaconf.tenpokei.myqrcodereader.event.BarcodeDetectEvent
import jp.gr.javaconf.tenpokei.myqrcodereader.event.RefreshScanResultEvent
import jp.gr.javaconf.tenpokei.myqrcodereader.event.ScanBarcodeEvent
import jp.gr.javaconf.tenpokei.myqrcodereader.event.SideMenuSelectedEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * app main activity. handling capture, show captured result and recent captured list
 */
class MyQrCodeReaderMainActivity : AppCompatActivity(), CommonDialogFragment.OnCommonDialogFragmentListener {
    // アイコンのサイズ
    // https://backport.net/blog/2018/02/17/adaptive_icon/

    // Adaptive Iconの作成
    // https://akira-watson.com/android/adaptive-icons.html

    // 少し親切なランタイムパーミッション対応
    // https://qiita.com/caad1229/items/35bab757217b204711df

    // Androidアプリにライセンス表示を埋め込むライブラリいくつか
    // https://qiita.com/tyoro/items/f7045cea7cf5d98a80b9

    // com.google.gms:oss-licenses を使ってオープンソースライセンスを表示する
    // https://qiita.com/sho5nn/items/f63ebd7ccc0c86d98e4b


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
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.side_menu, SideMenuFragment.newInstance())
        transaction.replace(R.id.container, CaptureResultFragment.newInstance(), CaptureResultFragment.TAG)
        transaction.commit()

        // setup action bar
        _drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout).apply {
            // Set a simple drawable used for the left or right shadow.
            setDrawerShadow(R.drawable.drawer_shadow, Gravity.START)
        }
        supportActionBar.run {
            supportActionBar?.setDisplayShowHomeEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }

        _drawerToggle = object : ActionBarDrawerToggle(this, _drawerLayout, R.string.drawer_open, R.string.drawer_close) {}
        _drawerLayout.addDrawerListener(_drawerToggle)
        _drawerToggle.syncState()
        // setup permission(only once)
        if (null == savedInstanceState) {
            this.setupPermission()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val count =  supportFragmentManager.backStackEntryCount
            supportActionBar?.setDisplayShowHomeEnabled(count == 0)
            supportActionBar?.setDisplayHomeAsUpEnabled(count == 0)
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

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after has occurred.
        _drawerToggle.syncState()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // this check is necessary. if delete, navigation drawer is not shown
        // when humberger button tapped.
        if (_drawerToggle.onOptionsItemSelected(item)) {
            // home is selected
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Pass any configuration change to the drawer toggle.
        _drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (_permissionRequestCamera == requestCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted
                this.setupFragment(BarcodeCaptureFragment.newInstance())
            } else {
                // show error
                CommonDialogFragment.show(supportFragmentManager, DialogId.PermissionDenied.rawValue,
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
    // EventBus
    //==============================================================================================
    /**
     * this event is fired when scan barcode button clicked.
     */
    @Suppress("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(@Suppress("UNUSED_PARAMETER") event: ScanBarcodeEvent) {
        this.setupFragment(BarcodeCaptureFragment.newInstance())
    }

    /**
     * this event is fired when barcode detected
     */
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: BarcodeDetectEvent) {
        // CaptureResultFragment does not receive this event directly,
        // because need to pop BarcodeCaptureFragment
        supportFragmentManager.popBackStack()
        EventBus.getDefault().postSticky(RefreshScanResultEvent(event.displayValue))
    }


    /**
     * this event is fired when barcode detected
     */
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: SideMenuSelectedEvent) {
        _drawerLayout.closeDrawer(Gravity.START)
        when(event.getItemType()) {
            SideMenuSelectedEvent.MenuItemType.License -> {
                val intent = Intent(this, OssLicensesMenuActivity::class.java)
                intent.putExtra("title", "OSS license")
                startActivity(intent)
            }

            SideMenuSelectedEvent.MenuItemType.Recent -> {
                setupFragment(CaptureHistoriesFragment.newInstance())
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
                CommonDialogFragment.show(supportFragmentManager, DialogId.UnavailableCamera.rawValue,
                        R.string.error_message_unavailable_camera, CommonDialogFragment.DialogType.Confirm)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), _permissionRequestCamera)
            }
        } else {
            // permission granted
            this.setupFragment(BarcodeCaptureFragment.newInstance())
        }
    }

    /**
     * set up fragment
     * @param fragment fragment
     */
    private fun setupFragment(fragment : Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
