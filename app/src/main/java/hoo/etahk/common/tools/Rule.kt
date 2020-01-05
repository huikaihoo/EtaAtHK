package hoo.etahk.common.tools

data class Rule(
    val basedOn: String,
    val target: String,
    val includeFrom: String = "",
    val includeTo: String = "",
    val excludeFrom: String = "",
    val excludeTo: String = ""
)