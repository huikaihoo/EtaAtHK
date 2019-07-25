import android.os.Bundle

/**
 * Source: https://blogs.naxam.net/sharedpreferences-made-easy-with-kotlin-generics-extensions-6189d8902fb0
 */

inline fun <reified T> Bundle.getValue(key: String): T {
    return when (T::class) {
        Boolean::class -> this.getBoolean(key, false) as T
        BooleanArray::class -> (this.getBooleanArray(key) ?: BooleanArray(0)) as T
        Byte::class -> this.getByte(key, 0) as T
        ByteArray::class -> (this.getByteArray(key) ?: ByteArray(0)) as T
        Char::class -> this.getChar(key, Char.MIN_VALUE) as T
        CharArray::class -> (this.getCharArray(key) ?: CharArray(0)) as T
        CharSequence::class -> this.getCharSequence(key, "") as T
        Float::class -> this.getFloat(key, 0.0f) as T
        FloatArray::class -> (this.getFloatArray(key) ?: FloatArray(0)) as T
        Int::class -> this.getInt(key, 0) as T
        IntArray::class -> (this.getIntArray(key) ?: IntArray(0)) as T
        Long::class -> this.getLong(key, 0L) as T
        LongArray::class -> (this.getLongArray(key) ?: LongArray(0)) as T
        Short::class -> this.getShort(key, 0) as T
        ShortArray::class -> (this.getShortArray(key) ?: ShortArray(0)) as T
        String::class -> this.getString(key, "") as T
        else -> this.get(key) as T
    }
}

inline fun <reified T> Bundle.getValue(key: String, defaultValue: T): T {
    return when (T::class) {
        Boolean::class -> this.getBoolean(key, defaultValue as Boolean) as T
        BooleanArray::class -> (this.getBooleanArray(key) ?: defaultValue) as T
        Byte::class -> this.getByte(key, defaultValue as Byte) as T
        ByteArray::class -> (this.getByteArray(key) ?: defaultValue) as T
        Char::class -> this.getChar(key, defaultValue as Char) as T
        CharArray::class -> (this.getCharArray(key) ?: defaultValue) as T
        CharSequence::class -> this.getCharSequence(key, defaultValue as CharSequence) as T
        Float::class -> this.getFloat(key, defaultValue as Float) as T
        FloatArray::class -> (this.getFloatArray(key) ?: defaultValue) as T
        Int::class -> this.getInt(key, defaultValue as Int) as T
        IntArray::class -> (this.getIntArray(key) ?: defaultValue) as T
        Long::class -> this.getLong(key, defaultValue as Long) as T
        LongArray::class -> (this.getLongArray(key) ?: defaultValue) as T
        Short::class -> this.getShort(key, defaultValue as Short) as T
        ShortArray::class -> (this.getShortArray(key) ?: defaultValue) as T
        String::class -> this.getString(key, defaultValue as String) as T
        else -> defaultValue
    }
}
