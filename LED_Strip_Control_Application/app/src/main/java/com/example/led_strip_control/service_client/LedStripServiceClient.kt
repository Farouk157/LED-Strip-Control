package com.example.led_strip_control.service_client

import android.annotation.SuppressLint
import android.content.Context
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import ledstrip.service.api.ILedstripService
import ledstrip.service.api.LedstripStatus
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class LedStripServiceClient private constructor(private val context: Context) {

    companion object {
        private const val TAG = "SHERIF"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: LedStripServiceClient? = null

        fun getInstance(context: Context): LedStripServiceClient {
            Log.d(TAG, "getInstance() called")
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LedStripServiceClient(context.applicationContext).also {
                    INSTANCE = it
                    Log.d(TAG, "New instance created")
                }
            }
        }
    }

    private var ledstripService: ILedstripService? = null

    init {
        initializeService()
    }

    @SuppressLint("PrivateApi")
    private fun initializeService() {
        try {
            Log.d(TAG, "Attempting to access ServiceManager.")
            val serviceManagerClass = Class.forName("android.os.ServiceManager")
            val getService: Method? = serviceManagerClass.getMethod("getService", String::class.java)

            if (getService != null) {
                Log.d(TAG, "getService method found.")
                val result: Any? = getService.invoke(serviceManagerClass, "ledstrip.service.api.ILedstripService/default")

                if (result != null) {
                    val binder = result as IBinder
                    ledstripService = ILedstripService.Stub.asInterface(binder)
                    Log.d(TAG, "Service connected successfully: $ledstripService")
                } else {
                    Log.e(TAG, "ServiceManager did not return a valid binder.")
                }
            } else {
                Log.e(TAG, "getService method not found.")
            }
        } catch (e: Exception) {
            handleReflectionError(e)
        }
    }


    // Function to unbind the service
    fun unbindService() {
        try {
            val unbindMethod = this::class.java.getMethod("unbindService")
            unbindMethod.invoke(this)
            Log.i(TAG, "Service unbound successfully using reflection.")
        } catch (e: NoSuchMethodException) {
            Log.e(TAG, "unbindService method not found: ${e.message}")
        } catch (e: IllegalAccessException) {
            Log.e(TAG, "Cannot access unbindService method: ${e.message}")
        } catch (e: InvocationTargetException) {
            Log.e(TAG, "Error invoking unbindService method: ${e.message}")
        }
    }



    // Function to set color
    fun setColor(index: Int, red: Int, green: Int, blue: Int): LedstripStatus? {
        return executeServiceCall("setColor") {
            ledstripService?.setColor(index, red, green, blue)
        }
    }

    // Function to clear the LED strip
    fun clear(): LedstripStatus? {
        return executeServiceCall("clear") {
            ledstripService?.clear()
        }
    }

    // Function to show the LED strip
    fun show(): LedstripStatus? {
        return executeServiceCall("show") {
            ledstripService?.show()
        }
    }

    // Function to enable global fade mode
    fun setGlobalFade(): LedstripStatus? {
        return executeServiceCall("setGlobalFade") {
            ledstripService?.setGlobalFade()
        }
    }

    // Function to enable or disable random mode
    fun setRandom(): LedstripStatus? {
        return executeServiceCall("setRandom") {
            ledstripService?.setRandom()
        }
    }

    // Function to set brightness
    fun setBrightness(brightnessPercentage: Int): LedstripStatus? {
        return executeServiceCall("setBrightness") {
            ledstripService?.setBrightness(brightnessPercentage)
        }
    }

    // Function to stop all modes
    fun stopAllModes(): LedstripStatus? {
        return executeServiceCall("stopAllModes") {
            ledstripService?.stopAllModes()
        }
    }


    private fun <T> executeServiceCall(action: String, block: () -> T?): T? {
        Log.d(TAG, "$action() called")
        return try {
            block()?.also {
                Log.i(TAG, "$action result: $it")
            }
        } catch (e: RemoteException) {
            Log.e(TAG, "Remote exception during $action: ${e.message}", e)
            Toast.makeText(context, "Remote exception: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun handleReflectionError(e: Exception) {
        val errorMessage = when (e) {
            is ClassNotFoundException -> "ServiceManager class not found: ${e.message}"
            is NoSuchMethodException -> "Method not found: ${e.message}"
            is InvocationTargetException -> "Invocation target exception: ${e.message}"
            is IllegalAccessException -> "Illegal access exception: ${e.message}"
            else -> "Unexpected error: ${e.message}"
        }
        Log.e(TAG, errorMessage, e)
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }
}