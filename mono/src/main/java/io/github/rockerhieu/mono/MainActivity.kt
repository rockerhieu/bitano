package io.github.rockerhieu.mono

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.ColorInt
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.android.things.contrib.driver.apa102.Apa102
import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import com.google.android.things.contrib.driver.ht16k33.Ht16k33
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import io.github.rockerhieu.bitano.Camera
import io.github.rockerhieu.duet.instrument.Bitano
import io.github.rockerhieu.duet.instrument.Note
import io.github.rockerhieu.duet.instrument.NotePlayCallback
import io.github.rockerhieu.duet.instrument.NoteStopCallback
import io.github.rockerhieu.duet.instrument.Song
import java.lang.Exception
import java.util.Random

class MainActivity : Activity(), Button.OnButtonEventListener,
        ChildEventListener by DefaultChildEventListener() {
    val TAG = "Mono"

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val offsetRef: DatabaseReference = database.getReference(".info/serverTimeOffset")
    private val notesRef: DatabaseReference = database.getReference("notes")
    private val segment: AlphanumericDisplay by lazy { RainbowHat.openDisplay() }
    private val ledstrip: Apa102 by lazy { RainbowHat.openLedStrip() }
    private val speaker: Sbiter by lazy { Sbiter(RainbowHat.openPiezo()) }
    private val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private val temperatureEventListener: SensorEventListener by lazy {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                updateTemperature(event.values[0].toDouble())
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
    }

    private val environmentalSensorDriver: Bmx280SensorDriver by lazy { RainbowHat.createSensorDriver() }
    private val dynamicSensorCallback: SensorManager.DynamicSensorCallback by lazy {
        object : SensorManager.DynamicSensorCallback() {
            override fun onDynamicSensorConnected(sensor: Sensor) {
                if (sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                    sensorManager.registerListener(temperatureEventListener, sensor,
                            SensorManager.SENSOR_DELAY_NORMAL)
                }
            }

            override fun onDynamicSensorDisconnected(sensor: Sensor?) {
                if (sensor?.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                    sensorManager.unregisterListener(temperatureEventListener)
                }
            }
        }
    }
    private val random = Random()
    private val notePlayCallback: NotePlayCallback = {
        segment.display(it.label)
        lightstripOn(Color.argb(255, random.nextInt(256), random.nextInt(256),
                random.nextInt()))
    }
    private val noteStopCallback: NoteStopCallback = {
        segment.clear()
        lightstripOff()
    }
    private val bitano: Bitano by lazy { Bitano(speaker, notePlayCallback, noteStopCallback) }
    private val buttonA: Button by lazy { RainbowHat.openButtonA() }
    private val buttonB: Button by lazy { RainbowHat.openButtonB() }
    private val buttonC: Button by lazy { RainbowHat.openButtonC() }
    private var showTemperature = false

    private var storage: FirebaseStorage = FirebaseStorage.getInstance()
    private lateinit var camera: Camera
    private lateinit var cameraHandler: Handler
    private lateinit var cameraThread: HandlerThread
    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        val image = reader.acquireLatestImage()
        // get image bytes
        val imageBuf = image.planes[0].buffer
        val imageBytes = ByteArray(imageBuf.remaining())
        imageBuf.get(imageBytes)
        image.close()

        onPictureTaken(imageBytes)
    }
    private val label: TextView by lazy { findViewById<TextView>(R.id.label) }
    private val image: ImageView by lazy { findViewById<ImageView>(R.id.image) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        segment.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX)
        segment.setEnabled(true)
        sensorManager.registerDynamicSensorCallback(dynamicSensorCallback)
        environmentalSensorDriver.registerTemperatureSensor()
        segment.clear()
        segment.setEnabled(true)
        ledstrip.brightness = 1
        lightstripOff()
        buttonA.setOnButtonEventListener(this)
        buttonB.setOnButtonEventListener(this)
        buttonC.setOnButtonEventListener(this)
        offsetRef.addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(e: DatabaseError?) {
                    }

                    override fun onDataChange(data: DataSnapshot) {
                        val offset = data.getValue(Double::class.java) ?: 0.0
                        val estimatedServerTimeMs = System.currentTimeMillis() + offset
                        watchForNotes(estimatedServerTimeMs)
                    }
                }
        )

        // Creates new handlers and associated threads for camera and networking operations.
        cameraThread = HandlerThread("CameraBackground")
        cameraThread.start()
        cameraHandler = Handler(cameraThread.looper)

        // Camera code is complicated, so we've shoved it all in this closet class for you.
        camera = Camera.instance
        camera.initializeCamera(this, cameraHandler, mOnImageAvailableListener)
    }

    private fun watchForNotes(estimatedServerTimeMs: Double) {
        notesRef.startAt(estimatedServerTimeMs).addChildEventListener(this)
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(temperatureEventListener)
        sensorManager.unregisterDynamicSensorCallback(dynamicSensorCallback)
        environmentalSensorDriver.close()
        speaker.close()
        segment.close()
        bitano.close()
        ledstrip.close()
        buttonA.close()
        buttonB.close()
        buttonC.close()
        super.onDestroy()
        camera.shutDown()
        cameraThread.quitSafely()
        super.onDestroy()
    }

    override fun onChildAdded(data: DataSnapshot, previousChildName: String?) {
        Log.d(TAG, "onChildAdded: ${data.value}")
        val note = Note.parse("2${data.value}")
        if (note != null)
            bitano.play(note)
    }

    override fun onButtonEvent(button: Button, pressed: Boolean) {;
        when (button) {
            buttonA -> if (pressed) bitano.play(Song.CHAU_LEN_3) else {
                bitano.stop()
                segment.display("STOP")
            }
            buttonB -> if (pressed) showTemperature = true else {
                showTemperature = false
                segment.clear()
            }
            buttonC -> if (pressed) takePhoto()
        }
    }

    private fun takePhoto() {
        camera.takePicture()
    }

    private var count = 0
    private fun updateTemperature(temperature: Double) {
        if (showTemperature) segment.display(temperature)
        if (count++ % 1000 == 0) BitanoService.updateTemperatureAsync(temperature)
    }

    private fun lightstripOn(@ColorInt color: Int) {
        val rainbow = IntArray(RainbowHat.LEDSTRIP_LENGTH)
        for (i in 0 until rainbow.size) {
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

    /**
     * Upload image data to Firebase.
     */
    private fun onPictureTaken(imageBytes: ByteArray?) {
        if (imageBytes != null) {
            val log = database.getReference("logs").push()
            log.setPriority(ServerValue.TIMESTAMP)
            val imageRef = storage.getReference().child(log.getKey())

            // upload image to storage
            val task = imageRef.putBytes(imageBytes)
            task.addOnSuccessListener({ taskSnapshot ->
                val downloadUrl = taskSnapshot.downloadUrl!!
                // mark image in the database
                Log.i(TAG, "Image upload successful")
                log.child("timestamp").setValue(ServerValue.TIMESTAMP)
                log.child("image").setValue(downloadUrl.toString())
                // process image annotations
                annotateImage(downloadUrl.toString(), log)
            }).addOnFailureListener({
                // clean up this entry
                Log.w(TAG, "Unable to upload image to Firebase")
                log.removeValue()
            })
        }
    }

    /**
     * Process image contents with Cloud Vision.
     */
    private fun annotateImage(url: String, log: DatabaseReference) {
        Picasso.get().load(url).into(image)
        BitanoService.annotateImageAsync(url, object : BitanoService.Callback<List<String>> {
            override fun onError(e: Exception) {
                Log.w(TAG, "Can not annotate image", e)
            }

            override fun onSuccess(result: List<String>?) {
                Log.d(TAG, "$url is annotated with $result")
                val labels = result!!.toString()
                runOnUiThread({
                    log.child("labels").setValue(labels)
                    label.text = labels
                })
            }
        })
    }
}
