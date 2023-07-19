package com.jhr.dsxysplugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import com.jhr.dsxysplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.components.IMediaDetailPageDataComponent
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.action.PlayAction
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.TextUtil.urlEncode
import com.su.mediabox.pluginapi.util.UIUtil.dp
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class MediaDetailPageDataComponent : IMediaDetailPageDataComponent {

    override suspend fun getMediaDetailData(partUrl: String): Triple<String, String, List<BaseData>> {
        var cover = ""
        var title = ""
        var desc = ""
        var score = -1F
        // 导演
        var director = ""
        // 主演
        var protagonist = ""
        // 编剧
        var scriptwriter= ""
        // 上映
        var show = ""
        // 更新时间
        var time = ""
        var upState = ""
        val url = Const.host + partUrl
        val tags = mutableListOf<TagData>()
        val details = mutableListOf<BaseData>()

        val document = JsoupUtil.getDocument(url)

        val content = document.select(".main")
        val moduleInfo = document.select(".module-main")[0]
        // ------------- 番剧头部信息
        cover = moduleInfo.select(".module-item-pic").select("img").attr("data-original")
        title = moduleInfo.select(".module-info-heading").select("h1").text()
        // 更新状况
        val upStateItems = moduleInfo.select(".module-info-items").select(".module-info-item")
        for (upStateEm in upStateItems){
            val t = upStateEm.text()
            if (t.contains("集数：") || t.contains("备注：") || t.contains("连载：") || t.contains("片长：")){
                upState = t
            }
            if (t.contains("上映：")){
                show = t
            }
            if (t.contains("更新：")){
                time = t
            }
            if (t.contains("导演：")){
                director = t
            }
            if (t.contains("编剧：")){
                scriptwriter = t
            }
            if (t.contains("主演：")){
                protagonist = t
            }
        }
        val span = moduleInfo.select(".module-info-tag").select(".module-info-tag-link")
        //年份
        val yearEm = span[0].select("a")
        val year = Regex("\\d+").find(yearEm.text())?.value
        if (year != null)
            tags.add(TagData(year).apply {
                action = ClassifyAction.obtain(
                    yearEm.attr("href"),
                    "", year
                )
            })
        //地区
        val animeArea = span[1].select("a")
        tags.add(TagData(animeArea.text()).apply {
            action = ClassifyAction.obtain(
                animeArea.attr("href"),
                "",
                animeArea.text()
            )
        })
        //类型
        val typeElements: Elements = span[2].select("a")
        for (l in typeElements.indices) {
            tags.add(TagData(typeElements[l].text()).apply {
                action = ClassifyAction.obtain(
                    typeElements[l].attr("href"),
                    "",
                    typeElements[l].text()
                )
            })
        }

        //评分
        score = 0F
        //动漫介绍
        desc = moduleInfo.select(".module-info-introduction-content").text()

        // ---------------------------------- 播放列表+header
        val module = content.select("div[class='module']")[0]
        val playNameList = module.select(".module-tab-items-box").select(".module-tab-item")
        val playEpisodeList = module.select("#panel1")
        for (index in 0..playNameList.size) {
            val playName = playNameList.getOrNull(index)
            val playEpisode = playEpisodeList.getOrNull(index)
            if (playName != null && playEpisode != null) {

                val episodes = parseEpisodes(playEpisode)

                if (episodes.isNullOrEmpty())
                    continue

                details.add(
                    SimpleTextData(
                        playName.select("span")
                            .text() + "(${episodes.size}集)"
                    ).apply {
                        fontSize = 16F
                        fontColor = Color.WHITE
                    }
                )

                details.add(EpisodeListData(episodes))
            }
        }
        // ----------------------------------  系列动漫推荐
        content.select("div[class='module']")[1].also {
            val series = parseSeries(it)
            if (series.isNotEmpty()) {
                details.add(
                    SimpleTextData("其他系列作品").apply {
                        fontSize = 16F
                        fontColor = Color.WHITE
                    }
                )
                details.addAll(series)
            }
        }
        return Triple(cover, title, mutableListOf<BaseData>().apply {
            add(Cover1Data(cover, score = score).apply {
                layoutConfig =
                    BaseData.LayoutConfig(
                        itemSpacing = 12.dp,
                        listLeftEdge = 12.dp,
                        listRightEdge = 12.dp
                    )
            })
            add(
                SimpleTextData(title).apply {
                    fontColor = Color.WHITE
                    fontSize = 20F
                    gravity = Gravity.CENTER
                    fontStyle = 1
                }
            )
            add(TagFlowData(tags))
            add(
                LongTextData(desc).apply {
                    fontColor = Color.WHITE
                }
            )
            add(SimpleTextData("·$director").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            if (scriptwriter.isNotBlank()) add(SimpleTextData("·$scriptwriter").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$protagonist").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            if (show.isNotBlank()) add(SimpleTextData("·$show").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$time").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$upState").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(LongTextData(douBanSearch(title)).apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            addAll(details)
        })
    }

    private fun parseEpisodes(element: Element): List<EpisodeData> {
        val episodeList = mutableListOf<EpisodeData>()
        val elements: Elements = element.select(".module-play-list").select("a")
        for (k in elements.indices) {
            val episodeUrl = elements[k].attr("href")
            episodeList.add(
                EpisodeData(elements[k].text(), episodeUrl).apply {
                    action = PlayAction.obtain(episodeUrl)
                }
            )
        }
        return episodeList
    }

    private fun parseSeries(element: Element): List<MediaInfo1Data> {
        val videoInfoItemDataList = mutableListOf<MediaInfo1Data>()
        val results: Elements = element.select(".module-main ").select("a")
        for (i in results.indices) {
            val cover = results[i].select("img").attr("data-original")
            val title = results[i].attr("title")
            val url = results[i].attr("href").replace("/p/","/v/").replace("-1-1","")
            val item = MediaInfo1Data(
                title, cover, Const.host + url,
                nameColor = Color.WHITE, coverHeight = 120.dp
            ).apply {
                action = DetailAction.obtain(url)
            }
            videoInfoItemDataList.add(item)
        }
        return videoInfoItemDataList
    }

    private fun douBanSearch(name: String) =
        "·豆瓣评分：https://m.douban.com/search/?query=${name.urlEncode()}"
}