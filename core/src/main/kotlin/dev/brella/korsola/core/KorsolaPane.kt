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
    }
}