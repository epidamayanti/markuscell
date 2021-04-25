package my.id.phyton06.markuscell.commons

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_dashboard.*
import my.id.phyton06.markuscell.R
import my.id.phyton06.markuscell.models.*
import my.id.phyton06.markuscell.responses.LoginResponseModel
import my.id.phyton06.markuscell.responses.PaymentResponseModel
import my.id.phyton06.markuscell.responses.TransaksiResponseModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class Utils {

    companion object {

        //public
        val ENDPOINT = "https://markuscell.id/api/"

        //services
        const val LOGIN_ENDPOINT ="login"
        const val TRANSAKSI_ENDPOINT ="transaksi"
        const val PAYMENT_ENDPOINT ="payment"
        const val UPLOAD_ENDPOINT ="payment"
        const val NOTIF_ENDPOINT ="notif"
        const val ALL_PAYMENT = "payment/me"
        const val TOTAL_TAGIHAN = "dashboard"

        var mFragment: Fragment? = null
        var mActivity: Activity? = null

        //var pict
        var pick_gallery = 0
        var arrayImageData: ImageData? = null
        var arrayImage = ""
        var imageName = ""
        var arrayImagePath = ""
        lateinit var imageUri:Uri

        //fragment
        val LOGIN = "login"
        val DASHBOARD = "dashboard"
        val PROFILE = "profile"
        val NOTIF = "notif"
        val PAYMENT = "payment"
        val PAYMENT_INFO = "payment info"
        val CONTRACT = "contract"
        val CONTRACT_DETAIL = "contract_detail"
        val ABOUT = "about"

        //statement
        var isLogin = false

        //variabel login
        var username = ""
        var password = ""
        var token_device = ""
        var device_id= ""
        var after_login = false

        //variabel dashboard
        var kontrak = mutableListOf<String>()
        var data_transaksi : MutableList<TransaksiResponseModel> = mutableListOf<TransaksiResponseModel>()
        var hash_data_payment : HashMap<Int,  MutableList<PaymentResponseModel>> = hashMapOf()
        var hash_data_transaksi : HashMap<Int,  TransaksiResponseModel> = hashMapOf()
        var data_id_transaksi : MutableList<Int> = mutableListOf()
        var data_payment : MutableList<PaymentResponseModel> = mutableListOf<PaymentResponseModel>()
        var data_payment_history:MutableList<RiwayatPembayaranModel> = mutableListOf()
        var moreThanOne = false
        var page = ""
        var isDashboard = false
        lateinit var transaksi : TransaksiResponseModel
        var COUNT_DIALOG = 0
        var COUNT_DIALOG_2 = 0
        var COUNT_DIALOG_3 = 0
        var kontrakTempo : MutableList<KontrakTempoModel> = mutableListOf()
        var TRANSAKSI_COUNT = 0
        var tagihan = ""
        var tempo = ""
        var tenor = ""
        var denda = ""

        //variabel profile
        lateinit var dataProfile : LoginResponseModel
        var nama = ""
        var bod = ""
        var no_telp = ""
        var alamat = ""

        //variabel payment
        var tagihan_id = ""
        var tagihan_payment = ""
        var tagihan_tenor = ""
        var tagihan_telah_dibayar = ""
        var tagihan_sisa_cicilan = ""
        var tagihan_nomor_kontrak = ""
        var tagihan_jumlah_angsuran = ""
        var tagihan_tempo = ""
        var tagihan_denda = ""
        var riwayat_bayar: MutableList<RiwayatPembayaranModel> = mutableListOf<RiwayatPembayaranModel>()

        //variabel contract
        lateinit var detailContract : ContractModel
        var contract_no = ""
        var contract_peminjaman = ""
        var contract_tenor = ""
        var contract_angsuran = ""
        var contract_tempo = ""
        var contract_produk_nama = ""
        var contract_produk_harga = ""
        var contract_produk_jumlah = ""

        //variabel upload bukti tf
        var image = ""

        //variabel notifikasi
        lateinit var notif : NotificationModel

        fun progressDialog(context: Context, activity: Activity): Dialog {
            val dialog = Dialog(context)
            val inflate = LayoutInflater.from(context).inflate(R.layout.loading_alert, activity.findViewById<ViewGroup>(R.id.custom_toast_layout))
            dialog.setContentView(inflate)
            dialog.setCancelable(false)
            dialog.window!!.setBackgroundDrawable(
                    ColorDrawable(Color.TRANSPARENT)
            )
            return dialog
        }

        fun progressLogout(context: Context, activity: Activity): Dialog {
            val dialog = Dialog(context)
            val inflate = LayoutInflater.from(context).inflate(R.layout.logout_alert, activity.findViewById<ViewGroup>(R.id.custom_toast_layout))
            dialog.setContentView(inflate)
            dialog.setCancelable(false)
            dialog.window!!.setBackgroundDrawable(
                    ColorDrawable(Color.TRANSPARENT)
            )
            return dialog
        }

        fun sendDialog(context: Context, activity: Activity): Dialog {
            val dialog = Dialog(context)
            val inflate = LayoutInflater.from(context).inflate(R.layout.send_alert, activity.findViewById<ViewGroup>(R.id.custom_toast_layout))
            dialog.setContentView(inflate)
            dialog.setCancelable(false)
            dialog.window!!.setBackgroundDrawable(
                    ColorDrawable(Color.TRANSPARENT)
            )
            return dialog
        }

        @SuppressLint("SimpleDateFormat")
        fun dateConvert(tgl : String):String{
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date: Date = formatter.parse(tgl)
            val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
            var dateFormat =""
            try{
                dateFormat = dateFormatter.format(date)
            } catch (e : Exception){
                dateFormat = ""
            } catch (e : NullPointerException ) {
                dateFormat = ""
            }
            return dateFormat
        }

        fun dateConvert2(tgl : String):String{
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date: Date = formatter.parse(tgl)
            val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            var dateFormat =""
            try{
                dateFormat = dateFormatter.format(date)
            } catch (e : Exception){
                dateFormat = ""
            } catch (e : NullPointerException ) {
                dateFormat = ""
            }
            return dateFormat
        }

        fun convertToRp(nominal : String): String{
            val nf: NumberFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
            var RpFormat =""
            try{
                RpFormat = "Rp. "+ nf.format(nominal.toDouble())
            } catch (e : Exception){
                RpFormat = ""
            } catch (e : NullPointerException ) {
                RpFormat = ""
            }
            return RpFormat
        }

        fun convertFromJson(tag : String, json:String): String{
            val convertJson1 = JSONObject(json)
            val data =  convertJson1.getString("0")
            val convertJson2 = JSONObject(data)
            return convertJson2.getString(tag)
        }



        // fungsi retrofit
        fun buildClient(): OkHttpClient.Builder {
            val clientBuilder = OkHttpClient.Builder()
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            clientBuilder.addInterceptor(loggingInterceptor)
                .connectTimeout(5 , TimeUnit.MINUTES)
            return clientBuilder
        }
    }
}