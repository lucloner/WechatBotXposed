package com.baidu.ai.aip.auth

import net.vicp.biggee.xposed.wechat.Aichat
import org.json.JSONObject

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * 获取token类
 */
object AuthService {

    /**
     * 获取权限token
     * @return 返回示例：
     * {
     * "access_token": "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567",
     * "expires_in": 2592000
     * }
     */
    // 官网获取的 API Key 更新为你注册的
    // 官网获取的 Secret Key 更新为你注册的
    val auth: String?
        get() {
            val clientId = Aichat.APIKEY
            val clientSecret = Aichat.SECRETKEY
            return getAuth(clientId, clientSecret)
        }

    /**
     * 获取API访问token
     * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     * @param ak - 百度云官网获取的 API Key
     * @param sk - 百度云官网获取的 Securet Key
     * @return assess_token 示例：
     * "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
     */
    fun getAuth(ak: String, sk: String): String? {
        // 获取token地址
        val authHost = "https://aip.baidubce.com/oauth/2.0/token?"
        val getAccessTokenUrl = (authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + ak
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + sk)
        try {
            val realUrl = URL(getAccessTokenUrl)
            // 打开和URL之间的连接
            val connection = realUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            // 获取所有响应头字段
            val map = connection.headerFields
            // 遍历所有的响应头字段
            for (key in map.keys) {
                System.err.println(key + "--->" + map[key])
            }
            // 定义 BufferedReader输入流来读取URL的响应
            val `in` = BufferedReader(InputStreamReader(connection.inputStream))
            var result = ""
            var line: String? = `in`.readLine()
            while (line != null) {
                result += line
                line = `in`.readLine()
            }
            /**
             * 返回结果示例
             */
            System.err.println("result:$result")
            val jsonObject = JSONObject(result)
            try {
                expires_in = jsonObject.getString("expires_in").toLong()
            } catch (_: Exception) {
            }
            return jsonObject.getString("access_token")
        } catch (e: Exception) {
            System.err.printf("获取token失败！")
            e.printStackTrace(System.err)
        }

        return null
    }

    var expires_in = 2592000L

}