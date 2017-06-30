package io.github.rockerhieu.duet.instrument

import com.google.android.things.contrib.driver.pwmspeaker.Speaker
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by rockerhieu on 6/29/17.
 */
class Piano8bit(val speaker: Speaker) {
    val queue = LinkedBlockingQueue<Note>()
    var closed = false

    init {
        async(CommonPool) {
            while (!closed) {
                while (!closed && queue.isEmpty()) {
                    delay(50)
                }
                while (!closed && !queue.isEmpty()) {
                    play(queue.take())
                }
            }
        }
    }

    fun close() {
        speaker.close()
        closed = true
    }

    fun play(note: Note) {
        ensureNotClosed()
        synchronized(speaker, {
            speaker.play(note.frequency)
            Thread.sleep(note.duration.toLong())
            speaker.stop()
        })
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

    fun ensureNotClosed() {
        if (closed) throw IllegalStateException("Piano8bit is closed")
    }
}