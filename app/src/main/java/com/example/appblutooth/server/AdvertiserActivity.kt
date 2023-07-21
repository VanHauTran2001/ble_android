package com.example.appblutooth.server

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.example.appblutooth.R
import com.example.appblutooth.databinding.ActivityAdvertiserBinding

class AdvertiserActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAdvertiserBinding
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
        onClickStartAdvertising()
        onClickBack()
    }

    private fun onClickBack() {
        binding.imgBack.setOnClickListener {
            stopAdvertising()
            finish()
        }
    }
    @SuppressLint("MissingPermission")
    private fun stopAdvertising(){
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        isStartAdvertising = false
    }
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Toast.makeText(this@AdvertiserActivity,"Advertising started successfully", Toast.LENGTH_SHORT).show()
        }
    }
    @SuppressLint("MissingPermission")
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
//            val advertiseData = AdvertiseData.Builder()
//                .setIncludeDeviceName(true)
//                .setIncludeTxPowerLevel(true)
//                .build()
            val scanResponseData  = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .build()
            bluetoothLeAdvertiser!!.startAdvertising(settings,scanResponseData,advertiseCallback)
            isStartAdvertising = true
        }
    }

    @SuppressLint("MissingPermission")
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
}