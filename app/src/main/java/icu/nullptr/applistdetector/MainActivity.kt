package icu.nullptr.applistdetector

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Switch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.core.content.ContextCompat.startActivity
import icu.nullptr.applistdetector.MyApplication.Companion.accList
import icu.nullptr.applistdetector.MyApplication.Companion.accenable
import icu.nullptr.applistdetector.MyApplication.Companion.appContext
import icu.nullptr.applistdetector.theme.MyTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkDisabled()
        setContent {
            MyTheme {
                var showDialog by remember { mutableStateOf(false) }
                if (showDialog) AboutDialog { showDialog = false }
                Scaffold(
                    topBar = { MainTopBar() },
                    content = { MainPage() },
                    floatingActionButton = { MainFab { showDialog = true } },
                )
            }
        }
    }
}

@Composable
private fun MainTopBar() {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(id = R.string.app_name) +"V${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")}
    )
}

@Composable
private fun MainFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        icon = { Icon(Icons.Outlined.EmojiObjects, (stringResource(id = R.string.about)))},
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
                    Text(stringResource(id = R.string.app_name))
                    Text(stringResource(id = R.string.authored)+": Nullptr")
                }
                Spacer(Modifier.height(10.dp))
                val annotatedString = buildAnnotatedString {
                    pushStringAnnotation("GitHub", "https://github.com/Dr-TSNG/ApplistDetector")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(appContext.getString(R.string.source))
                    }
                    pop()
                    append("    ")
                    pushStringAnnotation("Telegram", "https://t.me/HideMyApplist")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(appContext.getString(R.string.telegram))
                    }
                    pop()
                }
                ClickableText(annotatedString, style = MaterialTheme.typography.bodyLarge) { offset ->
                    annotatedString.getStringAnnotations("GitHub", offset, offset).firstOrNull()?.let {
                        startActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(it.item)), null)
                    }
                    annotatedString.getStringAnnotations("Telegram", offset, offset).firstOrNull()?.let {
                        startActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(it.item)), null)
                    }
                }
            }
        },
    )
}
fun gettext(string: String): String {
    when(string){
        "not_found" -> {
            return appContext.getString(R.string.not_found)
        }
        "method" -> {
            return appContext.getString(R.string.method)
        }
        "suspicious" -> {
            return appContext.getString(R.string.suspicious)
        }
        "found" -> {
            return appContext.getString(R.string.found)
        }
        "abnormal" -> {
            return appContext.getString(R.string.abnormal)
        }
        "filedet" -> {
            return appContext.getString(R.string.filedet)
        }
        "pmc" -> {
            return appContext.getString(R.string.pmc)
        }
        "pmca" -> {
            return appContext.getString(R.string.pmca)
        }
        "pmsa" -> {
            return appContext.getString(R.string.pmsa)
        }
        "pmiq" -> {
            return appContext.getString(R.string.pmiq)
        }
        "xposed" -> {
            return appContext.getString(R.string.xposed)
        }
        "magisk" -> {
            return appContext.getString(R.string.magisk)
        }
        "accessibility" -> {
            return appContext.getString(R.string.accessibility)
        }
        else -> {
            return "none"
        }
    }
}


private fun checkDisabled() {
    accList = getFromAccessibilityManager()+getFromSettingsSecure()
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
        accenable=true
    }
    return nameList
}
