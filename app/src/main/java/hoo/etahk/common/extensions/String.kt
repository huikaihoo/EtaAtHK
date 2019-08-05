package hoo.etahk.common.extensions

fun String.toIntOrDefault(default: Int): Int =
    this.toIntOrNull() ?: default
