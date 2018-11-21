package hoo.etahk.model.json

data class Info(var boundIds: List<String> = emptyList(),
                val rdv: String = "",
                val bound: String = "",
                val startSeq: Long = -1L,
                val endSeq: Long = -1L,
                val stopId: String = "",
                val fareHoliday: Double = -1.0,
                val partial: Long = 0L)