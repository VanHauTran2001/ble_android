package com.example.appblutooth.server.ble_ultis

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.example.appblutooth.server.ble_ultis.Constants.CHARACTERISTIC_ECHO_STRING
import com.example.appblutooth.server.ble_ultis.Constants.CHARACTERISTIC_TIME_STRING
import com.example.appblutooth.server.ble_ultis.Constants.CLIENT_CONFIGURATION_DESCRIPTOR_SHORT_ID
import com.example.appblutooth.server.ble_ultis.Constants.SERVICE_STRING

object BluetoothUtils {
    // Characteristics
    fun findCharacteristics(bluetoothGatt: BluetoothGatt): List<BluetoothGattCharacteristic> {
        val matchingCharacteristics: MutableList<BluetoothGattCharacteristic> = ArrayList()
        val serviceList = bluetoothGatt.services
        val service = findService(serviceList) ?: return matchingCharacteristics
        val characteristicList = service.characteristics
        for (characteristic in characteristicList) {
            if (isMatchingCharacteristic(characteristic)) {
                matchingCharacteristics.add(characteristic)
            }
        }
        return matchingCharacteristics
    }

    fun findEchoCharacteristic(bluetoothGatt: BluetoothGatt): BluetoothGattCharacteristic? {
        return findCharacteristic(bluetoothGatt, CHARACTERISTIC_ECHO_STRING)
    }

    fun findTimeCharacteristic(bluetoothGatt: BluetoothGatt): BluetoothGattCharacteristic? {
        return findCharacteristic(bluetoothGatt, CHARACTERISTIC_TIME_STRING)
    }

    private fun findCharacteristic(
        bluetoothGatt: BluetoothGatt,
        uuidString: String
    ): BluetoothGattCharacteristic? {
        val serviceList = bluetoothGatt.services
        val service = findService(serviceList) ?: return null
        val characteristicList = service.characteristics
        for (characteristic in characteristicList) {
            if (characteristicMatches(characteristic, uuidString)) {
                return characteristic
            }
        }
        return null
    }

    fun isEchoCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
        return characteristicMatches(characteristic, CHARACTERISTIC_ECHO_STRING)
    }

    fun isTimeCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
        return characteristicMatches(characteristic, CHARACTERISTIC_TIME_STRING)
    }

    private fun characteristicMatches(
        characteristic: BluetoothGattCharacteristic?,
        uuidString: String
    ): Boolean {
        if (characteristic == null) {
            return false
        }
        val uuid = characteristic.uuid
        return uuidMatches(uuid.toString(), uuidString)
    }

    private fun isMatchingCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
        if (characteristic == null) {
            return false
        }
        val uuid = characteristic.uuid
        return matchesCharacteristicUuidString(uuid.toString())
    }

    private fun matchesCharacteristicUuidString(characteristicIdString: String): Boolean {
        return uuidMatches(
            characteristicIdString,
            CHARACTERISTIC_ECHO_STRING,
            CHARACTERISTIC_TIME_STRING
        )
    }

    @JvmStatic
    fun requiresResponse(characteristic: BluetoothGattCharacteristic): Boolean {
        return (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
                != BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
    }

    fun requiresConfirmation(characteristic: BluetoothGattCharacteristic): Boolean {
        return (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE
                == BluetoothGattCharacteristic.PROPERTY_INDICATE)
    }

    // Descriptor
    fun findClientConfigurationDescriptor(descriptorList: List<BluetoothGattDescriptor?>): BluetoothGattDescriptor? {
        for (descriptor in descriptorList) {
            if (isClientConfigurationDescriptor(descriptor)) {
                return descriptor
            }
        }
        return null
    }

    private fun isClientConfigurationDescriptor(descriptor: BluetoothGattDescriptor?): Boolean {
        if (descriptor == null) {
            return false
        }
        val uuid = descriptor.uuid
        val uuidSubstring = uuid.toString().substring(4, 8)
        return uuidMatches(uuidSubstring, CLIENT_CONFIGURATION_DESCRIPTOR_SHORT_ID)
    }

    // Service
    private fun matchesServiceUuidString(serviceIdString: String): Boolean {
        return uuidMatches(serviceIdString, SERVICE_STRING)
    }

    private fun findService(serviceList: List<BluetoothGattService>): BluetoothGattService? {
        for (service in serviceList) {
            val serviceIdString = service.uuid
                .toString()
            if (matchesServiceUuidString(serviceIdString)) {
                return service
            }
        }
        return null
    }

    private fun uuidMatches(uuidString: String, vararg matches: String): Boolean {
        for (match in matches) {
            if (uuidString.equals(match, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}