package com.jhr.dsxysplugin.components

import com.jhr.dsxysplugin.actions.CustomAction
import com.jhr.dsxysplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.ICustomPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.MediaInfo2Data
import com.su.mediabox.pluginapi.data.TagData
import com.su.mediabox.pluginapi.data.ViewPagerData
import org.jsoup.nodes.Element

/**
 * FileName: LatestHotComponent
 * Founder: Jiang Houren
 * Create Date: 2023/6/27 19:59
 * Profile: 热门榜单
 */
class LatestHotComponent: ICustomPageDataComponent {
    override val pageName: String
        get() = "热门榜单"
    override fun menus() = mutableListOf(CustomAction())

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val hostUrl = Const.host + "/label/hot.html"
        val doc = JsoupUtil.getDocument(hostUrl)

        val rank1 = doc.select(".module-card-items")[1].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "最近热门"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        val rank2 = doc.select(".module-card-items")[2].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "近期热门"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        return listOf(ViewPagerData(mutableListOf(rank1, rank2)).apply {
            layoutConfig = BaseData.LayoutConfig(
                itemSpacing = 0,
                listLeftEdge = 0,
                listRightEdge = 0
            )
        })

    }

    private fun getTotalRankData(element: Element): List<BaseData> {
        val data = mutableListOf<BaseData>()

        val li = element.select(".module-card-item")
        for (liE in li){
            val title = liE.select(".module-item-top").text() +" "+ liE.select(".module-card-item-title").text()
            val cover = liE.select("img").attr("data-original")
            val url = liE.select(".module-card-item-poster").attr("href")
            val episode = liE.select(".module-card-item-class").text()+ " [" +liE.select(".module-item-note").text()+"]"
            val describe = liE.select(".module-info-item-content")[1].text()
            val tag = liE.select(".module-info-item-content")[0].text().replace(" ","").split("/")
            val tags = mutableListOf<TagData>()
            for (type in tag){
                if (type.contains(",")){
                    type.split(",").forEach{
                        tags.add(TagData(it))
                    }
                }else{
                    tags.add(TagData(type))
                }
            }
            data.add(
                MediaInfo2Data(
                    title, cover, Const.host + url, episode, describe, tags
                ).apply {
                    action = DetailAction.obtain(url)
                })
        }
        return data
    }
}