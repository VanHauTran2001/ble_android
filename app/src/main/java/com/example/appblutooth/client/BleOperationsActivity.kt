@file:Suppress( "DEPRECATION")

package com.example.appblutooth.client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.appblutooth.R
import com.example.appblutooth.databinding.ActivityBleOperationsBinding

class BleOperationsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityBleOperationsBinding
    private lateinit var device : BluetoothDevice
    private var bluetoothManager : BleManager?= null
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ble_operations)
        onClickBack()
        bluetoothManager = BleManager(applicationContext)
        device = intent.getParcelableExtra("device")?: error("Missing BluetoothDevice from MainActivity!")
        binding.txtDeviceName.text = device.name
        onConnectDevice()
        onClickDisconnect()
    }

    private fun onClickDisconnect() {
//        val serviceUuid = "0000fff0-0000-1000-8000-00805f9b34fb"
//        val characteristicUuid = "0000fff3-0000-1000-8000-00805f9b34fb"
//        binding.txtDisConnect.setOnClickListener {
//            bluetoothManager?.sendData(bluetoothManager!!.getCharacteristic(serviceUuid,characteristicUuid)!!,"Hau112".toByteArray())
//          //  bluetoothManager!!.enableNotifications(bluetoothManager!!.getCharacteristic(serviceUuid,characteristicUuid)!!,characteristicUuid)
//        }
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