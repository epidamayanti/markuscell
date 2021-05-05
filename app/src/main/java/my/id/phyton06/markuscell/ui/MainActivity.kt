package my.id.phyton06.markuscell.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import my.id.phyton06.markuscell.R
import my.id.phyton06.markuscell.commons.BaseActivity
import my.id.phyton06.markuscell.commons.RxBus
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.database.DbHelper
import my.id.phyton06.markuscell.models.SharedPrefManager
import rebus.permissionutils.PermissionEnum
import rebus.permissionutils.PermissionManager
import java.io.File


class MainActivity : BaseActivity() {

    private lateinit var sharedPrefManager : SharedPrefManager
    private val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                dialog.dismiss()
                finish()
            }

            DialogInterface.BUTTON_NEGATIVE -> {
                dialog.dismiss()

            }
        }
    }

    private fun getNotif() {
        val bundle = intent.extras
        if (intent.extras != null) {
            for (key in intent.extras!!.keySet()) {
                val value = intent.extras!![key]
                Log.d("Notif : ", "Key: $key Value: $value")
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this,
                OnSuccessListener<InstanceIdResult> { instanceIdResult ->
                    val newToken = instanceIdResult.token
                    Log.d("Token", newToken)
                    Utils.device_id = newToken
                })

        sharedPrefManager = SharedPrefManager(this)
        val dbHelper = DbHelper(this)
        try {
            val FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator
            var process = Runtime.getRuntime().exec("logcat -d")
            process = Runtime.getRuntime().exec("logcat -f " + FOLDER + "MCK_Log_Error.txt")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        getNotif()
        if (savedInstanceState == null) {
            manageSubscription()
            Utils.mActivity = this
            //changeFragment(Login(), false, Utils.LOGIN)
            if (sharedPrefManager.spSudahLogin!!) {
                Utils.isLogin = true
                Utils.token_device = sharedPrefManager.spToken.toString()
                changeFragment(Dashboard(), false, Utils.DASHBOARD)
            } else {
                Utils.isLogin = false
                changeFragment(Login(), false, Utils.LOGIN)
            }
        }
        permission()


    }

    // fungsi untuk subcriber event
    private fun manageSubscription() {
        subscriptions.add(
                RxBus.get().toObservable().subscribe(
                        { event -> manageBus(event) },
                        { Toast.makeText(this, "Timeout", Toast.LENGTH_SHORT).show() })
        )
    }

    private fun manageBus(event: Any) {
        when (event) {
            Utils.LOGIN -> changeFragment(Login(), false, Utils.LOGIN)
            Utils.DASHBOARD -> changeFragment(Dashboard(), false, Utils.DASHBOARD)
            Utils.PROFILE -> changeFragment(Profile(), false, Utils.PROFILE)
            Utils.NOTIF -> changeFragment(Notifikasi(), false, Utils.NOTIF)
            Utils.PAYMENT -> changeFragment(Payment(), false, Utils.PAYMENT)
            Utils.PAYMENT_INFO -> changeFragment(PaymentInfo(), false, Utils.PAYMENT_INFO)
            Utils.CONTRACT -> changeFragment(Contract(), false, Utils.CONTRACT)
            Utils.CONTRACT_DETAIL -> changeFragment(ContractDetail(), false, Utils.CONTRACT_DETAIL)
            Utils.CONTRACT_DUE_DATE -> changeFragment(ContractDueDate(), false, Utils.CONTRACT_DUE_DATE)
            Utils.ABOUT -> changeFragment(About(), false, Utils.ABOUT)
        }
    }

    fun permission(){
        PermissionManager.Builder()
            .permission(
                    PermissionEnum.WRITE_EXTERNAL_STORAGE,
                    PermissionEnum.ACCESS_FINE_LOCATION,
                    PermissionEnum.ACCESS_COARSE_LOCATION,
                    PermissionEnum.READ_PHONE_STATE,
                    PermissionEnum.CAMERA)
            .askAgain(false)
            .ask(this)
    }

    override fun onBackPressed() {

        when (supportFragmentManager.fragments.last()){
            is Dashboard -> {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Keluar aplikasi ? ")
                        .setPositiveButton("YES", dialogClickListener)
                        .setNegativeButton("NO", dialogClickListener)
                        .setCancelable(false).show()
            }
            is Login -> {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Keluar aplikasi ? ")
                        .setPositiveButton("YES", dialogClickListener)
                        .setNegativeButton("NO", dialogClickListener)
                        .setCancelable(false).show()
            }
            is Profile -> {
                changeFragment(Dashboard(), false, Utils.DASHBOARD)
            }
            is Payment -> {
                if (Utils.moreThanOne && !Utils.isDashboard)
                    changeFragment(Contract(), false, Utils.CONTRACT)
                else if (Utils.isContractDueDate) {
                    Utils.isContractDueDate = false
                    changeFragment(ContractDueDate(), false, Utils.CONTRACT_DUE_DATE)
                }
                else
                    changeFragment(Dashboard(), false, Utils.DASHBOARD)
            }
            is PaymentInfo -> {
                changeFragment(Payment(), false, Utils.PAYMENT)
            }
            is ContractDetail -> {
                if (Utils.moreThanOne)
                    changeFragment(Contract(), false, Utils.CONTRACT)
                else
                    changeFragment(Dashboard(), false, Utils.DASHBOARD)
            }
            is Contract -> {
                changeFragment(Dashboard(), false, Utils.DASHBOARD)
            }
            is ContractDueDate -> {
                changeFragment(Dashboard(), false, Utils.DASHBOARD)
            }
            is Notifikasi -> {
                Utils.page = ""
                changeFragment(Dashboard(), false, Utils.DASHBOARD)
            }
            is About -> {
                changeFragment(Dashboard(), false, Utils.DASHBOARD)
            }
        }
    }

}