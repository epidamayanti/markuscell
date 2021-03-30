package my.id.phyton06.markuscell.services

import io.reactivex.Observable
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.models.LoginModel
import my.id.phyton06.markuscell.responses.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by Phyton06 on 3/20/2021.
 */
interface ServiceApi {

    @POST(Utils.LOGIN_ENDPOINT)
    fun userLogin(@Body login: LoginModel): Observable<LoginResponse>


}