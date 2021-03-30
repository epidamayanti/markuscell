package my.id.phyton06.markuscell.models

/**
 * Created by Phyton06 on 3/11/2021.
 */
data class NotificationModel (
        val id:Int,
        val title:String,
        val date:String,
        val content:String,
        val status: Boolean
)