package my.id.phyton06.markuscell.models

import my.id.phyton06.markuscell.responses.LoginResponseModel

/**
 * Created by Phyton06 on 3/11/2021.
 */
data class NotificationResModel (
        val responsecode:Int,
        val message: String,
        val data: MutableList<NotificationModel>
)