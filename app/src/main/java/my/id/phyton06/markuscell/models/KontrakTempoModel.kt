package my.id.phyton06.markuscell.models

data class KontrakTempoModel (
    val id_transaksi:Int,
    val nama_member:String,
    val no_kontrak:String,
    val no_angsuran:String,
    val cicilan:String,
    val jatuh_tempo:String
)