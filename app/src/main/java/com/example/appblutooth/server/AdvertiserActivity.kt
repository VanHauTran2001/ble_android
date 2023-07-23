package com.example.appblutooth.server

import android.Manifest
import android.R.attr.value
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.appblutooth.R
import com.example.appblutooth.databinding.ActivityAdvertiserBinding
import com.example.appblutooth.server.ble_ultis.BluetoothUtils.findClientConfigurationDescriptor
import com.example.appblutooth.server.ble_ultis.BluetoothUtils.requiresConfirmation
import com.example.appblutooth.server.ble_ultis.Constants.CHARACTERISTIC_ECHO_UUID
import com.example.appblutooth.server.ble_ultis.Constants.CHARACTERISTIC_TIME_UUID
import com.example.appblutooth.server.ble_ultis.Constants.CLIENT_CONFIGURATION_DESCRIPTOR_UUID
import com.example.appblutooth.server.ble_ultis.Constants.SERVICE_UUID
import com.example.appblutooth.server.ble_ultis.StringUtils.byteArrayInHexFormat
import java.util.UUID


@SuppressLint("MissingPermission")
class AdvertiserActivity : AppCompatActivity() , GattServerActionListener {
    private lateinit var binding : ActivityAdvertiserBinding
    private var mGattServer: BluetoothGattServer? = null
    private var mBluetoothManager: BluetoothManager? = null
    private var mHandler: Handler? = null
    private var mLogHandler: Handler? = null
    private var mDevices: java.util.ArrayList<BluetoothDevice>? = null
    private var mClientConfigurations: java.util.HashMap<String, ByteArray>? = null
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private var isStartAdvertising = false
        set(value) {
            field = value
            runOnUiThread { binding.btnAdvertising.text = if (value) "Stop Advertising" else "Start Advertising" }
        }
    private val bluetoothLeAdvertiser: BluetoothLeAdvertiser? by lazy {
        bluetoothAdapter.bluetoothLeAdvertiser
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_advertiser)
        mHandler = Handler()
        mLogHandler = Handler(Looper.getMainLooper())
        mDevices = ArrayList()
        mClientConfigurations = HashMap()
        mBluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val gattServerCallback = GattServerCallback(this@AdvertiserActivity)
        mGattServer = mBluetoothManager!!.openGattServer(this, gattServerCallback)
        onClickStartAdvertising()
        onClickBack()
        setupServer()
        onClickCleanLog()
    }
//    fun createTimeService(): BluetoothGattService {
//        val service = BluetoothGattService(SERVICE_UUID,
//            BluetoothGattService.SERVICE_TYPE_PRIMARY)
//
//        // Current Time characteristic
//        val currentTime = BluetoothGattCharacteristic(CHARACTERISTIC_ECHO_UUID,
//            //Read-only characteristic, supports notifications
//            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
//            BluetoothGattCharacteristic.PERMISSION_READ)
//        val configDescriptor = BluetoothGattDescriptor(CHARACTERISTIC_ECHO_UUID,
//            //Read/write descriptor
//            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY)
//        currentTime.addDescriptor(configDescriptor)
//
//        // Local Time Information characteristic
//        val localTime = BluetoothGattCharacteristic(CHARACTERISTIC_ECHO_UUID,
//            //Read-only characteristic
//            BluetoothGattCharacteristic.PROPERTY_READ,
//            BluetoothGattCharacteristic.PERMISSION_READ)
//        val currentWriteTime = BluetoothGattCharacteristic(CHARACTERISTIC_ECHO_UUID,
//            //Read-only characteristic, supports notifications
//            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_READ,
//            BluetoothGattCharacteristic.PERMISSION_WRITE or BluetoothGattDescriptor.PERMISSION_READ)
//        service.addCharacteristic(currentTime)
//        service.addCharacteristic(currentWriteTime)
//        service.addCharacteristic(localTime)
//
//        return service
//    }
    private fun onClickCleanLog() {
        binding.viewServerLog.clearLogButton.setOnClickListener {
            clearLogs()
        }
    }

    private fun setupServer() {
        val service = BluetoothGattService(
            SERVICE_UUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        // Write characteristic
        val writeCharacteristic = BluetoothGattCharacteristic(
            CHARACTERISTIC_ECHO_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        // Read characteristic
        val readCharacteristic = BluetoothGattCharacteristic(
            CHARACTERISTIC_ECHO_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        // Characteristic with Descriptor
        val notifyCharacteristic = BluetoothGattCharacteristic(
            CHARACTERISTIC_TIME_UUID,
            //                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            0,
            0
        )

        val clientConfigurationDescriptor = BluetoothGattDescriptor(
            CLIENT_CONFIGURATION_DESCRIPTOR_UUID,
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        clientConfigurationDescriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE

        notifyCharacteristic.addDescriptor(clientConfigurationDescriptor)

        service.addCharacteristic(writeCharacteristic)
        service.addCharacteristic(notifyCharacteristic)
        service.addCharacteristic(readCharacteristic)

        mGattServer!!.addService(service)
    }

    private fun onClickBack() {
        binding.imgBack.setOnClickListener {
            stopAdvertising()
            clearLogs()
            finish()
        }
    }

    private fun stopAdvertising(){
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        isStartAdvertising = false
    }
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Toast.makeText(this@AdvertiserActivity,"Advertising started successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startAdvertising(){
        if (bluetoothLeAdvertiser == null){
            Toast.makeText(this@AdvertiserActivity,"Failed to create advertiser", Toast.LENGTH_SHORT).show()
        }else{
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build()
            val scanResponseData  = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .build()
            bluetoothLeAdvertiser!!.startAdvertising(settings,scanResponseData,advertiseCallback)
            isStartAdvertising = true
        }
    }


    private fun onClickStartAdvertising() {
        binding.btnAdvertising.setOnClickListener {
            if (isStartAdvertising){
                stopAdvertising()
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestMultiplePermissions.launch(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        )
                    )
                } else {
                    blueToothLauncherPermision.launch(Manifest.permission.BLUETOOTH_ADMIN)
                }
            }
        }
    }
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { entry ->
                val isGranted = entry.value
                if (isGranted){
                    startAdvertising()
                }else{
                    val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enabledBluetoothLauncher.launch(enableBluetoothIntent)
                }
            }
        }
    private val enabledBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){}
    private val blueToothLauncherPermision = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (!bluetoothAdapter.isEnabled) {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                btActivityResultLaucher.launch(enableBluetoothIntent)
            } else {
                startAdvertising()
            }
        }
    }
    private val btActivityResultLaucher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startAdvertising()
        }
    }
    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enabledBluetoothLauncher.launch(enableBluetoothIntent)
        }
    }
    private fun notifyCharacteristic(value: ByteArray, uuid: UUID) {
        val service = mGattServer!!.getService(SERVICE_UUID)
        val characteristic = service.getCharacteristic(uuid)
        log(
            "Notifying characteristic " + characteristic.uuid.toString()
                    + ", new value: " + byteArrayInHexFormat(value)
        )
        characteristic.value = value
        val confirm = requiresConfirmation(characteristic)
        for (device: BluetoothDevice in mDevices!!) {
            if (clientEnabledNotifications(device, characteristic)) {
                mGattServer!!.notifyCharacteristicChanged(device, characteristic, confirm)
            }
        }
    }
    private fun clientEnabledNotifications(
        device: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic
    ): Boolean {
        val descriptorList = characteristic.descriptors
        val descriptor = findClientConfigurationDescriptor(descriptorList)
            ?:
            return true
        val deviceAddress = device.address
        val clientConfiguration = mClientConfigurations!![deviceAddress]
            ?:
            return false
        val notificationEnabled = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        return clientConfiguration.size == notificationEnabled.size && clientConfiguration[0].toInt() and notificationEnabled[0].toInt() == notificationEnabled[0].toInt() && clientConfiguration[1].toInt() and notificationEnabled[1].toInt() == notificationEnabled[1].toInt()
    }
    private fun clearLogs() {
        mLogHandler!!.post { binding.viewServerLog.logTextView.text = "" }
    }
    @SuppressLint("LogNotTimber")
    override fun log(message: String?) {
        mLogHandler!!.post {
        binding.viewServerLog.logTextView.append(message + "\n")
        binding.viewServerLog.logScrollView.post {
            binding.viewServerLog.logScrollView.fullScroll(
                View.FOCUS_DOWN
            )
        }
        }
    }

    override fun addDevice(device: BluetoothDevice?) {
        log("Deviced added: " + device!!.address)
    }

    override fun removeDevice(device: BluetoothDevice?) {
        log("Deviced removed: " + device!!.address)
        mHandler!!.post {
            mDevices?.remove(device)
            val deviceAddress = device.address
            mClientConfigurations?.remove(deviceAddress)
        }
    }

    override fun addClientConfiguration(device: BluetoothDevice?, value: ByteArray?) {
        val deviceAddress = device!!.address
        mClientConfigurations?.put(deviceAddress, value!!)
    }

    override fun sendResponse(
        device: BluetoothDevice?,
        requestId: Int,
        status: Int,
        offset: Int,
        value: ByteArray?
    ) {
        mGattServer!!.sendResponse(device, requestId, status, 0, null)
    }

    override fun notifyCharacteristicEcho(value: ByteArray?) {
        notifyCharacteristic(value!!, CHARACTERISTIC_ECHO_UUID)
    }
}