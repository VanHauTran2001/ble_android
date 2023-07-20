package com.example.appblutooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appblutooth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(){
    lateinit var binding: ActivityMainBinding
    private var bluetoothGatt: BluetoothGatt? = null
    //private var SERVICE_UUID = "0000fff1-0000-1000-8000-00805f9b34"
    private var bluetoothManager : BleManager ?= null
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread { binding.btnScan.text = if (value) "Stop Scan" else "Start Scan" }
        }
    private val scanResults = mutableListOf<ScanResult>()

    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) { result ->
            if (isScanning){
                stopBleScan()
            }
            with(result.device){
                val intent = Intent(this@MainActivity,BleOperationsActivity::class.java)
                intent.putExtra("device",this)
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.btnScan.setOnClickListener {
            if (isScanning) {
                stopBleScan()
            } else {
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
        setupRecyclerView()
        bluetoothManager = BleManager(applicationContext)
    }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enabledBluetoothLauncher.launch(enableBluetoothIntent)
        }
    }
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { entry ->
                val isGranted = entry.value
                if (isGranted){
                    startBleScan()
                }else{
                    val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enabledBluetoothLauncher.launch(enableBluetoothIntent)
                }
            }
        }

    private fun setupRecyclerView() {
        binding.scanResultsRecyclerView.apply {
            adapter = scanResultAdapter
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

    }

    @SuppressLint("NotifyDataSetChanged", "ObsoleteSdkInt", "MissingPermission")
    private fun startBleScan() {
        scanResults.clear()
        scanResultAdapter.notifyDataSetChanged()
        val scanFilter = ScanFilter.Builder().build()
        val scanFilters: MutableList<ScanFilter> = mutableListOf()
        scanFilters.add(scanFilter)
        bleScanner.startScan(scanFilters, scanSettings, scanCallback)
        isScanning = true
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
                startBleScan()
            }
        }

    }
    private val btActivityResultLaucher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startBleScan()
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private val scanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("NotifyDataSetChanged", "MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val indexQuery =
                scanResults.indexOfFirst { it.device.address == result!!.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result!!
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                if (result!!.device.name != null){
                    scanResults.add(result)
                    scanResultAdapter.notifyItemInserted(scanResults.size)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {

        }
    }
//    @SuppressLint("MissingPermission")
//    private fun connectToDevice(device: BluetoothDevice) {
//        val gattCallback = object : BluetoothGattCallback() {
//            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
//                if (newState == BluetoothProfile.STATE_CONNECTED) {
//                    Toast.makeText(this@MainActivity, "Connected to GATT server.", Toast.LENGTH_SHORT).show()
//                    gatt.discoverServices()
//                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                    Toast.makeText(this@MainActivity, "Disconnected from GATT server.", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    val service: BluetoothGattService? = gatt!!.getService(UUID.fromString("Your desired service UUID"))
//                    val characteristic: BluetoothGattCharacteristic? = service?.getCharacteristic(UUID.fromString("Your desired characteristic UUID"))
//                    if (characteristic != null) {
//                        val data = "Your data packet".toByteArray()
//                        characteristic.value = data
//                        gatt.writeCharacteristic(characteristic)
//                    }
//                } else {
//                    Toast.makeText(this@MainActivity, "onServicesDiscovered received: $status", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//        device.connectGatt(this@MainActivity, false, gattCallback)
//    }
}