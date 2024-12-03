package com.example.led_strip_control.service_client

import java.util.concurrent.atomic.AtomicBoolean
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class I2cServiceApp(private val i2cServiceClient: I2cServiceClient) {
    companion object {
        private const val TAG = "SHERIF_I2C_APP"
    }

    private val isRunning = AtomicBoolean(true) // Used to control the loop
    @Volatile
    private var lastValue: Int? = null // Stores the last read ADC value

    fun run() = runBlocking {

        isRunning.set(true) // Reset the flag to allow the loop to start again

        val i2cDevicePath = "/dev/i2c-1" // Adjust the path based on your system
        val initStatus = i2cServiceClient.initDevice(i2cDevicePath)

        if (initStatus == null || !initStatus.success) {
            Log.e(TAG, "Failed to initialize I2C device: ${initStatus?.message ?: "Unknown error"}")
            return@runBlocking
        }
        Log.i(TAG, "I2C device initialized successfully: $i2cDevicePath")

        val address = 0x48 // Adjust the I2C address as needed

        // Configure the ADC
        val configRegister = 0x01
        val configValue = 0xC183 // Example configuration value (adjust as needed)
        val configStatus = i2cServiceClient.writeValue(address, configRegister, configValue)
        if (configStatus == null || !configStatus.success) {
            Log.e(TAG, "Failed to configure the ADC device: ${configStatus?.message ?: "Unknown error"}")
            return@runBlocking
        }
        Log.i(TAG, "ADC device configured successfully")

        val conversionRegister = 0x00 // Adjust based on your ADC's conversion register

        while (isRunning.get()) {
            // Trigger ADC conversion if needed
            val triggerRegister = 0x01 // Register to start conversion
            val triggerValue = 0x00 // Trigger value
            val triggerStatus = i2cServiceClient.writeValue(address, triggerRegister, triggerValue)
            if (triggerStatus == null || !triggerStatus.success) {
                Log.e(TAG, "Failed to trigger ADC conversion: ${triggerStatus?.message ?: "Unknown error"}")
            } else {
                Log.i(TAG, "ADC conversion triggered.")
            }

            // Read ADC conversion result
            val value = i2cServiceClient.readValue(address, conversionRegister)
            if (value == null) {
                Log.e(TAG, "Failed to read conversion register.")
                delay(500)
                continue
            }

            if (value != lastValue && value < 100) {
                lastValue = value // Update the last read value
                Log.i(TAG, "Read value from ADC: $value")
                println("ADC Value: $value")
            } else {
                Log.i(TAG, "ADC value unchanged: $value")
            }

            delay(100) // Delay for 100 milliseconds
        }

        Log.i(TAG, "ADC reading loop stopped.")
    }

    fun stop() {
        isRunning.set(false)
    }

    fun getLastValue(): Int? {
        return (lastValue?.times(3.12))?.toInt()
    }
}
