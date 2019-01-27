import android.content.SharedPreferences

/**
 * Source: https://blogs.naxam.net/sharedpreferences-made-easy-with-kotlin-generics-extensions-6189d8902fb0
 */

inline fun <reified T> SharedPreferences.get(key: String): T {
    return when(T::class) {
        Boolean::class -> this.getBoolean(key, false) as T
        Float::class -> this.getFloat(key, 0.0f) as T
        Int::class -> this.getInt(key, 0) as T
        Long::class -> this.getLong(key, 0L) as T
        String::class -> this.getString(key, "") as T
        is Set<*> -> this.getStringSet(key, setOf<String>()) as T
        else -> "" as T
    }
}

inline fun <reified T> SharedPreferences.get(key: String, defaultValue: T): T {
    return when(T::class) {
        Boolean::class -> this.getBoolean(key, defaultValue as Boolean) as T
        Float::class -> this.getFloat(key, defaultValue as Float) as T
        Int::class -> this.getInt(key, defaultValue as Int) as T
        Long::class -> this.getLong(key, defaultValue as Long) as T
        String::class -> this.getString(key, defaultValue as String) as T
        is Set<*> -> this.getStringSet(key, defaultValue as Set<String>) as T
        else -> "" as T
    }
}

inline fun <reified T> SharedPreferences.put(key: String, value: T) {
    val editor = this.edit()

    when(T::class) {
        Boolean::class -> editor.putBoolean(key, value as Boolean)
        Float::class -> editor.putFloat(key, value as Float)
        Int::class -> editor.putInt(key, value as Int)
        Long::class -> editor.putLong(key, value as Long)
        String::class -> editor.putString(key, value as String)
        is Set<*> -> editor.putStringSet(key, value as Set<String>)
    }

    editor.apply()
}