package hoo.etahk.model.data

data class StringLang(
        var tc: String = "",
        var en: String = "") {
    // TODO("Need to Support English")
    var value : String
        get() = tc
        set(value) {
            tc = value
        }
}
