package io.github.rockerhieu.mono

import io.github.rockerhieu.bitano.Speaker

/**
 * Created by rockerhieu on 6/30/17.
 */
class Sbiter(val speaker: com.google.android.things.contrib.driver.pwmspeaker.Speaker) : Speaker {
    override fun play(frequency: Double) {
        speaker.play(frequency)
    }

    override fun stop() {
        speaker.stop()
    }

    override fun close() {
        speaker.close()
    }
}