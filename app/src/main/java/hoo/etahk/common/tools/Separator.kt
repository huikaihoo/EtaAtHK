package hoo.etahk.common.tools

import android.util.Log

class Separator(val regexRow: Regex,
                val regexColumn: Regex? = null,
                val columnSize: Int = 1,
                val removeBlank: Boolean = true,
                val removeInvalidRow: Boolean = true) {

    var original: String = ""
        set(value) = process(value)

    var result: List<List<String>> = listOf()
        private set

    private fun process(originalString: String) {
        Log.d("XXX", originalString)
        val mutableResult = mutableListOf<List<String>>()

        if (!originalString.isBlank()) {
            val rows = originalString.split(regexRow)
            if (columnSize <= 1 || regexColumn == null) {
                // One column per row
                rows.forEach {
                    if (!it.isBlank() || !removeBlank) {
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
                            mutableColumns.addAll(Array(columnSize - columns.size, { "" }))
                            mutableResult.add(mutableColumns.toList())
                        }
                    } else if (!removeBlank){
                        mutableResult.add(Array(columnSize, { "" }).toList())
                    }
                }
            }
        }

        result = mutableResult.toList()
    }
}