package hoo.etahk.view.base

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

open class RefreshViewModel : ViewModel() {
    val isRefreshing = MutableLiveData<Boolean>()
}
