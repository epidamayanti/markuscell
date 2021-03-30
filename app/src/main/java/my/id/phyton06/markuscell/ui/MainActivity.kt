package my.id.phyton06.markuscell.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import my.id.phyton06.markuscell.R
import my.id.phyton06.markuscell.commons.BaseActivity
import my.id.phyton06.markuscell.commons.RxBaseFragment
import my.id.phyton06.markuscell.commons.RxBus
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.database.DbHelper
import my.id.phyton06.markuscell.models.SharedPrefManager
import rebus.permissionutils.PermissionEnum
import rebus.permissionutils.PermissionManager


class MainActivity :   BaseActivity() {

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

    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this,
            OnSuccessListener<InstanceIdResult> { instanceIdResult ->
                val newToken = instanceIdResult.token
                Log.e("Token", newToken)
                Utils.device_id = newToken
            })

        sharedPrefManager = SharedPrefManager(this)
        val dbHelper = DbHelper(this)

        if (savedInstanceState == null) {
            manageSubscription()
            Utils.mActivity = this
            //changeFragment(Login(), false, Utils.LOGIN)
            if (sharedPrefManager.spSudahLogin!!) {
                Utils.isLogin = false
                sharedPrefManager.saveSPBoolean(SharedPrefManager.SUDAH_LOGIN, false)
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
            is Dashboard ->{
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
                changeFragment(Dashboard(), false, Utils.DASHBOARD)
            }
            is PaymentInfo -> {
                changeFragment(Payment(), false, Utils.PAYMENT)
            }
            is ContractDetail -> {
                changeFragment(Dashboard(), false, Utils.DASHBOARD)
            }
            is Notifikasi ->{
                changeFragment(Dashboard(), false, Utils.DASHBOARD)
            }
            is About ->{
                changeFragment(Dashboard(), false, Utils.DASHBOARD)
            }
        }
    }

}