package my.id.phyton06.markuscell.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_seen_photo.view.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_payment_info.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.photo
import my.id.phyton06.markuscell.BuildConfig
import my.id.phyton06.markuscell.R
import my.id.phyton06.markuscell.commons.RxBaseFragment
import my.id.phyton06.markuscell.commons.RxBus
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.database.DbHelper
import my.id.phyton06.markuscell.models.UploadModel
import my.id.phyton06.markuscell.services.ServiceApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PaymentInfo : RxBaseFragment() {

    private val GALLERY = 1
    private val CAMERA = 2
    private var currentPath: String? = null
    private lateinit var uri: Uri
    lateinit var DbHelper : DbHelper
    var path = ""
    private var bpImg = ""

    private lateinit var progressDialog: Dialog
    private val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                dialog.dismiss()
                RxBus.get().send(Utils.DASHBOARD)
            }
            DialogInterface.BUTTON_NEGATIVE -> {
                dialog.dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DbHelper = DbHelper(this.requireContext())
        progressDialog = Utils.sendDialog(context!!, activity!!)

        nominal.text = Utils.convertToRp(Utils.tagihan_payment)

        bukti_tf.setOnClickListener {
            showPictureDialog()
        }

        copy.setOnClickListener {
            val clipboard =
                context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", norek.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Berhasil tersalin", Toast.LENGTH_SHORT).show()
        }
        copy_nom.setOnClickListener {
            val clipboard =
                context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", Utils.tagihan_payment.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Berhasil tersalin", Toast.LENGTH_SHORT).show()
        }
        seen.setOnClickListener {
            val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_seen_photo, null)
            val mBuilder = AlertDialog.Builder(context)
                .setView(mDialogView)

            val  mAlertDialog = mBuilder.setCancelable(false).show()

            mDialogView.imageView.setImageURI(uri)

            mDialogView.btn_close.setOnClickListener {
                mAlertDialog.dismiss()
            }
        }

        try {
            if (!DbHelper.readTransaksi().isEmpty()) {
                val myBitmap = BitmapFactory.decodeFile(DbHelper.readTransaksi())
                photo.setImageBitmap(myBitmap)
            }
        } catch (e : RuntimeException){
            val builder = AlertDialog.Builder(context)
            builder.setMessage("ERROR "+e.localizedMessage)
                    .setNegativeButton("OK", DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                dialog.dismiss()
                            }
                            DialogInterface.BUTTON_NEGATIVE -> {
                                dialog.dismiss()
                            }
                        }
                    })
                    .setCancelable(false)
                    .show()
        }

        sendTf.setOnClickListener {
            progressDialog.show()
            if(bpImg.isEmpty()){
                progressDialog.dismiss()
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Gambar tidak boleh kosong")
                        .setNegativeButton("OK", DialogInterface.OnClickListener { dialog, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> {
                                    dialog.dismiss()
                                }
                                DialogInterface.BUTTON_NEGATIVE -> {
                                    dialog.dismiss()
                                }
                            }
                        })
                        .setCancelable(false)
                        .show()
            }
            else{
                uploadImage(Utils.tagihan_id, bpImg)
            }
        }
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(context)
        val pictureDialogItems = arrayOf("Gallery", "Kamera")
        pictureDialog.setItems(pictureDialogItems)
        { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallery() {
        seen.visibility = View.VISIBLE
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        seen.visibility = View.VISIBLE
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(context!!.packageManager) != null){
            var photoFile: File? = null
            try {
                photoFile = createImage()
                Utils.arrayImage = photoFile.toString()
                Utils.arrayImageData?.img_file = photoFile.toString()
                Log.d("take pict", "" + photoFile.toString())
            }catch (e: IOException){
                e.printStackTrace()
            }
            if (photoFile != null){
                val photoUri =  FileProvider.getUriForFile(
                    context!!,
                        BuildConfig.APPLICATION_ID + ".provider",
                    photoFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, CAMERA)
            }
        }
    }


    @SuppressLint("SimpleDateFormat")
    private  fun createImage(): File{
        val imageDate =  SimpleDateFormat("ddMMyyHHmmss").format(Date())
        val imageName = "MCKAPP"+"_"+imageDate
        val storageDir = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageName, ".jpg", storageDir)
        currentPath = image.absolutePath
        return image
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA && resultCode == Activity.RESULT_OK){
            try {
                val myBitmap = BitmapFactory.decodeFile(currentPath)
                bpImg = "data:image/jpeg;base64," + getStringImage(myBitmap)
                bukti_tf.setImageBitmap(myBitmap)
            }catch (e: IOException){
                e.printStackTrace()
            }
        }

        if (requestCode == GALLERY && resultCode == Activity.RESULT_OK){
            try {
                uri = data!!.data!!
                path = File(getRealPathFromURI(uri).toString()).toString()

                val myBitmap = BitmapFactory.decodeFile(path)
                bpImg = "data:image/jpeg;base64," + getStringImage(myBitmap)

                bukti_tf.setImageURI(uri)
            } catch (e: IOException){
                e.printStackTrace()
            }
        }
    }

    fun getRealPathFromURI(contentUri: Uri?): String? {
        var path: String? = null
        val proj = arrayOf(MediaStore.MediaColumns.DATA)
        val cursor: Cursor? =
            contentUri?.let { context?.getContentResolver()?.query(it, proj, null, null, null) }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                path = cursor.getString(columnIndex)
            }
        }
        cursor?.close()
        return path
    }

    private fun getStringImage(bmp: Bitmap):String {
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 10, baos)
        val imageBytes = baos.toByteArray()
        val encodedImage  = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        return encodedImage
    }

    private fun uploadImage(idPayment: String, image:String){
        val dataUpload = UploadModel(idPayment.toInt(), image)
        subscriptions.add(
                provideService().upload(Utils.token_device, dataUpload)
                        .retry(3)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ user ->
                            progressDialog.dismiss()

                            if (user.responsecode == 200) {
                                val builder = AlertDialog.Builder(context)
                                builder
                                        .setMessage("" + user.message)
                                        .setPositiveButton("OK", dialogClickListener)
                                        .setCancelable(false)
                                        .show()
                            } else {
                                val builder = AlertDialog.Builder(context)
                                builder
                                        .setMessage("Gagal mengupload data error code : " + user.message)
                                        .setNegativeButton("OK", dialogClickListener)
                                        .setCancelable(false)
                                        .show()
                            }

                        }, { err ->
                            progressDialog.dismiss()
                            val builder = AlertDialog.Builder(context)
                            builder.setMessage("Gagal mengupload data : " + err.localizedMessage)
                                    .setNegativeButton("OK", dialogClickListener)
                                    .setCancelable(false)
                                    .show()
                        })
        )
    }

    private fun provideService() : ServiceApi {
        val clientBuilder : OkHttpClient.Builder = Utils.buildClient()
        val retrofit = Retrofit.Builder()
                .baseUrl(Utils.ENDPOINT)
                .client(
                        clientBuilder
                                .connectTimeout(10, TimeUnit.SECONDS)
                                .writeTimeout(10, TimeUnit.SECONDS)
                                .readTimeout(10, TimeUnit.SECONDS)
                                .build()
                )
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
        return retrofit.create(ServiceApi::class.java)
    }
}