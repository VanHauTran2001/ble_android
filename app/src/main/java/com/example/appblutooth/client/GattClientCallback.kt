package com.example.appblutooth.client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.util.Log
import com.example.appblutooth.server.ble_ultis.BluetoothUtils
import com.example.appblutooth.server.ble_ultis.StringUtils

@SuppressLint("MissingPermission")
class GattClientCallback(private val mClientActionListener: GattClientActionListener) :
    BluetoothGattCallback() {


    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        mClientActionListener.log("onConnectionStateChange newState: $newState")
        if (status == BluetoothGatt.GATT_FAILURE) {
            mClientActionListener.logError("Connection Gatt failure status $status")
            mClientActionListener.disconnectGattServer()
            return
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            // handle anything not SUCCESS as failure
            mClientActionListener.logError("Connection not GATT sucess status $status")
            mClientActionListener.disconnectGattServer()
            return
        }
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            mClientActionListener.log("Connected to device " + gatt.device.address)
            mClientActionListener.setConnected(true)
            gatt.discoverServices()
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mClientActionListener.log("Disconnected from device")
            mClientActionListener.disconnectGattServer()
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if (status != BluetoothGatt.GATT_SUCCESS) {
            mClientActionListener.log("Device service discovery unsuccessful, status $status")
            return
        }
        val matchingCharacteristics: List<BluetoothGattCharacteristic> =
            BluetoothUtils.findCharacteristics(gatt!!)
        if (matchingCharacteristics.isEmpty()) {
            mClientActionListener.logError("Unable to find characteristics.")
            return
        }
        mClientActionListener.log("Initializing: setting write type and enabling notification")
        for (characteristic in matchingCharacteristics) {
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            enableCharacteristicNotification(gatt, characteristic)
        }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mClientActionListener.log("Characteristic written successfully")
        } else {
            mClientActionListener.logError("Characteristic write unsuccessful, status: $status")
            mClientActionListener.disconnectGattServer()
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mClientActionListener.log("Characteristic read successfully")
            readCharacteristic(characteristic!!)
        } else {
            mClientActionListener.logError("Characteristic read unsuccessful, status: $status")
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic
    ) {
        super.onCharacteristicChanged(gatt, characteristic)
        mClientActionListener.log("Characteristic changed, " + characteristic.uuid.toString())
        readCharacteristic(characteristic)
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mClientActionListener.log(
                "Descriptor written successfully: " + descriptor.uuid.toString()
            )
            mClientActionListener.initializeTime()
        } else {
            mClientActionListener.logError(
                "Descriptor write unsuccessful: " + descriptor.uuid.toString()
            )
        }
    }

    private fun enableCharacteristicNotification(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        val characteristicWriteSuccess: Boolean =
            gatt.setCharacteristicNotification(characteristic, true)
        if (characteristicWriteSuccess) {
            mClientActionListener.log(
                "Characteristic notification set successfully for " + characteristic.uuid
                    .toString()
            )
            if (BluetoothUtils.isEchoCharacteristic(characteristic)) {
                mClientActionListener.initializeEcho()
            } else if (BluetoothUtils.isTimeCharacteristic(characteristic)) {
                enableCharacteristicConfigurationDescriptor(gatt, characteristic)
            }
        } else {
            mClientActionListener.logError(
                "Characteristic notification set failure for " + characteristic.uuid.toString()
            )
        }
    }

    private fun enableCharacteristicConfigurationDescriptor(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        val descriptorList: List<BluetoothGattDescriptor> = characteristic.descriptors
        val descriptor = BluetoothUtils.findClientConfigurationDescriptor(descriptorList)!!
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        val descriptorWriteInitiated: Boolean = gatt.writeDescriptor(descriptor)
        if (descriptorWriteInitiated) {
            mClientActionListener.log(
                "Characteristic Configuration Descriptor write initiated: " + descriptor.uuid
                    .toString()
            )
        } else {
            mClientActionListener.logError(
                "Characteristic Configuration Descriptor write failed to initiate: " + descriptor.uuid
                    .toString()
            )
        }
    }

    private fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        val messageBytes = characteristic.value
        mClientActionListener.log("Read: " + StringUtils.byteArrayInHexFormat(messageBytes))
        val message: String = StringUtils.stringFromBytes(messageBytes)!!
        mClientActionListener.log("Received message: $message")
    }
}