package dev.brella.korsola.core.ansi

import dev.brella.korsola.core.ansi.SGRParameter.Companion.ALTERNATIVE_FONT_CODE_START
import dev.brella.korsola.core.ansi.SGRParameter.Companion.BACKGROUND_COLOUR_CODE_START
import dev.brella.korsola.core.ansi.SGRParameter.Companion.BACKGROUND_EIGHT_BIT_COLOUR_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.BLINK_OFF_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.BOLD_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.BRIGHT_BACKGROUND_COLOUR_CODE_START
import dev.brella.korsola.core.ansi.SGRParameter.Companion.BRIGHT_FOREGROUND_COLOUR_CODE_START
import dev.brella.korsola.core.ansi.SGRParameter.Companion.CONCEAL_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.CROSSED_OUT_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.DEFAULT_BACKGROUND_COLOUR_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.DEFAULT_FOREGROUND_COLOUR_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.DOUBLY_UNDERLINE_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.FAINT_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.FOREGROUND_COLOUR_CODE_START
import dev.brella.korsola.core.ansi.SGRParameter.Companion.FOREGROUND_EIGHT_BIT_COLOUR_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.FRAKTUR_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.ITALIC_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.NORMAL_COLOUR_AND_INTENSITY_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.NOT_CROSSED_OUT_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.NOT_ITALIC_OR_FRAKTUR_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.PRIMARY_FONT_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.PROPORTIONAL_SPACING_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.RAPID_BLINK_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.RESET_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.REVEAL_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.REVERSE_OFF_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.REVERSE_VIDEO_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.SLOW_BLINK_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.UNDERLINE_CODE
import dev.brella.korsola.core.ansi.SGRParameter.Companion.UNDERLINE_OFF_CODE
import dev.brella.korsola.core.css.CssStyle
import dev.brella.korsola.core.css.styleBuilder

interface KorsolaAnsiParser {
    companion object Default : KorsolaAnsiParser {
        private val HEX = mapOf(
            '0' to 0,
            '1' to 1,
            '2' to 2,
            '3' to 3,
            '4' to 4,
            '5' to 5,
            '6' to 6,
            '7' to 7,
            '8' to 8,
            '9' to 9,
            'a' to 10,
            'A' to 10,
            'b' to 11,
            'B' to 11,
            'c' to 12,
            'C' to 12,
            'd' to 13,
            'D' to 13,
            'e' to 14,
            'E' to 14,
            'f' to 15,
            'F' to 15
        )

        const val BOLD_KEY = "ansi-bold"
        const val FAINT_KEY = "ansi-faint"
        const val ITALIC_KEY = "ansi-italic"
        const val UNDERLINE_KEY = "ansi-underline"
        const val SLOW_BLINK_KEY = "ansi-slow-blink"
        const val RAPID_BLINK_KEY = "ansi-rapid-blink"
        const val REVERSE_VIDEO_KEY = "ansi-reverse-video"
        const val CONCEAL_KEY = "ansi-conceal"
        const val CROSSED_OUT_KEY = "ansi-crossed-out"

        const val FONT_PREFIX = "ansi-font"
//    val ALTERNATIVE_FONT_KEYS = Array(9) { "$FONT_PREFIX-${it + 1}"}

        const val FRAKTUR_KEY = "ansi-fraktur"
        const val DOUBLY_UNDERLINE_KEY = "ansi-double-underline"

        const val FOREGROUND_PREFIX = "ansi-foreground"
        const val THREE_BIT_FOREGROUND_PREFIX = "$FOREGROUND_PREFIX-3bit"
        const val BRIGHT_THREE_BIT_FOREGROUND_PREFIX = "$FOREGROUND_PREFIX-bright-3bit"
        const val EIGHT_BIT_FOREGROUND_PREFIX = "$FOREGROUND_PREFIX-8bit"
        val THREE_BIT_FOREGROUNDS = arrayOf(
            "$THREE_BIT_FOREGROUND_PREFIX-black",
            "$THREE_BIT_FOREGROUND_PREFIX-red",
            "$THREE_BIT_FOREGROUND_PREFIX-green",
            "$THREE_BIT_FOREGROUND_PREFIX-yellow",
            "$THREE_BIT_FOREGROUND_PREFIX-blue",
            "$THREE_BIT_FOREGROUND_PREFIX-magenta",
            "$THREE_BIT_FOREGROUND_PREFIX-cyan",
            "$THREE_BIT_FOREGROUND_PREFIX-white"
        )
        val BRIGHT_THREE_BIT_FOREGROUNDS = arrayOf(
            "$BRIGHT_THREE_BIT_FOREGROUND_PREFIX-black",
            "$BRIGHT_THREE_BIT_FOREGROUND_PREFIX-red",
            "$BRIGHT_THREE_BIT_FOREGROUND_PREFIX-green",
            "$BRIGHT_THREE_BIT_FOREGROUND_PREFIX-yellow",
            "$BRIGHT_THREE_BIT_FOREGROUND_PREFIX-blue",
            "$BRIGHT_THREE_BIT_FOREGROUND_PREFIX-magenta",
            "$BRIGHT_THREE_BIT_FOREGROUND_PREFIX-cyan",
            "$BRIGHT_THREE_BIT_FOREGROUND_PREFIX-white"
        )

        const val BACKGROUND_PREFIX = "ansi-background"
        const val THREE_BIT_BACKGROUND_PREFIX = "$BACKGROUND_PREFIX-3bit"
        const val BRIGHT_THREE_BIT_BACKGROUND_PREFIX = "$BACKGROUND_PREFIX-bright-3bit"
        const val EIGHT_BIT_BACKGROUND_PREFIX = "$BACKGROUND_PREFIX-8bit"

        val THREE_BIT_BACKGROUNDS = arrayOf(
            "$THREE_BIT_BACKGROUND_PREFIX-black",
            "$THREE_BIT_BACKGROUND_PREFIX-red",
            "$THREE_BIT_BACKGROUND_PREFIX-green",
            "$THREE_BIT_BACKGROUND_PREFIX-yellow",
            "$THREE_BIT_BACKGROUND_PREFIX-blue",
            "$THREE_BIT_BACKGROUND_PREFIX-magenta",
            "$THREE_BIT_BACKGROUND_PREFIX-cyan",
            "$THREE_BIT_BACKGROUND_PREFIX-white"
        )
        val BRIGHT_THREE_BIT_BACKGROUNDS = arrayOf(
            "$BRIGHT_THREE_BIT_BACKGROUND_PREFIX-black",
            "$BRIGHT_THREE_BIT_BACKGROUND_PREFIX-red",
            "$BRIGHT_THREE_BIT_BACKGROUND_PREFIX-green",
            "$BRIGHT_THREE_BIT_BACKGROUND_PREFIX-yellow",
            "$BRIGHT_THREE_BIT_BACKGROUND_PREFIX-blue",
            "$BRIGHT_THREE_BIT_BACKGROUND_PREFIX-magenta",
            "$BRIGHT_THREE_BIT_BACKGROUND_PREFIX-cyan",
            "$BRIGHT_THREE_BIT_BACKGROUND_PREFIX-white"
        )

        override suspend fun containsAnsi(csq: CharSequence): Boolean = csq.any { char ->
            when (char) {
                '\\' -> true
                '' -> true
                else -> false
            }
        }

        enum class AnsiParserMode {
            DEFAULT,
            QUOTE_TEXT_MODE,
            ANSI_MODE,
            CSI_MODE;
        }

        override suspend fun parseAnsi(csq: CharSequence, startingStyle: CssStyle?): Pair<List<ConsoleOperation>, CssStyle> {
            val tokens: MutableList<ConsoleOperation> = ArrayList()
            val modeStack: MutableList<AnsiParserMode> = arrayListOf(AnsiParserMode.DEFAULT)
            val builder = StringBuilder()

            val csiParameters: MutableList<Int> = ArrayList()

            var currentStyle = styleBuilder(startingStyle)

            var i = 0
            while (i in csq.indices) {
                when (modeStack[0]) {
                    AnsiParserMode.DEFAULT -> when (val char = csq[i++]) {
                        '\\' -> when (val next = csq[i++]) {
                            'Q' -> modeStack.add(0, AnsiParserMode.QUOTE_TEXT_MODE)
                            'u' -> builder.append(
                                ((HEX.getValue(csq[i++]) shl 12) or
                                        (HEX.getValue(csq[i++]) shl 8) or
                                        (HEX.getValue(csq[i++]) shl 4) or
                                        (HEX.getValue(csq[i++]))).toChar()
                            )
                            '\\' -> builder.append('\\')
                            '/' -> builder.append('/')
                            'b' -> builder.append('\b')
                            'f' -> builder.append('\u000C')
                            'n' -> builder.append('\n')
                            'r' -> builder.append('\r')
                            't' -> builder.append('\t')
                            'E' ->
                                if (csq[i + 1] == 'S' && csq[i + 2] == 'C') {
                                    i += 2
                                    if (builder.isNotEmpty()) {
                                        tokens.add(ConsoleOperation.PrintOut(builder.toString(), currentStyle.build()))
                                        builder.clear()
                                    }
                                    modeStack.add(0, AnsiParserMode.ANSI_MODE)
                                } else {
                                    builder.append('E')
                                }
                            else -> builder.append(next)
                        }
                        '' -> {
                            if (builder.isNotEmpty()) {
                                tokens.add(ConsoleOperation.PrintOut(builder.toString(), currentStyle.build()))
                                builder.clear()
                            }

                            modeStack.add(0, AnsiParserMode.ANSI_MODE)
                        }
                        else -> builder.append(char)
                    }
                    AnsiParserMode.QUOTE_TEXT_MODE -> when (val char = csq[i++]) {
                        '\\' -> when (val next = csq[i++]) {
                            'Q' -> modeStack.add(0, AnsiParserMode.QUOTE_TEXT_MODE)
                            'E' -> modeStack.removeAt(0)
                            'u' -> builder.append(
                                ((HEX.getValue(csq[i++]) shl 12) or
                                        (HEX.getValue(csq[i++]) shl 8) or
                                        (HEX.getValue(csq[i++]) shl 4) or
                                        (HEX.getValue(csq[i++]))).toChar()
                            )
                            '\\' -> builder.append('\\')
                            '/' -> builder.append('/')
                            'b' -> builder.append('\b')
                            'f' -> builder.append('\u000C')
                            'n' -> builder.append('\n')
                            'r' -> builder.append('\r')
                            't' -> builder.append('\t')
                            else -> builder.append(next)
                        }
                        else -> builder.append(char)
                    }
                    AnsiParserMode.ANSI_MODE -> when (csq[i++]) {
                        'N' -> {
                            TODO()
//                            tokens.add(EnumAnsiEscapeSequence.SINGLE_SHIFT_TWO)
//                            modeStack.removeAt(0)
                        }
                        'O' -> {
                            TODO()
//                            tokens.add(EnumAnsiEscapeSequence.SINGLE_SHIFT_THREE)
//                            modeStack.removeAt(0)
                        }
                        'P' -> {
                            TODO()
//                            tokens.add(EnumAnsiEscapeSequence.DEVICE_CONTROL_STRING)
//                            modeStack.removeAt(0)
                        }
                        '[' -> modeStack[0] = AnsiParserMode.CSI_MODE
                        '\\' -> {
                            TODO()
//                            tokens.add(EnumAnsiEscapeSequence.STRING_TERMINATOR)
//                            modeStack.removeAt(0)
                        }
                        ']' -> {
                            TODO()
//                            tokens.add(EnumAnsiEscapeSequence.OPERATING_SYSTEM_COMMAND)
//                            modeStack.removeAt(0)
                        }
                        'X' -> {
                            TODO()
//                            tokens.add(EnumAnsiEscapeSequence.START_OF_STRING)
//                            modeStack.removeAt(0)
                        }
                        '^' -> {
                            TODO()
//                            tokens.add(EnumAnsiEscapeSequence.PRIVACY_MESSAGE)
//                            modeStack.removeAt(0)
                        }
                        '_' -> {
                            TODO()
//                            tokens.add(EnumAnsiEscapeSequence.APPLICATION_PROGRAM_COMMAND)
//                            modeStack.removeAt(0)
                        }
                        'c' -> {
                            TODO()
//                            tokens.add(EnumAnsiEscapeSequence.RESET_TO_INITIAL_STATE)
//                            modeStack.removeAt(0)
                        }
                        else -> modeStack.removeAt(0)
                    }
                    AnsiParserMode.CSI_MODE -> when (val n = csq[i++]) {
                        '0' -> builder.append('0')
                        '1' -> builder.append('1')
                        '2' -> builder.append('2')
                        '3' -> builder.append('3')
                        '4' -> builder.append('4')
                        '5' -> builder.append('5')
                        '6' -> builder.append('6')
                        '7' -> builder.append('7')
                        '8' -> builder.append('8')
                        '9' -> builder.append('9')
                        else -> {
                            csiParameters.add(builder.toString().toInt())
                            builder.clear()

                            when (n) {
                                ';' -> {
                                }

                                'A' -> {
                                    tokens.add(ConsoleOperation.Action(AnsiCursorUp(csiParameters.getOrNull(0) ?: 1)::perform))
                                    modeStack.removeAt(0)
                                }
                                'B' -> {
                                    tokens.add(ConsoleOperation.Action(AnsiCursorDown(csiParameters.getOrNull(0) ?: 1)::perform))
                                    modeStack.removeAt(0)
                                }
                                'C' -> {
                                    tokens.add(ConsoleOperation.Action(AnsiCursorForward(csiParameters.getOrNull(0) ?: 1)::perform))
                                    modeStack.removeAt(0)
                                }
                                'D' -> {
                                    tokens.add(ConsoleOperation.Action(AnsiCursorBack(csiParameters.getOrNull(0) ?: 1)::perform))
                                    modeStack.removeAt(0)
                                }
                                'E' -> {
                                    TODO()
//                            tokens.add(AnsiCursorNextLine(csiParameters.getOrNull(0) ?: 1))
//                            modeStack.removeAt(0)
                                }
                                'F' -> {
                                    TODO()
//                            tokens.add(AnsiCursorPreviousLine(csiParameters.getOrNull(0) ?: 1))
//                            modeStack.removeAt(0)
                                }
                                'G' -> {
                                    TODO()
//                            tokens.add(AnsiCursorHorizontalAbsolute(csiParameters.getOrNull(0) ?: 1))
//                            modeStack.removeAt(0)
                                }
                                'H' -> {
                                    TODO()
//                            tokens.add(AnsiCursorPosition(csiParameters.getOrNull(0) ?: 1, csiParameters.getOrNull(1) ?: 1))
//                            modeStack.removeAt(0)
                                }
                                'J' -> {
                                    TODO()
//                            tokens.add(AnsiEraseInDisplay(AnsiEraseInDisplay.EraseInDisplayMode.values()[csiParameters.getOrNull(0) ?: 0]))
//                            modeStack.removeAt(0)
                                }
                                'K' -> {
                                    TODO()
//                            tokens.add(AnsiEraseInLine(AnsiEraseInLine.EraseInLineMode.values()[csiParameters.getOrNull(0) ?: 0]))
//                            modeStack.removeAt(0)
                                }
                                'S' -> {
                                    TODO()
//                            tokens.add(AnsiScrollUp(csiParameters.getOrNull(0) ?: 1))
//                            modeStack.removeAt(0)
                                }
                                'T' -> {
                                    TODO()
//                            tokens.add(AnsiScrollDown(csiParameters.getOrNull(0) ?: 1))
//                            modeStack.removeAt(0)
                                }
                                'f' -> {
                                    TODO()
//                            tokens.add(AnsiHorizontalVerticalPosition(csiParameters.getOrNull(0) ?: 1, csiParameters.getOrNull(1) ?: 1))
//                            modeStack.removeAt(0)
                                }
                                'm' -> {
                                    var j = 0
//                            val parsed: MutableList<SGRParameter> = ArrayList()
                                    while (j in csiParameters.indices) {
                                        when (val n = csiParameters[j++]) {
                                            RESET_CODE -> currentStyle = styleBuilder(startingStyle)
                                            BOLD_CODE -> currentStyle.pseudoClass(BOLD_KEY, true)
                                            FAINT_CODE -> currentStyle.pseudoClass(FAINT_KEY, true)
                                            ITALIC_CODE -> currentStyle.pseudoClass(ITALIC_KEY, true)
                                            UNDERLINE_CODE -> currentStyle.pseudoClass(UNDERLINE_KEY, true)
                                            SLOW_BLINK_CODE -> currentStyle.pseudoClass(SLOW_BLINK_KEY, true)
                                            RAPID_BLINK_CODE -> currentStyle.pseudoClass(RAPID_BLINK_KEY, true)
                                            REVERSE_VIDEO_CODE -> currentStyle.pseudoClass(REVERSE_VIDEO_KEY, true)
                                            CONCEAL_CODE -> currentStyle.pseudoClass(CONCEAL_KEY, true)
                                            CROSSED_OUT_CODE -> currentStyle.pseudoClass(CROSSED_OUT_KEY, true)
                                            PRIMARY_FONT_CODE -> currentStyle.disableAllPseudoClasses(FONT_PREFIX)
                                            //Alternative Font
                                            in (ALTERNATIVE_FONT_CODE_START..(ALTERNATIVE_FONT_CODE_START + 8)) -> SGRParameter.AlternativeFont(n - ALTERNATIVE_FONT_CODE_START)
                                            FRAKTUR_CODE -> currentStyle.pseudoClass(FRAKTUR_KEY, true)
                                            DOUBLY_UNDERLINE_CODE -> currentStyle.pseudoClass(DOUBLY_UNDERLINE_KEY, true)
                                            NORMAL_COLOUR_AND_INTENSITY_CODE -> {
                                                currentStyle.pseudoClass(BOLD_KEY, false)
                                                currentStyle.pseudoClass(FAINT_KEY, false)
                                            }
                                            NOT_ITALIC_OR_FRAKTUR_CODE -> {
                                                currentStyle.pseudoClass(ITALIC_KEY, false)
                                                currentStyle.pseudoClass(FRAKTUR_KEY, false)
                                            }
                                            UNDERLINE_OFF_CODE -> {
                                                currentStyle.pseudoClass(UNDERLINE_KEY, false)
                                                currentStyle.pseudoClass(DOUBLY_UNDERLINE_KEY, false)
                                            }
                                            PROPORTIONAL_SPACING_CODE -> TODO()
                                            BLINK_OFF_CODE -> {
                                                currentStyle.pseudoClass(SLOW_BLINK_KEY, false)
                                                currentStyle.pseudoClass(RAPID_BLINK_KEY, false)
                                            }
                                            REVERSE_OFF_CODE -> currentStyle.pseudoClass(REVERSE_VIDEO_KEY, false)
                                            REVEAL_CODE -> currentStyle.pseudoClass(CONCEAL_KEY, false)
                                            NOT_CROSSED_OUT_CODE -> currentStyle.pseudoClass(CROSSED_OUT_KEY, false)
                                            in (FOREGROUND_COLOUR_CODE_START..(FOREGROUND_COLOUR_CODE_START + 7)) -> {
                                                currentStyle.inlineStyle.removeFill()
                                                currentStyle.inlineStyle.removeTextFill()
                                                currentStyle.disableAllPseudoClasses(FOREGROUND_PREFIX)
                                                currentStyle.pseudoClass(THREE_BIT_FOREGROUNDS[n - FOREGROUND_COLOUR_CODE_START], true)
                                            }
                                            FOREGROUND_EIGHT_BIT_COLOUR_CODE -> {
                                                when (val b = csiParameters[j++]) {
                                                    5 -> {
                                                        currentStyle.inlineStyle.removeFill()
                                                        currentStyle.inlineStyle.removeTextFill()
                                                        currentStyle.disableAllPseudoClasses(FOREGROUND_PREFIX)
                                                        currentStyle.pseudoClass("$EIGHT_BIT_FOREGROUND_PREFIX-${csiParameters[j++]}", true)
                                                    }
                                                    2 -> {
                                                        currentStyle.disableAllPseudoClasses(FOREGROUND_PREFIX)

                                                        currentStyle.inlineCSS {
                                                            val r = csiParameters[j++]
                                                            val g = csiParameters[j++]
                                                            val b = csiParameters[j++]

                                                            fill("rgb($r,$g,$b)")
                                                            textFill("rgb($r,$g,$b)")
                                                        }
                                                    }
                                                }
                                            }
                                            DEFAULT_FOREGROUND_COLOUR_CODE -> {
                                                currentStyle.inlineStyle.removeFill()
                                                currentStyle.inlineStyle.removeTextFill()
                                                currentStyle.disableAllPseudoClasses(FOREGROUND_PREFIX)
                                            }

                                            in (BACKGROUND_COLOUR_CODE_START..(BACKGROUND_COLOUR_CODE_START + 7)) -> {
                                                currentStyle.inlineStyle.removeRtfxBackgroundColour()
                                                currentStyle.disableAllPseudoClasses(BACKGROUND_PREFIX)
                                                currentStyle.pseudoClass(THREE_BIT_BACKGROUNDS[n - BACKGROUND_COLOUR_CODE_START], true)
                                            }
                                            BACKGROUND_EIGHT_BIT_COLOUR_CODE -> {
                                                when (val b = csiParameters[j++]) {
                                                    5 -> {
                                                        currentStyle.inlineStyle.removeRtfxBackgroundColour()
                                                        currentStyle.disableAllPseudoClasses(BACKGROUND_PREFIX)
                                                        currentStyle.pseudoClass("$EIGHT_BIT_BACKGROUND_PREFIX-${csiParameters[j++]}", true)
                                                    }
                                                    2 -> {
                                                        currentStyle.disableAllPseudoClasses(BACKGROUND_PREFIX)
                                                        currentStyle.inlineCSS { rtfxBackgroundColour("rgb(${csiParameters[j++]}, ${csiParameters[j++]}, ${csiParameters[j++]}") }
                                                    }
                                                }
                                            }

                                            DEFAULT_BACKGROUND_COLOUR_CODE -> {
                                                currentStyle.inlineStyle.removeRtfxBackgroundColour()
                                                currentStyle.disableAllPseudoClasses(BACKGROUND_PREFIX)
                                            }

                                            in (BRIGHT_FOREGROUND_COLOUR_CODE_START..(BRIGHT_FOREGROUND_COLOUR_CODE_START + 7)) -> {
                                                currentStyle.inlineStyle.removeFill()
                                                currentStyle.inlineStyle.removeTextFill()
                                                currentStyle.disableAllPseudoClasses(FOREGROUND_PREFIX)
                                                currentStyle.pseudoClass(BRIGHT_THREE_BIT_FOREGROUNDS[n - BRIGHT_FOREGROUND_COLOUR_CODE_START], true)
                                            }

                                            in (BRIGHT_BACKGROUND_COLOUR_CODE_START..(BRIGHT_BACKGROUND_COLOUR_CODE_START + 7)) -> {
                                                currentStyle.inlineStyle.removeRtfxBackgroundColour()
                                                currentStyle.disableAllPseudoClasses(BACKGROUND_PREFIX)
                                                currentStyle.pseudoClass(BRIGHT_THREE_BIT_BACKGROUNDS[n - BRIGHT_BACKGROUND_COLOUR_CODE_START], true)
                                            }

                                            else -> TODO()
                                        }
                                    }

                                    modeStack.removeAt(0)
                                }
                                'i' -> {
                                    TODO()
//                            when (csiParameters.getOrNull(0)) {
//                                5 -> tokens.add(AnsiAuxPortOn)
//                                6 -> tokens.add(AnsiAuxPortOff)
//                            }
//                            modeStack.removeAt(0)
                                }
                                'n' -> {
                                    TODO()
//                            if (csiParameters.getOrNull(0) == 6) {
//                                tokens.add(AnsiDeviceStatusReport)
//                            }
//
//                            modeStack.removeAt(0)
                                }
                            }
                        }
                    }
                }
            }

            if (builder.isNotEmpty()) {
                tokens.add(ConsoleOperation.PrintOut(builder.toString(), currentStyle.build()))
                builder.clear()
            }

            return Pair(tokens, currentStyle.build())
        }
    }

    suspend fun containsAnsi(csq: CharSequence): Boolean
    suspend fun parseAnsi(csq: CharSequence, startingStyle: CssStyle?): Pair<List<ConsoleOperation>, CssStyle>
}