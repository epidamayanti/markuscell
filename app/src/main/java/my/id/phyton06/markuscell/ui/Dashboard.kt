 package my.id.phyton06.markuscell.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.widget.RxAdapterView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_dashboard.*
import my.id.phyton06.markuscell.R
import my.id.phyton06.markuscell.adapters.DashboardAdapter
import my.id.phyton06.markuscell.commons.RxBaseFragment
import my.id.phyton06.markuscell.commons.RxBus
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.database.DbHelper
import my.id.phyton06.markuscell.models.ContractModel
import my.id.phyton06.markuscell.models.KontrakTempoModel
import my.id.phyton06.markuscell.models.LoginModel
import my.id.phyton06.markuscell.models.SharedPrefManager
import my.id.phyton06.markuscell.responses.TransaksiResponseModel
import my.id.phyton06.markuscell.services.ServiceApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.properties.Delegates

 class Dashboard : RxBaseFragment() {

     private lateinit var sharedPrefManager : SharedPrefManager
     private lateinit var DbHelper : DbHelper
     private lateinit var progressDialog: Dialog
     private lateinit var progressLogout: Dialog
     private val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                dialog.dismiss()
            }
        }
     }
     private lateinit var dataKontrak : HashMap<String, TransaksiResponseModel>
     private var nomorKontrak : HashMap<Int, TransaksiResponseModel> = HashMap<Int, TransaksiResponseModel>()
     private var idtransaksi = 0
     private var kodetransaksi = ""
     private var jum_dibayar_morethan1 by Delegates.notNull<Int>()
     private var jum_dibayar by Delegates.notNull<Int>()
     private var jum_ditagih_morethan1 by Delegates.notNull<Int>()
     private var jum_ditagih by Delegates.notNull<Int>()
     private var index_lunas = 0
     private var txt_tenor = ""

     override fun onCreateView(
             inflater: LayoutInflater, container: ViewGroup?,
             savedInstanceState: Bundle?
     ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefManager = SharedPrefManager(context!!)
        DbHelper = DbHelper(this.requireContext())
        dataKontrak = HashMap<String, TransaksiResponseModel>()
        progressDialog = Utils.progressDialog(context!!, activity!!)
        progressLogout = Utils.progressLogout(context!!, activity!!)

        //getNotif()
        if(Utils.page == "notif"){
            RxBus.get().send(Utils.NOTIF)
        }
        else {
            progressDialog.show()
            getTransaksi()
            if(!DbHelper.getPhoto().equals("-")){
                val myBitmap = BitmapFactory.decodeFile(DbHelper.getPhoto())
                photo.setImageBitmap(myBitmap)
            }
        }


        rl_personal.setOnClickListener {
            personal.setTextColor(Color.WHITE)
            rl_personal.setBackgroundColor(Color.rgb(163, 197, 255))
            RxBus.get().send(Utils.PROFILE)
        }

        ll_notif.setOnClickListener {
            notif.setTextColor(Color.WHITE)
            ll_notif.setBackgroundColor(Color.rgb(163, 197, 255))
            RxBus.get().send(Utils.NOTIF)
        }

        ll_payment.setOnClickListener {
            payment.setTextColor(Color.WHITE)
            ll_payment.setBackgroundColor(Color.rgb(163, 197, 255))
            Utils.page = "payment"

             if(Utils.COUNT_DIALOG == 1 ){
                val builder = AlertDialog.Builder(context)
                builder.setMessage("ANDA BELUM MEMILIKI TRANSAKSI")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> {
                                    dialog.dismiss()
                                    payment.setTextColor(Color.parseColor("#37474F"))
                                    ll_payment.setBackgroundColor(Color.WHITE)
                                }
                            }
                        })
                        .setCancelable(false)
                        .show()
            } else {
                 if (Utils.moreThanOne)
                     RxBus.get().send(Utils.CONTRACT)
                 else
                     getSinglePayment(Utils.data_transaksi[0], Utils.page)
             }
        }

        ll_contract.setOnClickListener {
            contract.setTextColor(Color.WHITE)
            ll_contract.setBackgroundColor(Color.rgb(163, 197, 255))
            Utils.page = "contract"

            if(Utils.COUNT_DIALOG == 1 ){
                val builder = AlertDialog.Builder(context)
                builder.setMessage("ANDA BELUM MEMILIKI TRANSAKSI")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> {
                                    dialog.dismiss()
                                    contract.setTextColor(Color.parseColor("#37474F"))
                                    ll_contract.setBackgroundColor(Color.WHITE)
                                }
                            }
                        })
                        .setCancelable(false)
                        .show()
            } else {
                if (Utils.moreThanOne)
                    RxBus.get().send(Utils.CONTRACT)
                else
                    getSinglePayment(Utils.data_transaksi[0], Utils.page)
            }
        }

        ll_about.setOnClickListener {
            about.setTextColor(Color.WHITE)
            ll_about.setBackgroundColor(Color.rgb(163, 197, 255))
            RxBus.get().send(Utils.ABOUT)
        }

        ll_logout.setOnClickListener {
            val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        dialog.dismiss()
                        Utils.isLogin = false
                        sharedPrefManager.spUsername?.let { sharedPrefManager.spPass?.let { it1 -> validateUser(it, it1, "") } }
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        dialog.dismiss()
                        logout.setTextColor(Color.BLACK)
                        ll_logout.setBackgroundColor(ContextCompat.getColor(context!!, R.color.whte))
                    }
                }
            }

            logout.setTextColor(Color.WHITE)
            ll_logout.setBackgroundColor(Color.rgb(163, 197, 255))

            val builder = AlertDialog.Builder(context)
            builder
                .setMessage("Yakin ingin logout ?")
                .setPositiveButton("IYA", dialogClickListener)
                .setNegativeButton("TIDAK", dialogClickListener)
                .setCancelable(false)
                .show()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun initData(){
        Utils.dataProfile = DbHelper.readUser()

        uname.text = "Hallo "+Utils.dataProfile.nama+"!"

        index_lunas = checkLunas(Utils.data_transaksi)

        idtransaksi =  Utils.data_transaksi[index_lunas].idTransaksi
        kodetransaksi =  Utils.data_transaksi[index_lunas].kodetransaksi
        Log.d("inTransaksi", ""+idtransaksi)

        getPayment(idtransaksi, kodetransaksi)
        tenor.text = txt_tenor

        if(Utils.dataProfile.sales == 1){
            icon_vip.visibility = View.VISIBLE
            kontrak_tempo.visibility = View.VISIBLE
        }

       if(Utils.moreThanOne){
           sp_kontrak.visibility = View.VISIBLE
           tv_kontrak.visibility = View.GONE
           handleKontrak()
           getDashboard()
           getKontrakJatuhTempo()
       }
       else{
           val data = Utils.data_transaksi[0]
           icon_vip.visibility = View.INVISIBLE
           sp_kontrak.visibility = View.GONE
           tv_kontrak.visibility = View.VISIBLE
           tv_kontrak.text = data.kodetransaksi

           total_bayar.text = Utils.convertToRp(jum_dibayar.toString())
           total_tagihan.text = Utils.convertToRp(jum_ditagih.toString())
       }

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

                                    tagihan.text = jum_tagihan
                                    denda.text = jum_denda

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
                                    else
                                        RxBus.get().send(Utils.CONTRACT_DETAIL)

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

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    private fun getKontrakJatuhTempo(){
        progressDialog.show()
        Utils.kontrakTempo.clear()

        subscriptions.add(
                provideService().allPayment(Utils.token_device)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ user ->
                            if (user.responsecode == 200) {
                                Utils.data_payment = user.data

                                for (payment in Utils.data_payment) {
                                    if (payment.status.equals("3") || payment.status.equals("0")) {

                                        //jum_ditagih_morethan1 = jum_ditagih_morethan1 + (payment.debitkredit.toInt() + payment.denda.toInt())
                                       // total_tagihan.text = Utils.convertToRp("" + jum_ditagih_morethan1)

                                        val currentDate: LocalDate = LocalDate.now()
                                        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                        val dateTime: LocalDate = LocalDate.parse(payment.jatuhtempo, formatter)
                                        val currentDateMinus7Days: LocalDate = dateTime.minusDays(7)

                                        if (currentDateMinus7Days.isBefore(currentDate)) {
                                            Utils.kontrakTempo.add(KontrakTempoModel(
                                                    payment.idTransaksi,
                                                    ""+nomorKontrak.get(payment.idTransaksi)?.membername,
                                                    "" + nomorKontrak.get(payment.idTransaksi)?.kodetransaksi,
                                                    "Angsuran ke-" + payment.jumlah,
                                                    "" + Utils.convertToRp("" + payment.debitkredit),
                                                    "" + Utils.dateConvert(payment.jatuhtempo)))
                                        }
                                    }
                                }

                                listview.layoutManager = LinearLayoutManager(this.context)
                                Utils.kontrakTempo.sortBy { it.jatuh_tempo }
                                val listAdapter = DashboardAdapter(this.context!!, Utils.kontrakTempo) {
                                    Utils.isDashboard = true
                                    getSinglePayment(nomorKontrak.get(it.id_transaksi)!!, "payment")
                                }
                                listview.adapter = listAdapter
                                progressDialog.dismiss()
                            } else {
                                progressDialog.dismiss()
                                val builder = AlertDialog.Builder(context)
                                builder
                                        .setMessage("Gagal mengambil data jatuh tempo error code : " + user.message)
                                        .setPositiveButton("OK", dialogClickListener)
                                        .setCancelable(false)
                                        .show()
                            }
                        }, { err ->
                            progressDialog.dismiss()
                            val builder = AlertDialog.Builder(context)
                            builder.setMessage("Gagal mengambil data jatuh tempo : " + err.localizedMessage)
                                    .setPositiveButton("OK", dialogClickListener)
                                    .setCancelable(false)
                                    .show()
                        })
        )
    }

     private fun getDashboard(){
         progressDialog.show()

         subscriptions.add(
                 provideService().dashboard(Utils.token_device)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribe({ user ->

                             if (user.responsecode == 200) {
                                 val dataTagihan = user.data
                                 total_tagihan.text = Utils.convertToRp(dataTagihan.totalhutang)
                                 total_bayar.text = Utils.convertToRp(dataTagihan.totaluangmasuk)

                             } else {
                                 val builder = AlertDialog.Builder(context)
                                 builder
                                         .setMessage("Gagal mengambil data jatuh tempo error code : " + user.message)
                                         .setPositiveButton("OK", dialogClickListener)
                                         .setCancelable(false)
                                         .show()
                             }
                             progressDialog.dismiss()
                         }, { err ->
                             progressDialog.dismiss()
                             val builder = AlertDialog.Builder(context)
                             builder.setMessage("Gagal mengambil data jatuh tempo : " + err.localizedMessage)
                                     .setPositiveButton("OK", dialogClickListener)
                                     .setCancelable(false)
                                     .show()
                         })
         )
     }

     private fun checkLunas(trans : MutableList<TransaksiResponseModel>): Int{
         var index = 0
         for(data in trans){
             if(!data.status.equals("Lunas")) {
                 Log.d("lunas-data", ""+index+" "+data.status)
                 break
             }
             else{
                 index++
             }
         }
         return index
     }

    @SuppressLint("CheckResult")
    private fun handleKontrak(){
        val kontrak = Utils.data_transaksi
        val listKontrak = mutableListOf<String>()
        val spinnerKontrak = mutableListOf<TransaksiResponseModel>()

        Utils.data_id_transaksi.clear()
        Utils.kontrak.clear()
        Utils.hash_data_transaksi.clear()

        kontrak.map {
            nomorKontrak.put(it.idTransaksi, it)
            Utils.hash_data_transaksi.put(it.idTransaksi, it)
            Utils.kontrak.add(it.kodetransaksi)
            Utils.data_id_transaksi.add(it.idTransaksi)

            if(!it.status.equals("Lunas")){
                listKontrak.add(it.kodetransaksi)
                spinnerKontrak.add(it)
            }


        }

        val adapterKontrak = ArrayAdapter(
                context!!,
                R.layout.spinner_temp,
                listKontrak
        )
        adapterKontrak.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sp_kontrak.adapter = adapterKontrak
        adapterKontrak.notifyDataSetChanged()

        RxAdapterView.itemSelections(sp_kontrak)
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
                .subscribe({
                    idtransaksi = spinnerKontrak[it].idTransaksi
                    kodetransaksi = spinnerKontrak[it].kodetransaksi
                    getPayment(idtransaksi, kodetransaksi)
                    Log.d("spinner kontrak", "${it+1}-"+idtransaksi+"-"+kodetransaksi)
                }, { /*RxBus.get().send(ToastMsg("error tipe identitas: " + it.stackTrace, 0))*/ })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTransaksi(){
        subscriptions.add(
                provideService().transaksi(Utils.token_device)
                        .retry(3)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ user ->
                            progressDialog.dismiss()

                            if (user.responsecode == 200) {

                                Utils.data_transaksi = user.data

                                if (Utils.data_transaksi.size > 1)
                                    Utils.moreThanOne = true

                                initData()

                            } else {
                                val builder = AlertDialog.Builder(context)
                                builder
                                        .setMessage("Gagal mengambil api transaksi error code : " + user.message)
                                        .setPositiveButton("OK", dialogClickListener)
                                        .setCancelable(false)
                                        .show()
                            }
                        }, { err ->
                            progressDialog.dismiss()
                            if (Utils.data_transaksi.isEmpty()) {

                                if (Utils.dataProfile.sales == 1) {
                                    listview.visibility = View.GONE
                                    not_found.visibility = View.VISIBLE
                                } else {
                                    listview.visibility = View.VISIBLE
                                    not_found.visibility = View.GONE
                                }

                                if (Utils.COUNT_DIALOG == 0) {
                                    val builder = AlertDialog.Builder(context)
                                    builder.setMessage("ANDA BELUM MEMILIKI TRANSAKSI")
                                            .setPositiveButton("OK", dialogClickListener)
                                            .setCancelable(false)
                                            .show()
                                    Utils.COUNT_DIALOG = 1
                                }

                            } else {
                                val builder = AlertDialog.Builder(context)
                                builder.setMessage("Gagal mengambil data transaksi: " + err.localizedMessage)
                                        .setPositiveButton("OK", dialogClickListener)
                                        .setCancelable(false)
                                        .show()
                            }
                        }
                        )
        )
    }

    private fun getPayment(idtrans: Int, nokontrak: String){
        var tenor_lunas = 0
        jum_ditagih = 0
        jum_dibayar = 0
        progressDialog.show()

        subscriptions.add(
                provideService().payment(Utils.token_device, idtrans)
                        .retry(3)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ user ->
                            progressDialog.dismiss()
                            var status = 0

                            if (user.responsecode == 200) {
                                Utils.data_payment = user.data

                                for (payment in Utils.data_payment) {
                                    if (payment.status.equals("3") || payment.status.equals("0")) {
                                        jatuh_tempo.text = Utils.dateConvert(payment.jatuhtempo)
                                        tagihan.text = Utils.convertToRp(payment.debitkredit)
                                        denda.text = Utils.convertToRp(payment.denda)
                                        break
                                    }
                                }
                                for (payment in Utils.data_payment) {
                                    if (payment.status.equals("3")) {
                                        status = 3
                                        break
                                    }
                                }
                                for (payment in Utils.data_payment) {
                                    if (payment.status.equals("4") || payment.status.equals("2")) {
                                        tenor_lunas++
                                        if (!Utils.moreThanOne)
                                            jum_dibayar = jum_dibayar + payment.debitkredit.toInt()
                                    } else {
                                        if (!Utils.moreThanOne)
                                            jum_ditagih = jum_ditagih + (payment.debitkredit.toInt() + payment.denda.toInt())
                                    }
                                }

                                if (tenor_lunas == Utils.data_payment.size && status == 3) {
                                    if (Utils.COUNT_DIALOG_2 == 0) {
                                        val builder = AlertDialog.Builder(context)
                                        builder
                                                .setMessage("Transaksi dengan nomor kontrak " + kodetransaksi + " sedang menunggu konfirmasi pembayaran")
                                                .setPositiveButton("OK", dialogClickListener)
                                                .setCancelable(false)
                                                .show()
                                        Utils.COUNT_DIALOG_2 = 1
                                    }
                                } else {
                                    tenor.text = "" + tenor_lunas + "/" + Utils.data_payment.size + " Bulan"
                                }

                                if (!Utils.moreThanOne) {
                                    total_tagihan.text = Utils.convertToRp("" + jum_ditagih)
                                    total_bayar.text = Utils.convertToRp("" + jum_dibayar)
                                }

                            } else {
                                val builder = AlertDialog.Builder(context)
                                builder
                                        .setMessage("Gagal mengambil data payment : " + user.message)
                                        .setPositiveButton("OK", dialogClickListener)
                                        .setCancelable(false)
                                        .show()
                            }
                        }, { err ->
                            progressDialog.dismiss()
                            val builder = AlertDialog.Builder(context)
                            builder.setMessage("Gagal mengambil data get payment : " + err.localizedMessage)
                                    .setPositiveButton("OK", dialogClickListener)
                                    .setCancelable(false)
                                    .show()
                        })
        )
    }

     //fungsi validasi untuk login
     private fun validateUser(username: String, pass: String, deviceId : String) {
         progressLogout.show()

         val login = LoginModel(username.toUpperCase(Locale.ROOT), pass, "")
                     subscriptions.add(
                             provideLoginService().userLogin(login)
                                     .retry(3)
                                     .observeOn(AndroidSchedulers.mainThread())
                                     .subscribe({ user ->

                                         if (user.responsecode == 201) {
                                             progressLogout.dismiss()
                                             if(!Utils.isLogin) {
                                                 progressLogout.dismiss()
                                                 Utils.moreThanOne = false
                                                 DbHelper.deleteUser()
                                                 sharedPrefManager.saveSPBoolean(SharedPrefManager.SUDAH_LOGIN, false)
                                                 RxBus.get().send(Utils.LOGIN)
                                             }
                                         } else {
                                             logout.setTextColor(Color.parseColor("#37474F"))
                                             ll_logout.setBackgroundColor(Color.WHITE)
                                             val builder = AlertDialog.Builder(context)
                                             builder.setMessage("Gagal logout : " + user.message)
                                                     .setPositiveButton("OK", dialogClickListener)
                                                     .setCancelable(false)
                                                     .show()
                                         }
                                     }, { err ->
                                         progressLogout.dismiss()
                                         logout.setTextColor(Color.parseColor("#37474F"))
                                         ll_logout.setBackgroundColor(Color.WHITE)
                                         val builder = AlertDialog.Builder(context)
                                         builder.setMessage("Gagal logout : " + err.localizedMessage)
                                                 .setPositiveButton("OK", dialogClickListener)
                                                 .setCancelable(false)
                                                 .show()
                                     })
                     )

     }

     //fungsi untuk nembak ke api login
     private fun provideLoginService() : ServiceApi {
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


    //fungsi untuk nembak ke api
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

     private fun getNotif() {
         val bundle = this.activity?.intent?.extras
         if (bundle != null) {
             for (key in bundle.keySet()) {
                 val value = bundle[key]
                 Log.d("MainActivity: ", "Key: $key Value: $value")
             }
         }
     }


}