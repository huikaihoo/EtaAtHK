package hoo.etahk.common.tools

class Separator(
    private val regexRow: Regex,
    private val regexColumn: Regex? = null,
    private val columnSize: Int = 1,
    private val removeBlankRow: Boolean = true,
    private val removeInvalidRow: Boolean = true) {

    var original: String = ""
        set(value) = process(value)

    var result: List<List<String>> = listOf()
        private set

    private fun process(originalString: String) {
        val mutableResult = mutableListOf<List<String>>()

        if (!originalString.isBlank()) {
            val rows = originalString.split(regexRow)
            if (columnSize <= 1 || regexColumn == null) {
                // One column per row
                rows.forEach {
                    if (!it.isBlank() || !removeBlankRow) {
                        mutableResult.add(listOf(it.trim()))
                    }
                }
            } else {
                // Multiple columns per row
                rows.forEach {
                    if (!it.isBlank()) {
                        val columns = it.split(regexColumn)
                        columns.forEach { it.trim() }
                        if (columns.size >= columnSize) {
                            mutableResult.add(columns)
                        } else if (!removeInvalidRow) {
                            val mutableColumns = columns.toMutableList()
                            mutableColumns.addAll(Array(columnSize - columns.size) { "" })
                            mutableResult.add(mutableColumns.toList())
                        }
                    } else if (!removeBlankRow){
                        mutableResult.add(Array(columnSize) { "" }.toList())
                    }
                }
            }
        }

        result = mutableResult.toList()
    }
}