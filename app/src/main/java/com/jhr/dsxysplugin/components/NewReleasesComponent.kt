package com.jhr.dsxysplugin.components

import com.jhr.dsxysplugin.components.Const.host
import com.jhr.dsxysplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.ICustomPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.MediaInfo2Data
import com.su.mediabox.pluginapi.data.TagData

/**
 * FileName: NewReleasesComponent
 * Founder: Jiang Houren
 * Create Date: 2023/6/27 19:31
 * Profile: 新片上线
 */
class NewReleasesComponent: ICustomPageDataComponent {

    override val pageName: String
        get() = "新片上线"

    /**
     * bug -> 这里只有一页，所以上拉加载会添加重复数据。
     */
    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val hostUrl = Const.host + "/label/new.html"
        val document = JsoupUtil.getDocument(hostUrl)

        val data = mutableListOf<BaseData>()

        val li = document.select(".module-main")[1].select(".module-card-item")
        for (liE in li){
            val title =  liE.select(".module-card-item-title").text()
            val cover = liE.select("img").attr("data-original")
            val url = liE.select(".module-card-item-poster").attr("href")
            val episode = liE.select(".module-card-item-class").text()+ " [" +liE.select(".module-item-note").text()+"]"
            val describe = liE.select(".module-info-item-content")[1].text()
            val tags = mutableListOf<TagData>()
            val tag = liE.select(".module-info-item-content")[0].text().replace(" ","").split("/")
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
                    title, cover, host + url, episode, describe, tags
                ).apply {
                    action = DetailAction.obtain(url)
                })
        }
        return data
    }
}