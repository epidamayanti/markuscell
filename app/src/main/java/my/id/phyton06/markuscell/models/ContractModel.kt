package my.id.phyton06.markuscell.models

/**
 * Created by Phyton06 on 3/20/2021.
 */
data class ContractModel (
        val idPayment:Int,
        val idTransaksi: Int,
        val idPelanggan: String,
        val idBarang: String,
        val kodetransaksi: String,
        val total: String,
        val modal: String,
        val jual: String,
        val dp: String,
        val jangkawaktu: String,
        val cicilan: String,
        val jatuhtempo:String,
        val tenor : String,
        val jumlahtenor : String,
        val komisi: String,
        val komisiperbulan: String,
        val jumlahdenda: String,
        val membername: String,
        val memberhp: String,
        val status : Int
)