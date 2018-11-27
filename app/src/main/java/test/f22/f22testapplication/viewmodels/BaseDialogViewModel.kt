package test.f22.f22testapplication.viewmodels

import android.arch.lifecycle.ViewModel
import java.lang.ref.WeakReference

open class BaseFragmentViewModel<T> : ViewModel() {

    var fragmentReference : WeakReference<T>? = null

    fun registerFragment(dialog : T) {
        fragmentReference = WeakReference(dialog)
    }
}