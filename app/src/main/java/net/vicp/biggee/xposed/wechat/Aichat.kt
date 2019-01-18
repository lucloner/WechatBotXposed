package net.vicp.biggee.xposed.wechat

import com.baidu.ai.aip.auth.AuthService
import com.baidu.ai.aip.unit.UnitService
import de.robv.android.xposed.XposedBridge
import org.json.JSONObject
import kotlin.random.Random

class Aichat(private val msg: String, private val userid: String?) {

    fun processCMD() {
        CMDS.forEach {
            if ("[$it]" in msg) {
                when (it) {
                    BOT_CMD_ON -> BOTON = true
                    BOT_CMD_OFF -> BOTON = false
                    BOT_CMD_SKIP -> BOTSKIP = true
                    BOT_CMD_ONTHIS -> {
//                        OFFLIST.add(SESSIONMAP[userid] ?: return)
                        OFFLIST.add(userid ?: "")
                    }
                    BOT_CMD_OFFTHIS -> {
//                        OFFLIST.remove(SESSIONMAP[userid] ?: return)
                        OFFLIST.remove(userid)
                    }
                    BOT_CMD_CLEAROFFLIST -> OFFLIST.clear()
                    BOT_CMD_CLEARSESSIONS -> SESSIONS.clear()
                    BOT_CMD_ONTROOM -> BOTONROOM = true
                    BOT_CMD_OFFROOM -> BOTONROOM = false
                }
            }
        }
    }

    private fun convertGroupMSG(gM: String, gN: String): Array<String> {
        val g = gN.split("@")[0]
        val u = gM.split(":")[0]
        if (gM.length < u.length + 2) {
            return arrayOf("", "$u@$g")
        }
        val msg = gM.substring(u.length + 2)
        return arrayOf(msg, "$u@$g")
    }

    override fun toString(): String {
        if (BOTSKIP) {
            BOTSKIP = false
            return "[$BOT_CMD_SKIP]"
        }
        if (userid in OFFLIST) {
            return "[$BOT_CMD_SKIP]"
        }
        var userid = this.userid ?: ""
        if (!BOTONROOM && "chatroom" in userid) {
            return "[$BOT_CMD_SKIP]"
        }
        var sessionid = SESSIONS[userid] ?: ""
        var msg = this.msg
        XposedBridge.log("Aichat uid=$userid,msg=$msg")
        if ("chatroom" in userid) {
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
            OFFLIST.add(userid)
        }
        return s//"$s[${this.userid}]"
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
        const val BOT_TAG = "bot"
        const val BOT_CMD_SKIP = "BOTSKIP"
        const val BOT_CMD_ON = "BOTON"
        const val BOT_CMD_OFF = "BOTOFF"
        const val BOT_CMD_OFFTHIS = "BOTOFFTHIS"
        const val BOT_CMD_ONTHIS = "BOTONTHIS"
        const val BOT_CMD_OFFROOM = "BOTOFFROOM"
        const val BOT_CMD_ONTROOM = "BOTONROOM"
        const val BOT_CMD_CLEAROFFLIST = "BOTCLEAROFFLIST"
        const val BOT_CMD_CLEARSESSIONS = "BOTCLEARSESSIONS"
        val CMDS = arrayOf(
                BOT_CMD_ON,
                BOT_CMD_OFF,
                BOT_CMD_SKIP,
                BOT_CMD_ONTHIS,
                BOT_CMD_OFFTHIS,
                BOT_CMD_CLEAROFFLIST,
                BOT_CMD_CLEARSESSIONS,
                BOT_CMD_ONTROOM,
                BOT_CMD_OFFROOM
        )
        val SESSIONS: HashMap<String, String> by lazy { HashMap<String, String>() }
        val SESSIONMAP: HashMap<String, String> by lazy { HashMap<String, String>() }
        val OFFLIST: ArrayList<String> by lazy { ArrayList<String>() }
        val TOKEN: String by lazy { AuthService.auth ?: "" }
        val RANDOM by lazy { Random(System.currentTimeMillis()) }
        var BOTON = true
        var BOTSKIP = false
        var BOTONROOM = false
    }
}