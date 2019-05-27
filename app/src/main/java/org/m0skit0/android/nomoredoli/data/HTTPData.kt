package org.m0skit0.android.nomoredoli.data

internal const val BOUTON_PUNCH_IN = "boutonE"
internal const val BOUTON_PUNCH_OUT = "boutonS"

internal val tokenRegex = "<input type=\"hidden\" name=\"token\" value=\"(.*?)\" />".toRegex()
internal val punchActionRegex = "<input type=\"hidden\" name=\"action\" value=\"(.*?)\">".toRegex()
internal val punchUserIdRegex = "<input type=\"hidden\" name=\"idUser\" value=\"(.*?)\">".toRegex()
internal val punchBoutonRegex = "<input type=\"submit\" class=\"button\" name=\"(bouton[ES])\" value=\"(.*?)\">".toRegex()

internal const val baseCookie = "hibext_instdsigdipv2=1;"

internal val baseHeaders = mapOf(
    "User-Agent" to "Mozilla/5.0 (X11; Linux x86_64; rv:60.0) Gecko/20100101 Firefox/60.0",
    "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Host" to "innocv.on.dolicloud.com",
    "Connection" to "keep-alive",
    "Accept-Encoding" to "gzip, deflate, br"
)

internal val baseLoginParameters = mapOf(
    "loginfunction" to "loginfunction",
    "tz" to "1",
    "tz_string" to "Europe/Madrid",
    "dst_observed" to "1",
    "dst_first" to "2019-03-31T01:59:00Z",
    "dst_second" to "2019-10-27T02:59:00Z",
    "screenwidth" to "1920",
    "screenheight" to "947",
    "dol_hide_topmenu" to "",
    "dol_hide_leftmenu" to "",
    "dol_optimize_smallscreen" to "",
    "dol_no_mouse_hover" to "",
    "dol_use_jmobile" to ""
)