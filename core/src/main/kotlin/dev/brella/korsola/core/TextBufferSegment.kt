package dev.brella.korsola.core

import dev.brella.korsola.core.css.CssStyle
import org.kornea.toolkit.common.KorneaStringBuilder
import org.kornea.toolkit.common.SharedState
import org.kornea.toolkit.common.SharedStateRW
import org.kornea.toolkit.common.freeze
import kotlin.contracts.ExperimentalContracts

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class DslBufferSegment

@DslBufferSegment
sealed class TextBufferSegmentScope {
    object EXPLICIT : TextBufferSegmentScope()
}

class SegmentIterator(var node: TextBufferSegment?) : Iterator<TextBufferSegment> {
    override fun hasNext(): Boolean = node != null
    override fun next(): TextBufferSegment {
        val next = node!!
        node = (next as? TextBufferSegment.HasNext)?.next
        return next
    }
}

@Suppress("EXPERIMENTAL_API_USAGE")
sealed class TextBufferSegment(val backing: SharedState<String, StringBuilder> = SharedState.of(StringBuilder()), val segmentStyle: CssStyle) : Iterable<TextBufferSegment> {
    constructor(builder: StringBuilder, segmentStyle: CssStyle) : this(SharedState.of(builder), segmentStyle)
    constructor(string: String, segmentStyle: CssStyle) : this(SharedState.of(StringBuilder(string)), segmentStyle)

    interface HasNext {
        var next: TextBufferSegment?
    }

    interface HasPrevious {
        var previous: TextBufferSegment?
    }

    class HeadSegment(backing: SharedState<String, StringBuilder>, style: CssStyle, override var next: TextBufferSegment?) : TextBufferSegment(backing, style), HasNext {
        constructor(builder: StringBuilder, style: CssStyle, next: TextBufferSegment?) : this(SharedState.of(builder), style, next)
        constructor(string: String, style: CssStyle, next: TextBufferSegment?) : this(SharedState.of(StringBuilder(string)), style, next)
        constructor(style: CssStyle, next: TextBufferSegment?) : this(SharedState.of(StringBuilder()), style, next)

        override fun detach() {
            if ((next as? HasPrevious)?.previous === this)
                (next as? HasPrevious)?.previous = null

            next = null
        }

        override fun withPrevious(previous: TextBufferSegment): Pair<BodySegment, HeadSegment> {
            val head = HeadSegment(previous.backing, previous.segmentStyle, this)
            return BodySegment(backing, segmentStyle, previous = head, next = next) to head
        }

        override fun withNext(next: TextBufferSegment): Pair<HeadSegment, TextBufferSegment> {
            this.next = next
            return this to next
        }

        init {
            if (next != null) {
                (next as HasPrevious).previous = this
            }
        }
    }

    class BodySegment(backing: SharedState<String, StringBuilder>, style: CssStyle, override var next: TextBufferSegment?, override var previous: TextBufferSegment?) : TextBufferSegment(backing, style), HasNext, HasPrevious {
        constructor(builder: StringBuilder, style: CssStyle, next: TextBufferSegment?, previous: TextBufferSegment?) : this(SharedState.of(builder), style, next, previous)
        constructor(string: String, style: CssStyle, next: TextBufferSegment?, previous: TextBufferSegment?) : this(SharedState.of(StringBuilder(string)), style, next, previous)
        constructor(style: CssStyle, next: TextBufferSegment?, previous: TextBufferSegment?) : this(SharedState.of(StringBuilder()), style, next, previous)

        override fun detach() {
            if ((previous as? HasNext)?.next === this)
                (previous as? HasNext)?.next = null

            previous = null

            if ((next as? HasPrevious)?.previous === this)
                (next as? HasPrevious)?.previous = null

            next = null
        }

        override fun withPrevious(previous: TextBufferSegment): Pair<BodySegment, TextBufferSegment> {
            this.previous = previous
            return this to previous
        }

        override fun withNext(next: TextBufferSegment): Pair<BodySegment, TextBufferSegment> {
            this.next = next
            return this to next
        }

        init {
            if (previous is HasNext) {
                val prevAsNext = previous as HasNext

                if (prevAsNext.next != null && next == null) {
                    next = prevAsNext.next
                }

                prevAsNext.next = this
            }

            if (next != null) {
                val nextAsPrev = next as HasPrevious

                if (nextAsPrev.previous != null && previous == null) {
                    previous = nextAsPrev.previous
                }

                nextAsPrev.previous = this
            }
        }
    }

    class TailSegment(backing: SharedState<String, StringBuilder>, style: CssStyle, override var previous: TextBufferSegment?) : TextBufferSegment(backing, style), HasPrevious {
        constructor(builder: StringBuilder, style: CssStyle, previous: TextBufferSegment?) : this(SharedState.of(builder), style, previous)
        constructor(string: String, style: CssStyle, previous: TextBufferSegment?) : this(SharedState.of(StringBuilder(string)), style, previous)
        constructor(style: CssStyle, previous: TextBufferSegment?) : this(SharedState.of(StringBuilder()), style, previous)

        override fun detach() {
            if ((previous as? HasNext)?.next === this)
                (previous as? HasNext)?.next = null

            previous = null
        }

        override fun withPrevious(previous: TextBufferSegment): Pair<TailSegment, TextBufferSegment> {
            this.previous = previous
            return this to previous
        }

        override fun withNext(next: TextBufferSegment): Pair<BodySegment, TailSegment> {
            val tail = TailSegment(next.backing, next.segmentStyle, this)
            return BodySegment(backing, segmentStyle, previous, tail) to tail
        }

        init {
            if (previous is HasNext) {
                (previous as HasNext).next = this
            }
        }
    }

    var dirty: Boolean = true

//    var documentPosition: Int? = null
//    var documentLength: Int? = null

//    override fun toString(): String = "BufferSegment(text=$builder,style=$style,previous=[${previous?.builder}],next=[${next?.builder}])"

    abstract fun detach()
    abstract fun withPrevious(previous: TextBufferSegment): Pair<TextBufferSegment, TextBufferSegment>
    abstract fun withNext(next: TextBufferSegment): Pair<TextBufferSegment, TextBufferSegment>

    suspend inline fun length(): Int = backing.accessState { str -> str.length }
    suspend inline fun <T> withBuilder(crossinline block: suspend (StringBuilder) -> T): T? {
        var result: T? = null
        backing.mutateState { builder ->
            dirty = true
            result = block(builder)
            if (result === builder) result = null
            builder
        }

        return result
    }

    suspend inline fun cleanString(): String = backing.accessState { str ->
        dirty = false
        str
    }

    inline fun <T> scoped(block: TextBufferSegmentScope.EXPLICIT.(self: TextBufferSegment) -> T): T =
        TextBufferSegmentScope.EXPLICIT.block(this)

    override fun iterator(): Iterator<TextBufferSegment> = SegmentIterator(this)
}

inline fun TextBufferSegment.first(): TextBufferSegment {
    var node = (this as? TextBufferSegment.HasPrevious)?.previous ?: return this

    while (node is TextBufferSegment.HasPrevious) {
        node = node.previous ?: return node
    }

    return node
}

inline fun TextBufferSegment.last(): TextBufferSegment {
    var node = (this as? TextBufferSegment.HasNext)?.next ?: return this

    while (node is TextBufferSegment.HasNext) {
        node = node.next ?: return node
    }

    return node
}

suspend inline fun TextBufferSegment.startingPosition(): Int {
    var node = (this as? TextBufferSegment.HasPrevious)?.previous ?: return 0
    var pos = 0

    while (node is TextBufferSegment.HasPrevious) {
        pos += node.length()
        node = node.previous ?: return pos
    }

    return pos
}

suspend inline fun TextBufferSegment.endingPosition(): Int {
    var node = (this as? TextBufferSegment.HasPrevious)?.previous ?: return length()
    var pos = 0

    while (node is TextBufferSegment.HasPrevious) {
        pos += node.length()
        node = node.previous ?: return pos + length()
    }

    return pos + length()
}

suspend inline fun TextBufferSegment.seekInLine(column: Int): TextBufferSegment? {
    first().fold(0) { pos, segment: TextBufferSegment -> if (segment.contains(pos, column)) return segment else pos + segment.length() }

    return null
}

suspend inline fun TextBufferSegment.lineLength(): Long =
    first().fold(0L) { len, segment: TextBufferSegment -> len + segment.length() }

//inline operator fun TextBufferSegment.contains(cursor: ConsoleCursor): Boolean {
//    val start = startingPosition()
//
//    return cursor.column >= start && cursor.column < start + segmentLength
//}

suspend inline fun TextBufferSegment.contains(column: Int): Boolean = contains(startingPosition(), column)
suspend inline fun TextBufferSegment.contains(start: Int, column: Int): Boolean = column >= start && column < start + length()

suspend fun TextBufferSegment.insertSegment(position: Int, new: TextBufferSegment): TextBufferSegment = scoped { self ->
    val wasDirty = self.dirty
    var new = new

    val start = self.startingPosition()

    val segLen = new.length()
    val segPrefixLen = position - start
    val segSuffixLen = self.length() - (segPrefixLen + segLen)

    val prefixSegment: TextBufferSegment?
    val suffixSegment: TextBufferSegment?

    if (segPrefixLen == 0) {
        //Good news - we start at the beginning of this segment
        //Since there's no prefix, prefixSegment just becomes previous
        prefixSegment = (self as? TextBufferSegment.HasPrevious)?.previous
    } else if (segPrefixLen > 0) {
        //Uho, there's a prefix here
        //It's cool though, we just need a substring
        val prefix = self.withBuilder { builder ->
            val str = builder.substring(0, segPrefixLen)
            builder.delete(0, segPrefixLen)

            str
        }

        requireNotNull(prefix)

        //Create a new segment with the old style and prefix
        prefixSegment = TextBufferSegment.BodySegment(prefix, self.segmentStyle, (self as? TextBufferSegment.HasPrevious)?.previous, null)
//            .apply {
//                if (self.documentPosition != null) {
//                    this.documentPosition = self.documentPosition
//                    this.documentLength = prefix.length
//                }
//            }
    } else {
        //This is an overflowed segment, so just set prefixSegment to null
        prefixSegment = null
    }

    if (segSuffixLen == 0) {
        //Good news - we extend up to the end of this segment
        //Since there's no suffix, suffixSegment just becomes next
        suffixSegment = (self as? TextBufferSegment.HasNext)?.next
    } else if (segSuffixLen > 0) {
        //We have a suffix, n.b.d
        val suffix = self.withBuilder { builder ->
            val str = builder.substring(builder.length - segSuffixLen, builder.length)
            builder.delete(builder.length - segSuffixLen, builder.length)
            str
        }

        requireNotNull(suffix)

        //Create a new segment with the old style and suffix
        suffixSegment = TextBufferSegment.BodySegment(suffix, self.segmentStyle, null, (self as? TextBufferSegment.HasNext)?.next)
//            .apply {
//                if (self.documentPosition != null) {
//                    this.documentPosition = self.documentPosition!! + self.documentLength!! - suffix.length
//                    this.documentLength = suffix.length
//                }
//            }
    } else {
        //...uho
        // We've extended past the end of this segment
        // This...complicates things potentially

        // First, check something - is there actually a next segment?

        @OptIn(ExperimentalContracts::class)
        freeze((self as? TextBufferSegment.HasNext)?.next) { overflow ->
            if (overflow == null) {
                //No? Sweet, nothing to worry about. suffixSegment becomes null, and then we just work around it
                suffixSegment = null
            } else {
                //Damnit, okay so we basically need to do this whole thing again but for the next segment. Recursion ahoy!
                overflow.insertSegment(position, new)

                //If we've overflowed, then the next section will be set here, and it'll also be *our* next section
                //The overflow should have handled this, so we just set suffixSegment here to null
                suffixSegment = null
            }
        }
    }

//    if (self.documentPosition != null && self.documentLength != null) {
//        val selfPos = self.documentPosition!!
//        val selfLen = self.documentLength!!
//
//        if (prefixSegment == null && suffixSegment == null) {
//            //Perfect overwrite
//            new.documentPosition = selfPos
//            new.documentLength = selfLen
//        } else if (prefixSegment == null && !wasDirty) {
//            //Overwrite from the start
//            new.documentPosition = selfPos
//            new.documentLength = selfLen - segSuffixLen
//        } else if (suffixSegment == null && !wasDirty) {
//            //Overwrite at the end
//            new.documentPosition = selfPos + segPrefixLen
//            new.documentLength = selfLen - segPrefixLen
//        }
//    }

    //Detach ourself from the outside world
    self.detach()

    //Set the prefix segment's next segment to the new segment
    if (prefixSegment != null) {
        val (prefixWithNext, newWithNext) = prefixSegment.withNext(new)
        new = newWithNext.withPrevious(prefixWithNext).first
    }
    //Set the suffix segment's previous segment to the new segment
    if (suffixSegment != null) {
        val (suffixWithPrevious, newWithPrevious) = suffixSegment.withPrevious(new)
        new = newWithPrevious.withNext(suffixWithPrevious).first
    }

    return new

    //DEBUG: Print out the involved segments
//    println("$self#insert")
//    println("► Prefix: $prefixSegment")
//    println("► Insert: $new")
//    println("► Suffix: $suffixSegment")
}