package my.id.phyton06.markuscell.models

/**
 * Created by Phyton06 on 3/11/2021.
 */
data class NotificationModel (
        val idNotif:Int,
        val idPelanggan:String,
        val to:String,
        val type:String,
        val message: String,
        val created_at: String,
        val updated_at:String
)