package dev.brella.korsola.core

import dev.brella.kornea.toolkit.common.SharedState
import dev.brella.kornea.toolkit.common.freeze
import dev.brella.korsola.core.css.CssStyle
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.withContext
import org.abimon.kornea.io.common.AppendableAwait
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
        node = next.next
        return next
    }
}

@Suppress("EXPERIMENTAL_API_USAGE")
sealed class TextBufferSegment(val backing: SharedState<String, StringBuilder> = SharedState.of(StringBuilder()), val segmentStyle: CssStyle) : Iterable<TextBufferSegment>, AppendableAwait {
    constructor(builder: StringBuilder, segmentStyle: CssStyle) : this(SharedState.of(builder), segmentStyle) {
        textAttr.text = builder.toString()
    }

    constructor(string: String, segmentStyle: CssStyle) : this(SharedState.of(StringBuilder(string)), segmentStyle) {
        textAttr.text = string
    }

    interface HasPrevious {
        var previous: TextBufferSegment?

        //        abstract fun withPrevious(previous: TextBufferSegment): Pair<TextBufferSegment, TextBufferSegment>
        fun prependToChain(segment: BodySegment): BodySegment
    }

    val textAttr: Text = Text().apply { styleClass.add("text") }
    abstract var next: TextBufferSegment?

    class HeadSegment(backing: SharedState<String, StringBuilder>, style: CssStyle, override var next: TextBufferSegment?) : TextBufferSegment(backing, style) {
        constructor(builder: StringBuilder, style: CssStyle, next: TextBufferSegment?) : this(SharedState.of(builder), style, next) {
            textAttr.text = builder.toString()
        }

        constructor(string: String, style: CssStyle, next: TextBufferSegment?) : this(SharedState.of(StringBuilder(string)), style, next) {
            textAttr.text = string
        }

        constructor(style: CssStyle, next: TextBufferSegment?) : this(SharedState.of(StringBuilder()), style, next)

        val flow: TextFlow = TextFlow(textAttr)

        override fun appendToChain(segment: BodySegment): BodySegment {
            if (segment.next != null) {
                println("WARNING: ${segment.next} may be a lonely segment!")
            }

            (segment.next as? HasPrevious)?.previous = null
            segment.next = null

            val tail = segment.tail()
            val head = segment.head().let { head ->
                if (head is HeadSegment) {
                    head.replaceInChain(BodySegment(head.backing, head.segmentStyle, this, null))
                } else {
                    head.forEachIndexed { i, child -> flow.children.add(i + 1, child.textAttr) }
                    head as BodySegment
                }
            }

//
//            head.previous = this
            tail.next = next
            (next as? HasPrevious)?.previous = tail

            next = head
            head.previous = this

            return segment
        }

        override fun removeFromChain() {
            freeze(next) { next ->
                if (next == null) {
                    //Okay this is the easy one, just return
                    return
                }

                require(flow.children.removeAt(0) === textAttr)

                val head = next.replaceInChain(HeadSegment(next.backing, next.segmentStyle, null))
                head.flow.children.addAll(flow.children.drop(head.flow.children.size))
            }
        }

        override fun <T : TextBufferSegment> replaceInChain(segment: T): T = freeze(next) { next ->
            if (next == null) return segment

            when (segment) {
                is HeadSegment -> {
                    if (segment.next == null) {
                        //Nice and easy, just set the next segment and leave
                        segment.next = next
                        (next as? HasPrevious)?.previous = segment

                        segment.flow.children.add(next.textAttr)
                        val flow = segment.flow.children
                        next.forEach { seg -> flow.add(seg.textAttr) }
                        return segment
                    } else {
                        //We're replacing with a populated segment, oh boy
                        //Okay, so find the tail of this segment
                        val tail = segment.tail() as BodySegment

                        tail.next = next
                        (next as? HasPrevious)?.previous = tail

                        segment.flow.children.add(next.textAttr)

                        val flow = segment.flow.children
                        next.forEachIndexed { index, seg -> flow.add(index + 1, seg.textAttr) }

                        return segment
                    }
                }
                is BodySegment -> {
                    //Okay, so we *don't actually care* about this segment in this case... probably
                    //We need to get the head and tail

                    val head = segment.head()
                    val tail = segment.tail()

                    //And work with these

                    if (head !is HeadSegment) {
                        //...crap, we're dealing with an isolated segment probably
                        //Warning time!

                        println("WARN: $head is an isolated segment, but we have a following segment; what's going to happen to the text?")
                    } else {
                        val flow = head.flow.children
                        val indexIn = segment.indexInLine() + 1
                        next.forEachIndexed { index, seg -> flow.add(indexIn + index, seg.textAttr) }
                    }

                    tail.next = next
                    (next as? HasPrevious)?.previous = tail

                    return segment
                }

                else -> TODO("Implement $segment")
            }
        }
    }

    class BodySegment(backing: SharedState<String, StringBuilder>, style: CssStyle, override var previous: TextBufferSegment?, override var next: TextBufferSegment?) : TextBufferSegment(backing, style), HasPrevious {
        constructor(builder: StringBuilder, style: CssStyle, previous: TextBufferSegment?, next: TextBufferSegment?) : this(SharedState.of(builder), style, previous, next) {
            textAttr.text = builder.toString()
        }

        constructor(string: String, style: CssStyle, previous: TextBufferSegment?, next: TextBufferSegment?) : this(SharedState.of(StringBuilder(string)), style, previous, next) {
            textAttr.text = string
        }

        constructor(string: String, style: CssStyle) : this(SharedState.of(StringBuilder(string)), style, null, null) {
            textAttr.text = string
        }

        constructor(style: CssStyle, previous: TextBufferSegment?, next: TextBufferSegment?) : this(SharedState.of(StringBuilder()), style, previous, next)

        override fun prependToChain(segment: BodySegment): BodySegment {
            if (segment.next != null) {
                println("WARNING: ${segment.next} may be a lonely segment!")
            }

            (segment.next as? HasPrevious)?.previous = null
            segment.next = null

            val tail = segment.tail()
            val head = segment.head().let { head ->
                if (head is HeadSegment) {
                    head.replaceInChain(BodySegment(head.backing, head.segmentStyle, null, previous))
                } else {
                    val chainHead = head()
                    if (chainHead !is HeadSegment) {
                        println("WARN: $chainHead is not HeadSegment; $this is a faulty chain")
                    }

                    val chainIndex = indexInLine()
                    val flow = (chainHead as HeadSegment).flow.children
                    head.forEachIndexed { i, child -> flow.add(chainIndex + i, child.textAttr) }
                    head as BodySegment
                }
            }

//            head.forEachIndexed { i, child -> flow.children.add(i, child.textAttr) }
//            head.previous = this
            head.previous = previous
            previous?.next = head

            tail.next = this
            previous = tail

            return segment

//            segment.head().buildAndReplaceInChain { head ->
//                val body = BodySegment(head.backing, head.segmentStyle, null, null)
//                body.previous = previous
//                previous?.next = body
//
//                body.next = this
//                previous = body
//
//                body
//            }
//
//            return segment
        }

        override fun appendToChain(segment: BodySegment): BodySegment {
            if (segment.next != null) {
                println("WARNING: ${segment.next} may be a lonely segment!")
            }

            (segment.next as? HasPrevious)?.previous = null
            segment.next = null

            val tail = segment.tail()
            val head = segment.head().let { head ->
                if (head is HeadSegment) {
                    head.replaceInChain(BodySegment(head.backing, head.segmentStyle, this, null))
                } else {
                    val chainHead = head()
                    if (chainHead !is HeadSegment) {
                        println("WARN: $chainHead is not HeadSegment; $this is a faulty chain")
                    }

                    val chainIndex = indexInLine() + 1
                    val flow = (chainHead as HeadSegment).flow.children
                    head.forEachIndexed { i, child -> flow.add(chainIndex + i, child.textAttr) }
                    head as BodySegment
                }
            }

//            head.forEachIndexed { i, child -> flow.children.add(i, child.textAttr) }
//            head.previous = this
            tail.next = next
            (next as? HasPrevious)?.previous = tail

            next = head
            head.previous = this

            return segment
//
//            segment.head().buildAndReplaceInChain { head ->
//                val body = BodySegment(head.backing, head.segmentStyle, null, null)
//                body.next = next
//                (next as? HasPrevious)?.previous = body
//
//                body.previous = this
//                next = this
//
//                body
//            }
//
//            return segment
        }

        override fun removeFromChain() {
            (head() as? HeadSegment)?.flow?.children?.remove(textAttr)

            previous?.next = next
            (next as? HasPrevious)?.previous = previous
        }

        override fun <T : TextBufferSegment> replaceInChain(segment: T): T {
            when (segment) {
                is BodySegment -> {
//                    val flow = head.flow.children
//                    val indexIn = segment.indexInLine() + 1
//                    next.forEachIndexed { index, seg -> flow.add(indexIn + index, seg.textAttr) }

                    val chainIndex = indexInLine()
                    when (val chainHead = head()) {
                        is HeadSegment -> {
                            val flow = chainHead.flow.children
                            flow.removeAt(chainIndex)

                            val segHead = segment.head().let { head ->
                                if (head is HeadSegment) {
                                    //Parameters are null here because *we're* being replaced, and we don't want to cause a loop
                                    head.replaceInChain(BodySegment(head.backing, head.segmentStyle, null, null))
                                } else {
                                    head as BodySegment
                                }
                            }
                            val segTail = segment.tail()

                            segHead.forEachIndexed { index, seg -> flow.add(chainIndex + index, seg.textAttr) }

                            previous?.next = segHead
                            segHead.previous = previous

                            (next as? HasPrevious)?.previous = segTail
                            segTail.next = next
                        }

                        else -> throw IllegalStateException("$this has no proper head element ($chainHead)!")
                    }

                    return segment
                }
                is HeadSegment -> throw IllegalArgumentException("Cannot replace a body segment with a head segment!")

                else -> TODO("Implement replaceInChain#segment")
            }
        }

        init {
//            if (previous is HasNext) {
//                val prevAsNext = previous as HasNext
//
//                if (prevAsNext.next != null && next == null) {
//                    next = prevAsNext.next
//                }
//
//                prevAsNext.next = this
//            }
//
//            if (next != null) {
//                val nextAsPrev = next as HasPrevious
//
//                if (nextAsPrev.previous != null && previous == null) {
//                    previous = nextAsPrev.previous
//                }
//
//                nextAsPrev.previous = this
//            }
        }
    }

    abstract fun removeFromChain()
    abstract fun <T : TextBufferSegment> replaceInChain(segment: T): T

    abstract fun appendToChain(segment: BodySegment): BodySegment

    suspend fun text(): String = backing.accessState { it }

    override suspend fun appendAwait(value: Char) = withBuilder { append(value) }
    override suspend fun appendAwait(value: CharSequence?) = withBuilder { append(value) }
    override suspend fun appendAwait(value: CharSequence?, startIndex: Int, endIndex: Int) = withBuilder { append(value) }

    suspend inline fun length(): Int = backing.accessState { it.length }
    suspend inline fun withBuilder(flush: Boolean = true, noinline block: suspend StringBuilder.() -> StringBuilder): TextBufferSegment {
        backing.mutateState(block)

        if (flush) {
            withContext(Dispatchers.JavaFx) { textAttr.text = text() }
        }

        return this
    }

    suspend fun flush() =
        withContext(Dispatchers.JavaFx) { textAttr.text = text() }

    inline fun <T> scoped(block: TextBufferSegmentScope.EXPLICIT.(self: TextBufferSegment) -> T): T =
        TextBufferSegmentScope.EXPLICIT.block(this)

    override fun iterator(): Iterator<TextBufferSegment> = SegmentIterator(this)
}

inline fun <S : TextBufferSegment, T : TextBufferSegment> S.buildAndReplaceInChain(init: (self: S) -> T): T = replaceInChain(init(this))

inline fun TextBufferSegment.head(): TextBufferSegment {
    var node = (this as? TextBufferSegment.HasPrevious)?.previous ?: return this

    while (true) {
        node = (node as? TextBufferSegment.HasPrevious)?.previous ?: return node
    }
}

inline fun TextBufferSegment.indexInLine(): Int {
    var node = (this as? TextBufferSegment.HasPrevious)?.previous ?: return 0
    var index = 0

    while (true) {
        index++
        node = (node as? TextBufferSegment.HasPrevious)?.previous ?: return index
    }
}

inline fun TextBufferSegment.tail(): TextBufferSegment {
    var node = next ?: return this

    while (true) {
        node = node.next ?: return node
    }
}

inline fun TextBufferSegment.previousUntil(predicate: (TextBufferSegment) -> Boolean): TextBufferSegment {
    var node = (this as? TextBufferSegment.HasPrevious)?.previous ?: throw IllegalArgumentException("TextBufferSegment has no previous element")

    while (true) {
        if (predicate(node)) return node
        node = (node as? TextBufferSegment.HasPrevious)?.previous ?: throw IllegalArgumentException("TextBufferSegment has no previous element")
    }
}

suspend inline fun TextBufferSegment.previousUntilWithPosition(pos: Int, predicate: (TextBufferSegment, pos: Int) -> Boolean): TextBufferSegment {
    var node = (this as? TextBufferSegment.HasPrevious)?.previous ?: throw IllegalArgumentException("TextBufferSegment has no previous element")
    var pos = pos - this.length()

    while (true) {
        if (predicate(node, pos)) return node

        pos -= node.length()
        node = (node as? TextBufferSegment.HasPrevious)?.previous ?: throw IllegalArgumentException("TextBufferSegment has no previous element")
    }
}

suspend inline fun TextBufferSegment.nextUntilWithPosition(pos: Int, predicate: (TextBufferSegment, pos: Int) -> Boolean): TextBufferSegment {
    var node = next ?: throw IllegalArgumentException("TextBufferSegment has no next element")
    var pos = pos + this.length()

    while (true) {
        if (predicate(node, pos)) return node

        pos += node.length()
        node = node.next ?: throw IllegalArgumentException("TextBufferSegment has no next element")
    }
}

suspend inline fun TextBufferSegment.startingPosition(): Int {
    var node = (this as? TextBufferSegment.HasPrevious)?.previous ?: return 0
    var pos = 0

    while (true) {
        pos += node.length()
        node = (node as? TextBufferSegment.HasPrevious)?.previous ?: return pos
    }
}

suspend inline fun TextBufferSegment.endingPosition(): Int {
    var node = (this as? TextBufferSegment.HasPrevious)?.previous ?: return length()
    var pos = 0

    while (true) {
        pos += node.length()
        node = (node as? TextBufferSegment.HasPrevious)?.previous ?: return pos + length()
    }
}

suspend inline fun TextBufferSegment.seekInLine(column: Int): TextBufferSegment? {
    head().fold(0) { pos, segment: TextBufferSegment -> if (segment.contains(pos, column)) return segment else pos + segment.length() }

    return null
}

suspend inline fun TextBufferSegment.lineLength(): Long =
    head().fold(0L) { len, segment: TextBufferSegment -> len + segment.length() }

suspend inline fun TextBufferSegment.lineToString(): String =
    head().fold(StringBuilder()) { builder, segment: TextBufferSegment -> builder.append(segment.text()) }
        .toString()

//inline operator fun TextBufferSegment.contains(cursor: ConsoleCursor): Boolean {
//    val start = startingPosition()
//
//    return cursor.column >= start && cursor.column < start + segmentLength
//}

suspend inline fun TextBufferSegment.contains(column: Int): Boolean = contains(startingPosition(), column)
suspend inline fun TextBufferSegment.contains(start: Int, column: Int): Boolean = column >= start && column < start + length()

suspend fun TextBufferSegment.insertSegment(position: Int, new: TextBufferSegment, underflow: Boolean = false, overflow: Boolean = false): TextBufferSegment = scoped { self ->
//    val wasDirty = self.dirty
    var new = new

    val segLen = new.length()
    val selfLen = self.length()

    val start = self.startingPosition()
    val end = start + selfLen

    if (position < start && !overflow) return@scoped self.previousUntilWithPosition(start) { seg, segStart -> seg.contains(segStart, position) }.insertSegment(position, new)
    if (position > end && !underflow) return@scoped self.nextUntilWithPosition(start) { seg, segStart -> seg.contains(segStart, position) }.insertSegment(position, new)

    val segPrefixLen = position - start
    val segSuffixLen = self.length() - (segPrefixLen + segLen)

    val prefixSegment: TextBufferSegment?
    val suffixSegment: TextBufferSegment?

    var trimPrevious = false
    var trimNext = false

    if (segPrefixLen == 0) {
        //Good news - we start at the beginning of this segment
        //Since there's no prefix, prefixSegment just becomes previous
        if (self is TextBufferSegment.HasPrevious) {
            prefixSegment = self.previous
//            trimPrevious = true
        } else {
            prefixSegment = null
        }
    } else if (segPrefixLen > 0) {
        //Uho, there's a prefix here
        //It's cool though, we just need a substring
        val prefix = self.text().substring(0, segPrefixLen)

        //Create a new segment with the old style and prefix
        val prev = (self as? TextBufferSegment.HasPrevious)?.previous
//        (self as? TextBufferSegment.HasPrevious)?.previous = null

        prefixSegment = TextBufferSegment.BodySegment(prefix, self.segmentStyle, null, null).apply { previous = prev }
        prev?.next = prefixSegment


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
        suffixSegment = self.next
//        self.next = null
    } else if (segSuffixLen > 0) {
        //We have a suffix, n.b.d
        val suffix = self.text().let { str -> str.substring(str.length - segSuffixLen, str.length) }

        //Create a new segment with the old style and suffix
        suffixSegment = TextBufferSegment.BodySegment(suffix, self.segmentStyle, null, self.next)
        (self.next as? TextBufferSegment.HasPrevious)?.previous = suffixSegment

//        self.next = null
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
        freeze(self.next) { overflow ->
            if (overflow == null) {
                //No? Sweet, nothing to worry about. suffixSegment becomes null, and then we just work around it
                suffixSegment = null
            } else {
                //Damnit, okay so we basically need to do this whole thing again but for the next segment. Recursion ahoy!
                new = overflow.insertSegment(position, new, overflow = true)

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

    var beforeReplacement = (prefixSegment as? TextBufferSegment.HasPrevious)?.previous
    val afterReplacement = suffixSegment?.next

    //Set the prefix segment's next segment to the new segment
    if (prefixSegment != null) {
        (prefixSegment as? TextBufferSegment.HasPrevious)?.previous = null
        prefixSegment.next = new
        (new as? TextBufferSegment.HasPrevious)?.previous = prefixSegment
    }
    //Set the suffix segment's previous segment to the new segment
    if (suffixSegment != null) {
        suffixSegment.next = null
        (suffixSegment as? TextBufferSegment.HasPrevious)?.previous = new
        new.next = suffixSegment
    }

    //Detach ourself from the outside world
//    self.removeFromChain()
    self.replaceInChain(new)

    (prefixSegment as? TextBufferSegment.HasPrevious)?.previous = beforeReplacement
    suffixSegment?.next = afterReplacement

    return new

    //DEBUG: Print out the involved segments
//    println("$self#insert")
//    println("► Prefix: $prefixSegment")
//    println("► Insert: $new")
//    println("► Suffix: $suffixSegment")
}