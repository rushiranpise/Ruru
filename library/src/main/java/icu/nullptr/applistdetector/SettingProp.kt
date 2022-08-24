package icu.nullptr.applistdetector

import android.content.Context

/**
 *Created by byxiaorun on 2022/8/24/0024.
 */
class SettingProp (context: Context, var development_enable:Boolean,var adbenable:Boolean,var vpn_connect:Boolean, val array: Array<String>
) : IDetector(context) {
    override val name = array[0]
    override fun run(packages: Collection<String>?, detail: Detail?): Result {
        var result = Result.NOT_FOUND
        val add: (Pair<String, Result>) -> Unit = {
            result = result.coerceAtLeast(it.second)
            detail?.add(it)
        }
        if (development_enable==true){
            add(Pair(array[1], Result.SUSPICIOUS))
        }
        if (adbenable==true){
            add(Pair(array[2], Result.SUSPICIOUS))
        }
        if (vpn_connect==true){
            add(Pair(array[3],Result.SUSPICIOUS))
        }
        return result
    }

}