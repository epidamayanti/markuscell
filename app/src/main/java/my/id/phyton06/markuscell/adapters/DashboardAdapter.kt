package my.id.phyton06.markuscell.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_contract_row.view.*
import kotlinx.android.synthetic.main.fragment_dashboard_row.view.*
import my.id.phyton06.markuscell.R.layout.fragment_contract_row
import my.id.phyton06.markuscell.R.layout.fragment_dashboard_row
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.models.ContractModel
import my.id.phyton06.markuscell.models.KontrakTempoModel
import my.id.phyton06.markuscell.responses.TransaksiResponseModel

/**
 * Created by Phyton06 on 3/11/2021.
 */
class DashboardAdapter(private val context: Context, private val items: List<KontrakTempoModel>, private val listener: (KontrakTempoModel) -> Unit)
    : RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(fragment_dashboard_row, parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position], listener)
    }

    override fun getItemCount(): Int = items.size


    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
            LayoutContainer {

        fun bindItem(items: KontrakTempoModel , listener: (KontrakTempoModel) -> Unit) {
            containerView.row_kontrak.text = items.no_kontrak
            containerView.row_member.text = items.nama_member
            containerView.row_angsuran.text = items.no_angsuran
            containerView.row_cicilan.text = items.cicilan
            containerView.row_jatuh_tempo.text = items.jatuh_tempo
            containerView.setOnClickListener { listener(items) }
        }
    }
}