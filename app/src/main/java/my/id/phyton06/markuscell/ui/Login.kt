@file:Suppress("DEPRECATION")

package my.id.phyton06.markuscell.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_login.*
import my.id.phyton06.markuscell.R
import my.id.phyton06.markuscell.commons.RxBaseFragment
import my.id.phyton06.markuscell.commons.RxBus
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.database.DbHelper
import my.id.phyton06.markuscell.models.LoginModel
import my.id.phyton06.markuscell.models.SharedPrefManager
import my.id.phyton06.markuscell.responses.LoginResponseModel
import my.id.phyton06.markuscell.services.ServiceApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class Login : RxBaseFragment() {

    lateinit var DbHelper : DbHelper
    private lateinit var progressDialog: ProgressDialog
    private lateinit var sharedPrefManager : SharedPrefManager
    private val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                dialog.dismiss()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = ProgressDialog(context, R.style.Theme_MaterialComponents_Light_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Loading...")

        sharedPrefManager = SharedPrefManager(context!!)
        DbHelper = DbHelper(this.requireContext())

        sendTf.setOnClickListener {
            val username = inputUsername.text.toString()
            val password = inputPassword.text.toString()

            progressDialog.show()

            if (username.isEmpty()|| password.isEmpty()) {
                progressDialog.dismiss()
                Toast.makeText(context, "Please Insert Email and Password", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            else{
                //validateUser(username, password)
                val data = LoginResponseModel(1,"dina","570612","","Dina","jkt, 13 12 1996",
                "","","","08122232778","","","","jl. aaaaaaaaaaaaaaaaaaa",
                    "","","","","","","",
                "","","","","","","","","","",
                    "123wrwwrerwrwrrr")
                DbHelper.insertUser(data)
                Utils.isLogin = true
                sharedPrefManager.saveSPBoolean(SharedPrefManager.SUDAH_LOGIN, true)
                sharedPrefManager.saveSPString(SharedPrefManager.USERNAME, username)
                sharedPrefManager.saveSPString(SharedPrefManager.token, Utils.token_device)
                RxBus.get().send(Utils.DASHBOARD)
                progressDialog.dismiss()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    //fungsi validasi untuk login
    private fun validateUser(username: String, pass: String) {
        val login = LoginModel(username.toUpperCase(Locale.ROOT), pass, Utils.device_id)

        subscriptions.add(
            provideLoginService().userLogin(login)
                .retry(3)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ user ->
                    if (user.responsecode == 201) {
                        progressDialog.dismiss()
                        DbHelper.insertUser(user.data)
                        Utils.isLogin = true
                        sharedPrefManager.saveSPBoolean(SharedPrefManager.SUDAH_LOGIN, true)
                        sharedPrefManager.saveSPString(SharedPrefManager.USERNAME, username)
                        sharedPrefManager.saveSPString(SharedPrefManager.token, Utils.token_device)
                        RxBus.get().send(Utils.DASHBOARD)
                    } else {
                        val builder = AlertDialog.Builder(context)
                        builder
                            .setMessage("Gagal login : " + user.message)
                            .setPositiveButton("OK", dialogClickListener)
                            .setCancelable(false)
                            .show()
                    }
                }, { err ->
                    progressDialog.dismiss()
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("Gagal login : " + err.localizedMessage)
                        .setPositiveButton("OK", dialogClickListener)
                        .setCancelable(false)
                        .show()
                })
        )
    }

    //fungsi untuk nembak ke api login
    private fun provideLoginService() : ServiceApi {
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
