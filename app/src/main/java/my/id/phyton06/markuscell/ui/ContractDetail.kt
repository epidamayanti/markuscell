package my.id.phyton06.markuscell.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_contract_detail.*
import my.id.phyton06.markuscell.R
import my.id.phyton06.markuscell.commons.RxBus
import my.id.phyton06.markuscell.commons.Utils
import org.json.JSONObject


class ContractDetail : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contract_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val data = Utils.detailContract

        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener {
            if(Utils.moreThanOne)
                RxBus.get().send(Utils.CONTRACT)
            else
                RxBus.get().send(Utils.DASHBOARD)
        }

        noKontrak.text = data.kodetransaksi
        nama.text = data.membername
        tenor.text = data.jangkawaktu+" Bulan"
        cicilan.text = Utils.convertToRp(data.cicilan)
        denda.text = Utils.convertToRp(data.jumlahdenda)
        tempo.text = data.jatuhtempo
        jumPinjaman.text = data.jual

        namaProduct.text = Utils.convertFromJson("product", ""+data.idBarang)
        jumProduct.text = Utils.convertFromJson("jmlproduct", ""+data.idBarang ) + " Unit"
        hargaProduct.text = Utils.convertToRp(Utils.convertFromJson("jual", ""+data.idBarang))

        btnPayment.setOnClickListener {
            btnPayment.setBackgroundColor(Color.parseColor("#448aff"))
            RxBus.get().send(Utils.PAYMENT)
        }
    }

}