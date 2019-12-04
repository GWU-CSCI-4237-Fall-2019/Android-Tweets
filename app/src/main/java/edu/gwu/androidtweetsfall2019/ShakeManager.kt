package edu.gwu.androidtweetsfall2019

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class ShakeManager(context: Context) : SensorEventListener {

    private val MIN_INTERVAL_BETWEEN_SHAKES = 2000

    private val SHAKE_THRESHOLD = 20.0

    private var callback: (() -> Unit)? = null

    private var lastShakeTime = 0L

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun detectShakes(callback: () -> Unit) {
        this.callback = callback

        if (sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).isNotEmpty()) {
            val accelerometer: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )

        } else {
            Log.e("ShakeManager", "Device does not have an accelerometer.")
        }
    }

    fun stopDetectingShakes() {
        this.callback = null
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastShakeTime

        if (timeDifference > MIN_INTERVAL_BETWEEN_SHAKES) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val magnitude = Math.sqrt(
                Math.pow(x.toDouble(), 2.0) +
                    Math.pow(y.toDouble(), 2.0) +
                    Math.pow(z.toDouble(), 2.0)
            ) - SensorManager.GRAVITY_EARTH

            Log.d("ShakeManager", "[X, Y, Z] = [$x, $y, $z]; Acceleration = $magnitude")

            if (magnitude > SHAKE_THRESHOLD) {
                lastShakeTime = currentTime
                callback?.invoke()
            }
        }
    }
}