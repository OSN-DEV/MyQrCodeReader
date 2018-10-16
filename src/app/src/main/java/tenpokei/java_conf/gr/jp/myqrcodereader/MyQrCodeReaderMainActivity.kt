package tenpokei.java_conf.gr.jp.myqrcodereader

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast

class MyQrCodeReaderMainActivity() : Activity() {
    // Barcode reader sample(Github)
    // https://github.com/googlesamples/android-vision/tree/master/visionSamples/barcode-reader

    // アイコンのサイズ
    // https://backport.net/blog/2018/02/17/adaptive_icon/

    // Adaptive Iconの作成
    // https://akira-watson.com/android/adaptive-icons.html

    // 少し親切なランタイムパーミッション対応
    // https://qiita.com/caad1229/items/35bab757217b204711df

    private lateinit var _drawerLayout: DrawerLayout
    private lateinit var _drawerToggle: ActionBarDrawerToggle


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
        transaction.addToBackStack(null)
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

        // setup permission
        this.setupPermission()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }


    //==============================================================================================
    // Private method
    //==============================================================================================
    private fun setupPermission() {
        val result = this.checkPermission(Manifest.permission.CAMERA, android.os.Process.myPid(), Process.myUid())
        if (result != PackageManager.PERMISSION_GRANTED) {
            var shouldShowRequestPermission = false
            if (23 <= Build.VERSION.SDK_INT) {
                shouldShowRequestPermission = this.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            }
            if (shouldShowRequestPermission) {
                Toast.makeText(this, "show request permission", Toast.LENGTH_SHORT).show()

                // permission denied and user check never show dialog again.
            } else {
                // request permission

            }
        } else {
            Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show()
        }
    }

}
