package com.byxiaorun.detector

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiObjects
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.byxiaorun.detector.BuildConfig
import com.byxiaorun.detector.MyApplication.Companion.appContext
import com.byxiaorun.detector.MyApplication.Companion.maps_string
import com.byxiaorun.detector.MyApplication.Companion.vpn_connect
import icu.nullptr.applistdetector.MainPage
import icu.nullptr.applistdetector.theme.MyTheme
import java.io.*
import java.net.NetworkInterface
import java.util.*
import kotlin.text.StringBuilder


/**
 *Created by byxiaorun on 2022/4/20/0020.
 */
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkDisabled()
        checkSetting()
        CheckProcSelfMaps()
        setContent {
            MyTheme {
                var showDialog by remember { mutableStateOf(false) }
                if (showDialog) AboutDialog { showDialog = false }
                Scaffold(
                    topBar = { MainTopBar() },
                    floatingActionButton = { MainFab { showDialog = true } },
                ) { innerPadding ->
                    MainPage(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
private fun MainTopBar() {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(id = R.string.app_name) +" V${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})") }
    )
}

@Composable
private fun MainFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        icon = { Icon(Icons.Outlined.EmojiObjects, (stringResource(id = R.string.about))) },
        text = { Text(stringResource(id = R.string.about)) },
        onClick = onClick
    )
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
        title = { Text(stringResource(id = R.string.about)) },
        text = {
            Column(horizontalAlignment = Alignment.Start) {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
                    Text(stringResource(R.string.app_name) +" V${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    Text(stringResource(R.string.authored) +": Nullptr & byxiaorun")
                }
                Spacer(Modifier.height(10.dp))
                val annotatedString = buildAnnotatedString {
                    pushStringAnnotation("GitHub", "https://github.com/byxiaorun/ApplistDetector/tree/new")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(appContext.getString(R.string.source))
                    }
                    pop()
                    append("  ")
                    pushStringAnnotation("Telegram", "https://t.me/HideMyApplist")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(appContext.getString(R.string.telegram))
                    }
                    pop()
                    append("  ")
                    pushStringAnnotation("Telegram", "https://t.me/xrshop")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(appContext.getString(R.string.telegram))
                    }
                }
                ClickableText(annotatedString, style = MaterialTheme.typography.bodyLarge) { offset ->
                    annotatedString.getStringAnnotations("GitHub", offset, offset).firstOrNull()?.let {
                        ContextCompat.startActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(it.item)), null)
                    }
                    annotatedString.getStringAnnotations("Telegram", offset, offset).firstOrNull()?.let {
                        ContextCompat.startActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(it.item)), null)
                    }
                }
            }
        },
    )
}

fun gettext(string: String): Array<String> {
    when(string){
        "not_found" -> {
            return arrayOf(appContext.getString(R.string.not_found))
        }
        "method" -> {
            return arrayOf(appContext.getString(R.string.method))
        }
        "suspicious" -> {
            return arrayOf(appContext.getString(R.string.suspicious))
        }
        "found" -> {
            return arrayOf(appContext.getString(R.string.found))
        }
        "abnormal" -> {
            return arrayOf(appContext.getString(R.string.abnormal))
        }
        "filedet" -> {
            return arrayOf(appContext.getString(R.string.filedet))
        }
        "pmc" -> {
            return arrayOf(appContext.getString(R.string.pmc))
        }
        "pmca" -> {
            return arrayOf(appContext.getString(R.string.pmca))
        }
        "pmsa" -> {
            return arrayOf(appContext.getString(R.string.pmsa))
        }
        "pmiq" -> {
            return arrayOf(appContext.getString(R.string.pmiq))
        }
        "xposed" -> {
            return arrayOf(appContext.getString(R.string.xposed))
        }
        "lspatch" -> {
            return arrayOf(appContext.getString(R.string.lspatch))
        }
        "magisk" -> {
            return arrayOf(appContext.getString(R.string.magisk))
        }
        "accessibility" -> {
            return arrayOf(appContext.getString(R.string.accessibility))
        }
        "settingprops" -> {
            return appContext.resources.getStringArray(R.array.settingprops)
        }
        else -> {
            return arrayOf("none")
        }
    }
}


private fun checkDisabled() {
    MyApplication.accList = getFromAccessibilityManager()+getFromSettingsSecure()
}

private fun getFromAccessibilityManager(): List<String> {
    val accessibilityManager =
        ContextCompat.getSystemService(appContext, AccessibilityManager::class.java)
            ?: error("unreachable")
    val serviceList: List<AccessibilityServiceInfo> =
        accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            ?: emptyList()
    val nameList = serviceList.map {
        appContext.packageManager.getApplicationLabel(it.resolveInfo.serviceInfo.applicationInfo)
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

private fun getFromSettingsSecure():List<String> {
    val settingValue= Settings.Secure.getString(
        appContext.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    val nameList=if (settingValue.isNullOrEmpty()){
        emptyList()
    }else{
        settingValue.split(':')
    }.toMutableList()
    val enabled = Settings.Secure.getInt(appContext.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
    if (enabled != 0) {
        MyApplication.accenable =true
    }
    return nameList
}
fun checkSetting() {
    if((Settings.Secure.getInt(appContext.contentResolver,Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,0)==1)){ MyApplication.development_enable=true }
    if((Settings.Secure.getInt(appContext.contentResolver,Settings.Global.ADB_ENABLED,0)==1)){ MyApplication.adbenable=true }


    try {
        val nilist = NetworkInterface.getNetworkInterfaces()
    if (nilist!=null){
        for (obj in Collections.list(nilist)){
            val intf:NetworkInterface=obj
            if (!intf.isUp()||intf.interfaceAddresses.size==0) {
                continue
            }
            if ("tun0".equals(intf.name) || "ppp0".equals(intf.name)){
                vpn_connect=true
            }
            try {
                val conMgr= appContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                if (conMgr.getNetworkInfo(17)?.isConnectedOrConnecting == true) vpn_connect=true

            } catch (e: Exception) {
            }
        }
    }
    }catch (e:Throwable){
        e.printStackTrace()
    }
}
fun CheckProcSelfMaps() {
    try {
        val ret=StringBuilder()
        val ins= FileInputStream("/proc/self/maps")
        val reader=InputStreamReader(ins,Charsets.UTF_8)
        val bufReader= BufferedReader(reader)

        while (bufReader.readLine()!=null){
            val line:String= bufReader.readLine()
            val line2=line.split("\\/._-").toString()
            if (line2.contains("magisk",ignoreCase = true)
                ||(line2.contains("riru",ignoreCase = true)
                ||(line2.contains("zygisk",ignoreCase = true)))
            ){
                ret.append(line2).append('\n')
            }
        }
        bufReader.close()
        reader.close()
        ins.close()
        if (ret.toString().trim().length!=0) maps_string=true
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
