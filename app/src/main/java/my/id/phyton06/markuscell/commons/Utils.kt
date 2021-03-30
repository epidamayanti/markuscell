package my.id.phyton06.markuscell.commons

import android.app.Activity
import android.net.Uri
import androidx.fragment.app.Fragment
import my.id.phyton06.markuscell.models.ImageData
import my.id.phyton06.markuscell.models.NotificationModel
import my.id.phyton06.markuscell.models.RiwayatPembayaranModel
import my.id.phyton06.markuscell.responses.LoginResponseModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class Utils {

    companion object {


        //public
        val ENDPOINT = "https://markuscell.id/api/"

        //services
        const val LOGIN_ENDPOINT ="login"
        const val TRANSAKSI_ENDPOINT ="transaksi"

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

        //variabel dashboard
        var kontrak = mutableListOf<String>()
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
        var tagihan_kontrak = ""
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