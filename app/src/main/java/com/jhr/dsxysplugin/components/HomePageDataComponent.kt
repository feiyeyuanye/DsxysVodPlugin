package com.jhr.dsxysplugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.ImageView
import com.jhr.dsxysplugin.components.Const.host
import com.jhr.dsxysplugin.components.Const.layoutSpanCount
import com.jhr.dsxysplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.CustomPageAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.action.WebBrowserAction
import com.su.mediabox.pluginapi.components.IHomePageDataComponent
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.AppUtil
import com.su.mediabox.pluginapi.util.PluginPreferenceIns
import com.su.mediabox.pluginapi.util.UIUtil.dp
import com.su.mediabox.pluginapi.util.WebUtil
import com.su.mediabox.pluginapi.util.WebUtilIns
import org.jsoup.Jsoup

class HomePageDataComponent : IHomePageDataComponent {

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val url = host
        val data = mutableListOf<BaseData>()
        val doc = JsoupUtil.getDocument(url)
        //2.菜单第一行
        data.add(
            MediaInfo1Data(
                "", Const.Icon.RANK, "", "电影库",
                otherColor = 0xff757575.toInt(),
                coverScaleType = ImageView.ScaleType.FIT_CENTER,
                coverHeight = 24.dp,
                gravity = Gravity.CENTER
            ).apply {
                layoutConfig = BaseData.LayoutConfig(layoutSpanCount)
                spanSize = layoutSpanCount / 4
                action = ClassifyAction.obtain(Const.host+"/vodshow/1-----------.html", "电影库")
            })
        data.add(
            MediaInfo1Data(
                "", Const.Icon.TABLE, "", "剧集库",
                otherColor = 0xff757575.toInt(),
                coverScaleType = ImageView.ScaleType.FIT_CENTER,
                coverHeight = 24.dp,
                gravity = Gravity.CENTER
            ).apply {
                spanSize = layoutSpanCount / 4
                action = ClassifyAction.obtain(Const.host+"/vodshow/2-----------.html", "剧集库")
            })
        data.add(
            MediaInfo1Data(
                "", Const.Icon.UPDATE, "", "综艺库",
                otherColor = 0xff757575.toInt(),
                coverScaleType = ImageView.ScaleType.FIT_CENTER,
                coverHeight = 24.dp,
                gravity = Gravity.CENTER
            ).apply {
                spanSize = layoutSpanCount / 4
                action = ClassifyAction.obtain(Const.host+"/vodshow/3-----------.html", "综艺库")
            })
        data.add(
            MediaInfo1Data(
                "", Const.Icon.TOPIC, "", "动漫库",
                otherColor = 0xff757575.toInt(),
                coverScaleType = ImageView.ScaleType.FIT_CENTER,
                coverHeight = 24.dp,
                gravity = Gravity.CENTER
            ).apply {
                spanSize = layoutSpanCount / 4
                action = ClassifyAction.obtain(Const.host+"/vodshow/4-----------.html", "动漫库")
            })
        //2.菜单第二行
        data.add(
            MediaInfo1Data(
                "", Const.Icon.RANK, "", "今日更新",
                otherColor = 0xff757575.toInt(),
                coverScaleType = ImageView.ScaleType.FIT_CENTER,
                coverHeight = 24.dp,
                gravity = Gravity.CENTER
            ).apply {
                spanSize = layoutSpanCount / 4
                action = CustomPageAction.obtain(UpdatedTodayComponent::class.java)
            })
        data.add(
            MediaInfo1Data(
                "", Const.Icon.RANK, "", "新片上线",
                otherColor = 0xff757575.toInt(),
                coverScaleType = ImageView.ScaleType.FIT_CENTER,
                coverHeight = 24.dp,
                gravity = Gravity.CENTER
            ).apply {
                spanSize = layoutSpanCount / 4
                action = CustomPageAction.obtain(NewReleasesComponent::class.java)
            })
        data.add(
            MediaInfo1Data(
                "", Const.Icon.RANK, "", "热门榜单",
                otherColor = 0xff757575.toInt(),
                coverScaleType = ImageView.ScaleType.FIT_CENTER,
                coverHeight = 24.dp,
                gravity = Gravity.CENTER
            ).apply {
                spanSize = layoutSpanCount / 4
                action = CustomPageAction.obtain(LatestHotComponent::class.java)
            })
        data.add(
            MediaInfo1Data(
                "", Const.Icon.RANK, "", "排行榜单",
                otherColor = 0xff757575.toInt(),
                coverScaleType = ImageView.ScaleType.FIT_CENTER,
                coverHeight = 24.dp,
                gravity = Gravity.CENTER
            ).apply {
                spanSize = layoutSpanCount / 4
                action = CustomPageAction.obtain(RankPageDataComponent::class.java)
            })

        val modules = doc.select("div[class='main']").select(".content").select("div[class='module']")
        for (module in modules){
            val typeName = module.select(".module-heading").select("a").first()?.ownText()
            if (!typeName.isNullOrBlank()) {
                data.add(SimpleTextData(typeName).apply {
                    fontSize = 16F
                    fontStyle = Typeface.BOLD
                    fontColor = Color.BLACK
                    spanSize = layoutSpanCount
                })
            }
            val list = module.select(".module-main")[0].select("a")
                for ((index,video) in list.withIndex()){
                    video.apply {
                        val name = attr("title")
                        // https://dsxys.pro/p/615568-1-1.html
                        // https://dsxys.pro/v/615568.html
                        val videoUrl = attr("href").replace("/p/","/v/").replace("-1-1","")
                        val coverUrl = select("img").attr("data-original")
                        val episode = select(".module-item-note").text()
//                        Log.e("TAG", "添加视频 ($name) ($videoUrl) ($coverUrl) ($episode)")
                        if (!name.isNullOrBlank() && !videoUrl.isNullOrBlank() && !coverUrl.isNullOrBlank()) {
                            data.add(
                                MediaInfo1Data(name, coverUrl, videoUrl, episode ?: "")
                                    .apply {
                                        spanSize = layoutSpanCount / 3
                                        action = DetailAction.obtain(videoUrl)
                                    })
                        }
                    }
                    if (index == 11) break
                }
        }
        return data
    }
}