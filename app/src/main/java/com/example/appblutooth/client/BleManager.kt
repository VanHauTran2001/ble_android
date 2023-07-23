@file:Suppress("DEPRECATION")

package com.example.appblutooth.client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import com.example.appblutooth.databinding.ViewLogBinding
import com.example.appblutooth.server.ble_ultis.BluetoothUtils.findEchoCharacteristic
import com.example.appblutooth.server.ble_ultis.BluetoothUtils.findTimeCharacteristic
import com.example.appblutooth.server.ble_ultis.StringUtils
import com.example.appblutooth.server.ble_ultis.StringUtils.bytesFromString


@SuppressLint("MissingPermission")
class BleManager(private val context: Context, private val viewClientLog: ViewLogBinding) :
    GattClientActionListener {
    private var mGatt: BluetoothGatt? = null
    private var mLogHandler: Handler? = null
    private var mConnected: Boolean? = null
    private var mTimeInitialized: Boolean? = null
    private var mEchoInitialized: Boolean? = null

    init {
        mLogHandler = Handler(Looper.getMainLooper())
    }

    fun connectToDevice(device: BluetoothDevice) {
        val gattClientCallback = GattClientCallback(this)
        mGatt = device.connectGatt(context, false, gattClientCallback)
    }

    fun disconnect() {
        mGatt?.disconnect()
        mGatt?.close()
    }

    fun sendMessage(messageEditText: EditText) {
        if (!mConnected!! || !mEchoInitialized!!) {
            return
        }
        val characteristic = findEchoCharacteristic(mGatt!!)
        if (characteristic == null) {
            logError("Unable to find echo characteristic.")
            disconnectGattServer()
            return
        }
        val message = messageEditText.text.toString()
        log("Sending message: $message")
        val messageBytes = bytesFromString(message)
        if (messageBytes.isEmpty()) {
            logError("Unable to convert message to bytes")
            return
        }
        characteristic.value = messageBytes
        val success = mGatt!!.writeCharacteristic(characteristic)
        if (success) {
            log("Wrote: "+StringUtils.byteArrayInHexFormat(messageBytes))
        } else {
            logError("Failed to write data")
        }
        //requestTimestamp()
    }

    private fun requestTimestamp() {
        if (!mConnected!! || !mTimeInitialized!!) {
            return
        }
        val characteristic = findTimeCharacteristic(mGatt!!)
        if (characteristic == null) {
            logError("Unable to find time charactaristic")
            return
        }
        mGatt!!.readCharacteristic(characteristic)
    }

    override fun log(message: String?) {
        mLogHandler!!.post {
            viewClientLog.logTextView.append(message + "\n")
            viewClientLog.logScrollView.post {
                viewClientLog.logScrollView.fullScroll(
                    View.FOCUS_DOWN
                )
            }
        }
    }

    fun clearLogs() {
        mLogHandler!!.post { viewClientLog.logTextView.text = "" }
    }

    override fun logError(message: String?) {
        log("Error: $message")
    }

    override fun setConnected(connected: Boolean) {
        mConnected = connected
    }

    override fun initializeTime() {
        mTimeInitialized = true
    }

    override fun initializeEcho() {
        mEchoInitialized = true
    }

    override fun disconnectGattServer() {
        log("Closing Gatt connection")
        mConnected = false
        mEchoInitialized = false
        mTimeInitialized = false
        clearLogs()
        if (mGatt != null) {
            mGatt!!.disconnect()
            mGatt!!.close()
        }
    }
}