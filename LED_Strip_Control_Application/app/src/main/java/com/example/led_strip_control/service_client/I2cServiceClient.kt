package com.example.led_strip_control.service_client

import android.annotation.SuppressLint
import android.content.Context
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import i2c.service.api.II2cService
import i2c.service.api.I2cStatus
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class I2cServiceClient private constructor(private val context: Context) {

    companion object {
        private const val TAG = "SHERIF_I2C_SERVICE_CLIENT"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: I2cServiceClient? = null

        fun getInstance(context: Context): I2cServiceClient {
            Log.d(TAG, "getInstance() called")
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: I2cServiceClient(context.applicationContext).also {
                    INSTANCE = it
                    Log.d(TAG, "New instance created")
                }
            }
        }
    }

    private var i2cService: II2cService? = null

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
                val result: Any? = getService.invoke(serviceManagerClass, "i2c.service.api.II2cService/default")

                if (result != null) {
                    val binder = result as IBinder
                    i2cService = II2cService.Stub.asInterface(binder)
                    Log.d(TAG, "Service connected successfully: $i2cService")
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

    fun initDevice(i2cDevicePath: String): I2cStatus? {
        return executeServiceCall("init") {
            i2cService?.init(i2cDevicePath)
        }
    }

    fun readValue(address: Int, register: Int): Int? {
        return executeServiceCall("readValue") {
            i2cService?.readValue(address, register)
        }
    }

    fun writeValue(address: Int, register: Int, value: Int): I2cStatus? {
        return executeServiceCall("writeValue") {
            i2cService?.writeValue(address, register, value)
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
