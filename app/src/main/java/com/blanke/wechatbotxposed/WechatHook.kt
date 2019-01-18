package com.blanke.wechatbotxposed

import com.blanke.wechatbotxposed.hook.SendMsgHooker
import com.blanke.wechatbotxposed.hook.WechatMessageHook
import com.gh0u1l5.wechatmagician.spellbook.SpellBook
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.vicp.biggee.xposed.wechat.ProcessException

class WechatHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        Thread.setDefaultUncaughtExceptionHandler(ProcessException())
        BasicUtil.tryVerbosely {
            if (SpellBook.isImportantWechatProcess(lpparam)) {
                XposedBridge.log("Hello Wechat!")
                SpellBook.startup(lpparam, listOf(
                        WechatMessageHook
                ), listOf(
                        SendMsgHooker
                ))
            }
        }
    }
}