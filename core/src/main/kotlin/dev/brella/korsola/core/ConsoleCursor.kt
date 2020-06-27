package dev.brella.korsola.core

inline class ConsoleCursor(val cursor: Long) {
    val row: Int get() = (cursor and 0xFFF).toInt()

    val column: Int get() = ((cursor shr 24) and 0xFFF).toInt()

    inline infix fun moveRow(rowShift: Int): ConsoleCursor = withRow(row + rowShift)
    inline infix fun moveColumn(columnShift: Int): ConsoleCursor = withColumn(column + columnShift)
    inline fun move(rowShift: Int, columnShift: Int): ConsoleCursor = with(row + rowShift, column + columnShift)

    inline infix fun withRow(row: Int): ConsoleCursor = ConsoleCursor((cursor and 0xFFF000) or row.toLong())
    inline infix fun withColumn(column: Int): ConsoleCursor = ConsoleCursor((cursor and 0xFFF) or (column.toLong() shl 24))

    inline fun with(row: Int, column: Int): ConsoleCursor = ConsoleCursor(row.toLong() and 0xFFF or (column.toLong() shl 24))

    override fun toString(): String = "ConsoleCursor($row,$column)"
}