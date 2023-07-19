package com.jhr.dsxysplugin.components

import android.util.Log
import com.jhr.dsxysplugin.util.JsoupUtil
import com.jhr.dsxysplugin.util.ParseHtmlUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.WebBrowserAction
import com.su.mediabox.pluginapi.components.IMediaClassifyPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.ClassifyItemData
import com.su.mediabox.pluginapi.util.AppUtil
import com.su.mediabox.pluginapi.util.PluginPreferenceIns
import com.su.mediabox.pluginapi.util.TextUtil.urlDecode
import com.su.mediabox.pluginapi.util.WebUtil
import com.su.mediabox.pluginapi.util.WebUtilIns
import org.jsoup.Jsoup
import java.lang.StringBuilder

class MediaClassifyPageDataComponent : IMediaClassifyPageDataComponent {
    var classify : String = Const.host +"/vodshow/2-----------.html"

    override suspend fun getClassifyItemData(): List<ClassifyItemData> {
        val classifyItemDataList = mutableListOf<ClassifyItemData>()
        //示例：使用WebUtil解析动态生成的分类项
        val cookies = mapOf("cookie" to PluginPreferenceIns.get(JsoupUtil.cfClearanceKey, ""))
        Log.e("TAG","classify ${classify}")
        val document = Jsoup.parse(
            WebUtilIns.getRenderedHtmlCode(
                 classify, loadPolicy = object :
                    WebUtil.LoadPolicy by WebUtil.DefaultLoadPolicy {
                    override val headers = cookies
                    override val userAgentString = Const.ua
                    override val isClearEnv = false
                }
            )
        )
        if (document.select("title").text() == "大师兄安全验证"){
            WebBrowserAction.obtain(classify).go(AppUtil.appContext)
            throw RuntimeException("大师兄安全验证")
        }
        document.select("div[class='module-main module-class']").select(".module-class-items").forEach {
            classifyItemDataList.addAll(ParseHtmlUtil.parseClassifyEm(it))
        }
        return classifyItemDataList
    }

    override suspend fun getClassifyData(
        classifyAction: ClassifyAction,
        page: Int
    ): List<BaseData> {
        val classifyList = mutableListOf<BaseData>()
        // /vodshow/3---%E7%A7%91%E5%B9%BB--------.html
        Log.e("TAG", "获取分类数据 ${classifyAction.url}")

        val str = classifyAction.url?.urlDecode() ?: ""
        // 指定要插入的字符 charToInsert
        val charToInsert = "${page}"
        // 计算要插入字符的索引 indexToInsert 为 str.length - 8，即字符串的倒数第 9 个位置。
        var indexToInsert = str.length - 8
        // 时间选项会插入到字符串末尾
        if (str[indexToInsert] != '-') {
            indexToInsert -= 4
        }
        // 使用 StringBuilder 创建一个可变的字符串，调用 insert() 方法将字符插入到指定位置，最后将结果转换回不可变字符串。
        var url = StringBuilder(str).insert(indexToInsert, charToInsert).toString()
        if (!url.startsWith(Const.host)){
            url = Const.host + url
        }else{
            // 默认进入分类项和选择分类项，url 类似于：/vodshow/3---%E7%83%AD%E8%A1%80--------.html
            // 通过自定义页面第一次进入分类项，url 类似于：https://www.freeok.vip/vodshow/3--------1---.html
            // 电影、剧集、动漫、综艺的分类项都不同，所以需要动态改变
            classify = url
        }
        Log.e("TAG", "获取分类数据 $url")
        val document = JsoupUtil.getDocument(url)
        classifyList.addAll(ParseHtmlUtil.parseClassifyEm(document, url))
        return classifyList
    }
}