package dev.brella.korsola.core.ansi

import dev.brella.korsola.core.KorsolaBuffer
import dev.brella.korsola.core.ansi.AnsiControlSequence.Companion.CSI
import dev.brella.korsola.core.ansi.AnsiEscapeSequence.Companion.ESC
import dev.brella.korsola.core.css.CssStyle

sealed class ConsoleOperation {
    data class PrintOut(val string: String, val style: CssStyle) : ConsoleOperation()
    data class Action(val perform: suspend (buffer: KorsolaBuffer) -> Unit) : ConsoleOperation()
}

interface AnsiEscapeSequence {
    companion object {
        const val ESC = "\u001B"
    }

    fun serialise(): String
}

inline class ParsedAnsi(val ansi: Array<AnsiEscapeSequence>) : AnsiEscapeSequence {
    override fun serialise(): String = ansi.joinToString("", transform = AnsiEscapeSequence::serialise)
}

inline class AnsiRawString(val string: String) : AnsiEscapeSequence {
    override fun serialise(): String = string
}

interface AnsiControlSequence : AnsiEscapeSequence {
    companion object {
        const val CSI = "${ESC}["
    }
}

interface AnsiPerformAction : AnsiEscapeSequence {
    suspend fun perform(buffer: KorsolaBuffer)
}

inline class AnsiCursorUp(val cells: Int) : AnsiPerformAction {
    override fun serialise(): String = "${CSI}${cells}A"

    override suspend fun perform(buffer: KorsolaBuffer) {
//        buffer.moveCursorUp(cells)
    }
}

inline class AnsiCursorDown(val cells: Int) : AnsiPerformAction {
    override fun serialise(): String = "${CSI}${cells}B"

    override suspend fun perform(buffer: KorsolaBuffer) {
//        buffer.moveCursorDown(cells)
    }
}

inline class AnsiCursorForward(val cells: Int) : AnsiPerformAction {
    override fun serialise(): String = "${CSI}${cells}C"

    override suspend fun perform(buffer: KorsolaBuffer) {
//        buffer.moveCursorForward(cells)
    }
}

inline class AnsiCursorBack(val cells: Int) : AnsiPerformAction {
    override fun serialise(): String = "${CSI}${cells}D"

    override suspend fun perform(buffer: KorsolaBuffer) {
//        buffer.moveCursorBack(cells)
    }
}

inline class AnsiCursorNextLine(val linesDown: Int) : AnsiEscapeSequence {
    override fun serialise(): String = "${CSI}${linesDown}E"
}

inline class AnsiCursorPreviousLine(val linesUp: Int) : AnsiEscapeSequence {
    override fun serialise(): String = "${CSI}${linesUp}F"
}

inline class AnsiCursorHorizontalAbsolute(val column: Int) : AnsiPerformAction {
    override fun serialise(): String = "${CSI}${column}G"

    override suspend fun perform(buffer: KorsolaBuffer) {
//        buffer.moveCursorToHorizontal(column)
    }
}

data class AnsiCursorPosition(val row: Int, val column: Int) : AnsiEscapeSequence {
    override fun serialise(): String = "${CSI}${row};${column}H"
}

inline class AnsiEraseInDisplay(val mode: EraseInDisplayMode) : AnsiEscapeSequence {
    enum class EraseInDisplayMode {
        CURSOR_TO_END_OF_SCREEN,
        CURSOR_TO_BEGINNING_OF_SCREEN,
        ENTIRE_SCREEN,
        ENTIRE_SCREEN_AND_SCROLLBACK
    }

    override fun serialise(): String = "${CSI}${mode.ordinal}J"
}

inline class AnsiEraseInLine(val mode: EraseInLineMode) : AnsiEscapeSequence {
    enum class EraseInLineMode {
        CURSOR_TO_END_OF_LINE,
        CURSOR_TO_BEGINNING_OF_LINE,
        ENTIRE_SCREEN
    }

    override fun serialise(): String = "${CSI}${mode.ordinal}K"
}

inline class AnsiScrollUp(val lines: Int) : AnsiEscapeSequence {
    override fun serialise(): String = "${CSI}${lines}S"
}

inline class AnsiScrollDown(val lines: Int) : AnsiEscapeSequence {
    override fun serialise(): String = "${CSI}${lines}T"
}

data class AnsiHorizontalVerticalPosition(val row: Int, val column: Int) : AnsiEscapeSequence {
    override fun serialise(): String = "${CSI}${row};${column}"
}

inline class AnsiSelectGraphicRendition(val params: Array<out SGRParameter>) : AnsiEscapeSequence {
    override fun serialise(): String = "${CSI}${params.joinToString(";", transform = SGRParameter::serialise)}m"
}

sealed class SGRParameter(val defaultSerialisation: Any? = null) {
    companion object {
        const val RESET_CODE = 0
        const val BOLD_CODE = 1
        const val FAINT_CODE = 2
        const val ITALIC_CODE = 3
        const val UNDERLINE_CODE = 4
        const val SLOW_BLINK_CODE = 5
        const val RAPID_BLINK_CODE = 6
        const val REVERSE_VIDEO_CODE = 7
        const val CONCEAL_CODE = 8
        const val CROSSED_OUT_CODE = 9
        const val PRIMARY_FONT_CODE = 10
        const val ALTERNATIVE_FONT_CODE_START = 11

        const val FRAKTUR_CODE = 20
        const val DOUBLY_UNDERLINE_CODE = 21
        const val NORMAL_COLOUR_AND_INTENSITY_CODE = 22
        const val NOT_ITALIC_OR_FRAKTUR_CODE = 23
        const val UNDERLINE_OFF_CODE = 24
        const val BLINK_OFF_CODE = 25
        const val PROPORTIONAL_SPACING_CODE = 26
        const val REVERSE_OFF_CODE = 27
        const val REVEAL_CODE = 28
        const val NOT_CROSSED_OUT_CODE = 29

        const val FOREGROUND_COLOUR_CODE_START = 30
        const val FOREGROUND_EIGHT_BIT_COLOUR_CODE = 38
        const val DEFAULT_FOREGROUND_COLOUR_CODE = 39

        const val BACKGROUND_COLOUR_CODE_START = 40
        const val BACKGROUND_EIGHT_BIT_COLOUR_CODE = 48
        const val DEFAULT_BACKGROUND_COLOUR_CODE = 49

        const val DISABLE_PROPORTIONAL_SPACING_CODE = 50
        const val FRAMED_CODE = 51
        const val ENCIRCLED_CODE = 52
        const val OVERLINED_CODE = 53
        const val NOT_FRAMED_OR_ENCIRCLED_CODE = 54
        const val NOT_OVERLINED_CODE = 55

        const val IDEOGRAM_UNDERLINE_CODE = 60
        const val IDEOGRAM_DOUBLE_UNDERLINE_CODE = 61
        const val IDEOGRAM_OVERLINE_CODE = 62
        const val IDEOGRAM_DOUBLE_OVERLINE_CODE = 63
        const val IDEOGRAM_STRESS_MARKING_CODE = 64
        const val IDEOGRAM_ATTRIBUTES_OFF_CODE = 65

        const val SUPERSCRIPT_CODE = 73
        const val SUBSCRIPT_CODE = 74

        const val BRIGHT_FOREGROUND_COLOUR_CODE_START = 90
        const val BRIGHT_BACKGROUND_COLOUR_CODE_START = 100

        val CODE_MAP by lazy {
            mapOf(
                RESET_CODE to Reset,
                BOLD_CODE to Bold,
                FAINT_CODE to Faint,
                ITALIC_CODE to Italic,
                UNDERLINE_CODE to Underline,
                SLOW_BLINK_CODE to SlowBlink,
                RAPID_BLINK_CODE to RapidBlink,
                REVERSE_VIDEO_CODE to ReverseVideo,
                CONCEAL_CODE to Conceal,
                CROSSED_OUT_CODE to CrossedOut,
                PRIMARY_FONT_CODE to PrimaryFont,

                FRAKTUR_CODE to Fraktur,
                DOUBLY_UNDERLINE_CODE to DoublyUnderline,
                NORMAL_COLOUR_AND_INTENSITY_CODE to NormalColourAndIntensity,
                NOT_ITALIC_OR_FRAKTUR_CODE to NotItalicOrFraktur,
                UNDERLINE_OFF_CODE to UnderlineOff,
                BLINK_OFF_CODE to BlinkOff,
                PROPORTIONAL_SPACING_CODE to ProportionalSpacing,
                REVERSE_OFF_CODE to ReverseOff,
                REVEAL_CODE to Reveal,
                NOT_CROSSED_OUT_CODE to NotCrossedOut,

                DEFAULT_FOREGROUND_COLOUR_CODE to DefaultForegroundColour,
                DEFAULT_BACKGROUND_COLOUR_CODE to DefaultBackgroundColour,

                DISABLE_PROPORTIONAL_SPACING_CODE to DisableProportionalSpacing,
                FRAMED_CODE to Framed,
                ENCIRCLED_CODE to Encircled,
                OVERLINED_CODE to Overlined,
                NOT_FRAMED_OR_ENCIRCLED_CODE to NotFramedOrEncircled,
                NOT_OVERLINED_CODE to NotOverlined,
//            58 to UnderlineColour
                IDEOGRAM_UNDERLINE_CODE to IdeogramUnderline,
                IDEOGRAM_DOUBLE_UNDERLINE_CODE to IdeogramDoubleUnderline,
                IDEOGRAM_OVERLINE_CODE to IdeogramOverline,
                IDEOGRAM_DOUBLE_OVERLINE_CODE to IdeogramDoubleOverline,
                IDEOGRAM_STRESS_MARKING_CODE to IdeogramStressMarking,
                IDEOGRAM_ATTRIBUTES_OFF_CODE to IdeogramAttributesOff,

                SUPERSCRIPT_CODE to Superscript,
                SUBSCRIPT_CODE to Subscript
            )
        }
    }

    object Reset : SGRParameter(RESET_CODE)
    object Bold : SGRParameter(BOLD_CODE)
    object Faint : SGRParameter(FAINT_CODE)
    object Italic : SGRParameter(ITALIC_CODE)
    object Underline : SGRParameter(UNDERLINE_CODE)
    object SlowBlink : SGRParameter(SLOW_BLINK_CODE)
    object RapidBlink : SGRParameter(RAPID_BLINK_CODE)
    object ReverseVideo : SGRParameter(REVERSE_VIDEO_CODE)
    object Conceal : SGRParameter(CONCEAL_CODE)
    object CrossedOut : SGRParameter(CROSSED_OUT_CODE)
    object PrimaryFont : SGRParameter(PRIMARY_FONT_CODE)
    data class AlternativeFont(val n: Int) : SGRParameter(ALTERNATIVE_FONT_CODE_START + n.coerceAtMost(8))
    object Fraktur : SGRParameter(FRAKTUR_CODE)
    object DoublyUnderline : SGRParameter(DOUBLY_UNDERLINE_CODE)
    object NormalColourAndIntensity : SGRParameter(NORMAL_COLOUR_AND_INTENSITY_CODE)
    object NotItalicOrFraktur : SGRParameter(NOT_ITALIC_OR_FRAKTUR_CODE)
    object UnderlineOff : SGRParameter(UNDERLINE_OFF_CODE)
    object ProportionalSpacing : SGRParameter(PROPORTIONAL_SPACING_CODE)
    object BlinkOff : SGRParameter(BLINK_OFF_CODE)
    object ReverseOff : SGRParameter(REVERSE_OFF_CODE)
    object Reveal : SGRParameter(REVEAL_CODE)
    object NotCrossedOut : SGRParameter(NOT_CROSSED_OUT_CODE)

    data class SetForegroundColourThreeBit(val index: Int) : SGRParameter(FOREGROUND_COLOUR_CODE_START + index.coerceAtMost(7)) {
        companion object {
            val BLACK = SetForegroundColourThreeBit(0)
            val RED = SetForegroundColourThreeBit(1)
            val GREEN = SetForegroundColourThreeBit(2)
            val YELLOW = SetForegroundColourThreeBit(3)
            val BLUE = SetForegroundColourThreeBit(4)
            val MAGENTA = SetForegroundColourThreeBit(5)
            val CYAN = SetForegroundColourThreeBit(6)
            val WHITE = SetForegroundColourThreeBit(7)
        }
    }

    data class SetForegroundColourEightBit(val index: Int) : SGRParameter("$FOREGROUND_EIGHT_BIT_COLOUR_CODE;5;$index")
    data class SetForegroundColourRGB(val red: Int, val green: Int, val blue: Int) : SGRParameter("$FOREGROUND_EIGHT_BIT_COLOUR_CODE;2;$red;$green;$blue")
    object DefaultForegroundColour : SGRParameter(DEFAULT_FOREGROUND_COLOUR_CODE)

    data class SetBackgroundColourThreeBit(val index: Int) : SGRParameter(BACKGROUND_COLOUR_CODE_START + index.coerceAtMost(7)) {
        companion object {
            val BLACK = SetBackgroundColourThreeBit(0)
            val RED = SetBackgroundColourThreeBit(1)
            val GREEN = SetBackgroundColourThreeBit(2)
            val YELLOW = SetBackgroundColourThreeBit(3)
            val BLUE = SetBackgroundColourThreeBit(4)
            val MAGENTA = SetBackgroundColourThreeBit(5)
            val CYAN = SetBackgroundColourThreeBit(6)
            val WHITE = SetBackgroundColourThreeBit(7)
        }
    }

    data class SetBackgroundColourEightBit(val index: Int) : SGRParameter("$BACKGROUND_EIGHT_BIT_COLOUR_CODE;5;$index")
    data class SetBackgroundColourRGB(val red: Int, val green: Int, val blue: Int) : SGRParameter("$BACKGROUND_EIGHT_BIT_COLOUR_CODE;2;$red;$green;$blue")
    object DefaultBackgroundColour : SGRParameter(DEFAULT_BACKGROUND_COLOUR_CODE)

    object DisableProportionalSpacing : SGRParameter(DISABLE_PROPORTIONAL_SPACING_CODE)
    object Framed : SGRParameter(FRAMED_CODE)
    object Encircled : SGRParameter(ENCIRCLED_CODE)
    object Overlined : SGRParameter(OVERLINED_CODE)
    object NotFramedOrEncircled : SGRParameter(NOT_FRAMED_OR_ENCIRCLED_CODE)
    object NotOverlined : SGRParameter(NOT_OVERLINED_CODE)

    //object UnderlineColour: SGRParameter()
    object IdeogramUnderline : SGRParameter(IDEOGRAM_UNDERLINE_CODE)
    object IdeogramDoubleUnderline : SGRParameter(IDEOGRAM_DOUBLE_UNDERLINE_CODE)
    object IdeogramOverline : SGRParameter(IDEOGRAM_OVERLINE_CODE)
    object IdeogramDoubleOverline : SGRParameter(IDEOGRAM_DOUBLE_OVERLINE_CODE)
    object IdeogramStressMarking : SGRParameter(IDEOGRAM_STRESS_MARKING_CODE)
    object IdeogramAttributesOff : SGRParameter(IDEOGRAM_ATTRIBUTES_OFF_CODE)
    object Superscript : SGRParameter(SUPERSCRIPT_CODE)
    object Subscript : SGRParameter(SUBSCRIPT_CODE)

    data class SetBrightForegroundColourThreeBit(val index: Int) : SGRParameter(BRIGHT_FOREGROUND_COLOUR_CODE_START + index.coerceAtMost(7)) {
        companion object {
            val BLACK = SetBrightForegroundColourThreeBit(0)
            val RED = SetBrightForegroundColourThreeBit(1)
            val GREEN = SetBrightForegroundColourThreeBit(2)
            val YELLOW = SetBrightForegroundColourThreeBit(3)
            val BLUE = SetBrightForegroundColourThreeBit(4)
            val MAGENTA = SetBrightForegroundColourThreeBit(5)
            val CYAN = SetBrightForegroundColourThreeBit(6)
            val WHITE = SetBrightForegroundColourThreeBit(7)
        }
    }

    data class SetBrightBackgroundColourThreeBit(val index: Int) : SGRParameter(BRIGHT_BACKGROUND_COLOUR_CODE_START + index.coerceAtMost(7)) {
        companion object {
            val BLACK = SetBrightBackgroundColourThreeBit(0)
            val RED = SetBrightBackgroundColourThreeBit(1)
            val GREEN = SetBrightBackgroundColourThreeBit(2)
            val YELLOW = SetBrightBackgroundColourThreeBit(3)
            val BLUE = SetBrightBackgroundColourThreeBit(4)
            val MAGENTA = SetBrightBackgroundColourThreeBit(5)
            val CYAN = SetBrightBackgroundColourThreeBit(6)
            val WHITE = SetBrightBackgroundColourThreeBit(7)
        }
    }

    open fun serialise(): String = requireNotNull(defaultSerialisation).toString()
    fun ansi(): String = "${CSI}${serialise()}m"
}

object AnsiAuxPortOn : AnsiEscapeSequence {
    override fun serialise(): String = "${CSI}5i"
}

object AnsiAuxPortOff : AnsiEscapeSequence {
    override fun serialise(): String = "${CSI}4i"
}

object AnsiDeviceStatusReport : AnsiEscapeSequence {
    override fun serialise(): String = "${CSI}6n"
}

enum class EnumAnsiEscapeSequence(val sequence: Char) : AnsiEscapeSequence {
    SINGLE_SHIFT_TWO('N'),
    SINGLE_SHIFT_THREE('O'),
    DEVICE_CONTROL_STRING('P'),
    STRING_TERMINATOR('\\'),
    OPERATING_SYSTEM_COMMAND(']'),
    START_OF_STRING('X'),
    PRIVACY_MESSAGE('^'),
    APPLICATION_PROGRAM_COMMAND('_'),
    RESET_TO_INITIAL_STATE('c');

    override fun serialise(): String = "${ESC}${sequence}"
}

inline fun <T> StringBuilder.appendResult(result: T): StringBuilder =
    when (result) {
        is AnsiEscapeSequence -> append(result.serialise())
        is SGRParameter -> append(result.ansi())
        is CharArray -> append(String(result))
        is Array<*> -> {
            if (result.isArrayOf<SGRParameter>()) {
                append(CSI)
                @Suppress("UNCHECKED_CAST")
                append((result as Array<SGRParameter>).joinToString(";", transform = SGRParameter::serialise))
                append('m')
            } else {
                append(result.joinToString())
            }
        }
        is Char -> append(result)
        is CharSequence -> append(result)
        is Unit -> this
        else -> append(result.toString())
    }