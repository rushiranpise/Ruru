package com.tsng.applistdetector.detections

import android.provider.Settings
import com.tsng.applistdetector.MyApplication.Companion.accContext


/**
 *Created by byxiaorun on 2022/2/9/0009.
 */

 object Accessibility:IDetector() {
    override val name = "accessibility checker"
    override fun execute() {
        results.clear()
        if (accContext!="pass") {
            results.add(Pair(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, Results.FOUND))
            results.add(Pair(accContext, Results.FOUND))
        } else {
            results.add(Pair(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, Results.NOT_FOUND))
            results.add(Pair(Settings.Secure.ACCESSIBILITY_ENABLED, Results.NOT_FOUND))
        }
}}

