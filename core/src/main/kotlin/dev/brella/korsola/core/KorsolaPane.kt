package dev.brella.korsola.core

import dev.brella.korsola.core.css.CssStyle
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch

class KorsolaPane(inputLocation: InputLocation = InputLocation.BOTTOM) : BorderPane() {
    enum class InputLocation {
        TOP,
        BOTTOM;
    }

    val inputField = TextField()
    val outputLines = VBox()

    init {
        when (inputLocation) {
            InputLocation.TOP -> top = inputField
            InputLocation.BOTTOM -> bottom = inputField
        }

        center = outputLines

//        arrayOf("Hewwo", "World!").forEach { str ->
//            val line = TextFlow()
//            outputLines.children.add(line)
//
//            val text = Text(str)
//            text.styleClass.add("text")
//            line.children.add(text)
//        }

        GlobalScope.launch(Dispatchers.JavaFx) {
            repeat(4) { i ->
                val segment = TextBufferSegment.HeadSegment("[$i] Hello, World! ", CssStyle.EMPTY, null)
                outputLines.children.add(segment.flow)

                delay(2_000)

                segment.appendToChain(TextBufferSegment.BodySegment("This is a test!", CssStyle.EMPTY))
                    .apply { delay(2_000) }
                    .appendToChain(TextBufferSegment.BodySegment(" >:(", CssStyle.EMPTY))

                println(segment.lineToString())
                val newSegment = segment.insertSegment(23, TextBufferSegment.BodySegment("uwu_owo_", CssStyle.EMPTY))
//            val head = (newSegment.head() as TextBufferSegment.HeadSegment)
            }
        }
    }
}