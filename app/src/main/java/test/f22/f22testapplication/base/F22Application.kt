package test.f22.f22testapplication.base

import com.orm.SugarApp

class F22Application : SugarApp() {

    companion object {
        lateinit var context: F22Application
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}