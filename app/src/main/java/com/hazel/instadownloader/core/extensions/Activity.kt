package com.hazel.instadownloader.core.extensions

import android.app.Activity
import com.hazel.instadownloader.R
import com.hazel.instadownloader.app.models.LanguageModel

fun Activity.langList(): ArrayList<LanguageModel> {
    val langList = ArrayList<LanguageModel>()
    langList.add(
        LanguageModel(
            languageIcon = R.drawable.english,
            languageName = "English",
            languageCode = "en"
        )
    )
    langList.add(
        LanguageModel(
            languageIcon = R.drawable.arbi,
            languageName = "عربي",
            languageCode = "ar"
        )
    )
    langList.add(
        LanguageModel(
            languageIcon = R.drawable.bangladash,
            languageName = "অ আ",
            languageCode = "bn"
        )
    )
    langList.add(
        LanguageModel(
            languageIcon = R.drawable.france,
            languageName = "François",
            languageCode = "fr"
        )
    )

    /*langList.add(
        LanguageModel(
            languageIcon = R.drawable.japan,
            languageName = "Japanese, japan",
            languageCode = "zh"
        )
    )*/
    langList.add(
        LanguageModel(
            languageIcon = R.drawable.china,
            languageName = "中国人",
            languageCode = "zh-CN"
        )
    )
    langList.add(
        LanguageModel(
            languageIcon = R.drawable.garmany,
            languageName = "Deutsch",
            languageCode = "de"
        )
    )
    langList.add(
        LanguageModel(
            languageIcon = R.drawable.hindi,
            languageName = "हिंदी",
            languageCode = "hi"
        )
    )
    langList.add(
        LanguageModel(
            languageIcon = R.drawable.indonasia,
            languageName = "Bahasa",
            languageCode = "in"
        )
    )
    langList.add(
        LanguageModel(
            languageIcon = R.drawable.italy,
            languageName = "Italiana",
            languageCode = "it"
        )
    )
    langList.add(
        LanguageModel(
            languageIcon = R.drawable.malysia,
            languageName = "Melayu",
            languageCode = "ms"
        )
    )

    langList.add(
        LanguageModel(
            languageIcon = R.drawable.russia,
            languageName = "Русский",
            languageCode = "ru"
        )
    )

    langList.add(
        LanguageModel(
            languageIcon = R.drawable.swedan,
            languageName = "Svenska",
            languageCode = "sv"
        )
    )
    langList.add(
        LanguageModel(
            languageIcon = R.drawable.denmark,
            languageName = "Dansk",
            languageCode = "da"
        )
    )

    langList.add(
        LanguageModel(
            languageIcon = R.drawable.natherland,
            languageName = "Dutch",
            languageCode = "nl"
        )
    )
    langList.add(
        LanguageModel(
            languageIcon = R.drawable.turky,
            languageName = "Türkçe",
            languageCode = "tr"
        )
    )
//    langList.add(
//        LanguageModel(
//            languageIcon = R.drawable.kurdistan,
//            languageName = "Kurdî",
//            languageCode = "ku"
//        )
//    )
    langList.add(
        LanguageModel(
            languageIcon = R.drawable.iran,
            languageName = "فارسی",
            languageCode = "fa"
        )
    )
    langList.add(
        LanguageModel(
            languageIcon = R.drawable.hebrew,
            languageName = "Hebrew",
            languageCode = "iw"
        )
    )
    langList.add(
        LanguageModel(
            languageIcon = R.drawable.purtgal,
            languageName = "Português",
            languageCode = "pt"
        )
    )

    langList.add(
        LanguageModel(
            languageIcon = R.drawable.pakistan,
            languageName = "اردو",
            languageCode = "ur"
        )
    )
    return langList
}