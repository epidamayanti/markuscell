package my.id.phyton06.markuscell.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_contract.*
import my.id.phyton06.markuscell.R
import my.id.phyton06.markuscell.adapters.ContractAdapter
import my.id.phyton06.markuscell.commons.RxBaseFragment
import my.id.phyton06.markuscell.commons.RxBus
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.commons.Utils.Companion.data_id_transaksi
import my.id.phyton06.markuscell.commons.Utils.Companion.hash_data_payment
import my.id.phyton06.markuscell.commons.Utils.Companion.hash_data_transaksi
import my.id.phyton06.markuscell.models.ContractModel
import my.id.phyton06.markuscell.responses.PaymentResponseModel
import my.id.phyton06.markuscell.services.ServiceApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class Contract : RxBaseFragment() {

    private lateinit var listAdapter: ContractAdapter
    private lateinit var progressDialog: Dialog
    private lateinit var data_all_payment : MutableList<PaymentResponseModel>
    private var detailPayment: HashMap<String,  MutableList<PaymentResponseModel>> = HashMap<String,  MutableList<PaymentResponseModel>>()
    private val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                dialog.dismiss()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contract, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener {
            RxBus.get().send(Utils.DASHBOARD)
        }
        progressDialog = Utils.progressDialog(context!!, activity!!)

        progressDialog.show()
        getAllPayment()

    }


    private fun getAllPayment(){
        subscriptions.add(
                provideService().allPayment(Utils.token_device)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ user ->
                            progressDialog.dismiss()

                            if (user.responsecode == 200) {
                                data_all_payment = user.data

                                for(trans in data_id_transaksi){
                                    val data_pay : MutableList<PaymentResponseModel> = mutableListOf()
                                    for (payment in data_all_payment) {
                                        if(trans == payment.idTransaksi){
                                            data_pay.add(payment)
                                        }
                                    }
                                    hash_data_payment.put(trans, data_pay)
                                }

                                val listContract:MutableList<ContractModel> = mutableListOf()
                                for(trans in data_id_transaksi){
                                    val data_payment = hash_data_payment.get(trans)
                                    val data = hash_data_transaksi.get(trans)!!
                                    Utils.transaksi = hash_data_transaksi.get(trans)!!
                                    var idPayment = 0
                                    var jatuhtempo = ""
                                    var tenor_lunas = 0
                                    var denda = 0
                                    var lunas = 0
                                    var member = ""
                                    val jum_tenor = data_payment?.size

                                    for (payment in data_payment!!) {
                                        if (payment.status.equals("3") || payment.status.equals("0")) {
                                            jatuhtempo = Utils.dateConvert(payment.jatuhtempo)
                                            idPayment = payment.idPayment
                                            denda = payment.denda.toInt()
                                            break
                                        } else {
                                            jatuhtempo = "-"
                                            tenor_lunas++
                                        }
                                    }

                                    member = if (data.membername.isNullOrBlank()) "-" else data.membername
                                    lunas = if(data.status.equals("Lunas")) 1 else 0

                                    listContract.add(
                                            ContractModel(
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
                                                    "" + tenor_lunas ,
                                                    "" + tenor_lunas,
                                                    "" + data.komisi,
                                                    "" + data.komisiperbulan,
                                                    "" + denda,
                                                    "" + member,
                                                    "" + data.memberhp,
                                                    lunas
                                            )
                                    )
                                }

                                listContract.sortBy{it.status}
                                listView.layoutManager = LinearLayoutManager(this.context)
                                listAdapter = ContractAdapter(this.context!!, listContract){
                                    Utils.data_payment = hash_data_payment.get(it.idTransaksi)!!
                                    Utils.detailContract = it

                                    if(Utils.page == "payment")
                                        RxBus.get().send(Utils.PAYMENT)
                                    else
                                        RxBus.get().send(Utils.CONTRACT_DETAIL)

                                }
                                listView.setAdapter(listAdapter)


                            } else {
                                val builder = AlertDialog.Builder(context)
                                builder
                                        .setMessage("Gagal mengambil data get all payment error code : " + user.message)
                                        .setPositiveButton("OK", dialogClickListener)
                                        .setCancelable(false)
                                        .show()
                            }
                        }, { err ->
                            progressDialog.dismiss()
                            val builder = AlertDialog.Builder(context)
                            builder.setMessage("Gagal mengambil data get all payment : " + err.localizedMessage)
                                    .setPositiveButton("OK", dialogClickListener)
                                    .setCancelable(false)
                                    .show()
                        })
        )
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