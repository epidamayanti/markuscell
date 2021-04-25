package my.id.phyton06.markuscell.services

import io.reactivex.Observable
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.models.LoginModel
import my.id.phyton06.markuscell.models.NotificationResModel
import my.id.phyton06.markuscell.models.UploadModel
import my.id.phyton06.markuscell.responses.DashboardResponse
import my.id.phyton06.markuscell.responses.LoginResponse
import my.id.phyton06.markuscell.responses.PaymentResponse
import my.id.phyton06.markuscell.responses.TransaksiResponse
import retrofit2.http.*

/**
 * Created by Phyton06 on 3/20/2021.
 */
interface ServiceApi {

    @POST(Utils.LOGIN_ENDPOINT)
    fun userLogin(@Body login: LoginModel): Observable<LoginResponse>

    @GET(Utils.TRANSAKSI_ENDPOINT)
    fun transaksi(@Header ("token") token: String): Observable<TransaksiResponse>

    @GET(Utils.PAYMENT_ENDPOINT)
    fun payment(@Header ("token") token: String, @Query ("idtrans") idtrans:Int): Observable<PaymentResponse>

    @POST(Utils.PAYMENT_ENDPOINT)
    fun upload(@Header ("token") token: String, @Body upload: UploadModel): Observable<PaymentResponse>

    @GET(Utils.NOTIF_ENDPOINT)
    fun notif(@Header ("token") token: String): Observable<NotificationResModel>

    @GET(Utils.ALL_PAYMENT)
    fun allPayment(@Header ("token") token: String): Observable<PaymentResponse>

    @GET(Utils.TOTAL_TAGIHAN)
    fun dashboard(@Header ("token") token: String): Observable<DashboardResponse>


}