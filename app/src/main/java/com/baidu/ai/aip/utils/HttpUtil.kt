package com.baidu.ai.aip.utils

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * http 工具类
 */
object HttpUtil {

    @Throws(Exception::class)
    fun post(requestUrl: String, accessToken: String, params: String): String {
        val contentType = "application/x-www-form-urlencoded"
        return HttpUtil.post(requestUrl, accessToken, contentType, params)
    }

    @Throws(Exception::class)
    fun post(requestUrl: String, accessToken: String, contentType: String, params: String): String {
        var encoding = "UTF-8"
        if (requestUrl.contains("nlp")) {
            encoding = "GBK"
        }
        return HttpUtil.post(requestUrl, accessToken, contentType, params, encoding)
    }

    @Throws(Exception::class)
    fun post(requestUrl: String, accessToken: String, contentType: String, params: String, encoding: String): String {
        val url = "$requestUrl?access_token=$accessToken"
        return HttpUtil.postGeneralUrl(url, contentType, params, encoding)
    }

    @Throws(Exception::class)
    fun postGeneralUrl(generalUrl: String, contentType: String, params: String, encoding: String): String {
        val url = URL(generalUrl)
        // 打开和URL之间的连接
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        // 设置通用的请求属性
        connection.setRequestProperty("Content-Type", contentType)
        connection.setRequestProperty("Connection", "Keep-Alive")
        connection.useCaches = false
        connection.doOutput = true
        connection.doInput = true

        // 得到请求的输出流对象
        val out = DataOutputStream(connection.outputStream)
        out.write(params.toByteArray(charset(encoding)))
        out.flush()
        out.close()

        // 建立实际的连接
        connection.connect()
        // 获取所有响应头字段
        val headers = connection.headerFields
        // 遍历所有的响应头字段
        for (key in headers.keys) {
            System.err.println(key + "--->" + headers[key])
        }
        // 定义 BufferedReader输入流来读取URL的响应
        var `in`: BufferedReader? = null
        `in` = BufferedReader(
                InputStreamReader(connection.inputStream, encoding))
        var result = ""
        var getLine: String? = `in`.readLine()
        while (getLine != null) {
            result += getLine
            getLine = `in`.readLine()
        }
        `in`.close()
        System.err.println("result:$result")
        return result
    }
}