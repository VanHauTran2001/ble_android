@file:Suppress("DEPRECATION")

package com.example.appblutooth.client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.appblutooth.R
import com.example.appblutooth.databinding.ActivityBleOperationsBinding


class BleOperationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBleOperationsBinding
    private lateinit var device: BluetoothDevice
    private var bluetoothManager: BleManager? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ble_operations)
        onClickBack()
        bluetoothManager = BleManager(applicationContext, binding.viewClientLog)
        device = intent.getParcelableExtra("device")
            ?: error("Missing BluetoothDevice from MainActivity!")
        binding.txtDeviceName.text = device.name
        onConnectDevice()
        onClickDisconnect()
        onClickSendMessage()
        onClickClean()
    }

    private fun onClickClean() {
        binding.viewClientLog.clearLogButton.setOnClickListener {
            bluetoothManager!!.clearLogs()
        }
    }

    private fun onClickSendMessage() {
        binding.btnSend.setOnClickListener {
            bluetoothManager!!.sendMessage(binding.edtMessage)
            binding.edtMessage.setText("")
        }
    }

    private fun onClickDisconnect() {
        binding.txtDisConnect.setOnClickListener {
            bluetoothManager!!.disconnectGattServer()
        }
    }

    private fun onConnectDevice() {
        bluetoothManager?.connectToDevice(device)
    }

    private fun onClickBack() {
        binding.imgBack.setOnClickListener {
            finish()
            bluetoothManager?.disconnect()
        }
    }
}