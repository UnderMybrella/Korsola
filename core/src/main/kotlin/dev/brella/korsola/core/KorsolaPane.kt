package dev.brella.korsola.core

import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.text.TextFlow

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

        arrayOf("Hewwo", "World!").forEach { str ->
            val line = TextFlow()
            outputLines.children.add(line)

            val text = Text(str)
            text.styleClass.add("text")
            line.children.add(text)
        }
    }
}