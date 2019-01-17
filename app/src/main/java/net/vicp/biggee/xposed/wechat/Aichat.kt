package net.vicp.biggee.xposed.wechat

import com.baidu.ai.aip.auth.AuthService
import com.baidu.ai.aip.unit.UnitService
import org.json.JSONObject

class Aichat(private val msg: String, private val userid: String?) {

    override fun toString(): String {
        val sessionid = SESSIONS.get(userid) ?: ""
        val userid = this.userid ?: ""
        var s = UnitService.utterance(arrayOf(LOGID, sessionid, msg, userid)) ?: return "[BOTSKIP]"
        try {
            var js = JSONObject(s)
            js = js.getJSONObject("resault")
            SESSIONS.put(userid, js.getString("session_id"))
            var jsa = js.getJSONArray("response_list")
            js = JSONObject(jsa.getString(0))
            jsa = js.getJSONArray("action_list")
            js = jsa.getJSONObject(0)
            s = js.getString("say")
        } catch (e: Exception) {
            s = e.localizedMessage
        }
        return s
    }

    companion object {
        const val SANDBOX = "https://aip.baidubce.com/rpc/2.0/unit/service/chat"
        const val APPID = "15436845"
        const val APIKEY = "xW1FwQ2dGdGuC4fyZqwq3HKj"
        const val SECRETKEY = "FnKI3fLsinbEKavRhW1dKuI0tuewwbxf"
        const val TOKEN_BASE_URL = "https://aip.baidubce.com/oauth/2.0/token"
        const val GRANTTYPE = "client_credentials"
        const val TOKEN_URL = "$TOKEN_BASE_URL?grant_type=$GRANTTYPE&client_id=$APIKEY&client_secret=$SECRETKEY"
        const val SERVICEID = "S12624"
        const val LOGID = "WeChatExposed"
        val SESSIONS: HashMap<String, String> by lazy { HashMap<String, String>() }
        val TOKEN: String by lazy { AuthService.auth ?: "" }
    }
}