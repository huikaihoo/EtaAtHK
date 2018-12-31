import android.content.SharedPreferences

/**
 * Source: https://blogs.naxam.net/sharedpreferences-made-easy-with-kotlin-generics-extensions-6189d8902fb0
 */

inline fun <reified T> SharedPreferences.get(key: String): T {
    when(T::class) {
        Boolean::class -> return this.getBoolean(key, false) as T
        Float::class -> return this.getFloat(key, 0.0f) as T
        Int::class -> return this.getInt(key, 0) as T
        Long::class -> return this.getLong(key, 0L) as T
        String::class -> return this.getString(key, "") as T
    }

    return "" as T
}

inline fun <reified T> SharedPreferences.get(key: String, defaultValue: T): T {
    when(T::class) {
        Boolean::class -> return this.getBoolean(key, defaultValue as Boolean) as T
        Float::class -> return this.getFloat(key, defaultValue as Float) as T
        Int::class -> return this.getInt(key, defaultValue as Int) as T
        Long::class -> return this.getLong(key, defaultValue as Long) as T
        String::class -> return this.getString(key, defaultValue as String) as T
        else -> {
            if (defaultValue is Set<*>)
                return this.getStringSet(key, defaultValue as Set<String>) as T
        }
    }

    return defaultValue
}

inline fun <reified T> SharedPreferences.put(key: String, value: T) {
    val editor = this.edit()

    when(T::class) {
        Boolean::class -> editor.putBoolean(key, value as Boolean)
        Float::class -> editor.putFloat(key, value as Float)
        Int::class -> editor.putInt(key, value as Int)
        Long::class -> editor.putLong(key, value as Long)
        String::class -> editor.putString(key, value as String)
        else -> {
            if (value is Set<*>)
                editor.putStringSet(key, value as Set<String>)
        }
    }

    editor.apply()
}