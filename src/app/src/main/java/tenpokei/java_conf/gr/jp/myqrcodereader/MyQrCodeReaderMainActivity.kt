package tenpokei.java_conf.gr.jp.myqrcodereader

import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView

class MyQrCodeReaderMainActivity : AppCompatActivity() {

    private var _sideMenu: DrawerLayout? = null
    private var _menuToggle: ActionBarDrawerToggle? = null;
    private var _menuList: ListView? = null;

    //==============================================================================================
    // Activity
    //==============================================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_qr_reader_main)

        _sideMenu = findViewById(R.id.side_menu)
        this.initDrawer()

        _menuList = findViewById(R.id.lis_tview)
        _menuList?.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayOf("A", "B")))

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        _menuToggle?.syncState();
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_main, menu)
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return super.onOptionsItemSelected(item)
    }
    //==============================================================================================
    // Private method
    //==============================================================================================
    private fun initDrawer() {
        _menuToggle = ActionBarDrawerToggle(this, _sideMenu, R.string.app_name, R.string.app_name)
        _menuToggle?.isDrawerIndicatorEnabled = true
        _sideMenu?.addDrawerListener(_menuToggle!!)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

}
