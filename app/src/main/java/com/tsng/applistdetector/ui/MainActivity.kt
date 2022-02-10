package com.tsng.applistdetector.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.MobileAds
import com.tsng.applistdetector.BuildConfig
import com.tsng.applistdetector.MyApplication
import com.tsng.applistdetector.MyApplication.Companion.accList
import com.tsng.applistdetector.MyApplication.Companion.accenable
import com.tsng.applistdetector.MyApplication.Companion.detectionAppList
import com.tsng.applistdetector.R
import com.tsng.applistdetector.detections.*
import com.tsng.applistdetector.ui.components.AdvertView
import com.tsng.applistdetector.ui.components.FoldLayout
import com.tsng.applistdetector.ui.theme.AppTheme
import kotlin.concurrent.thread


@ExperimentalAnimationApi
class MainActivity : AppCompatActivity() {

    class TagData(
        var status: FoldLayout.Status,
        var targets: MutableList<Pair<String, IDetector.Results>>
    )

    private val detectors = listOf(
        AbnormalEnvironment,
        PMCommand,
        PMConventionalAPIs,
        PMSundryAPIs,
        PMGetPackagesHoldingPermissions,
        PMQueryIntentActivities,
        FileDetections(useSyscall = false),
        FileDetections(useSyscall = true),
        RandomPackageName,
        XposedModules,
        Accessibility
    )
     fun checkDisabled() {
         accList = getFromAccessibilityManager() + getFromSettingsSecure()
    }

    private fun getFromAccessibilityManager(): List<String> {
        val accessibilityManager =
            ContextCompat.getSystemService(this, AccessibilityManager::class.java)
                ?: error("unreachable")
        val serviceList: List<AccessibilityServiceInfo> =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                ?: emptyList()
        val nameList = serviceList.map {
            packageManager.getApplicationLabel(it.resolveInfo.serviceInfo.applicationInfo)
                .toString()
        }.toMutableList()
        if (accessibilityManager.isEnabled) {
            nameList.add("AccessibilityManager.isEnabled")
        }
        if (accessibilityManager.isTouchExplorationEnabled) {
            nameList.add("AccessibilityManager.isTouchExplorationEnabled")
        }
        return nameList
    }

    private fun getFromSettingsSecure(): List<String> {
        val settingValue = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        val nameList = if (settingValue.isEmpty()) {
            emptyList()
        } else {
            settingValue.split(':')
        }.toMutableList()
        val enabled = Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        if (enabled != 0) {
            accenable=true
        }
        return nameList
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        checkDisabled()
        detectionAppList = getPreferences(MODE_PRIVATE).getStringSet("appList", null)?.toList() ?: IDetector.basicAppList
        setContent {
            AppTheme {
                val data = remember {
                    mutableStateListOf<TagData>()
                }
                for (detector in detectors) {
                    val tagData = TagData(FoldLayout.Status.NotStarted, mutableListOf())
                    data.add(tagData)
                }

                Column {
                    TopAppBar(
                        modifier = Modifier.padding(bottom = 4.dp),
                        title = { Text(stringResource(R.string.app_name) + " V" + BuildConfig.VERSION_NAME) },
                        navigationIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = null
                            )
                        }
                    )
                    AdvertView()
                    LazyColumn(modifier = Modifier.padding(horizontal = 10.dp)) {
                        for (i in detectors.indices) {
                            item {
                                FoldLayout(
                                    status = data[i].status,
                                    title = detectors[i].name,
                                    list = data[i].targets
                                )
                            }
                        }
                    }
                }

                thread {
                    for (i in detectors.indices) {
                        runOnUiThread {
                            data[i] = TagData(FoldLayout.Status.Loading, mutableListOf())
                        }

                        detectors[i].execute()

                        runOnUiThread {
                            data[i] = TagData(FoldLayout.Status.Completed, detectors[i].results)
                        }
                    }
                }
            }
        }
    }

}