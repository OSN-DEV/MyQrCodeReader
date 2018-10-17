package tenpokei.java_conf.gr.jp.myqrcodereader

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView


/**
 * side menu fragment
 */
class SideMenuFragment : Fragment() {

    /**
     * use for identifying to which button is pressed.
     */
    enum class MenuItemType(val rawValue: Int) {
        Recent(1),
        Favorite(2),
        License(3)
    }

    //==============================================================================================
    // Fragment
    //==============================================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_side_menu, container, false)
        view.findViewById<TextView>(R.id.side_menu_recent).setOnClickListener { button -> onMenuItemClicked(MenuItemType.Recent) }
        view.findViewById<TextView>(R.id.side_menu_favorite).setOnClickListener { button -> onMenuItemClicked(MenuItemType.Favorite) }
        view.findViewById<TextView>(R.id.side_menu_license).setOnClickListener { button -> onMenuItemClicked(MenuItemType.License) }
        view.findViewById<TextView>(R.id.app_version).text = BuildConfig.VERSION_NAME
        return view
    }

    //==============================================================================================
    // public method
    //==============================================================================================
    companion object {
        /**
         * create a instance
         */
        @JvmStatic
        fun newInstance() = SideMenuFragment()
    }


    //==============================================================================================
    // private method
    //==============================================================================================
    private fun onMenuItemClicked(type: MenuItemType) {
        val menu = PopupMenu(activity, null).menu
        val item = menu.add(0, type.rawValue, 0, "")
        activity.onOptionsItemSelected(item)
    }
}
