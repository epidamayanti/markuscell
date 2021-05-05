package my.id.phyton06.markuscell.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_contract.*
import kotlinx.android.synthetic.main.fragment_contract_due_date.*
import kotlinx.android.synthetic.main.fragment_contract_due_date.toolbar
import kotlinx.android.synthetic.main.fragment_dashboard.*
import my.id.phyton06.markuscell.R
import my.id.phyton06.markuscell.adapters.DashboardAdapter
import my.id.phyton06.markuscell.commons.RxBaseFragment
import my.id.phyton06.markuscell.commons.RxBus
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.models.ContractModel
import my.id.phyton06.markuscell.responses.TransaksiResponseModel
import my.id.phyton06.markuscell.services.ServiceApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ContractDueDate : RxBaseFragment() {

    private lateinit var progressDialog: Dialog
    private val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                dialog.dismiss()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener {
            RxBus.get().send(Utils.DASHBOARD)
        }
        progressDialog = Utils.progressDialog(context!!, activity!!)

        list_due_date.layoutManager = LinearLayoutManager(this.context)
        Utils.kontrakTempo.sortBy { it.jatuh_tempo }
        val listAdapter = DashboardAdapter(this.context!!, Utils.kontrakTempo) {
            Utils.isDashboard = true
            getSinglePayment(Utils.hash_data_transaksi.get(it.id_transaksi)!!, "payment")
        }
        list_due_date.adapter = listAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contract_due_date, container, false)
    }

    private fun getSinglePayment(trans: TransaksiResponseModel, page: String){
        progressDialog.show()
        Handler().postDelayed({
            subscriptions.add(
                provideService().payment(Utils.token_device, trans.idTransaksi)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ user ->
                        if (user.responsecode == 200) {
                            Utils.data_payment = user.data
                            val data = trans
                            Utils.transaksi = trans
                            var jatuhtempo = "- "
                            var tenor_lunas = 0
                            var member = ""
                            var lunas = 0
                            var idPayment = 0
                            val jum_tenor = Utils.data_payment.size
                            var jum_denda = "0"
                            var jum_tagihan = "0"

                            //status 4 adalah gratis
                            for (payment in Utils.data_payment) {
                                if (payment.status.equals("3") || payment.status.equals("0")) {
                                    idPayment = payment.idPayment
                                    jatuhtempo = Utils.dateConvert(payment.jatuhtempo)
                                    jum_denda = payment.denda
                                    jum_tagihan = payment.debitkredit
                                    break
                                }
                            }

                            for (payment in Utils.data_payment) {
                                if (payment.status.equals("4") || payment.status.equals("2")) {
                                    tenor_lunas++
                                }
                            }

                            member = if (data.membername.isNullOrBlank()) "-" else data.membername
                            lunas = if(data.status.equals("Lunas")) 1 else 0

                            Utils.detailContract = ContractModel(
                                idPayment,
                                data.idTransaksi,
                                "" + data.idPelanggan,
                                "" + data.idBarang,
                                "" + data.kodetransaksi,
                                "" + data.total,
                                "" + data.modal,
                                "" + Utils.convertToRp(data.jual),
                                "" + data.dp,
                                "" + jum_tenor,
                                "" + data.cicilan,
                                "" + jatuhtempo,
                                "" + tenor_lunas,
                                "" + tenor_lunas,
                                "" + data.komisi,
                                "" + data.komisiperbulan,
                                "" + jum_denda,
                                "" + member,
                                "" + data.memberhp,
                                lunas
                            )
                            progressDialog.dismiss()
                            if (page == "payment")
                                RxBus.get().send(Utils.PAYMENT)

                            Utils.isContractDueDate = true

                        } else {
                            val builder = AlertDialog.Builder(context)
                            builder
                                .setMessage("Gagal mengambil data single payment error code: " + user.message)
                                .setPositiveButton("OK", dialogClickListener)
                                .setCancelable(false)
                                .show()
                        }
                    }, { err ->
                        progressDialog.dismiss()
                        val builder = AlertDialog.Builder(context)
                        builder.setMessage("Gagal mengambil data single payment : " + err.localizedMessage)
                            .setPositiveButton("OK", dialogClickListener)
                            .setCancelable(false)
                            .show()
                    })
            )
        }, 2000 + 400.toLong())
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