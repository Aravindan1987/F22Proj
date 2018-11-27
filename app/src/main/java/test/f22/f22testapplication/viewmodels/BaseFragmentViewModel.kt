package test.f22.f22testapplication.viewmodels

import android.arch.lifecycle.ViewModel
import java.lang.ref.WeakReference

open class BaseViewModel<T> : ViewModel() {

    var activityReference : WeakReference<T>? = null

    fun registerWithActivity(activity : T) {
        activityReference = WeakReference(activity)
    }
}