package my.id.phyton06.markuscell.models

data class RiwayatPembayaranModel (
    val cicilan:String,
    val no_angsuran:String,
    val denda :String,
    val tgl_pembayaran:String,
    val tgl_jatuh_tempo:String,
    val status: String
)