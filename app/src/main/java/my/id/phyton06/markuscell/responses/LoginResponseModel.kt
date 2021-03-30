package my.id.phyton06.markuscell.responses

/**
 * Created by Phyton06 on 3/20/2021.
 */
data class LoginResponseModel (
    val idPelanggan: Int,
    val username: String,
    val password: String,
    val idDevice: String,
    val nama: String,
    val ttl: String,
    val noktp: String,
    val nonpwp: String,
    val nokk: String,
    val nohp: String,
    val jk: String,
    val statuspernikahan: String,
    val tanggungan: String,
    val alamat: String,
    val statuskepemilikan: String,
    val pekerjaan: String,
    val statuspekerjaan: String,
    val jabatan: String,
    val namaperusahaan: String,
    val alamatperusahaan: String,
    val notelkantor: String,
    val penghasilan: String,
    val waktupenghasilan: String,
    val kontak: String,
    val filektp: String,
    val filekk: String,
    val filenpwp: String,
    val filebpjs: String,
    val filesim: String,
    val created_at: String,
    val updated_at: String,
    val token: String
)