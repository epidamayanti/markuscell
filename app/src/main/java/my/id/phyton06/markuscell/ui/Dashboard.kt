package my.id.phyton06.markuscell.ui

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.photo
import kotlinx.android.synthetic.main.fragment_profile.*
import my.id.phyton06.markuscell.R
import my.id.phyton06.markuscell.commons.RxBaseFragment
import my.id.phyton06.markuscell.commons.RxBus
import my.id.phyton06.markuscell.commons.Utils
import my.id.phyton06.markuscell.database.DbHelper
import my.id.phyton06.markuscell.models.SharedPrefManager
import my.id.phyton06.markuscell.responses.LoginResponseModel

class Dashboard : RxBaseFragment() {

    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var getProfile : LoginResponseModel
    lateinit var DbHelper : DbHelper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefManager = SharedPrefManager(context!!)
        DbHelper = DbHelper(this.requireContext())

        initData()

        if(!DbHelper.getPhoto().equals("-")){
            val myBitmap = BitmapFactory.decodeFile(DbHelper.getPhoto())
            photo.setImageBitmap(myBitmap)
        }

        rl_personal.setOnClickListener {
            personal.setTextColor(Color.WHITE)
            rl_personal.setBackgroundColor(Color.rgb(163, 197, 255))
            RxBus.get().send(Utils.PROFILE)
        }

        ll_notif.setOnClickListener {
            notif.setTextColor(Color.WHITE)
            ll_notif.setBackgroundColor(Color.rgb(163, 197, 255))
            RxBus.get().send(Utils.NOTIF)
        }

        ll_payment.setOnClickListener {
            payment.setTextColor(Color.WHITE)
            ll_payment.setBackgroundColor(Color.rgb(163, 197, 255))
            RxBus.get().send(Utils.PAYMENT)
        }
        ll_contract.setOnClickListener {
            contract.setTextColor(Color.WHITE)
            ll_contract.setBackgroundColor(Color.rgb(163, 197, 255))
            RxBus.get().send(Utils.CONTRACT_DETAIL)
        }
        ll_about.setOnClickListener {
            about.setTextColor(Color.WHITE)
            ll_about.setBackgroundColor(Color.rgb(163, 197, 255))
            RxBus.get().send(Utils.ABOUT)
        }
        ll_logout.setOnClickListener {
            logout.setTextColor(Color.WHITE)
            ll_logout.setBackgroundColor(Color.rgb(163, 197, 255))
            Utils.isLogin = false
            DbHelper.deleteUser()
            sharedPrefManager.saveSPBoolean(SharedPrefManager.SUDAH_LOGIN, false)
            RxBus.get().send(Utils.LOGIN)
        }
    }

    private fun initData(){
        //getProfile = sharedPrefManager.getList()
        Utils.dataProfile = DbHelper.readUser()

        uname.text = "Hallo "+Utils.dataProfile.nama+"!"

        if(!Utils.dataProfile.username.toLowerCase().equals("ujang")){
            icon_vip.visibility = View.INVISIBLE
            sp_kontrak.visibility = View.GONE
            tv_kontrak.visibility = View.VISIBLE
        }
    }

}