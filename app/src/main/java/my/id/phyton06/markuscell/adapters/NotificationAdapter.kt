package my.id.phyton06.markuscell.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_notifikasi_row.view.*
import my.id.phyton06.markuscell.R.layout.fragment_notifikasi_row
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.models.NotificationModel

/**
 * Created by Phyton06 on 3/11/2021.
 */
class NotificationAdapter(private val context: Context, private val items: List<NotificationModel>)
    : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(fragment_notifikasi_row, parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position])
    }

    override fun getItemCount(): Int = items.size


    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
            LayoutContainer {

        fun bindItem(items: NotificationModel) {
            containerView.textJudulNotif.text = ""
            containerView.textNotif.text = items.message
            containerView.textTglNotif.text = Utils.dateConvert(items.created_at.replace("T", " ").substring(0,19))
        }
    }
}