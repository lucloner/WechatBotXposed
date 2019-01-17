package com.baidu.ai.aip.unit

import com.baidu.ai.aip.utils.HttpUtil
import net.vicp.biggee.xposed.wechat.Aichat

/*
 * unit对话服务
 */
object UnitService {
    /**
     * 重要提示代码中所需工具类
     * FileUtil,Base64Util,HttpUtil,GsonUtils请从
     * https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72
     * https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2
     * https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3
     * https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3
     * 下载
     */
    fun utterance(args: Array<String>): String? {
        // 请求URL
        val talkUrl = "https://aip.baidubce.com/rpc/2.0/unit/service/chat"
        try {
            // 请求参数
            val params = "{" +
                    "\"log_id\":\"${args[0]}\"," +
                    "\"version\":\"2.0\"," +
                    "\"service_id\":\"${Aichat.SERVICEID}\"," +
                    "\"session_id\":\"${args[1]}\"," +
                    "\"request\":{" +
                    "\"query\":\"${args[2]}\"," +
                    "\"user_id\":\"${args[3]}\"" +
                    "}" +
                    "}" //,"dialog_state":{"contexts":{"SYS_REMEMBERED_SKILLS":["1057"]}}
            val accessToken = Aichat.TOKEN
            return HttpUtil.post(talkUrl, accessToken, "application/json", params)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

//    @JvmStatic
//    fun main(args: Array<String>) {
//        utterance()
//    }
}