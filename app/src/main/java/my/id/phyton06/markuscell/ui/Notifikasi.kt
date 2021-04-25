package my.id.phyton06.markuscell.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_contract_detail.*
import my.id.phyton06.markuscell.commons.RxBaseFragment
import kotlinx.android.synthetic.main.fragment_notifikasi.*
import kotlinx.android.synthetic.main.fragment_notifikasi.toolbar
import my.id.phyton06.markuscell.R
import my.id.phyton06.markuscell.adapters.NotificationAdapter
import my.id.phyton06.markuscell.commons.RxBus
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.database.DbHelper
import my.id.phyton06.markuscell.models.NotificationModel
import my.id.phyton06.markuscell.models.NotificationResModel
import my.id.phyton06.markuscell.models.SharedPrefManager
import my.id.phyton06.markuscell.responses.TransaksiResponseModel
import my.id.phyton06.markuscell.services.ServiceApi
import okhttp3.OkHttpClient
import okhttp3.internal.http2.Http2Reader
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class Notifikasi : RxBaseFragment() {

    private var listNotif:MutableList<NotificationModel> = mutableListOf()
    private lateinit var listAdapter:NotificationAdapter
    private lateinit var progressDialog: Dialog
    private val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                dialog.dismiss()
            }
        }
    }

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
        toolbar?.setNavigationOnClickListener {
            RxBus.get().send(Utils.DASHBOARD)
        }
        progressDialog = Utils.progressDialog(context!!, activity!!)

        getNotification()

        //listNotif.add(NotificationModel(1, "Pembayaran Diterima", "11 Maret 2021", "Kami telah menerima pembayaran cicilan sebesar Rp. 350.000", true))
        //listNotif.add(NotificationModel(2, "Jatuh Tempo", "10 Maret 2021", "Jangan lupa tagihan mu sebesar Rp. 350.000 akan jatuh tempo di tanggal 11 Maret 2021", true))
    }

    private fun getNotification(){
        progressDialog.show()
       // Handler().postDelayed({
            subscriptions.add(
                    provideService().notif(Utils.token_device)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ user ->
                                progressDialog.dismiss()
                                if (user.responsecode == 200) {
                                    listNotif = user.data
                                    listView.layoutManager = LinearLayoutManager(this.context)
                                    listAdapter = NotificationAdapter(this.context!!,listNotif)
                                    listView.setAdapter(listAdapter)
                                } else {
                                    val builder = AlertDialog.Builder(context!!)
                                    builder
                                            .setMessage("Gagal mengambil data notifikasi error code: " + user.message)
                                            .setPositiveButton("OK", dialogClickListener)
                                            .setCancelable(false)
                                            .show()
                                }
                            }, { err ->
                                progressDialog.dismiss()
                                val builder = AlertDialog.Builder(context!!)
                                builder.setMessage("Gagal mengambil data notifikasi : " + err.localizedMessage )
                                        .setPositiveButton("OK", dialogClickListener)
                                        .setCancelable(false)
                                        .show()
                            })
            )
       // }, 2000 + 400.toLong())

    }

    //fungsi untuk nembak ke api login
    private fun provideService() : ServiceApi {
        val clientBuilder : OkHttpClient.Builder = Utils.buildClient()
        val retrofit = Retrofit.Builder()
                .baseUrl(Utils.ENDPOINT)
                .client(
                        clientBuilder
                                .connectTimeout(10, TimeUnit.SECONDS)
                                .writeTimeout(10, TimeUnit.SECONDS)
                                .readTimeout(10, TimeUnit.SECONDS)
                                .build()
                )
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
        return retrofit.create(ServiceApi::class.java)
    }

}