package test.f22.f22testapplication.base

import android.support.v4.app.DialogFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe

open class BaseDialogFragment : DialogFragment() {

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onEventReceived(event: Event) {}

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}