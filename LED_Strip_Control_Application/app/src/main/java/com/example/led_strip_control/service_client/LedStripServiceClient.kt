package com.example.led_strip_control.service_client

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import ledstrip.service.api.ILedstripService
import ledstrip.service.api.LedstripStatus

class LedStripServiceClient private constructor(private val context: Context) {

    private var ledstripService: ILedstripService? = null
    private var isBound = false

    // Define the ServiceConnection
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            ledstripService = ILedstripService.Stub.asInterface(service)
            isBound = true
            Log.i("LedstripClient", "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            ledstripService = null
            isBound = false
            Log.i("LedstripClient", "Service disconnected")
        }
    }

    // Bind to the service
    fun bindService() {
        val intent = android.content.Intent("ledstrip.service.api.ILedstripService")
        intent.setPackage("com.example.aospimage") // Replace with the actual package name
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    // Unbind from the service
    fun unbindService() {
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }

    // Function to set color
    fun setColor(index: Int, red: Int, green: Int, blue: Int): LedstripStatus? {
        return try {
            ledstripService?.setColor(index, red, green, blue)
        } catch (e: Exception) {
            Log.e("LedstripClient", "Error setting color", e)
            null
        }
    }

    // Function to clear the LED strip
    fun clear(): LedstripStatus? {
        return try {
            ledstripService?.clear()
        } catch (e: Exception) {
            Log.e("LedstripClient", "Error clearing LED strip", e)
            null
        }
    }

    // Function to show the LED strip
    fun show(): LedstripStatus? {
        return try {
            ledstripService?.show()
        } catch (e: Exception) {
            Log.e("LedstripClient", "Error showing LED strip", e)
            null
        }
    }

    // Function to enable global fade mode
    fun setGlobalFade(): LedstripStatus? {
        return try {
            ledstripService?.setGlobalFade()
        } catch (e: Exception) {
            Log.e("LedstripClient", "Error enabling global fade mode", e)
            null
        }
    }

    // Function to enable or disable random mode
    fun setRandom(value: Boolean): LedstripStatus? {
        return try {
            ledstripService?.setRandom(value)
        } catch (e: Exception) {
            Log.e("LedstripClient", "Error setting random mode", e)
            null
        }
    }

    // Function to set brightness
    fun setBrightness(brightnessPercentage: Int): LedstripStatus? {
        return try {
            ledstripService?.setBrightness(brightnessPercentage)
        } catch (e: Exception) {
            Log.e("LedstripClient", "Error setting brightness", e)
            null
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: LedStripServiceClient? = null

        fun getInstance(context: Context): LedStripServiceClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LedStripServiceClient(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
