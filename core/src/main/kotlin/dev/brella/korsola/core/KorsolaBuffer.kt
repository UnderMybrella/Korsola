package dev.brella.korsola.core

import dev.brella.kornea.io.common.flow.PrintFlow
import dev.brella.korsola.core.ansi.KorsolaAnsiParser
import dev.brella.korsola.core.css.CssStyle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class KorsolaBuffer(val defaultStyle: CssStyle, val parser: KorsolaAnsiParser, val newLineAdded: suspend (TextBufferSegment.HeadSegment) -> Unit): PrintFlow {
    enum class AppendResult {
        NONE,
        ROW_SHIFTED,
        COLUMN_SHIFTED,
        CURSOR_SHIFTED;

        val didRowShift: Boolean
            get() = this == ROW_SHIFTED || this == CURSOR_SHIFTED

        val didColumnShift: Boolean
            get() = this == COLUMN_SHIFTED || this == CURSOR_SHIFTED
    }

    private val consoleLines: MutableList<TextBufferSegment.HeadSegment>
    private var cursor: ConsoleCursor = ConsoleCursor(0)
    private var style: CssStyle = defaultStyle

    override suspend fun print(value: Char): KorsolaBuffer {
//        TODO("Not yet implemented")

        consoleLines.last().appendAwait(value)
        return this
    }

    override suspend fun print(value: CharSequence?): KorsolaBuffer {
//        TODO("Not yet implemented")

        consoleLines.last().appendAwait(value)
        return this
    }

    override suspend fun print(value: CharSequence?, startIndex: Int, endIndex: Int): KorsolaBuffer {
//        TODO("Not yet implemented")

        consoleLines.last().appendAwait(value, startIndex, endIndex)
        return this
    }

    init {
        val firstLine = TextBufferSegment.HeadSegment(style, null)
        consoleLines = arrayListOf(firstLine)
        GlobalScope.launch { newLineAdded(firstLine) }
    }
}