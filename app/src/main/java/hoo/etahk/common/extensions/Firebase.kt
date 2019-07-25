import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue

/**
 * Source: https://blogs.naxam.net/sharedpreferences-made-easy-with-kotlin-generics-extensions-6189d8902fb0
 */

inline fun <reified T> FirebaseRemoteConfig.get(key: String): T {
    return when (T::class) {
        Boolean::class -> this.getBoolean(key) as T
        ByteArray::class -> this.getByteArray(key) as T
        Double::class -> this.getDouble(key) as T
        FirebaseRemoteConfigValue::class -> this.getValue(key) as T
        Long::class -> this.getLong(key) as T
        String::class -> this.getString(key) as T
        else -> "" as T
    }
}