package com.jhr.dsxysplugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import com.jhr.dsxysplugin.actions.CustomAction
import com.jhr.dsxysplugin.components.Const.host
import com.jhr.dsxysplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.ICustomPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.SimpleTextData
import com.su.mediabox.pluginapi.data.TagData
import com.su.mediabox.pluginapi.data.ViewPagerData
import com.su.mediabox.pluginapi.util.UIUtil.dp
import org.jsoup.nodes.Element

class RankPageDataComponent : ICustomPageDataComponent {

    override val pageName = "排行榜"
    override fun menus() = mutableListOf(CustomAction())

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val url = "$host/label/hot.html"
        val doc = JsoupUtil.getDocument(url)

        val rank1 = doc.select(".module-card-items")[0].select(".module-paper-item")[0].let {
                    object : ViewPagerData.PageLoader {
                        override fun pageName(page: Int): String {
                            return "电影榜"
                        }

                        override suspend fun loadData(page: Int): List<BaseData> {
                            return getTotalRankData(it)
                        }
                    }
                }
        val rank2 = doc.select(".module-card-items")[0].select(".module-paper-item")[1].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "连续剧榜"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        val rank3 = doc.select(".module-card-items")[0].select(".module-paper-item")[2].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "综艺榜"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        val rank4 = doc.select(".module-card-items")[0].select(".module-paper-item")[3].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "动漫榜"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        return listOf(ViewPagerData(mutableListOf(rank1, rank2,rank3,rank4)).apply {
            layoutConfig = BaseData.LayoutConfig(
                itemSpacing = 0,
                listLeftEdge = 0,
                listRightEdge = 0
            )
        })
    }

    private fun getTotalRankData(element: Element): List<BaseData> {
        val data = mutableListOf<BaseData>()
        val SPAN_COUNT = 16
        element.select(".module-paper-item-main").select("a").forEach {
            val rankIdx = it.select(".module-paper-item-num").text()
            val textName = it.select(".module-paper-item-info").select("span").text()
            val href = it.select("a").attr("href").replace("/p/","/v/").replace("-1-1","")
            val rankValue = it.select(".module-paper-item-info").select("p").text()
            // 序号
            data.add(TagData(rankIdx).apply {
                spanSize = 2
                paddingLeft = 6.dp
            })
            // 名称
            data.add(
                SimpleTextData(textName).apply {
                    spanSize = 9
                    fontStyle = Typeface.BOLD
                    fontColor = Color.BLACK
                    paddingTop = 6.dp
                    paddingBottom = 6.dp
                    paddingLeft = 0.dp
                    paddingRight = 0.dp
                    action = DetailAction.obtain(href)
                })
            //更新集数
            data.add(SimpleTextData(rankValue).apply {
                spanSize = (SPAN_COUNT / 4) + 1
                fontStyle = Typeface.BOLD
                paddingRight = 6.dp
                gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
            })
        }
        data[0].layoutConfig = BaseData.LayoutConfig(spanCount = SPAN_COUNT)
        return data
    }
}