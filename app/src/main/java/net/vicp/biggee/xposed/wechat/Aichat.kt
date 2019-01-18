package net.vicp.biggee.xposed.wechat

import com.baidu.ai.aip.auth.AuthService
import com.baidu.ai.aip.unit.UnitService
import de.robv.android.xposed.XposedBridge
import org.json.JSONObject
import kotlin.random.Random

class Aichat(private val msg: String, private val userid: String?) {

    fun processCMD() {
        CMDS.forEach {
            if (msg.contains("[$it]")) {
                when (it) {
                    BOT_CMD_ON -> BOTON = true
                    BOT_CMD_OFF -> BOTON = false
                    BOT_CMD_SKIP -> BOTSKIP = true
                    BOT_CMD_ONTHIS -> {
                        if (userid == null) {
                            return
                        }
                        OFFLIST.add(userid)
                    }
                    BOT_CMD_OFFTHIS -> {
                        if (userid == null) {
                            return
                        }
                        OFFLIST.remove(userid)
                    }
                    BOT_CMD_CLEAROFFLIST -> OFFLIST.clear()
                    BOT_CMD_CLEARSESSIONS -> SESSIONS.clear()
                }
            }
        }
    }

    private fun convertGroupMSG(gM: String, gN: String): Array<String> {
        val g = gN.split("@")[0]
        val u = gM.split(":")[0]
        val msg = gM.substring(u.length + 1)
        return arrayOf(msg, "$u@$g")
    }

    override fun toString(): String {
        if (BOTSKIP) {
            BOTSKIP = false
            return "[$BOT_CMD_SKIP]"
        }
        if (OFFLIST.contains(userid)) {
            return "[$BOT_CMD_SKIP]"
        }
        var sessionid = SESSIONS.get(userid) ?: ""
        var userid = this.userid ?: ""
        var msg = this.msg
        XposedBridge.log("Aichat uid=$userid,msg=$msg")
        if (userid.contains("chatroom")) {
            val r = convertGroupMSG(msg, userid)
            msg = r[0]
            userid = r[1]
            sessionid = SESSIONS.get(userid) ?: ""
            XposedBridge.log("Aichat groupConvert uid=$userid,msg=$msg")
        }
        var s = UnitService.utterance(arrayOf(LOGID, sessionid, msg, userid))
                ?: return "[$BOT_CMD_SKIP]"
        XposedBridge.log("Aichat reply=$s")
        try {
            val js = JSONObject(s).getJSONObject("result")
            SESSIONS.put(userid, js.getString("session_id"))
            val jsa = js.getJSONArray("response_list").getJSONObject(0).getJSONArray("action_list")
            try {
                val i = RANDOM.nextInt(jsa.length())
                s = jsa.getJSONObject(i).getString("say")
            } catch (_: Exception) {
                s = jsa.getJSONObject(0).getString("say")
            }
        } catch (e: Exception) {
            s = e.localizedMessage
            OFFLIST.add(userid)
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
        const val BOT_CMD_SKIP = "BOTSKIP"
        const val BOT_CMD_ON = "BOTON"
        const val BOT_CMD_OFF = "BOTOFF"
        const val BOT_CMD_OFFTHIS = "BOTOFFTHIS"
        const val BOT_CMD_ONTHIS = "BOTONTHIS"
        const val BOT_CMD_CLEAROFFLIST = "CLEAROFFLIST"
        const val BOT_CMD_CLEARSESSIONS = "CLEARSESSIONS"
        val CMDS = arrayOf(
                BOT_CMD_ON,
                BOT_CMD_OFF,
                BOT_CMD_SKIP,
                BOT_CMD_ONTHIS,
                BOT_CMD_OFFTHIS,
                BOT_CMD_CLEAROFFLIST,
                BOT_CMD_CLEARSESSIONS
        )
        val SESSIONS: HashMap<String, String> by lazy { HashMap<String, String>() }
        val OFFLIST: HashSet<String> by lazy { HashSet<String>() }
        val TOKEN: String by lazy { AuthService.auth ?: "" }
        val RANDOM by lazy { Random(System.currentTimeMillis()) }
        var BOTON = true
        var BOTSKIP = false
    }
}