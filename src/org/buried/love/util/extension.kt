package org.buried.love.util

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.awt.Color

/**
 * Extension Set
 *
 * @author safiir
 * @date 2019-05-13
 */
inline fun <reified T> Gson.fromJson(json: String) = fromJson(json, T::class.java)

fun Color.toRGB() : String {
    return "rgb(%s,%s,%s)".format(this.red, this.green, this.blue)
}


fun String.toJson() : JsonObject = Gson().fromJson<JsonObject>(this)

fun String.color(color : Color) : String {
    return "<b style=\"color:%s\">%s</b>".format(color.toRGB(), this)
}

fun StringBuilder.newline() : StringBuilder {
    this.append("\n")
    return this
}


fun JsonObject.stringify() : String {

    val errorCode = this.get("errorCode").asString

    when (errorCode) {
        "20" -> {
            return "要翻译的文本过长"
        }
        "30" -> {
            return "无法进行有效的翻译"
        }
        "40" -> {
            return "不支持的语言类型"
        }
        "50" -> {
            return "无效的key"
        }
        "60" -> {
            return "无词典结果"
        }
    }

    val builder = StringBuilder()

    builder.append(this.get("translation").asJsonArray.first().asString).newline()

    if(this.get("basic") == null){
        return "No results found"
    }

    val basic = this.get("basic").asJsonObject

    var onlyPhonetic = true

    if(basic.get("uk-phonetic") != null){
        builder.append("英[%s]".format(basic.get("uk-phonetic").asString))
        onlyPhonetic = false
    }

    if(basic.get("us-phonetic") != null){
        builder.append("美[%s]".format(basic.get("us-phonetic").asString))
        onlyPhonetic = false
    }

    if (onlyPhonetic && basic.get("phonetic") != null){
        builder.append("[%s]".format(basic.get("phonetic").asString))
    }

    builder.newline()

    basic.get("explains").asJsonArray.forEach {
        builder.append(it.asString).newline()
    }

    builder.append("网络释义").newline()

    val webInterpretation = this.get("web").asJsonArray

    webInterpretation.forEach {
        val interpretation = it.asJsonObject
        builder.append(interpretation.get("key").asString.color(Color(53, 161, 212)) + " ")
        val value = interpretation.get("value").asJsonArray.joinToString(";") { it.asString }
        builder.append(value).newline()
    }

    return builder.toString()
}