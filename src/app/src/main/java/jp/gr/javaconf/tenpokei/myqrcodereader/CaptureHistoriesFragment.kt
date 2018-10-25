package jp.gr.javaconf.tenpokei.myqrcodereader

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import jp.gr.javaconf.tenpokei.myqrcodereader.data.AppDatabase
import jp.gr.javaconf.tenpokei.myqrcodereader.event.CaptureHistoriesItemEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

/**
 * list recent read
 */
class CaptureHistoriesFragment : Fragment() {
    // 【kotlin】RecyclerViewの簡単な使い方【初心者向け】
    // https://qiita.com/saiki-ii/items/78ed73134784f3e5db7e

    // RecyclerViewの基本
    // https://qiita.com/naoi/items/f8a19d6278147e98bbc2

    private var _context: Context? = null

    //==============================================================================================
    // Declaration
    //==============================================================================================


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        _context = context
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_capture_histories, container, false)

        val itemList = view.findViewById<RecyclerView>(R.id.history_item_list)
        itemList.setHasFixedSize(true)
        itemList.layoutManager = LinearLayoutManager(activity)
        itemList.adapter = CaptureHisotriesItemAdapter(this.createList())

        return view
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


    //==============================================================================================
    // EventBus
    //==============================================================================================
    /**
     * this event is fired when scan barcode button clicked.
     */
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CaptureHistoriesItemEvent) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(event.displayValue)
        try {
            startActivity(intent)
        } catch (ex: Exception) {
            Toast.makeText(activity, R.string.error_message_launch_failed, Toast.LENGTH_SHORT).show()
        }
    }


    //==============================================================================================
    // Private Method
    //==============================================================================================
    private fun createList() : List<CaptureItem> {
        val list = mutableListOf<CaptureItem>()

        if (null != _context) {
            val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            format.timeZone = TimeZone.getTimeZone("Asia/Tokyo")
            val cursor = AppDatabase(_context!!.applicationContext).readAll()
            while(cursor.moveToNext()) {
//                val date = Date((cursor.getInt(0) / 1000).toLong())
                val date = Date((cursor.getLong(0) ).toLong())

                list.add(CaptureItem(
                        format.format(date),
                        cursor.getString(1),
                        cursor.getBlob(2),
                        cursor.getString(3)
                ))
            }
        }
        return list
    }


    //==============================================================================================
    // Static
    //==============================================================================================
    companion object {
        fun newInstance(): CaptureHistoriesFragment = CaptureHistoriesFragment()
    }
}
