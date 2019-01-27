package hoo.etahk.common.extensions

fun <T> Boolean.yn(yes: T, no: T): T = if (this) yes else no
