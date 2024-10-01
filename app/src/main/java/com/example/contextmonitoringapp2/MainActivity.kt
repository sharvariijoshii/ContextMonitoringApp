package com.example.contextmonitoringapp2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager



class MainActivity : AppCompatActivity() {

    private val STORAGE_PERMISSION_CODE = 101
    lateinit var heartRateButton: Button
    lateinit var heartRateTextView: TextView
    lateinit var respiratoryRateButton: Button
    lateinit var respiratoryRateTextView: TextView

    private var heartRate: Int = 0
    private var respiratoryRate: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        heartRateButton = findViewById(R.id.button_measure_heart_rate)
        heartRateTextView = findViewById(R.id.tv_heart_rate_value)

        respiratoryRateButton = findViewById(R.id.button_measure_respiratory_rate)
        respiratoryRateTextView = findViewById(R.id.tv_respiratory_rate_value)


        checkMediaPermission()

        heartRateButton.setOnClickListener {
            measureHeartRate()
        }

        respiratoryRateButton.setOnClickListener {
            calculateRespiratoryRate()
        }

        val symptomsButton = findViewById<Button>(R.id.button_symptoms)

        symptomsButton.setOnClickListener {
            val intent = Intent(this, SymptomLoggingActivity::class.java)
            intent.putExtra("heartRate", heartRate)  // Pass the calculated heart rate
            intent.putExtra("respiratoryRate", respiratoryRate)  // Pass the calculated respiratory rate
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun measureHeartRate() {
        val videoPath = "/storage/emulated/0/Download/Heart_Rate.mp4"
        val videoUri = Uri.parse(videoPath)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                heartRate = heartRateCalculator(videoUri)
                heartRateTextView.text = "Heart Rate: $heartRate"
            } catch (e: Exception) {
                Log.e("MainActivity", "Error measuring heart rate", e)
                heartRateTextView.text = "Error: ${e.localizedMessage}"
            }
        }
    }

    private fun checkMediaPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_VIDEO),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
            heartRateTextView.text = "Permission denied"
        }
    }

//    private inner class CalcRespRateThread : Runnable {
//        override fun run() {
//            val start = System.currentTimeMillis()
//            val end = start + 45 * 1000
//
//            // Sleep before vibration to simulate waiting period
//            Thread.sleep(3000)
//
//            // Vibrate to notify user
//            vibrator?.vibrate(500)
//
//            // Sleep to allow for vibration duration
//            Thread.sleep(2000)
//
//            val set = object : SensorEventListener {
//                override fun onSensorChanged(event: SensorEvent?) {
//                    event?.let {
//                        if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
//                            accelValuesX.add(it.values[0]) // Collect X-axis data
//                            accelValuesY.add(it.values[1]) // Collect Y-axis data
//                            accelValuesZ.add(it.values[2]) // Collect Z-axis data
//
//                            println("#################################")
//                            println("AccelData X: ${it.values[0]}, Y: ${it.values[1]}, Z: ${it.values[2]}")
//                        }
//                    }
//
//                    // Check if the collection period is over
//                    if (System.currentTimeMillis() > end) {
//                        accelManage!!.unregisterListener(this)
//
//                        // Calculate respiratory rate using helper function
//                        val respiratoryRate = respiratoryRateCalculator(accelValuesX, accelValuesY, accelValuesZ)
//
//                        // Vibrate to notify the user that the calculation is done
//                        vibrator?.vibrate(500)
//                        println("Respiratory rate: ${respiratoryRate}")
//                        runOnUiThread {
//                            findViewById<TextView>(R.id.respiratoryRate).text = "$respiratoryRate breaths per minute"
//                        }
//                    }
//                }
//
//                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//            }
//
//            accelManage!!.registerListener(set, senseAccel, SensorManager.SENSOR_DELAY_NORMAL)
//        }
//    }

    private fun calculateRespiratoryRate() {
        val accelValuesX = readCsvFromAssets(this, "CSVBreatheX.csv").toMutableList()
        val accelValuesY = readCsvFromAssets(this, "CSVBreatheY.csv").toMutableList()
        val accelValuesZ = readCsvFromAssets(this, "CSVBreatheZ.csv").toMutableList()

        respiratoryRate = respiratoryRateCalculator(accelValuesX, accelValuesY, accelValuesZ)

        respiratoryRateTextView.text = "Respiratory Rate: $respiratoryRate"
    }
}