package hoo.etahk.view.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchViewModel : ViewModel() {
    val searchText: MutableLiveData<String> = MutableLiveData()
    var configList: List<SearchTabConfig> = listOf()
    var selectedTabPosition: Int = -1
}
