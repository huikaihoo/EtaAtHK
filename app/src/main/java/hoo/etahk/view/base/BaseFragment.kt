package hoo.etahk.view.base

import android.os.Bundle
import android.support.v4.app.Fragment

abstract class BaseFragment : Fragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        retainInstance = true
    }
}
