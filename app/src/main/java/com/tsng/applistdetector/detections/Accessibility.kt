package com.tsng.applistdetector.detections

import android.provider.Settings
import com.tsng.applistdetector.MyApplication.Companion.accList
import com.tsng.applistdetector.MyApplication.Companion.accenable


/**
 *Created by byxiaorun on 2022/2/9/0009.
 */

 object Accessibility:IDetector() {
    override val name = "accessibility checker"
    override fun execute() {
        results.clear()
        if (accenable==true){
            results.add(Pair("AccessibilitySERVICES.isEnabled", Results.FOUND))

        }else{
            results.add(Pair("AccessibilitySERVICES", Results.NOT_FOUND))
        }
        if (accList.isNotEmpty()) {
            accList.forEach { results.add(Pair(it, Results.FOUND)) }
        } else {
             results.add(Pair("AccessibilityList", Results.NOT_FOUND))
        }

}}

