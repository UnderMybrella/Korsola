package dev.brella.korsola.core.css

interface CssSelector: Comparable<CssSelector> {
    data class FontFace(val fontName: String): CssSelector {
        override val priority: Int = Int.MIN_VALUE
        override fun asSelectorString(builder: StringBuilder): StringBuilder = builder.append("@font-face")
    }

    data class Element(val elementClass: String, val pseudoClasses: Array<out String>): CssSelector {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Element

            if (elementClass != other.elementClass) return false
            if (!pseudoClasses.contentEquals(other.pseudoClasses)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = elementClass.hashCode()
            result = 31 * result + pseudoClasses.contentHashCode()
            return result
        }

        override fun asSelectorString(builder: StringBuilder): StringBuilder {
            builder.append(elementClass)
            if (pseudoClasses.isNotEmpty()) {
                pseudoClasses.joinTo(builder, prefix = ":", separator = ":")
            }

            return builder
        }
    }

    val priority: Int
        get() = 0

    fun asSelectorString(builder: StringBuilder): StringBuilder

    override fun compareTo(other: CssSelector): Int = priority.compareTo(other.priority)
}

fun CssSelector.asSelectorString(): String = asSelectorString(StringBuilder()).toString()