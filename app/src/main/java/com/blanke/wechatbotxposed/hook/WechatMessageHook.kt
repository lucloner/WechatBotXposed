package com.blanke.wechatbotxposed.hook

import com.blanke.wechatbotxposed.hook.SendMsgHooker.wxMsgSplitStr
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IMessageStorageHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.vicp.biggee.xposed.wechat.Aichat

object WechatMessageHook : IMessageStorageHook {
    override fun onMessageStorageCreated(storage: Any) {
    }

    override fun onMessageStorageInserted(msgId: Long, msgObject: Any) {
        XposedBridge.log("onMessageStorageInserted msgId=$msgId,msgObject=$msgObject")
//        printMsgObj(msgObject)
        // 这些都是消息的属性，内容，发送人，类型等
        val field_content = XposedHelpers.getObjectField(msgObject, "field_content") as String?
        val field_talker = XposedHelpers.getObjectField(msgObject, "field_talker") as String?
        val field_type = (XposedHelpers.getObjectField(msgObject, "field_type") as Int).toInt()
        val field_isSend = (XposedHelpers.getObjectField(msgObject, "field_isSend") as Int).toInt()
        XposedBridge.log("field_content=$field_content,field_talker=$field_talker," +
                "field_type=$field_type,field_isSend=$field_isSend")
        if (field_isSend == 1) {// 代表自己发出的，不处理
            if (field_type == 1 && !field_content.isNullOrEmpty() && !field_talker.isNullOrEmpty()) {
//                try {
//                    Aichat.SESSIONMAP[field_talker] = field_content.split("[").last().dropLast(1)
//                } catch (_: Exception) {
//                }
                if ("[${Aichat.BOT_TAG}]" in field_content) {
                    return
                }
                Aichat(field_content, field_talker).processCMD()
            }
            return
        }
        if (!Aichat.BOTON) {
            return
        }
        if (field_type == 1) { //文本消息
            // field_content 就是消息内容，可以接入图灵机器人回复
            var s = ""
            try {
                s = field_content ?: return
            } catch (e: Exception) {
                e.printStackTrace()
            }
            s = Aichat(s, field_talker).toString()
            if (s.startsWith("[${Aichat.BOT_CMD_SKIP}")) {
                return
            }

            val replyContent = "[${Aichat.BOT_TAG}]$s"
            Objects.ChattingFooterEventImpl?.apply {
                // 将 wx_id 和 回复的内容用分隔符分开
                val content = "$field_talker$wxMsgSplitStr$replyContent"
                val success = Methods.ChattingFooterEventImpl_SendMsg.invoke(this, content) as Boolean
                XposedBridge.log("reply msg success = $success")
            }
        }
    }

    private fun printMsgObj(msg: Any) {
        val fieldNames = msg::class.java.fields
        fieldNames.forEach {
            val field = it.get(msg)
            if (field is Array<*>) {
                val s = StringBuffer()
                field.forEach {
                    s.append(it.toString() + " , ")
                }
                XposedBridge.log("$it = $s")
            } else {
                XposedBridge.log("$it = $field")
            }
        }
    }
}