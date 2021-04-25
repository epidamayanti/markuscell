package my.id.phyton06.markuscell.adapters

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_payment_row.view.*
import my.id.phyton06.markuscell.R.layout.fragment_payment_row
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.models.RiwayatPembayaranModel
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Phyton06 on 3/11/2021.
 */
class RiwayatAdapter(private val context: Context, private val items: List<RiwayatPembayaranModel>, private val listener: (RiwayatPembayaranModel) -> Unit)
    : RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(fragment_payment_row, parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position], listener)
    }

    override fun getItemCount(): Int = items.size


    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
            LayoutContainer {

        fun bindItem(items: RiwayatPembayaranModel, listener: (RiwayatPembayaranModel) -> Unit) {
            containerView.riwayat_cicilan.text = items.cicilan
            containerView.no.text = items.no_angsuran
            containerView.riwayat_tempo.text = Utils.dateConvert(items.tgl_jatuh_tempo)

            if(items.status.toInt() == 0){
                containerView.riwayat_status.text = "Belum Dibayar"
                containerView.riwayat_status.setTextColor(Color.RED)
            }
            else if(items.status.toInt() == 2){
                containerView.riwayat_status.text = "Sudah Dibayar"
                containerView.riwayat_status.setTextColor(Color.GREEN)
            }
            else if(items.status.toInt() == 4){
                containerView.riwayat_status.text = "Gratis"
                containerView.riwayat_status.setTextColor(Color.GREEN)
            }
            else if(items.status.toInt() == 3) {
                containerView.riwayat_status.text = "Dalam Proses"
                containerView.riwayat_status.setTextColor(Color.BLUE)
            }
            else {
                containerView.riwayat_status.text = "-"
                containerView.riwayat_status.setTextColor(Color.BLUE)
            }


            containerView.setOnClickListener { listener(items) }
        }
    }

}