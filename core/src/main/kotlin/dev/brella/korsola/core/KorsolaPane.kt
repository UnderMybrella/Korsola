package dev.brella.korsola.core

import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane

class KorsolaPane: BorderPane() {
    val inputField = TextField()

    init {
        bottom = inputField
    }
}