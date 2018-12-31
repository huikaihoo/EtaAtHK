package hoo.etahk.view.settings

import androidx.lifecycle.ViewModel
import hoo.etahk.common.Constants
import hoo.etahk.common.helper.SharedPrefsHelper

class SettingsViewModel : ViewModel() {
    var isInit = false
    var showTesting = SharedPrefsHelper.getAppMode() == Constants.AppMode.DEV
}