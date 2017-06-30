package io.github.rockerhieu.bitano

/**
 * Created by rockerhieu on 6/30/17.
 */
interface Speaker {
    fun play(frequency: Double)
    fun stop()
    fun close()
}