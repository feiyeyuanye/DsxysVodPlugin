package com.jhr.dsxysplugin.components

import android.util.Log
import com.jhr.dsxysplugin.components.Const.host
import com.jhr.dsxysplugin.util.JsoupUtil
import com.jhr.dsxysplugin.util.ParseHtmlUtil
import com.su.mediabox.pluginapi.components.IMediaSearchPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData

class MediaSearchPageDataComponent : IMediaSearchPageDataComponent {

    override suspend fun getSearchData(keyWord: String, page: Int): List<BaseData> {
        val searchResultList = mutableListOf<BaseData>()
        // https://dsxys.pro/sb/kemksmaksdl7nhZe3c1%E9%BE%99-/page/2.html
        // https://dsxys.pro/sb/kemksmaksdl7nhZe3c1%E7%8E%8B-/page/2.html
        val url = "${host}/sb/kemksmaksdl7nhZe3c1${keyWord}-/page/${page}.html"
        Log.e("TAG", url)

        val document = JsoupUtil.getDocument(url)
        searchResultList.addAll(ParseHtmlUtil.parseSearchEm(document, url))
        return searchResultList
    }

}