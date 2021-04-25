package my.id.phyton06.markuscell.database

import android.provider.BaseColumns

object DBContract {
    class UserEntry : BaseColumns {
        companion object {
            val TABLE_USER = "users"
            val COLUMN_USER_ID = "idPelanggan"
            val COLUMN_USERNAME = "username"
            val COLUMN_PASSWORD ="password"
            val COLUMN_ID_DEVICE = "idDevice"
            val COLUMN_NAMA = "nama"
            val COLUMN_TTL = "ttl"
            val COLUMN_NO_KTP ="noktp"
            val COLUMN_NO_NPWP = "nonpwp"
            val COLUMN_NO_KK = "nokk"
            val COLUMN_NO_HP = "nohp"
            val COLUMN_JK = "jk"
            val COLUMN_STATUS_PERNIKAHAN = "statuspernikahan"
            val COLUMN_TANGGUNGAN = "tanggungan"
            val COLUMN_ALAMAT = "alamat"
            val COLUMN_STATUS_KEPEMILIKAN = "statuskepemilikan"
            val COLUMN_PEKERJAAN ="pekerjaan"
            val COLUMN_STATUS_PEKERJAAN = "statuspekerjaan"
            val COLUMN_JABATAN = "jabatan"
            val COLUMN_NAMA_PERUSAHAAN = "namaperusahaan"
            val COLUMN_ALAMAT_PERUSAHAAN = "alamatperusahaan"
            val COLUMN_NOTEL_KANTOR = "notelkantor"
            val COLUMN_PENGHASILAN = "penghasilan"
            val COLUMN_WAKTU_PENGHASILAN = "waktupenghasilan"
            val COLUMN_KONTAK = "kontak"
            val COLUMN_FILE_KTP = "filektp"
            val COLUMN_FILE_KK = "filekk"
            val COLUMN_FILE_NPWP = "filenpwp"
            val COLUMN_FILE_BPJS = "filebpjs"
            val COLUMN_FILE_SIM = "filesim"
            val COLUMN_CREATED_AT = "created_at"
            val COLUMN_UPDATED_AT = "updated_at"
            val COLUMN_TOKEN = "token"
            val COLUMN_SALES = "sales"
            val COLUMN_PHOTO = "photo"

            val TABLE_TRANSAKSI = "transaksi"
            val COLUMN_TRANSAKSI_ID = "idTransaksi"
            val COLUMN_TRANSAKSI_PHOTO = "photo"



        }
    }
}
