package hoo.etahk.model.json

data class Info(val boundIds: List<String> = emptyList(),
                val rdv: String = "",
                val bound: String = "",
                val startSeq: Long = -1L,
                val endSeq: Long = -1L,
                val stopId: String = "")