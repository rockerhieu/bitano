package io.github.rockerhieu.duet

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.annotation.ColorInt
import android.util.Log
import com.google.android.things.contrib.driver.apa102.Apa102
import com.google.android.things.contrib.driver.bmx280.Bmx280
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import com.google.android.things.contrib.driver.ht16k33.Ht16k33
import com.google.android.things.contrib.driver.pwmspeaker.Speaker
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import io.github.rockerhieu.duet.instrument.Piano8bit
import io.github.rockerhieu.duet.instrument.Song


/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
class MainActivity : Activity(), Button.OnButtonEventListener {
    val TAG = "Main"

    val sensor: Bmx280 by lazy { RainbowHat.openSensor() }
    val segment: AlphanumericDisplay by lazy { RainbowHat.openDisplay() }
    val ledstrip: Apa102 by lazy { RainbowHat.openLedStrip() }
    val speaker: Speaker by lazy { RainbowHat.openPiezo() }
    val sensorManager: SensorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val temperatureEventListener: SensorEventListener by lazy {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                Log.i(TAG, "sensor changed: ${event.values[0]}")
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                Log.i(TAG, "accuracy buchanged: $accuracy")
                updateTemperature()
            }
        }
    }
    val dynamicSensorCallback: SensorManager.DynamicSensorCallback by lazy {
        object : SensorManager.DynamicSensorCallback() {
            override fun onDynamicSensorConnected(sensor: Sensor) {
                if (sensor.stringType === Sensor.STRING_TYPE_AMBIENT_TEMPERATURE) {
                    sensorManager.registerListener(temperatureEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST)
                }
            }

            override fun onDynamicSensorDisconnected(sensor: Sensor?) {
                if (sensor?.stringType === Sensor.STRING_TYPE_AMBIENT_TEMPERATURE) {
                    sensorManager.unregisterListener(temperatureEventListener)
                }
            }
        }
    }
    val piano: Piano8bit by lazy { Piano8bit(speaker) }
    val buttonA: Button by lazy { RainbowHat.openButtonA() }
    val buttonB: Button by lazy { RainbowHat.openButtonB() }
    val buttonC: Button by lazy { RainbowHat.openButtonC() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensor.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X)

        segment.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX)
        segment.setEnabled(true)
        sensorManager.registerDynamicSensorCallback(dynamicSensorCallback)
        segment.clear()
        segment.setEnabled(true)
        segment.display(sensor.readTemperature().toDouble())
        ledstrip.brightness = 1
        lightstripOff()
        buttonA.setOnButtonEventListener(this)
        buttonB.setOnButtonEventListener(this)
        buttonC.setOnButtonEventListener(this)
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(temperatureEventListener)
        sensorManager.unregisterDynamicSensorCallback(dynamicSensorCallback)
        speaker.close()
        segment.close()
        sensor.close()
        piano.close()
        ledstrip.close()
        buttonA.close()
        buttonB.close()
        buttonC.close()
        super.onDestroy()
    }

    override fun onButtonEvent(button: Button, pressed: Boolean) {
        when (button) {
            buttonA -> if (!pressed) lightstripOn(Color.BLUE) else lightstripOff()
            buttonB -> if (!pressed) rainbow() else lightstripOff()
            buttonC -> if (!pressed) piano.play(Song.CHAU_LEN_3) else speaker.stop()
        }
    }

    private fun updateTemperature() {
        val temperature = sensor.readTemperature().toDouble()
        Log.d(TAG, "Temperature: $temperature")
        segment.display(temperature)
    }

    private fun lightstripOn(@ColorInt color: Int) {
        val rainbow = IntArray(RainbowHat.LEDSTRIP_LENGTH)
        for (i in 0..rainbow.size - 1) {
            rainbow[i] = color // Color.HSVToColor(255, floatArrayOf(i * 360f / rainbow.size, 1.0f, 1.0f))
        }
        ledstrip.write(rainbow)
    }

    private fun lightstripOff() {
        lightstripOn(Color.TRANSPARENT)
    }

    private fun rainbow() {
        val rainbow = IntArray(RainbowHat.LEDSTRIP_LENGTH)
        for (i in rainbow.indices) {
            rainbow[i] = Color.HSVToColor(255, floatArrayOf(i * 360f / rainbow.size, 1.0f, 1.0f))
        }
        ledstrip.write(rainbow)
    }
}
