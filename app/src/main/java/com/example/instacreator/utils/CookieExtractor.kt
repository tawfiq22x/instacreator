package com.example.instacreator.utils

import okhttp3.HttpUrl
import org.instagram4j.Instagram4j

object CookieExtractor {
    fun extract(client: Instagram4j): Map<String, String> {
        val cookies = mutableMapOf<String, String>()
        client.client.httpClient.cookieJar.let { jar ->
            jar.loadForRequest(HttpUrl.get("https://i.instagram.com")).forEach { cookie ->
                when (cookie.name) {
                    "sessionid" -> cookies["sessionid"] = cookie.value
                    "csrftoken" -> cookies["csrftoken"] = cookie.value
                    "ds_user_id" -> cookies["ds_user_id"] = cookie.value
                    "ig_did" -> cookies["ig_did"] = cookie.value
                }
            }
        }
        return cookies
    }
}