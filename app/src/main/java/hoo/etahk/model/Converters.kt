package hoo.etahk.model

import android.arch.persistence.room.TypeConverter
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.model.json.EtaResult
import hoo.etahk.model.json.Extra
import hoo.etahk.model.json.Info
import hoo.etahk.model.json.StringLang
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

    // List<String>
    @TypeConverter
    fun stringToStringList(value: String): List<String> {
        val array = AppHelper.gson.fromJson(value, Array<String>::class.java)
        return  when (array != null && array.isNotEmpty()) {
            true -> array.toList()
            false -> emptyList()
        }
    }

    @TypeConverter
    fun stringListToString(item: List<String>): String {
        return AppHelper.gson.toJson(item)
    }

    // Info
    @TypeConverter
    fun stringToInfo(value: String): Info {
        return AppHelper.gson.fromJson(value, Info::class.java)
    }

    @TypeConverter
    fun infoToString(item: Info): String {
        return AppHelper.gson.toJson(item)
    }

    // List<EtaResult>
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

    // StringLang
    @TypeConverter
    fun stringToStringLang(value: String): StringLang {
        return AppHelper.gson.fromJson(value, StringLang::class.java)
    }

    @TypeConverter
    fun stringLangToString(item: StringLang): String {
        return Utils.replaceSpecialCharacters(AppHelper.gson.toJson(item))
    }

    // EtaStatus
    @TypeConverter
    fun stringToEtaStatus(value: String): Constants.EtaStatus {
        return Constants.EtaStatus.valueOf(value)
    }

    @TypeConverter
    fun etaStatusToString(item: Constants.EtaStatus): String {
        return item.toString()
    }

    // MiscType
    @TypeConverter
    fun stringToMiscType(value: String): Constants.MiscType {
        return Constants.MiscType.valueOf(value)
    }

    @TypeConverter
    fun miscTypeToString(item: Constants.MiscType): String {
        return item.toString()
    }

    // Info
    @TypeConverter
    fun stringToExtra(value: String): Extra {
        return AppHelper.gson.fromJson(value, Extra::class.java)
    }

    @TypeConverter
    fun extraToString(item: Extra): String {
        return AppHelper.gson.toJson(item)
    }
}
