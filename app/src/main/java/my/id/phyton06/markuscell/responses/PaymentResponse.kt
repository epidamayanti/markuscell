package my.id.phyton06.markuscell.responses

/**
 * Created by Phyton06 on 3/20/2021.
 */
data class PaymentResponse (
    val responsecode:Int,
    val message: String,
    val data:MutableList<PaymentResponseModel>
)