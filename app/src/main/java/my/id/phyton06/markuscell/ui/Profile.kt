package my.id.phyton06.markuscell.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.fragment_contract_detail.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.nama
import kotlinx.android.synthetic.main.fragment_profile.toolbar
import my.id.phyton06.markuscell.R
import my.id.phyton06.markuscell.commons.RxBaseFragment
import my.id.phyton06.markuscell.commons.RxBus
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.database.DbHelper
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Profile : RxBaseFragment() {

    private val GALLERY = 1
    private val CAMERA = 2
    private var currentPath: String? = null
    lateinit var DbHelper : DbHelper
    var path = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener {
            RxBus.get().send(Utils.DASHBOARD)
        }

        DbHelper = DbHelper(this.requireContext())

        ll_change.setOnClickListener {
            showPictureDialog()

        }
        ll_save_photo.setOnClickListener {
            ll_change.visibility = View.VISIBLE
            ll_save_photo.visibility = View.GONE

            DbHelper.updatePhoto(path)
            Toast.makeText(context, "Berhasil tersimpan", Toast.LENGTH_LONG).show()
        }

        if(!DbHelper.getPhoto().equals("-")){
            val myBitmap = BitmapFactory.decodeFile(DbHelper.getPhoto())
            photo.setImageBitmap(myBitmap)
        }

        initData()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    private fun initData(){
        nama.text = Utils.dataProfile.nama
        jk.text = Utils.dataProfile.jk
        bod.text = if (Utils.dataProfile.ttl.equals("null")) "-" else Utils.dataProfile.ttl
        no_telp.text = if (Utils.dataProfile.nohp.equals("null")) "-" else Utils.dataProfile.nohp
        alamat.text = if (Utils.dataProfile.alamat.equals("null")) "-" else Utils.dataProfile.alamat

        username.setText(Utils.dataProfile.username)
        pass.setText(Utils.dataProfile.password)
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
        ll_save_photo.visibility = View.VISIBLE
        ll_change.visibility = View.GONE

        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        ll_save_photo.visibility = View.VISIBLE
        ll_change.visibility = View.GONE

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(context!!.packageManager) != null){
            var photoFile: File? = null
            try {
                photoFile = createImage()
            }catch (e: IOException){
                e.printStackTrace()
            }
            if (photoFile != null){
                val photoUri =  FileProvider.getUriForFile(
                    context!!,
                    "my.id.phyton06.markuscell.provider",
                    photoFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, CAMERA)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private  fun createImage(): File {
        val imageDate =  SimpleDateFormat("ddMMyyHHmmss").format(Date())
        val imageName = Utils.imageName+"_"+imageDate
        val storageDir = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageName, ".jpg", storageDir)
        currentPath = image.absolutePath
        return image
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA && resultCode == Activity.RESULT_OK){
            try {
                val file = File(currentPath)
                val uri = Uri.fromFile(file)
                path = currentPath.toString()

                photo.setImageURI(uri)
            }catch (e: IOException){
                e.printStackTrace()
            }
        }

        if (requestCode == GALLERY && resultCode == Activity.RESULT_OK){
            try {
                val uri = data!!.data!!
                val imageDate =  SimpleDateFormat("ddMMyyHHmmss").format(Date())
                val imageName = Utils.imageName+"_"+imageDate

                val myFile = File(getRealPathFromURI(uri).toString())
                path = File(getRealPathFromURI(uri).toString()).toString()
                //val myBitmap = BitmapFactory.decodeFile(myFile.getAbsolutePath())

                photo.setImageURI(uri)
            }catch (e: IOException){
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

}