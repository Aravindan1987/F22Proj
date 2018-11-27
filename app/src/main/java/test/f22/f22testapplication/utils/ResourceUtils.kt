package test.f22.f22testapplication.utils

import android.support.v4.content.ContextCompat
import test.f22.f22testapplication.base.F22Application

class ResourceUtils {
    companion object {
        fun getString(resourceId : Int) : String = F22Application.context.getString(resourceId)

        fun getDrawable(resourceId : Int) = ContextCompat.getDrawable(F22Application.context, resourceId)
    }
}