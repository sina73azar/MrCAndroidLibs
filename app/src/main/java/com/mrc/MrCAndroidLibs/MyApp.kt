package com.mrc.MrCAndroidLibs

import android.app.Application
import com.mrc.MrCAndroidLibs.fcm.FcmTokenRegistrar
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Mr.C 04/May/2026
 */
@HiltAndroidApp
class MyApp : Application(){
/*    @Inject
    lateinit var fcmTokenRegistrar: FcmTokenRegistrar*/

    override fun onCreate() {
        super.onCreate()
//        fcmTokenRegistrar.registerCurrentToken()
    }
}
