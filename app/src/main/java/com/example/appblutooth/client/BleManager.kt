@file:Suppress("DEPRECATION")

package com.example.appblutooth.client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import timber.log.Timber
import java.nio.charset.Charset
import java.util.UUID


class BleManager(private val context: Context) {
    private var bluetoothGatt: BluetoothGatt? = null
    companion object{
        var TAG : String = "BLE"
    }
    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
    }

    // Hàm để gửi dữ liệu thông qua Bluetooth GATT characteristic
    @SuppressLint("MissingPermission")
    fun sendData(characteristic: BluetoothGattCharacteristic, data: ByteArray) {
        characteristic.value = data
        bluetoothGatt?.writeCharacteristic(characteristic)
    }
    // Hàm để đăng ký lắng nghe thông báo từ Bluetooth GATT characteristic
    @SuppressLint("MissingPermission")
    fun enableNotifications(characteristic: BluetoothGattCharacteristic,characteristic_uuid : String) {
        bluetoothGatt?.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(UUID.fromString(characteristic_uuid))
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        bluetoothGatt?.writeDescriptor(descriptor)
    }
    fun getCharacteristic(serviceUuid: String, characteristicUuid: String): BluetoothGattCharacteristic? {
        // Tìm dịch vụ thông qua serviceUuid
        val service: BluetoothGattService? = bluetoothGatt?.getService(UUID.fromString(serviceUuid))
        // Kiểm tra xem dịch vụ có tồn tại không
        if (service == null) {
            Timber.e("Service with UUID $serviceUuid not found.")
            return null
        }
        // Tìm đặc điểm thông qua characteristicUuid trong dịch vụ
        val characteristic: BluetoothGattCharacteristic? = service.getCharacteristic(UUID.fromString(characteristicUuid))

        // Kiểm tra xem đặc điểm có tồn tại không
        if (characteristic == null) {
            Timber.e("Characteristic with UUID $characteristicUuid not found in the service with UUID $serviceUuid.")
            return null
        }
        return characteristic
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            // Xử lý sự kiện kết nối thiết bị Bluetooth
        //    if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt!!.discoverServices()
        //    }
            //else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
             //   Toast.makeText(context, "Disconnected from GATT server.", Toast.LENGTH_SHORT).show()
         //   }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            // Xử lý sự kiện khi đã phát hiện các services của thiết bị
      //      if (status == BluetoothGatt.GATT_SUCCESS) {
                val serviceUuid = "0000fff0-0000-1000-8000-00805f9b34fb"
                val characteristicUuid = "0000fff3-0000-1000-8000-00805f9b34fb"
                val services= gatt?.getService(UUID.fromString(serviceUuid))
                val characteristic = services?.getCharacteristic(UUID.fromString(characteristicUuid))
                // Write data to the characteristic
                val dataToSend = "Hau3108".toByteArray(Charset.defaultCharset())
                if (characteristic != null) {
                    characteristic.value = dataToSend
                }
                gatt?.writeCharacteristic(characteristic)
                // Read the characteristic value
             //   gatt!!.readCharacteristic(characteristic)
//                services?.forEach { service ->
//                    val serviceUuid = service.uuid
//                    Utils.service_Uuid = serviceUuid
//                    // Lấy danh sách characteristics của service
//                    val characteristics: List<BluetoothGattCharacteristic>? =
//                        service.characteristics
//                    characteristics?.forEach { characteristic ->
//                        val characteristicUuid = characteristic.uuid
//                        Utils.characteristic_Uuid = characteristicUuid
//                    }
//                }
   //         }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
           // if (status == BluetoothGatt.GATT_SUCCESS) {
             //  Toast.makeText(context, "Data sent successfully", Toast.LENGTH_SHORT).show()
        //    } else {
            //    Toast.makeText(context, "Failed to send data, handle the error", Toast.LENGTH_SHORT).show()
           // }
        }
//
//        override fun onCharacteristicChanged(
//            gatt: BluetoothGatt,
//            characteristic: BluetoothGattCharacteristic,
//            value: ByteArray
//        ) {
//            // Khi thu được thông báo từ thiết bị client gửi dữ liệu
//            val data = characteristic.value
//            if (data != null) {
//                Log.d(TAG,data.toString())
//            }
//
//        }
    }
}