package my.id.phyton06.markuscell.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import my.id.phyton06.markuscell.commons.RxBaseFragment
import kotlinx.android.synthetic.main.fragment_notifikasi.*
import kotlinx.android.synthetic.main.fragment_notifikasi.toolbar
import my.id.phyton06.markuscell.R
import my.id.phyton06.markuscell.adapters.NotificationAdapter
import my.id.phyton06.markuscell.models.NotificationModel


class Notifikasi : RxBaseFragment() {

    private var listNotif:ArrayList<NotificationModel> = ArrayList()
    private lateinit var listAdapter:NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifikasi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        listNotif.add(NotificationModel(1, "Pembayaran Diterima", "11 Maret 2021", "Kami telah menerima pembayaran cicilan sebesar Rp. 350.000", true))
        listNotif.add(NotificationModel(2, "Jatuh Tempo", "10 Maret 2021", "Jangan lupa tagihan mu sebesar Rp. 350.000 akan jatuh tempo di tanggal 11 Maret 2021", true))

        listView.layoutManager = LinearLayoutManager(this.context)
        listAdapter = NotificationAdapter(this.context!!,listNotif)
        listView.setAdapter(listAdapter)

    }




}