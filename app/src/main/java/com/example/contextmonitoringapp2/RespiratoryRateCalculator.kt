package com.example.contextmonitoringapp2

import kotlin.math.pow

fun respiratoryRateCalculator(
    accelValuesX: MutableList<Float>,
    accelValuesY: MutableList<Float>,
    accelValuesZ: MutableList<Float>
): Int {
    var counter = 0
    var prevVal: Float = 10f
    var currVal: Float


    for (i in 11 until accelValuesY.size) {
        currVal = kotlin.math.sqrt(
            accelValuesZ[i].toDouble().pow(2.0) +
                    accelValuesX[i].toDouble().pow(2.0) +
                    accelValuesY[i].toDouble().pow(2.0)
        ).toFloat()

        if (kotlin.math.abs(prevVal - currVal) > 0.15) {
            counter++
        }
        prevVal = currVal
    }

    val ret = (counter.toDouble() / 45.00)
    return (ret * 30).toInt()
}
