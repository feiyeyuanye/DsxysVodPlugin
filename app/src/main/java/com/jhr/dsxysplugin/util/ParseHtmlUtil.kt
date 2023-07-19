package com.jhr.dsxysplugin.util

import com.jhr.dsxysplugin.components.Const.host
import com.jhr.dsxysplugin.components.Const.layoutSpanCount
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.UIUtil.dp
import java.net.URL

object ParseHtmlUtil {

    fun getCoverUrl(cover: String, imageReferer: String): String {
        return when {
            cover.startsWith("//") -> {
                try {
                    "${URL(imageReferer).protocol}:$cover"
                } catch (e: Exception) {
                    e.printStackTrace()
                    cover
                }
            }
            cover.startsWith("/") -> {
                //url不全的情况
                host + cover
            }
            else -> cover
        }
    }

    /**
     * 解析搜索的元素
     * @param element ul的父元素
     */
    fun parseSearchEm(element: Element, imageReferer: String): List<BaseData> {
        val videoInfoItemDataList = mutableListOf<BaseData>()

        val lpic = element.select("div[class='module-items module-card-items']")
        val results: Elements = lpic.select("div[class='module-card-item module-item']")
        for (i in results.indices) {
            var cover = results[i].select("img").attr("data-original")
            if (imageReferer.isNotBlank())
                cover = getCoverUrl(cover, imageReferer)
            val title = results[i].select(".module-card-item-title").select("a").text()
            val url = results[i].select(".module-card-item-poster").select("a").attr("href")
            val episode = results[i].select(".module-item-note").text()
            val tags = mutableListOf<TagData>()
            val tag = results[i].select(".module-info-item-content")[0].text().split("/")
            tags.add(TagData(tag[0]))
            tags.add(TagData(tag[1]))
            tag[2].split(",").forEach{
                tags.add(TagData(it))
            }
            val describe = results[i].select(".module-info-item-content")[1].text()
            val item = MediaInfo2Data(
                title, cover, host + url, episode, describe, tags
            ).apply {
                    action = DetailAction.obtain(url)
                }
            videoInfoItemDataList.add(item)
        }
        return videoInfoItemDataList
    }
    /**
     * 解析分类下的元素
     * @param element ul的父元素
     */
    fun parseClassifyEm(element: Element, imageReferer: String): List<BaseData> {
        val videoInfoItemDataList = mutableListOf<BaseData>()
        val results: Elements = element.select("a[class='module-poster-item module-item']")
        for (i in results.indices) {
            val title = results[i].attr("title")
            var cover = results[i].select("img").attr("data-original")
            if (imageReferer.isNotBlank())
                cover = getCoverUrl(cover, imageReferer)
            val url = results[i].attr("href").replace("/p/","/v/").replace("-1-1","")
            val episode = results[i].select(".module-item-note").text()
            val item = MediaInfo1Data(title, cover, host + url, episode ?: "")
                .apply {
                    layoutConfig = BaseData.LayoutConfig(layoutSpanCount, 14.dp)
                    spanSize = layoutSpanCount / 3
                    action = DetailAction.obtain(url)
                }
            videoInfoItemDataList.add(item)
        }
        return videoInfoItemDataList
    }
    /**
     * 解析分类元素
     */
    fun parseClassifyEm(element: Element): List<ClassifyItemData> {
        val classifyItemDataList = mutableListOf<ClassifyItemData>()
        val classifyCategory = element.select(".module-item-title").text()
        val li = element.select(".module-item-box").select("a")
        for (em in li){
            classifyItemDataList.add(ClassifyItemData().apply {
                    action = ClassifyAction.obtain(
                        em.attr("href").apply {
//                            Log.d("分类链接", this)
                        },
                        classifyCategory,
                        em.text()
                    )
                })
            }
        return classifyItemDataList
    }
}