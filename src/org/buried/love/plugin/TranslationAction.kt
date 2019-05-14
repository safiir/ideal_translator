package org.buried.love.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.JBColor
import org.apache.commons.lang3.StringUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.HttpClientUtils
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.TextUtils
import org.buried.love.util.stringify
import org.buried.love.util.toJson
import java.awt.Color
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Translation Action
 *
 * @author safiir
 * @date 2019-05-13
 */
class TranslationAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(PlatformDataKeys.EDITOR) ?: return
        val model = editor.selectionModel
        val selectedText = model.selectedText

        if (TextUtils.isEmpty(selectedText)) {
            return
        }

        val from = "please_replace_with_real_api_fromKey"
        val key = "please_replace_with_real_api_key"

        val baseUrl = "http://fanyi.youdao.com/openapi.do?keyfrom=%s&key=%s&type=data&doctype=json&version=1.1&q=".format(from, key)

        val httpClient = HttpClientBuilder.create().build()

        val httpGet = HttpGet(baseUrl + selectedText!!)

        try {
            val httpResponse = httpClient.execute(httpGet)

            val bufferedReader = BufferedReader(
                    InputStreamReader(httpResponse.entity.content, "UTF-8"))

            val builder = StringBuilder()

            var line: String? = StringUtils.EMPTY

            while (bufferedReader.readLine().also { line = it } != null) {
                builder.append(line)
            }

            showPopupBalloon(editor, builder.toString().toJson().stringify())

        } catch (e1: IOException) {
            e1.printStackTrace()
        } finally {
            HttpClientUtils.closeQuietly(httpClient)
        }
    }

    private fun showPopupBalloon(editor: Editor, result: String) {
        ApplicationManager.getApplication().invokeLater {
            val factory = JBPopupFactory.getInstance()
            factory.createHtmlTextBalloonBuilder(result, null, JBColor(Color(0, 0, 0), Color(0, 0, 0)), null)
//                    .setFadeoutTime(5000)
                    .createBalloon()
                    .show(factory.guessBestPopupLocation(editor), Balloon.Position.below)
        }
    }
}