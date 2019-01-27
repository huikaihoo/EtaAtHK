package hoo.etahk.common.extensions

import java.io.InputStream
import java.nio.charset.Charset

fun InputStream.readString(charset: Charset = Charsets.UTF_8): String =
    this.bufferedReader(charset).use { it.readText() }

