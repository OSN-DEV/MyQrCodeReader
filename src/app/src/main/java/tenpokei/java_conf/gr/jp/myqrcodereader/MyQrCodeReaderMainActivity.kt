package tenpokei.java_conf.gr.jp.myqrcodereader

import android.app.Activity

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import tenpokei.java_conf.gr.jp.myqrcodereader.barcode.SideMenuAdapter

class MyQrCodeReaderMainActivity() : Activity(), SideMenuAdapter.OnItemClickListener {

    private lateinit var _drawerLayout: DrawerLayout
    private lateinit var  _menuList: RecyclerView
    private lateinit var  _menuToggle: ActionBarDrawerToggle
    private lateinit var _drawerToggle: ActionBarDrawerToggle


    //==============================================================================================
    // Activity
    //==============================================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_qr_reader_main)

        _drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout).apply {
            // Set a simple drawable used for the left or right shadow.
            setDrawerShadow(R.drawable.drawer_shadow, Gravity.START)
        }

        val items = arrayOf("A")
        _menuList = findViewById<RecyclerView>(R.id.side_menu).apply {
            // Improve performance by indicating the list if fixed size.
            setHasFixedSize(true)
            // Set up the drawer's list view with items and click listener.
            adapter = SideMenuAdapter(items, this@MyQrCodeReaderMainActivity)
        }

        // Enable ActionBar app icon to behave as action to toggle nav drawer.
        // actionBar = getActionbar
        actionBar.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        _drawerToggle = object : ActionBarDrawerToggle(
                this,
                _drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close) {
            override fun onDrawerClosed(drawerView: View?) {
                actionBar.title = "close";
                invalidateOptionsMenu() // Creates call to onPrepareOptionsMenu().
            }

            override fun onDrawerOpened(drawerView: View?) {
                actionBar.title = "open"
                invalidateOptionsMenu() // Creates call to onPrepareOptionsMenu().
            }
        }

        // Set a custom shadow that overlays the main content when the drawer opens.
        _drawerLayout.addDrawerListener(_drawerToggle)

        if (savedInstanceState == null) {
//            selectItem(0)
        }




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


    /* The click listener for RecyclerView in the navigation drawer. */
    override fun onClick(view: View, position: Int) {
//        selectItem(position)
    }



    //==============================================================================================
    // Private method
    //==============================================================================================

//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<MyQrCodeReaderMainActivity> {
//        override fun createFromParcel(parcel: Parcel): MyQrCodeReaderMainActivity {
//            return MyQrCodeReaderMainActivity(parcel)
//        }
//
//        override fun newArray(size: Int): Array<MyQrCodeReaderMainActivity?> {
//            return arrayOfNulls(size)
//        }
//    }

}
