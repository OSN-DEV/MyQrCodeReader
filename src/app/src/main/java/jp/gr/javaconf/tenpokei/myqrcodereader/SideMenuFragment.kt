package jp.gr.javaconf.tenpokei.myqrcodereader

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import jp.gr.javaconf.tenpokei.myqrcodereader.event.SideMenuSelectedEvent
import org.greenrobot.eventbus.EventBus


/**
 * side menu fragment
 */
class SideMenuFragment : Fragment() {

    //==============================================================================================
    // Fragment
    //==============================================================================================
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_side_menu, container, false)
        view.findViewById<TextView>(R.id.side_menu_recent).setOnClickListener {
            EventBus.getDefault().post(SideMenuSelectedEvent(SideMenuSelectedEvent.MenuItemType.Recent))
        }
        view.findViewById<TextView>(R.id.side_menu_license).setOnClickListener {
            EventBus.getDefault().post(SideMenuSelectedEvent(SideMenuSelectedEvent.MenuItemType.License))
        }
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
}
