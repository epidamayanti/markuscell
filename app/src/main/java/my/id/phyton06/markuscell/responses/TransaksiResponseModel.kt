package my.id.phyton06.markuscell.responses

/**
 * Created by Phyton06 on 3/20/2021.
 */
data class TransaksiResponseModel (
        val idTransaksi: Int,
        val idPelanggan: String,
        val idBarang: String,
        val kodetransaksi: String,
        val total: String,
        val modal: String,
        val jual: String,
        val dp: String,
        var jangkawaktu: String,
        val cicilan: String,
        val komisi: String,
        val komisiperbulan: String,
        val jumlahdenda: String,
        val created_at: String,
        val updated_at: String,
        val membername: String,
        val memberhp: String,
        val status : String
)