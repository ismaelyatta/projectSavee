package fr.isen.francoisyatta.projectv2.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import fr.isen.francoisyatta.projectv2.R
import fr.isen.francoisyatta.projectv2.databinding.ActivityBleScanBinding

class BleScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBleScanBinding
    private var isScanning = false
    private val ENABLE_BLUETOOTH_REQUEST_CODE = 1
    private val ALL_PERMISSION_REQUEST_CODE = 10
    private lateinit var adapter: BleScanAdapter


    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_scan)
        binding = ActivityBleScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when {
            bluetoothAdapter?.isEnabled == true ->
                binding.scanBLEBtn.setOnClickListener {
                    startLeScanBLEWithPermission(!isScanning)
                    // startLeScanBLEWithPermission(true)
                }

            bluetoothAdapter != null ->
                askBluetoothPermission()
            else -> {
                displayBLEUnavailable()
            }

        }

        binding.scanBLEBtn.setOnClickListener {
            startLeScanBLEWithPermission(!isScanning)
        }
        binding.scanBLEText.setOnClickListener {
            startLeScanBLEWithPermission(!isScanning)
        }

        adapter = BleScanAdapter(arrayListOf()) {
            val intent = Intent(this, DeviceDetailActivity::class.java)
            intent.putExtra(DEVICE_KEY, it)
            startActivity(intent)
        }
        binding.scanBLEListe.layoutManager = LinearLayoutManager(this)
        binding.scanBLEListe.adapter = adapter

    }

    private fun checkAllPermissionGranted(): Boolean {
        return getAllPermissions().all { permission ->
            val test = ActivityCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
            return@all test
        }
    }

    private fun getAllPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startLeScanBLEWithPermission(enable: Boolean) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION // si on a bien activé la localisation
            ) == PackageManager.PERMISSION_GRANTED //ok
        ) {
            startLeScanBLE(enable)
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_CONNECT, //connexion (android 12)
                    Manifest.permission.BLUETOOTH_SCAN //scan (android 12)

                ), ALL_PERMISSION_REQUEST_CODE
            )

        }
    }


    @SuppressLint("MissingPermission")
    private fun startLeScanBLE(enable: Boolean) {

        bluetoothAdapter?.bluetoothLeScanner?.apply {
            if (enable) {
                isScanning = true
                startScan(scanCallBack)
            } else {
                isScanning = false
                stopScan(scanCallBack)
            }
            handlePlayPauseAction()
        }
    }


    private val scanCallBack = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d("BLEScanActivity", "result : ${result?.device?.address}, rssi : ${result?.rssi}")

            adapter?.apply {
                addElement(result)
                notifyDataSetChanged()
            }

        }
    }

    private fun displayBLEUnavailable() {
        binding.scanBLEBtn.isVisible = false
        binding.scanBLEText.text = getString(R.string.ble_scan_error)
        binding.scanBLEPogressBar.isIndeterminate = false
    }

    private fun askBluetoothPermission() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    private fun handlePlayPauseAction() {
        if (isScanning) {
            binding.scanBLEBtn.setImageResource(R.drawable.ic_pause)
            binding.scanBLEText.text = getString(R.string.ble_scan_pause)
            binding.scanBLEPogressBar.isIndeterminate = true
            binding.scanBLEPogressBar.isVisible = true
        } else {
            binding.scanBLEBtn.setImageResource(R.drawable.ic_play)
            binding.scanBLEText.text = getString(R.string.ble_scan_play)
            binding.scanBLEPogressBar.isIndeterminate = true
            binding.scanBLEPogressBar.isVisible = false
        }
    }

    companion object {
        private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
        private const val ALL_PEMISSION_REQUEST_CODE = 100
        const val DEVICE_KEY = "Device"
    }


}