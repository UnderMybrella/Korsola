package dev.brella.korsola.core.css

inline class ConsolasFont(override val defaultFontSize: String) : AnsiScheme.Fonts {
    constructor(size: Double, sizeUnits: CssBuilder.SizeUnits? = null) : this(if (sizeUnits != null) "$size${sizeUnits.str}" else size.toString())

    override val defaultFontFamily: String
        get() = "Consolas"
}

inline class JetbrainsMonoFont(override val defaultFontSize: String) : AnsiScheme.Fonts {
    constructor(size: Double, sizeUnits: CssBuilder.SizeUnits? = null) : this(if (sizeUnits != null) "$size${sizeUnits.str}" else size.toString())

    override val defaultFontFamily: String
        get() = "\"JetBrains Mono\""

    override val defaultFontUrls: Array<CssBuilder.FontUrl>
        get() = arrayOf(
            CssBuilder.FontUrl.of("https://raw.githubusercontent.com/JetBrains/JetBrainsMono/master/ttf/JetBrainsMono-Regular.ttf" to "truetype"),
            CssBuilder.FontUrl.ofBold("https://raw.githubusercontent.com/JetBrains/JetBrainsMono/master/ttf/JetBrainsMono-Bold.ttf" to "truetype"),
            CssBuilder.FontUrl.ofBoldItalics("https://raw.githubusercontent.com/JetBrains/JetBrainsMono/master/ttf/JetBrainsMono-Bold-Italic.ttf" to "truetype"),
            CssBuilder.FontUrl.ofItalicised("https://raw.githubusercontent.com/JetBrains/JetBrainsMono/master/ttf/JetBrainsMono-Italic.ttf" to "truetype"),
        )
}

//inline class SourceCodeProFont(override val defaultFontSize: String) : AnsiScheme.Fonts {
//    constructor(size: Double, sizeUnits: CssBuilder.SizeUnits? = null) : this(if (sizeUnits != null) "$size${sizeUnits.str}" else size.toString())
//
//    override val defaultFontFamily: String
//        get() = "\"Source Code pro\""
//}

inline class GoodbyeDespairFont(override val defaultFontSize: String) : AnsiScheme.Fonts {
    constructor(size: Double, sizeUnits: CssBuilder.SizeUnits? = null) : this(if (sizeUnits != null) "$size${sizeUnits.str}" else size.toString())

    override val defaultFontFamily: String
        get() = "\"Goodbye Despair\""

    override val defaultFontUrls: Array<CssBuilder.FontUrl>
        get() = arrayOf(
            CssBuilder.FontUrl.of("file:///C:/Users/under/Downloads/goodbye_despair/goodbyeDespair.ttf" to "truetype")
        )
}