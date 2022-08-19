package icu.nullptr.applistdetector

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.byxiaorun.detector.MyApplication.Companion.accList
import com.byxiaorun.detector.MyApplication.Companion.accenable
import com.byxiaorun.detector.MyApplication.Companion.appContext
import com.byxiaorun.detector.gettext
import icu.nullptr.applistdetector.component.CheckCard
import icu.nullptr.applistdetector.component.IconHintCard
import kotlinx.coroutines.*

val basicAppList = listOf(
    "com.topjohnwu.magisk",
    "io.github.vvb2060.magisk",
    "io.github.vvb2060.magisk.lite",
    "de.robv.android.xposed.installer",
    "org.meowcat.edxposed.manager",
    "org.lsposed.manager",
    "top.canyie.dreamland.manager",
    "me.weishu.exp",
    "com.tsng.hidemyapplist",
    "cn.geektang.privacyspace",
    "moe.shizuku.redirectstorage"
)

val snapShotList = mutableStateListOf<Triple<IDetector, IDetector.Result?, Detail?>>(
    Triple(AbnormalEnvironment(appContext,false, gettext("abnormal")), null, null),
    Triple(AbnormalEnvironment(appContext,true,"SuBusybox "+gettext("filedet")), null, null),
    Triple(PMCommand(appContext,gettext("pmc")), null, null),
    Triple(PMConventionalAPIs(appContext,gettext("pmca")), null, null),
    Triple(PMSundryAPIs(appContext,gettext("pmsa")), null, null),
    Triple(PMQueryIntentActivities(appContext,gettext("pmiq")), null, null),
    Triple(FileDetection(appContext, false,"Libc "+gettext("filedet")), null, null),
    Triple(FileDetection(appContext, true,"Syscall "+gettext("filedet")), null, null),
//    Triple(StatFile(appContext," StatFile "+gettext("filedet")), null, null),
    Triple(XposedModules(appContext,gettext("xposed"),false), null, null),
    Triple(XposedModules(appContext,gettext("lspatch"),true), null, null),
    Triple(MagiskApp(appContext,gettext("magisk")), null, null),
    Triple(Accessibility(appContext, accList = accList, accenable = accenable,gettext("accessibility")), null, null),
)

suspend fun runDetector(id: Int, packages: Collection<String>?) {
    withContext(Dispatchers.IO) {
        val detail = mutableListOf<Pair<String, IDetector.Result>>()
        val result = snapShotList[id].first.run(packages, detail)
        snapShotList[id] = Triple(snapShotList[id].first, result, detail)
    }
}

@Composable
fun MainPage(modifier: Modifier) {
    LaunchedEffect(appContext) {
        runDetector(0, null)
        runDetector(1, null)
        for (i in 2..7) runDetector(i, basicAppList)
        for (i in 8..11) runDetector(i, null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconHintCard()
        snapShotList.forEach {
            CheckCard(
                title = it.first.name,
                result = it.second,
                detail = it.third
            )
        }
    }
}
