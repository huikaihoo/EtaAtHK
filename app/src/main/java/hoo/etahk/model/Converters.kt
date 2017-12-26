package hoo.etahk.model

import android.arch.persistence.room.TypeConverter
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.model.data.EtaResult
import hoo.etahk.model.data.StringLang
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun stringToJsonObject(value: String): JSONObject {
        return JSONObject(value)
    }

    @TypeConverter
    fun jsonObjectToString(item: JSONObject): String {
        return item.toString()
    }

    @TypeConverter
    fun stringToEtaResultList(value: String): List<EtaResult> {
        val array = AppHelper.gson.fromJson(value, Array<EtaResult>::class.java)
        return  when (array != null && array.isNotEmpty()) {
            true -> array.toList()
            false -> emptyList()
        }
    }

    @TypeConverter
    fun etaResultListToString(item: List<EtaResult>): String {
        return AppHelper.gson.toJson(item)
    }

    @TypeConverter
    fun stringToStringLang(value: String): StringLang {
        return AppHelper.gson.fromJson(value, StringLang::class.java)
    }

    @TypeConverter
    fun stringLangToString(item: StringLang): String {
        return AppHelper.gson.toJson(item)
    }
}
