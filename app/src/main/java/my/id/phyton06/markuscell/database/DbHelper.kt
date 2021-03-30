package my.id.phyton06.markuscell.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.responses.LoginResponseModel
import kotlin.jvm.Throws

class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES_USER)
        db.execSQL(SQL_CREATE_ENTRIES_TRANSAKSI)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES_USER)
        db.execSQL(SQL_DELETE_ENTRIES_TRANSAKSI)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    @Throws(SQLiteConstraintException::class)
    fun insertUser(user: LoginResponseModel): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(DBContract.UserEntry.COLUMN_USER_ID, user.idPelanggan)
        values.put(DBContract.UserEntry.COLUMN_USERNAME, user.username)
        values.put(DBContract.UserEntry.COLUMN_PASSWORD, user.password)
        values.put(DBContract.UserEntry.COLUMN_ID_DEVICE, Utils.device_id)
        values.put(DBContract.UserEntry.COLUMN_NAMA, user.nama)
        values.put(DBContract.UserEntry.COLUMN_TTL, user.ttl)
        values.put(DBContract.UserEntry.COLUMN_NO_KTP, user.noktp)
        values.put(DBContract.UserEntry.COLUMN_NO_KK, user.nokk)
        values.put(DBContract.UserEntry.COLUMN_NO_HP, user.nohp)
        values.put(DBContract.UserEntry.COLUMN_NO_NPWP, user.nonpwp)
        values.put(DBContract.UserEntry.COLUMN_JK, user.jk)
        values.put(DBContract.UserEntry.COLUMN_STATUS_PERNIKAHAN, user.statuspernikahan)
        values.put(DBContract.UserEntry.COLUMN_TANGGUNGAN, user.tanggungan)
        values.put(DBContract.UserEntry.COLUMN_ALAMAT, user.alamat)
        values.put(DBContract.UserEntry.COLUMN_STATUS_KEPEMILIKAN, user.statuskepemilikan)
        values.put(DBContract.UserEntry.COLUMN_PEKERJAAN, user.pekerjaan)
        values.put(DBContract.UserEntry.COLUMN_STATUS_PEKERJAAN, user.statuspekerjaan)
        values.put(DBContract.UserEntry.COLUMN_JABATAN, user.jabatan)
        values.put(DBContract.UserEntry.COLUMN_NAMA_PERUSAHAAN, user.namaperusahaan)
        values.put(DBContract.UserEntry.COLUMN_ALAMAT_PERUSAHAAN, user.alamatperusahaan)
        values.put(DBContract.UserEntry.COLUMN_NOTEL_KANTOR, user.notelkantor)
        values.put(DBContract.UserEntry.COLUMN_PENGHASILAN, user.penghasilan)
        values.put(DBContract.UserEntry.COLUMN_WAKTU_PENGHASILAN, user.waktupenghasilan)
        values.put(DBContract.UserEntry.COLUMN_KONTAK, user.kontak)
        values.put(DBContract.UserEntry.COLUMN_FILE_KTP, user.filektp)
        values.put(DBContract.UserEntry.COLUMN_FILE_KK, user.filekk)
        values.put(DBContract.UserEntry.COLUMN_FILE_BPJS, user.filebpjs)
        values.put(DBContract.UserEntry.COLUMN_FILE_NPWP, user.filenpwp)
        values.put(DBContract.UserEntry.COLUMN_FILE_SIM, user.filesim)
        values.put(DBContract.UserEntry.COLUMN_CREATED_AT, user.created_at)
        values.put(DBContract.UserEntry.COLUMN_UPDATED_AT, user.updated_at)
        values.put(DBContract.UserEntry.COLUMN_TOKEN, user.token)
        values.put(DBContract.UserEntry.COLUMN_PHOTO, "-")

        // Insert the new row, returning the primary key value of the new row
        val row = db.insert(DBContract.UserEntry.TABLE_USER, null, values)

        Log.d("insertuser", ""+row)
        return true
    }

    @Throws(SQLiteConstraintException::class)
    fun insertTransaksi(photo:String): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(DBContract.UserEntry.COLUMN_TRANSAKSI_PHOTO, photo)

        // Insert the new row, returning the primary key value of the new row
        val row = db.insert(DBContract.UserEntry.TABLE_TRANSAKSI, null, values)

        Log.d("insert-transaksi", ""+row)
        return true
    }

    fun readTransaksi():String{
        var path_photo = ""
        val db = writableDatabase
        lateinit var cursor: Cursor
        try {
            cursor = db.rawQuery("SELECT photo FROM " + DBContract.UserEntry.TABLE_TRANSAKSI, null)
        } catch (e: SQLiteException) {
            db.execSQL(SQL_CREATE_ENTRIES_TRANSAKSI)
        }
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {

                path_photo = cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_PHOTO))

                cursor.moveToNext()

            }
        }
        return path_photo
    }

    fun updatePhotoTransaksi(path: String): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(DBContract.UserEntry.COLUMN_TRANSAKSI_PHOTO,path)

        // Insert the new row, returning the primary key value of the new row
        val row = db.update(DBContract.UserEntry.TABLE_TRANSAKSI, values, DBContract.UserEntry.COLUMN_TRANSAKSI_ID+" = 1", null)

        Log.d("update-photo", ""+row)
        return true
    }


    @SuppressLint("Recycle")
    fun readUser(): LoginResponseModel {
        lateinit var users : LoginResponseModel
        val db = writableDatabase
        lateinit var cursor: Cursor
        try {
            cursor = db.rawQuery("SELECT * FROM " + DBContract.UserEntry.TABLE_USER, null)
        } catch (e: SQLiteException) {
            db.execSQL(SQL_CREATE_ENTRIES_USER)
        }
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {

                users = LoginResponseModel(
                        Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_USER_ID))),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_USERNAME)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_PASSWORD)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_ID_DEVICE)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_NAMA)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_TTL)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_NO_KTP)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_NO_NPWP)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_NO_KK)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_NO_HP)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_JK)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_STATUS_PERNIKAHAN)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_TANGGUNGAN)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_ALAMAT)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_STATUS_KEPEMILIKAN)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_PEKERJAAN)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_STATUS_PEKERJAAN)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_JABATAN)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_NAMA_PERUSAHAAN)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_ALAMAT_PERUSAHAAN)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_NOTEL_KANTOR)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_PENGHASILAN)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_WAKTU_PENGHASILAN)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_KONTAK)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_FILE_KTP)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_FILE_KK)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_FILE_NPWP)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_FILE_BPJS)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_FILE_SIM)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_CREATED_AT)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_UPDATED_AT)),
                        ""+cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_TOKEN))
                )
                cursor.moveToNext()

            }
        }
        return users
    }

    @Throws(SQLiteConstraintException::class)
    fun updatePhoto(path: String): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(DBContract.UserEntry.COLUMN_PHOTO,path)

        // Insert the new row, returning the primary key value of the new row
        val row = db.update(DBContract.UserEntry.TABLE_USER, values, DBContract.UserEntry.COLUMN_USER_ID+" =  "+Utils.dataProfile.idPelanggan, null)

        Log.d("updatephoto", ""+row)
        return true
    }

    fun getPhoto(): String {
        var path_photo = ""
        val db = writableDatabase
        lateinit var cursor: Cursor
        try {
            cursor = db.rawQuery("SELECT photo FROM " + DBContract.UserEntry.TABLE_USER, null)
        } catch (e: SQLiteException) {
            db.execSQL(SQL_CREATE_ENTRIES_USER)
        }
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {

                path_photo = cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_PHOTO))

                cursor.moveToNext()

            }
        }
        return path_photo
    }


    @Throws(SQLiteConstraintException::class)
    fun deleteUser(): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase

        // Issue SQL statement.
        db.execSQL("DELETE FROM "+DBContract.UserEntry.TABLE_USER)

        return true
    }


    companion object {
        // If you change the database schema, you must increment the database version.
        val DATABASE_VERSION = 4
        val DATABASE_NAME = "MarkusCell.db"

        private val SQL_CREATE_ENTRIES_USER =
            "CREATE TABLE " + DBContract.UserEntry.TABLE_USER + " (" +
                    DBContract.UserEntry.COLUMN_USER_ID + " INTEGER PRIMARY KEY, " +
                    DBContract.UserEntry.COLUMN_USERNAME + " TEXT," +
                    DBContract.UserEntry.COLUMN_PASSWORD + " TEXT," +
                    DBContract.UserEntry.COLUMN_ID_DEVICE  + " TEXT," +
                    DBContract.UserEntry.COLUMN_NAMA  + " TEXT," +
                    DBContract.UserEntry.COLUMN_TTL  + " TEXT," +
                    DBContract.UserEntry.COLUMN_NO_KTP + " TEXT," +
                    DBContract.UserEntry.COLUMN_NO_NPWP  + " TEXT," +
                    DBContract.UserEntry.COLUMN_NO_KK  + " TEXT," +
                    DBContract.UserEntry.COLUMN_NO_HP  + " TEXT," +
                    DBContract.UserEntry.COLUMN_JK  + " TEXT," +
                    DBContract.UserEntry.COLUMN_STATUS_PERNIKAHAN  + " TEXT," +
                    DBContract.UserEntry.COLUMN_TANGGUNGAN  + " TEXT," +
                    DBContract.UserEntry.COLUMN_ALAMAT  + " TEXT," +
                    DBContract.UserEntry.COLUMN_STATUS_KEPEMILIKAN  + " TEXT," +
                    DBContract.UserEntry.COLUMN_PEKERJAAN+ " TEXT," +
                    DBContract.UserEntry.COLUMN_STATUS_PEKERJAAN  + " TEXT," +
                    DBContract.UserEntry.COLUMN_JABATAN  + " TEXT," +
                    DBContract.UserEntry.COLUMN_NAMA_PERUSAHAAN  + " TEXT," +
                    DBContract.UserEntry.COLUMN_ALAMAT_PERUSAHAAN  + " TEXT," +
                    DBContract.UserEntry.COLUMN_NOTEL_KANTOR  + " TEXT," +
                    DBContract.UserEntry.COLUMN_PENGHASILAN  + " TEXT," +
                    DBContract.UserEntry.COLUMN_WAKTU_PENGHASILAN  + " TEXT," +
                    DBContract.UserEntry.COLUMN_KONTAK  + " TEXT," +
                    DBContract.UserEntry.COLUMN_FILE_KTP  + " TEXT," +
                    DBContract.UserEntry.COLUMN_FILE_KK  + " TEXT," +
                    DBContract.UserEntry.COLUMN_FILE_NPWP  + " TEXT," +
                    DBContract.UserEntry.COLUMN_FILE_BPJS  + " TEXT," +
                    DBContract.UserEntry.COLUMN_FILE_SIM  + " TEXT," +
                    DBContract.UserEntry.COLUMN_CREATED_AT  + " TEXT," +
                    DBContract.UserEntry.COLUMN_UPDATED_AT  + " TEXT," +
                    DBContract.UserEntry.COLUMN_TOKEN  + " TEXT," +
                    DBContract.UserEntry.COLUMN_PHOTO  + " TEXT)"

        private val SQL_CREATE_ENTRIES_TRANSAKSI =
            "CREATE TABLE " + DBContract.UserEntry.TABLE_TRANSAKSI + " (" +
                    DBContract.UserEntry.COLUMN_TRANSAKSI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DBContract.UserEntry.COLUMN_TRANSAKSI_PHOTO + " TEXT)"

        private val SQL_DELETE_ENTRIES_USER = "DROP TABLE IF EXISTS " + DBContract.UserEntry.TABLE_USER
        private val SQL_DELETE_ENTRIES_TRANSAKSI = "DROP TABLE IF EXISTS " + DBContract.UserEntry.TABLE_TRANSAKSI
    }


}