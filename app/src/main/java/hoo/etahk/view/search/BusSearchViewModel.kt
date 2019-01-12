package hoo.etahk.view.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BusSearchViewModel : ViewModel() {
    val searchText: MutableLiveData<String> = MutableLiveData()
    var selectedTabPosition: Int = -1
}
