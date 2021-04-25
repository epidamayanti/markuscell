package my.id.phyton06.markuscell.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import my.id.phyton06.markuscell.responses.LoginResponseModel
import java.lang.reflect.Type


@SuppressLint("CommitPrefEdits")
class SharedPrefManager(context: Context) {

    internal var sp: SharedPreferences
    internal var spEditor: SharedPreferences.Editor

    val spUsername: String?
        get() = sp.getString(USERNAME, "")

    val spName: String?
        get() = sp.getString(name, "")

    val spSudahLogin: Boolean?
        get() = sp.getBoolean(SUDAH_LOGIN, false)

    val spToken: String?
        get() = sp.getString(token, "")

    val spPass: String?
        get() = sp.getString(pass, "")

    init {
        sp = context.getSharedPreferences(APP, Context.MODE_PRIVATE)
        spEditor = sp.edit()
    }

    fun saveSPString(keySP: String, value: String) {
        spEditor.putString(keySP, value)
        spEditor.commit()
    }

    fun saveSPInt(keySP: String, value: Int) {
        spEditor.putInt(keySP, value)
        spEditor.commit()
    }

    fun saveSPBoolean(keySP: String, value: Boolean) {
        spEditor.putBoolean(keySP, value)
        spEditor.commit()
    }

    fun <T> saveList(key: String?, list:T?) {
        val gson = Gson()
        val json = gson.toJson(list)
        set(key, json)
    }

    operator fun set(key: String?, value: String?) {
        spEditor.putString(key, value)
        spEditor.commit()
    }

/*    fun getList(): LoginResponseModel {
        lateinit var arrayItems: LoginResponseModel
        val serializedObject: String = sp.getString(APP, null).toString()
        val gson = Gson()
        val type: Type = object : TypeToken<List<LoginResponseModel?>?>() {}.type
        arrayItems = gson.fromJson<LoginResponseModel>(serializedObject, type)
        return arrayItems
    }*/

    companion object {
        val APP = "App"
        val USERNAME = "Username"
        val name = "name"
        val token = "token"
        val pass = "pass"
        val SUDAH_LOGIN = "SudahLogin"
    }


}
