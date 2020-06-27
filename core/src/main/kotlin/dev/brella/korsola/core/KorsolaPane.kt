package dev.brella.korsola.core

import dev.brella.korsola.core.ansi.KorsolaAnsiParser
import dev.brella.korsola.core.css.CssStyle
import javafx.scene.control.TextField
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx

class KorsolaPane(inputLocation: InputLocation = InputLocation.BOTTOM, defaultStyle: CssStyle, parser: KorsolaAnsiParser) : BorderPane() {
    enum class InputLocation {
        TOP,
        BOTTOM;
    }

    val inputField = TextField()
    val outputLines = VBox()
    val buffer = KorsolaBuffer(defaultStyle, parser, this::addNewLine)

    suspend fun addNewLine(head: TextBufferSegment.HeadSegment) {
        withContext(Dispatchers.JavaFx) {
            outputLines.children.add(head.flow)
        }
    }

    init {
        when (inputLocation) {
            InputLocation.TOP -> top = inputField
            InputLocation.BOTTOM -> bottom = inputField
        }

        center = outputLines

//        addEventHandler(KeyEvent.KEY_RELEASED) { event -> println(event) }
    }
}