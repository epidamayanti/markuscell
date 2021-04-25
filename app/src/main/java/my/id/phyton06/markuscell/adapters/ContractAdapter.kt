package my.id.phyton06.markuscell.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_contract_row.view.*
import my.id.phyton06.markuscell.R
import my.id.phyton06.markuscell.R.layout.fragment_contract_row
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.models.ContractModel
import my.id.phyton06.markuscell.responses.TransaksiResponseModel

/**
 * Created by Phyton06 on 3/11/2021.
 */
class ContractAdapter(private val context: Context, private val items: List<ContractModel>, private val listener: (ContractModel) -> Unit)
    : RecyclerView.Adapter<ContractAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(fragment_contract_row, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position], listener)
    }

    override fun getItemCount(): Int = items.size


    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
            LayoutContainer {

        @SuppressLint("ResourceAsColor")
        fun bindItem(items: ContractModel, listener: (ContractModel) -> Unit) {
            containerView.nama.text = items.membername
            containerView.kontrak.text = "No Kontrak : "+items.kodetransaksi
            containerView.tempo.text = "Jatuh Tempo : "+items.jatuhtempo
            containerView.bayar.text = "Cicilan : "+ Utils.convertToRp(items.cicilan)
            if(items.status == 1)
                containerView.ll_contract_row.setBackgroundColor(Color.parseColor("#66BB6A"))
            else
                containerView.ll_contract_row.setBackgroundColor(Color.parseColor("#FFFFFF"))

            containerView.setOnClickListener { listener(items) }
        }
    }
}