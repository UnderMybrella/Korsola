package dev.brella.korsola.core.css

interface AnsiScheme {
    interface Colours : AnsiScheme {
        companion object {
            private fun String.hexOrSelf(): String = if (!startsWith('#') && !startsWith("rgb") && length == 6) "#$this" else this

            /**
             * Black
             * Red
             * Green
             * Yellow
             * Blue
             * Magenta
             * Cyan
             * White
             */
            fun threeBitAlphaHex(black: String, blue: String, cyan: String, green: String, purple: String, red: String, white: String, yellow: String) = arrayOf(
                black.hexOrSelf(),
                red.hexOrSelf(),
                green.hexOrSelf(),
                yellow.hexOrSelf(),
                blue.hexOrSelf(),
                purple.hexOrSelf(),
                cyan.hexOrSelf(),
                white.hexOrSelf()
            )

            fun threeBitHex(black: String, red: String, green: String, yellow: String, blue: String, magenta: String, cyan: String, white: String) = arrayOf(
                black.hexOrSelf(),
                red.hexOrSelf(),
                green.hexOrSelf(),
                yellow.hexOrSelf(),
                blue.hexOrSelf(),
                magenta.hexOrSelf(),
                cyan.hexOrSelf(),
                white.hexOrSelf()
            )
        }

        val defaultForegroundColour: String
        val defaultBackgroundColour: String
        val defaultSelectionBackgroundColour: String

        val textInputForegroundColour: String
            get() = defaultForegroundColour
        val textInputPromptForegroundColour: String
            get() = textInputForegroundColour
        val textInputBackgroundColour: String
            get() = defaultBackgroundColour
        val textInputSelectionBackgroundColour: String
            get() = defaultSelectionBackgroundColour
        val textInputBorderColour: String
            get() = textInputForegroundColour

        val textInputDisabledBorderColour: String
            get() = "rgba(252,85,85)"
        val textInputDisabledForegroundColour: String
            get() = textInputForegroundColour
        val textInputDisabledPromptForegroundColour: String
            get() = textInputPromptForegroundColour
        val textInputDisabledBackgroundColour: String
            get() = textInputBackgroundColour

        val textInputCursorColour: String
        val textInputCursorOpacity: Double
            get() = 0.4

        /**
         * Black
         * Red
         * Green
         * Yellow
         * Blue
         * Magenta
         * Cyan
         * White
         */
        val threeBitColours: Array<String>
        val brightThreeBitColours: Array<String>

        class Composite(val baseTheme: Colours, val threeBitTheme: Colours) : Colours by baseTheme {
            override val threeBitColours: Array<String> by threeBitTheme::threeBitColours
            override val brightThreeBitColours: Array<String> by threeBitTheme::brightThreeBitColours
        }

        object Windows10Campbell : Colours {
            override val textInputCursorColour: String = "#FFFFFF"
            override val defaultSelectionBackgroundColour: String = "#FFFFFF"

            override val defaultBackgroundColour: String = "#0C0C0C"
            override val defaultForegroundColour: String = "#CCCCCC"

            /**
             * Black
             * Red
             * Green
             * Yellow
             * Blue
             * Magenta
             * Cyan
             * White
             */
            override val threeBitColours: Array<String> = threeBitAlphaHex(
                "0C0C0C",
                "0037da",
                "3a96dd",
                "13a10e",
                "881798",
                "C50F1F",
                "CCCCCC",
                "C19C00",
            )

            override val brightThreeBitColours: Array<String> = threeBitAlphaHex(
                "767676",
                "3B78FF",
                "61D6D6",
                "16C60C",
                "B4009E",
                "E74856",
                "F2F2F2",
                "F9F1A5"
            )
        }

        object TermiusDark : Colours {
            override val defaultForegroundColour: String = "#05CC6F"
            override val defaultBackgroundColour: String = "#141729"

            override val defaultSelectionBackgroundColour: String = "${defaultForegroundColour}70"
            override val textInputCursorColour: String = defaultForegroundColour

            /** These colours are FUCKED */

            override val threeBitColours: Array<String> = threeBitHex(
                "141729",
                "f24e50",
                "00cc74",
                "e6ebed", //Ah yes, yellow
                "00548c",
                "ff7375", //???purple?????
                "ff7375",
                "d5dde0"
            )

            override val brightThreeBitColours: Array<String> = threeBitHex(
                "8fa1a8",
                "8fa1a8",
                "00cc74",
                "ffffff",
                "186cb5",
                "ff7375",
                "42a1f5",
                "00cc74"
            )
        }
    }

    interface Fonts : AnsiScheme {
        val defaultFontFamily: String
        val defaultFontSize: String

        val defaultFontUrls: Array<CssBuilder.FontUrl>
            get() = emptyArray()
    }
}