package io.github.rockerhieu.duet

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread

/**
 * Created by rockerhieu on 6/29/17.
 */

class CameraActivity : Activity() {
    lateinit var cameraThread: HandlerThread
    lateinit var cameraHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraThread = HandlerThread("CameraBackground")
        cameraThread.start()
        cameraHandler = Handler(cameraThread.looper)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraThread.quitSafely()
    }
}