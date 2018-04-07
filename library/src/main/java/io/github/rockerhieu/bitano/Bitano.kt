package io.github.rockerhieu.duet.instrument

import io.github.rockerhieu.bitano.Speaker
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.newSingleThreadContext
import java.io.Closeable
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by rockerhieu on 6/29/17.
 */
typealias NotePlayCallback = (Note) -> Unit

typealias NoteStopCallback = () -> Unit

class Bitano(val speaker: Speaker,
        val playCallback: NotePlayCallback? = null,
        val stopCallback: NoteStopCallback? = null) : Closeable {
    val queue = LinkedBlockingQueue<Note>()
    var closed = AtomicBoolean()

    init {
        async(newSingleThreadContext("BitanoPlayer")) {
            while (!closed.get()) play(queue.take())
        }
    }

    override fun close() {
        speaker.close()
        closed.set(true)
    }

    fun queue(note: Note) {
        ensureNotClosed()
        queue.add(note)
    }

    fun play(note: Note) {
        ensureNotClosed()
        synchronized(speaker, {
            playCallback?.invoke(note)
            speaker.play(note.frequency)
            Thread.sleep(note.duration.toLong())
            speaker.stop()
            stopCallback?.invoke()
        })
    }

    fun stop() {
        ensureNotClosed()
        queue.clear()
        speaker.stop()
    }

    fun play(song: Song, immediate: Boolean = true) {
        ensureNotClosed()
        if (immediate) {
            queue.clear()
            speaker.stop()
        }
        val frequencies = song.notes.split(' ')
        frequencies.mapNotNullTo(queue) { Note.parse(it) }
    }

    private fun ensureNotClosed() {
        if (closed.get()) throw IllegalStateException("Bitano is closed")
    }
}