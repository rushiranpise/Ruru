package com.tsng.applistdetector.detections

import android.content.Intent
import android.content.pm.PackageManager
import com.tsng.applistdetector.MyApplication.Companion.appContext

object XposedModules : IDetector() {

    override val name = "xposed modules"

    override fun execute() {
        results.clear()
        val packages = mutableSetOf<String>()

        val pm = appContext.packageManager
        val intent = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (pkg in intent) {
            if (pkg.metaData?.get("xposedmodule") != null)
                packages.add(pm.getApplicationLabel(pkg) as String)
        }

        packages.forEach { results.add(Pair(it, Results.FOUND)) }
    }
}