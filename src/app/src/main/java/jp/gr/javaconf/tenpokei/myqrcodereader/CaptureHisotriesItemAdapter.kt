package jp.gr.javaconf.tenpokei.myqrcodereader

import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.gr.javaconf.tenpokei.myqrcodereader.event.CaptureHistoriesItemEvent
import org.greenrobot.eventbus.EventBus


data class CaptureItem(val readDate: String, val displayValue: String, val siteIcon: ByteArray?, val siteName: String?)

class CaptureHistoriesItemHolder(view: View) : RecyclerView.ViewHolder(view) {
    val readDate = view.findViewById<TextView>(R.id.read_date)
    val displayValue = view.findViewById<TextView>(R.id.display_value)
    val siteIcon = view.findViewById<ImageView>(R.id.site_icon)
    val siteName = view.findViewById<TextView>(R.id.site_name)
}

/**
 * list item
 */
class CaptureHisotriesItemAdapter(private val items: List<CaptureItem>) : RecyclerView.Adapter<CaptureHistoriesItemHolder>() {
    //==============================================================================================
    // Declaration
    //==============================================================================================
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaptureHistoriesItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_capture_histories, parent, false)
        return CaptureHistoriesItemHolder(view)
    }

    override fun onBindViewHolder(holder: CaptureHistoriesItemHolder, position: Int) {
        val item = items[position]
        holder.readDate.text = item.readDate
        holder.displayValue.text = item.displayValue
        if (null == item.siteIcon) {
            holder.siteIcon.setImageBitmap(null)
        } else {
            holder.siteIcon.setImageBitmap(BitmapFactory.decodeByteArray(item.siteIcon, 0, item.siteIcon.size))
        }
        holder.siteName.text = item.siteName?: ""
        if (null == item.siteIcon && null == item.siteName) {
            holder.siteIcon.visibility = View.INVISIBLE
            holder.siteName.visibility = View.INVISIBLE
        } else {
            holder.siteIcon.visibility = View.VISIBLE
            holder.siteName.visibility = View.VISIBLE
        }
        holder.itemView.setOnClickListener{
            EventBus.getDefault().post(CaptureHistoriesItemEvent(item.displayValue))
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}