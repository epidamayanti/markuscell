package my.id.phyton06.markuscell.commons

import android.graphics.Bitmap
import android.os.Environment
import android.util.Base64
import android.util.Log
import my.id.phyton06.markuscell.commons.LocBaseActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.DateFormat
import java.util.*

@Suppress("DEPRECATION")
open class BaseActivity: LocBaseActivity(){

    val dir = "/Pictures/MarkusCell/"

    fun checkDir(){
        val imgDir = File(Environment.getExternalStorageDirectory(), dir)
        if (!imgDir.exists()) {
            imgDir.mkdirs() // <----

        }
    }

    fun getStringImage(bmp: Bitmap):String {
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos)
        val imageBytes = baos.toByteArray()
        val encodedImage  = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        return encodedImage
    }
}