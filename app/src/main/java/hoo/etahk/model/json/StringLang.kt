package hoo.etahk.model.json

data class StringLang(
        var tc: String = "",
        var en: String = "",
        var sc: String = "") {

    companion object {
        fun newInstance(str: String): StringLang {
            val stringLang = StringLang()
            stringLang.value = str
            return stringLang
        }
    }

    // TODO("Need to Support English")
    var value : String
        get() = tc
        set(value) {
            tc = value
        }
}
