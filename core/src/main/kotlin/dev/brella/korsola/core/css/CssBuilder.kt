package dev.brella.korsola.core.css

import kotlin.collections.LinkedHashMap
import kotlin.text.StringBuilder

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class CssDsl

@Suppress("PublicApiImplicitType", "unused")
@CssDsl
open class CssBuilder<out T: CssBuilder<T>>(val element: CssSelector? = null) {
    companion object {
        const val FONT_SIZE = "-fx-font-size"
        const val FONT_FAMILY = "-fx-font-family"
        const val FONT_STYLE = "-fx-font-style"
        const val FONT_WEIGHT = "-fx-font-weight"

        const val OPACITY = "-fx-opacity"

        const val FILL = "-fx-fill"

        const val TEXT_FILL = "-fx-text-fill"
        const val PROMPT_TEXT_FILL = "-fx-prompt-text-fill"
        const val HIGHLIGHT_FILL = "-fx-highlight-fill"
        const val HIGHLIGHT_TEXT_FILL = "-fx-highlight-text-fill"
        const val DISPLAY_CARET = "-fx-display-caret"
        const val STROKE = "-fx-stroke"

        const val BORDER_COLOR = "-fx-border-color"
        const val BORDER_WIDTH = "-fx-border-width"
        const val BORDER_STYLE = "-fx-border-style"

        const val BACKGROUND_COLOR = "-fx-background-color"

        const val RTFX_BACKGROUND_COLOUR = "-rtfx-background-color"

        const val ACCENT = "-fx-accent"

        const val TEXT_FIELD = ".text-field"
        const val CARET_HANDLE = ".caret-handle"
        const val CARET_PATH = ".caret-path"

        inline fun generic(element: CssSelector? = null): CssBuilder<*> = CssBuilder<CssBuilder<*>>(element)
    }

    enum class SizeUnits(val str: String, val relative: Boolean) {
        PIXELS("px", true),
        EM("em", true),
        EX("ex", true),
        INCHES("in", false),
        CENTIMETERS("cm", false),
        MILLIMETERS("mm", false),
        POINTS("pt", false),
        PICAS("pc", false);
    }

    data class Region<T>(val top: T, val right: T, val bottom: T, val left: T) {
        fun toCSS(): String = "$top $right $bottom $left"
        fun toCSS(transform: (T) -> String): String = "${transform(top)} ${transform(right)} ${transform(bottom)} ${transform(left)}"
    }

    data class FontUrl(val urls: Array<out Pair<String, String>>, val weight: String = "normal", val style: String = "normal") {
        companion object {
            fun of(vararg urls: Pair<String, String>) = FontUrl(urls)
            fun ofItalicised(vararg urls: Pair<String, String>) = FontUrl(urls, style = "italic")
            fun ofBold(vararg urls: Pair<String, String>) = FontUrl(urls, weight = "bold")
            fun ofBoldItalics(vararg urls: Pair<String, String>) = FontUrl(urls, weight = "bold", style = "italic")
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FontUrl

            if (!urls.contentEquals(other.urls)) return false
            if (weight != other.weight) return false
            if (style != other.style) return false

            return true
        }

        override fun hashCode(): Int {
            var result = urls.contentHashCode()
            result = 31 * result + weight.hashCode()
            result = 31 * result + style.hashCode()
            return result
        }
    }

    class FontFace(element: CssSelector.FontFace): CssBuilder<FontFace>(element) {
        override infix fun fontStyle(style: String) = with("font-style", style)
        override infix fun fontWeight(weight: String) = with("font-weight", weight)

        fun src(vararg urls: Pair<String, String>): FontFace = with("src", urls.joinToString { (url, format) -> "url('$url') format('$format')" })
    }

    protected open val elements: MutableMap<CssSelector, CssBuilder<*>> = LinkedHashMap()
    protected open val properties: MutableMap<String, String> = LinkedHashMap()

    open fun with(key: String, value: String?): T {
        if (value == null) properties.remove(key)
        else properties[key] = value

        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    fun addAll(other: CssBuilder<*>): T {
        if (element == other.element) {
            properties.putAll(other.properties)
        }

        other.elements.forEach { (element, builder) -> elements.compute(element) { _, base -> base?.addAll(builder) ?: builder } }

        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    open fun fontSize(size: Int, sizeUnits: SizeUnits? = null) = with(FONT_SIZE, if (sizeUnits != null) "$size${sizeUnits.str}" else size.toString())
    open fun fontSize(size: String) = with(FONT_SIZE, size)
    open fun fontFamily(family: String) = with(FONT_FAMILY, family)
    open fun fontStyle(style: String) = with(FONT_STYLE, style)
    open fun fontStyleNormal() = fontStyle("normal")
    open fun fontStyleItalic() = fontStyle("italic")
    open fun fontStyleOblique() = fontStyle("oblique")
    open fun fontWeight(weight: String) = with(FONT_WEIGHT, weight)
    open fun fontWeightBold() = fontWeight("bold")
    open fun fontWeightNormal() = fontWeight("normal")

    open fun opacity(opacity: Double) = with(OPACITY, opacity.coerceIn(0.0, 1.0).toString())

    open fun fill(colour: String) = with(FILL, colour)

    open fun accent(colour: String) = with(ACCENT, colour)
    open fun textFill(colour: String) = with(TEXT_FILL, colour)
    open fun promptTextFill(colour: String) = with(PROMPT_TEXT_FILL, colour)
    open fun highlightFill(colour: String) = with(HIGHLIGHT_FILL, colour)
    open fun highlightTextFill(colour: String) = with(HIGHLIGHT_TEXT_FILL, colour)
    open fun displayCaret(boolean: Boolean) = with(DISPLAY_CARET, boolean.toString())

    open fun borderColour(vararg colours: String) = with(BORDER_COLOR, colours.joinToString())
    open fun borderColour(vararg colours: Region<String>) = with(BORDER_COLOR, colours.joinToString(transform = Region<*>::toCSS))

    open fun borderWidth(vararg sizes: Number) = with(BORDER_WIDTH, sizes.joinToString())
    open fun borderWidth(vararg sizes: Region<Number>) = with(BORDER_WIDTH, sizes.joinToString(transform = Region<*>::toCSS))
    open fun borderWidthWithUnits(vararg sizes: Pair<Number, SizeUnits?>) = with(BORDER_WIDTH, sizes.joinToString { (s, u) -> if (u != null) "$s${u.str}" else s.toString() })
    open fun borderWidthWithUnits(vararg sizes: Region<Pair<Number, SizeUnits?>>) = with(BORDER_WIDTH, sizes.joinToString { region -> region.toCSS { (s, u) -> if (u != null) "$s${u.str}" else s.toString() } })

    open fun borderStyle(vararg styles: String) = with(BORDER_STYLE, styles.joinToString())
    open fun borderStyle(vararg styles: Region<String>) = with(BORDER_STYLE, styles.joinToString(transform = Region<*>::toCSS))

    open fun backgroundColour(colour: String) = with(BACKGROUND_COLOR, colour)
    open fun rtfxBackgroundColour(colour: String) = with(RTFX_BACKGROUND_COLOUR, colour)

    open fun stroke(colour: String) = with(STROKE, colour)

    open fun element(styleClass: String, vararg pseudoClasses: String, builder: CssBuilder<*>.() -> CssBuilder<*>): T {
        elements.computeIfAbsent(CssSelector.Element(styleClass, pseudoClasses), Companion::generic).builder()

        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    open fun fontFace(fontName: String, vararg urls: FontUrl): T {
        urls.forEach { fontType ->
            val element = CssSelector.FontFace("$fontName-${fontType.style}-${fontType.weight}")
            elements[element] = FontFace(element)
                .with("font-style", fontType.style)
                .with("font-weight", fontType.weight)
                .fontStyle(fontType.style)
                .fontWeight(fontType.weight)
                .src(urls = fontType.urls)
        }

        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    open fun textField(vararg pseudoClasses: String, builder: CssBuilder<*>.() -> CssBuilder<*>) = element(TEXT_FIELD, pseudoClasses = pseudoClasses, builder)
    open fun caretHandle(vararg pseudoClasses: String, builder: CssBuilder<*>.() -> CssBuilder<*>) = element(
        CARET_HANDLE, pseudoClasses = pseudoClasses, builder)
    open fun caretPath(vararg pseudoClasses: String, builder: CssBuilder<*>.() -> CssBuilder<*>) = element(
        CARET_PATH, pseudoClasses = pseudoClasses, builder)
    open fun text(vararg pseudoClasses: String, builder: CssBuilder<*>.() -> CssBuilder<*>) = element(".text", pseudoClasses = pseudoClasses, builder)
    open fun anyElement(vararg pseudoClasses: String, builder: CssBuilder<*>.() -> CssBuilder<*>) = element("*", pseudoClasses = pseudoClasses, builder)

    open fun removeFill() = with(FILL, null)
    open fun removeTextFill() = with(TEXT_FILL, null)
    open fun removeRtfxBackgroundColour() = with(RTFX_BACKGROUND_COLOUR, null)

    fun toCSS(): String = buildString(builderAction = this::toCSS)
    fun toCSS(builder: StringBuilder): StringBuilder {
        if (element == null) {
            elements.values.sortedBy { it.element }.forEach(builder::appendCSS)

            properties.entries.joinTo(builder, ";", postfix = ";") { (k, v) -> "$k:$v" }
        } else {
            element.asSelectorString(builder)

            builder.append("{")
            elements.values.sortedBy { it.element }.forEach(builder::appendCSS)

            properties.entries.joinTo(builder, ";", postfix = ";") { (k, v) -> "$k:$v" }
            builder.append("}")
        }

        return builder
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CssBuilder<*>

        if (element != other.element) return false
        if (elements != other.elements) return false
        if (properties != other.properties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = element?.hashCode() ?: 0
        result = 31 * result + elements.hashCode()
        result = 31 * result + properties.hashCode()
        return result
    }
}

fun StringBuilder.appendCSS(cssBuilder: CssBuilder<*>): StringBuilder {
    cssBuilder.toCSS(this)
    return this
}

inline fun buildCSS(init: CssBuilder<*>.() -> Any?): String {
    val builder = CssBuilder.generic()
    builder.init()
    return builder.toCSS()
}

inline fun rgb(rgb: Int): String = "rgb(${(rgb shr 16) and 0xFF}, ${(rgb shr 8) and 0xFF}, ${(rgb shr 0) and 0xFF})"
inline fun rgba(rgba: Int): String = "rgba(${(rgba shr 16) and 0xFF}, ${(rgba shr 8) and 0xFF}, ${(rgba shr 0) and 0xFF}, ${(rgba shr 24) and 0xFF})"

inline fun <T> region(top: T, right: T, bottom: T, left: T) = CssBuilder.Region(top, right, bottom, left)