package hr.vgabriel.skillconnect.bll.helpers

import android.app.Activity
import android.app.Application
import android.os.Bundle

class AppLifecycleTracker : Application.ActivityLifecycleCallbacks {

    private var appInForeground = false
        private set

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        appInForeground = false
    }

    override fun onActivityPaused(activity: Activity) {
        appInForeground = true
    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    fun isAppInForeground(): Boolean {
        return appInForeground
    }
}
