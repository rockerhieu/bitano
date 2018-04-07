package io.github.rockerhieu.duet.instrument

/**
 * Created by rockerhieu on 6/29/17.
 */
data class Song(val notes: String) {
    companion object {
        @JvmStatic
        val CHAU_LEN_3 = Song("2B5 2A5 4G5 " +
                "1A5 2G5 2A5 3B5 " +
                "2A5 2A5 2B5 1G5 1B5 2A5 2B5 4G5 " +
                "2A5 2B5 2G5 1D5 1D5 2G5 2A5 4B5 " +
                "1A5 2G5 2G5 2B5 1A5 1G5 2A5 2B5 3G5 " +
                "1D5 2B5 2A5 2G5 1D5 1D5 2B5 2A5 8G5 ")

        // "Take Five, by the Dave Brubeck Quartet" from https://forums.tessel.io/t/8-bit-ish-music-player/453/11
        @JvmStatic
        val TAKE_5 = Song("2D#6 4F#6 2D#6 1B5 2G#5 2A#5 1B5 2C6 2C#6 4F6 2C#6 2A#5 " +
                "2F#5 1G#5 2A5 1A#5 2B5 3D#6 2B5 3G#5 " +
                "1F5 2F#5 2G#5 2A5 2A#5 1A5 2A#5 2B5 3C#6 1. 3C#6 2C6 1C#6 1. " +
                "2D#6 4F#6 2D#6 1B5 1. 1G#5 2A#5 1B5 2C6 2C#6 4F6 2C#6 2A#5 " +
                "2F#5 1G#5 2A5 1A#5 2B5 3D#6 2B5 3G#5 " +
                "2F5 2G#5 2C#6 1B5 5A#5")
    }
}