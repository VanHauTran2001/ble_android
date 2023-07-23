package com.example.appblutooth.server

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothProfile
import com.example.appblutooth.server.ble_ultis.BluetoothUtils.requiresResponse
import com.example.appblutooth.server.ble_ultis.ByteUtils
import com.example.appblutooth.server.ble_ultis.Constants.CHARACTERISTIC_ECHO_UUID
import com.example.appblutooth.server.ble_ultis.Constants.CLIENT_CONFIGURATION_DESCRIPTOR_UUID
import com.example.appblutooth.server.ble_ultis.StringUtils

class GattServerCallback(private val mServerActionListener: GattServerActionListener) :
    BluetoothGattServerCallback() {
    override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
        super.onConnectionStateChange(device, status, newState)
        mServerActionListener.log(
            """
    onConnectionStateChange ${device.address}
    status $status
    newState $newState
    """.trimIndent()
        )
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            mServerActionListener.addDevice(device)
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mServerActionListener.removeDevice(device)
        }
    }

    override fun onCharacteristicReadRequest(
        device: BluetoothDevice,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic
    ) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
        mServerActionListener.log("onCharacteristicReadRequest " + characteristic.uuid.toString())
        if (requiresResponse(characteristic)) {
            mServerActionListener.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_FAILURE,
                0,
                null
            )
        }
    }
    // so there is no need to check inside the callback
    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray
    ) {
        super.onCharacteristicWriteRequest(
            device,
            requestId,
            characteristic,
            preparedWrite,
            responseNeeded,
            offset,
            value
        )
        mServerActionListener.log(
            """
    onCharacteristicWriteRequest : ${characteristic.uuid}
    Received: ${StringUtils.stringFromBytes(value)}
    """.trimIndent()
        )
        if (CHARACTERISTIC_ECHO_UUID.equals(characteristic.uuid)) {
            mServerActionListener.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                0,
                null
            )

            // Reverse message to differentiate original message & response
            val response: ByteArray = ByteUtils.reverse(value)
            characteristic.value = response
            mServerActionListener.log("Sending: " + StringUtils.byteArrayInHexFormat(response))
            mServerActionListener.notifyCharacteristicEcho(response)
            mServerActionListener.log(response.toString())
        }
    }
    override fun onDescriptorReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        descriptor: BluetoothGattDescriptor
    ) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor)
        mServerActionListener.log("onDescriptorReadRequest" + descriptor.uuid.toString())
    }

    override fun onDescriptorWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        descriptor: BluetoothGattDescriptor,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        super.onDescriptorWriteRequest(
            device,
            requestId,
            descriptor,
            preparedWrite,
            responseNeeded,
            offset,
            value
        )
        mServerActionListener.log(
            """
    onDescriptorWriteRequest: ${descriptor.uuid}
    value: ${StringUtils.byteArrayInHexFormat(value)}
    """.trimIndent()
        )
        if (CLIENT_CONFIGURATION_DESCRIPTOR_UUID.equals(descriptor.uuid)) {
            mServerActionListener.addClientConfiguration(device, value)
            mServerActionListener.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                0,
                null
            )
        }
    }

    override fun onNotificationSent(device: BluetoothDevice, status: Int) {
        super.onNotificationSent(device, status)
        mServerActionListener.log("onNotificationSent")
    }
}