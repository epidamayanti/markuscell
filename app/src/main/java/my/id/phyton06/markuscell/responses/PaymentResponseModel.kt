package my.id.phyton06.markuscell.responses

/**
 * Created by Phyton06 on 3/20/2021.
 */
data class PaymentResponseModel (
    val idPayment: Int,
    val idTransaksi: Int,
    val jumlah: String,
    val status: String,
    val jatuhtempo: String,
    val denda: String,
    val metode: String,
    val debitkredit: String,
    val buktitransfer: String,
    val created_at: String,
    val updated_at: String,
    val tanggalbayar: String
)