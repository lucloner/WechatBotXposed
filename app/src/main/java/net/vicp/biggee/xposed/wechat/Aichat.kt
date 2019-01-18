package net.vicp.biggee.xposed.wechat

import com.baidu.ai.aip.auth.AuthService
import com.baidu.ai.aip.unit.UnitService
import de.robv.android.xposed.XposedBridge
import org.json.JSONObject
import kotlin.random.Random

class Aichat(private val msg: String, private val userid: String?) {

    fun processCMD() {
        XposedBridge.log("Aichat processCMD uid=$userid,msg=$msg,msglength=${msg.length}")
        val userid = this.userid ?: ""
        CMDS.forEach {
            if (msg.contains("[$it]")) {
                when (it) {
                    BOT_CMD_ON -> BOTON = true
                    BOT_CMD_OFF -> BOTON = false
                    BOT_CMD_SKIP -> BOTSKIP = true
                    BOT_CMD_ONTHIS -> {
                        OFFLIST.add(getGroupName(userid))
                    }
                    BOT_CMD_OFFTHIS -> {
                        OFFLIST.remove(getGroupName(userid))
                    }
                    BOT_CMD_CLEAROFFLIST -> OFFLIST.clear()
                    BOT_CMD_CLEARSESSIONS -> SESSIONS.clear()
                }
            }
        }
    }

    private fun getGroupName(gN: String): String {
        return if (gN.contains("chatroom")) {
            gN.split("@")[0]
        } else {
            gN
        }
    }

    private fun convertGroupMSG(gM: String, gN: String): Array<String> {
        val g = getGroupName(gN)
        val u = gM.split(":")[0]
        if (gM.length < u.length + 2) {
            return arrayOf("", "$u@$g")
        }
        val msg = gM.substring(u.length + 2)
        return arrayOf(msg, "$u@$g")
    }

    override fun toString(): String {
        if (TOKEN.equals("") || AuthService.expires_in < 60L) {
            TOKEN = AuthService.auth ?: ""
        }
        
        if (TOKEN.equals("")) {
            return "[$BOT_CMD_SKIP]"
        }

        if (BOTSKIP) {
            BOTSKIP = false
            return "[$BOT_CMD_SKIP]"
        }
        var userid = this.userid ?: ""
        if (OFFLIST.contains(getGroupName(userid))) {
            return "[$BOT_CMD_SKIP]"
        }
        var sessionid = SESSIONS[userid] ?: ""
        var msg = this.msg
        XposedBridge.log("Aichat uid=$userid,msg=$msg")
        if (userid.contains("chatroom")) {
            val r = convertGroupMSG(msg, userid)
            msg = r[0]
            userid = r[1]
            sessionid = SESSIONS[userid] ?: ""
            XposedBridge.log("Aichat groupConvert uid=$userid,msg=$msg,msglength=${msg.length}")
        }
        var s = UnitService.utterance(arrayOf(LOGID, sessionid, msg, userid))
                ?: return "[$BOT_CMD_SKIP]"
        XposedBridge.log("Aichat reply=$s")
        try {
            val js = JSONObject(s).getJSONObject("result")
            SESSIONS[userid] = js.getString("session_id")
            val jsa = js.getJSONArray("response_list").getJSONObject(0).getJSONArray("action_list")
            s = try {
                val i = RANDOM.nextInt(jsa.length())
                jsa.getJSONObject(i).getString("say")
            } catch (_: Exception) {
                jsa.getJSONObject(0).getString("say")
            }
        } catch (e: Exception) {
            s = e.localizedMessage
            OFFLIST.add(getGroupName(userid))
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
        const val BOT_CMD_CLEAROFFLIST = "BOTCLEAROFFLIST"
        const val BOT_CMD_CLEARSESSIONS = "BOTCLEARSESSIONS"
        val CMDS = arrayOf(
                BOT_CMD_ON,
                BOT_CMD_OFF,
                BOT_CMD_SKIP,
                BOT_CMD_ONTHIS,
                BOT_CMD_OFFTHIS,
                BOT_CMD_CLEAROFFLIST,
                BOT_CMD_CLEARSESSIONS
        )
        val SESSIONS: HashMap<String, String> = HashMap()
        val OFFLIST: HashSet<String> = HashSet()
        var TOKEN = ""
        val RANDOM = Random(System.currentTimeMillis())
        var BOTON = true
        var BOTSKIP = false
        fun loadTOKEN() {
            TOKEN = AuthService.auth ?: ""
        }
    }
}