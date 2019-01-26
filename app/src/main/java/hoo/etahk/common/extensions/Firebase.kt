import com.google.firebase.remoteconfig.FirebaseRemoteConfig

/**
 * Source: https://blogs.naxam.net/sharedpreferences-made-easy-with-kotlin-generics-extensions-6189d8902fb0
 */

inline fun <reified T> FirebaseRemoteConfig.get(key: String): T {
    when(T::class) {
        Boolean::class -> return this.getBoolean(key) as T
        Double::class -> return this.getDouble(key) as T
        Long::class -> return this.getLong(key) as T
        String::class -> return this.getString(key) as T
    }

    return "" as T
}