package dev.brella.korsola.core

import dev.brella.korsola.core.css.CssBuilder
import dev.brella.korsola.core.css.buildCSS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.jvm.FlowInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.net.spi.URLStreamHandlerProvider
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

@ExperimentalUnsignedTypes
class KorsolaUrlStreamHandler : URLStreamHandler(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()

    class Provider: URLStreamHandlerProvider() {
        /**
         * Creates a new `URLStreamHandler` instance with the specified
         * protocol.
         *
         * @param   protocol   the protocol ("`ftp`",
         * "`http`", "`nntp`", etc.).
         * @return  a `URLStreamHandler` for the specific protocol, or `null` if this factory cannot create a handler for the specific
         * protocol
         * @see java.net.URLStreamHandler
         */
        override fun createURLStreamHandler(protocol: String): URLStreamHandler? =
            if (protocol == PROTOCOL) KorsolaUrlStreamHandler() else null

    }
    companion object {
        const val PROTOCOL = "korsola"
        private val dynamicCSS: MutableMap<String, String> = ConcurrentHashMap()
        private val dynamicSources: MutableMap<String, DataSource<*>> = ConcurrentHashMap()

        fun registerCSS(path: String, contents: CssBuilder<*>.() -> Any?) {
            dynamicCSS[path] = buildCSS(contents)
        }

        fun registerCSS(path: String, contents: String) {
            dynamicCSS[path] = contents
        }

        fun registerSource(path: String, source: DataSource<*>) {
            dynamicSources[path] = source
        }
    }

    override fun openConnection(u: URL): URLConnection {
        val path = u.path
        if (path in dynamicSources) return KorneaURLConnection(this, u, dynamicSources.getValue(path))
        return StringURLConnection(u, dynamicCSS.getValue(path))
    }

    private class StringURLConnection(url: URL?, val contents: String) : URLConnection(url) {
        override fun connect() {}

        override fun getInputStream(): InputStream {
            return ByteArrayInputStream(contents.toByteArray())
        }
    }

    private class KorneaURLConnection(val scope: CoroutineScope, url: URL?, val source: DataSource<*>): URLConnection(url) {
        override fun connect() {}

        @ExperimentalCoroutinesApi
        override fun getInputStream(): InputStream = runBlocking { scope.FlowInputStream(source.openInputFlow().get(), true) }
    }
}