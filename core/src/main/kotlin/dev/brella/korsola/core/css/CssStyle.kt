package dev.brella.korsola.core.css

import dev.brella.korsola.core.KorsolaUrlStreamHandler
import javafx.css.PseudoClass
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene

data class CssStyle(val inlineStyle: CssBuilder<*>?, val styleClassStates: Map<String, Boolean>, val pseudoClassStates: Map<String, Boolean>) {
    companion object {
        val EMPTY = CssStyle(null, emptyMap(), emptyMap())

//        val SLOW_BLINK_ENABLED = CssStyle(null, emptySet(), emptySet(), mapOf(AnsiParser.SLOW_BLINK_KEY to true))
//        val SLOW_BLINK_DISABLED = CssStyle(null, emptySet(), emptySet(), mapOf(AnsiParser.SLOW_BLINK_KEY to false))
    }

    @CssDsl
    class Builder {
        var inlineStyleInitialised: Boolean = false
        val inlineStyle: CssBuilder<*> by lazy {
            inlineStyleInitialised = true
            CssBuilder.generic()
        }

        var styleClassStatesInitialised: Boolean = false
        val styleClassStates: MutableMap<String, Boolean> by lazy {
            styleClassStatesInitialised = true
            HashMap()
        }

        var pseudoClassStatesInitialised: Boolean = false
        val pseudoClassStates: MutableMap<String, Boolean> by lazy {
            pseudoClassStatesInitialised = true
            HashMap()
        }

        inline infix fun inlineCSS(init: CssBuilder<*>.() -> Unit): Builder {
            inlineStyle.init()
            return this
        }

        inline infix fun styleClass(styleClass: String): Builder {
            styleClassStates[styleClass] = true
            return this
        }

        inline fun styleClass(styleClass: String, state: Boolean): Builder {
            styleClassStates[styleClass] = state
            return this
        }

        inline fun styleClasses(vararg styleClasses: String): Builder {
            styleClasses.forEach { styleClass -> styleClassStates[styleClass] = true }
            return this
        }

        inline infix fun pseudoClass(pseudoClass: String): Builder {
            pseudoClassStates[pseudoClass] = true
            return this
        }

        inline fun pseudoClass(pseudoClass: String, state: Boolean): Builder {
            pseudoClassStates[pseudoClass] = state
            return this
        }

        inline fun pseudoClasses(vararg pseudoClasses: String): Builder {
            pseudoClasses.forEach { pseudo -> pseudoClassStates[pseudo] = true }
            return this
        }

        fun disableAllPseudoClasses(prefix: String) {
            pseudoClassStates.keys.forEach { key -> if (key.startsWith(prefix, true)) pseudoClassStates[key] = false }
        }

        fun build() = CssStyle(
            if (inlineStyleInitialised) inlineStyle else null,

            (if (styleClassStatesInitialised) styleClassStates
                .takeIf(MutableMap<String, Boolean>::isNotEmpty)
                ?.entries
                ?.toTypedArray()
                ?.associate { (k, v) -> Pair(k, v) } else null) ?: emptyMap(),

            (if (pseudoClassStatesInitialised) pseudoClassStates
                .takeIf(MutableMap<String, Boolean>::isNotEmpty)
                ?.entries
                ?.toTypedArray()
                ?.associate { (k, v) -> Pair(k, v) } else null) ?: emptyMap()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CssStyle

        if (inlineStyle != other.inlineStyle) return false
        if (styleClassStates != other.styleClassStates) return false
        if (pseudoClassStates != other.pseudoClassStates) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inlineStyle?.hashCode() ?: 0
        result = 31 * result + styleClassStates.hashCode()
        result = 31 * result + pseudoClassStates.hashCode()
        return result
    }
}

inline fun styleBuilder(from: CssStyle?): CssStyle.Builder {
    val builder = CssStyle.Builder()
    if (from != null) {
        from.inlineStyle?.let { builder.inlineStyle.addAll(it) }
        from.styleClassStates.takeIf(Map<String, Boolean>::isNotEmpty)?.let(builder.styleClassStates::putAll)
        from.pseudoClassStates.takeIf(Map<String, Boolean>::isNotEmpty)?.let(builder.pseudoClassStates::putAll)
    }

    return builder
}

inline fun buildStyle(from: CssStyle? = null, init: CssStyle.Builder.() -> Any?): CssStyle {
    val builder = styleBuilder(from)
    builder.init()
    return builder.build()
}

fun Node.addStylesheet(path: String = "autogen/${this.id}", from: CssStyle? = null, init: CssStyle.Builder.() -> Any?) {
    val style = buildStyle(from, init)

    style.inlineStyle?.let { css ->
        KorsolaUrlStreamHandler.registerCSS(path, css.toCSS())

        if (this is Parent) stylesheets.add("${KorsolaUrlStreamHandler.PROTOCOL}:$path")
        else if (this is Scene) stylesheets.add("${KorsolaUrlStreamHandler.PROTOCOL}:$path")
        else null
    }

    style.styleClassStates.forEach { (k, v) -> if (v) styleClass.add(k) else styleClass.remove(k) }
    style.pseudoClassStates.forEach { (k, v) -> pseudoClassStateChanged(PseudoClass.getPseudoClass(k), v) }
}

fun <N : Node> styleNodeWith(node: N, css: CssStyle) {
    node.style = css.inlineStyle?.toCSS()

    css.styleClassStates.forEach { (k, v) ->
        if (v) node.styleClass.add(k)
        else node.styleClass.remove(k)
    }
    css.pseudoClassStates.forEach { (k, v) ->
        node.pseudoClassStateChanged(PseudoClass.getPseudoClass(k), v)

//        AnsiCssStyling.styleForPseudoClass(node, k, v)
    }
}

inline fun CssStyle.Builder.styleColoursFromTheme(theme: AnsiScheme.Colours) = inlineCSS {
    anyElement { backgroundColour(theme.defaultBackgroundColour) }

    textField {
        backgroundColour(theme.textInputBackgroundColour)
            .textFill(theme.textInputForegroundColour)
            .promptTextFill(theme.textInputPromptForegroundColour)
            .borderColour(theme.textInputBorderColour)
            .borderWidth(region(1, 0, 0, 0))
    }

    textField("disabled") {
        backgroundColour(theme.textInputDisabledBackgroundColour)
            .textFill(theme.textInputDisabledForegroundColour)
            .promptTextFill(theme.textInputDisabledPromptForegroundColour)
            .borderColour(theme.textInputDisabledBorderColour)
    }

    caretPath { stroke(theme.textInputCursorColour).opacity(theme.textInputCursorOpacity) }

    text { fill(theme.defaultForegroundColour) }

    val threeBitColours = theme.threeBitColours
    val brightThreeBitColours = theme.brightThreeBitColours

//    repeat(8) { i ->
//        text(AnsiParser.THREE_BIT_FOREGROUNDS[i]) {
//            textFill(threeBitColours[i])
//                .fill(threeBitColours[i])
//        }
//
//        text(AnsiParser.THREE_BIT_BACKGROUNDS[i]) {
//            backgroundColour(threeBitColours[i])
//                .rtfxBackgroundColour(threeBitColours[i])
//        }
//
//        text(AnsiParser.BRIGHT_THREE_BIT_FOREGROUNDS[i]) {
//            textFill(brightThreeBitColours[i])
//                .fill(brightThreeBitColours[i])
//        }
//
//        text(AnsiParser.BRIGHT_THREE_BIT_BACKGROUNDS[i]) {
//            backgroundColour(brightThreeBitColours[i])
//                .rtfxBackgroundColour(brightThreeBitColours[i])
//        }
//    }
}

inline fun CssStyle.Builder.styleFontsFromTheme(theme: AnsiScheme.Fonts) = inlineCSS {
    fontFace(theme.defaultFontFamily, urls = theme.defaultFontUrls)

    textField {
        fontSize(theme.defaultFontSize)
            .fontFamily(theme.defaultFontFamily)
    }

    textField("disabled") {
        fontSize(theme.defaultFontSize)
            .fontFamily(theme.defaultFontFamily)
            .fontStyleItalic()
    }

    text {
        fontSize(theme.defaultFontSize)
            .fontFamily(theme.defaultFontFamily)
    }
}