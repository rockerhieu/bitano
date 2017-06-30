package io.github.rockerhieu.duet.instrument

import java.util.regex.Pattern

/**
 * Created by rockerhieu on 6/29/17.
 */
data class Note(val label: String, val frequency: Double, val duration: Int) {
    companion object {
        @JvmStatic private val pattern = Pattern.compile("(\\d+)(\\w#?\\d*)")!!

        @JvmStatic fun parse(note: String): Note? {
            val matcher = pattern.matcher(note)
            var time = if (matcher.find()) Integer.parseInt(matcher.group(1)) else 4
            time = time * 1000 / 8
            if (matcher.matches()) {
                val key = matcher.group(2)
                if (NOTES.containsKey(key)) return Note(key, NOTES[key]!!, time)
            }
            return null
        }

        @JvmStatic val NOTES: HashMap<String, Double> = hashMapOf(
                "C-" to 8.176, "G#1" to 51.913, "E4" to 329.63, "C7" to 2093.0,
                "C#-" to 8.662, "A1" to 55.000, "F4" to 349.23, "C#7" to 2217.5,
                "D-" to 9.177, "A#1" to 58.270, "F#4" to 369.99, "D7" to 2349.3,
                "D#-" to 9.723, "B1" to 61.735, "G4" to 391.99, "D#7" to 2489.0,
                "E-" to 10.301, "C2" to 65.406, "G#4" to 415.31, "E7" to 2637.0,
                "F-" to 10.913, "C#2" to 69.295, "A4" to 440.00, "F7" to 2793.8,
                "F#-" to 11.562, "D2" to 73.416, "A#4" to 466.16, "F#7" to 2960.0,
                "G-" to 12.250, "D#2" to 77.781, "B4" to 493.88, "G7" to 3136.0,
                "G#-" to 12.978, "E2" to 82.406, "C5" to 523.25, "G#7" to 3322.4,
                "A-" to 13.750, "F2" to 87.307, "C#5" to 554.37, "A7" to 3520.0,
                "A#-" to 14.568, "F#2" to 92.499, "D5" to 587.33, "A#7" to 3729.3,
                "B-" to 15.434, "G2" to 97.998, "D#5" to 622.25, "B7" to 3951.1,
                "C0" to 16.352, "G#2" to 103.82, "E5" to 659.26, "C8" to 4186.0,
                "C#0" to 17.324, "A2" to 110.00, "F5" to 698.46, "C#8" to 4434.9,
                "D0" to 18.354, "A#2" to 116.54, "F#5" to 739.99, "D8" to 4698.6,
                "D#0" to 19.445, "B2" to 123.47, "G5" to 783.99, "D#8" to 4978.0,
                "E0" to 20.601, "C3" to 130.81, "G#5" to 830.61, "E8" to 5274.0,
                "F0" to 21.826, "C#3" to 138.59, "A5" to 880.00, "F8" to 5587.7,
                "F#0" to 23.124, "D3" to 146.83, "A#5" to 932.32, "F#8" to 5919.9,
                "G0" to 24.499, "D#3" to 155.56, "B5" to 987.77, "G8" to 6271.9,
                "G#0" to 25.956, "E3" to 164.81, "C6" to 1046.5, "G#8" to 6644.9,
                "A0" to 27.50, "F3" to 174.61, "C#6" to 1108.7, "A8" to 7040.0,
                "A#0" to 29.135, "F#3" to 184.99, "D6" to 1174.7, "A#8" to 7458.6,
                "B0" to 30.867, "G3" to 195.99, "D#6" to 1244.5, "B8" to 7902.1,
                "C1" to 32.703, "G#3" to 207.65, "E6" to 1318.5, "C9" to 8372.0,
                "C#1" to 34.648, "A3" to 220.00, "F6" to 1396.9, "C#9" to 8869.8,
                "D1" to 36.708, "A#3" to 233.08, "F#6" to 1480.0, "D9" to 9397.3,
                "D#1" to 38.890, "B3" to 246.94, "G6" to 1568.0, "D#9" to 9956.1,
                "E1" to 41.203, "C4" to 261.63, "G#6" to 1661.2, "E9" to 10548.1,
                "F1" to 43.653, "C#4" to 277.18, "A6" to 1760.0, "F9" to 11175.3,
                "F#1" to 46.249, "D4" to 293.66, "A#6" to 1864.7, "F#9" to 11839.8,
                "G1" to 48.999, "D#4" to 311.13, "B6" to 1975.5, "G9" to 12543.9
        )
    }
}