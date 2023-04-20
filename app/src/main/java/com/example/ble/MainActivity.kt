package com.example.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var texto: TextView
    private lateinit var boton: Button
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val REQUEST_ENABLE_BT = 1
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission", "SetTextI18n")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                val device = it.device
                val deviceName = device.name ?: "Desconocido"
                val address = device.address
                val rssi = it.rssi
                texto.text = "$deviceName ($address) - RSSI: $rssi\n"
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar BluetoothLeScanner
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner

        // Comprobar si el dispositivo es compatible con Bluetooth Low Energy
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) || bluetoothAdapter.bluetoothLeScanner == null) {
            Toast.makeText(
                this,
                "Bluetooth Low Energy no está soportando en este dispositivo",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        requestEnableBluetooth()
        // Solicitar permisos de Bluetooth en tiempo de ejecución (si es necesario)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN
            )
            val permissionsToRequest = mutableListOf<String>()
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        this, permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(permission)
                }
            }
            if (permissionsToRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 0)
            } else {
                return
            }
        } else {
            return
        }
        boton = findViewById(R.id.btnScan)
        texto = findViewById(R.id.Dispositivos)

        boton.setOnClickListener {
            startScanning()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScanning()
    }

    @SuppressLint("MissingPermission")
    private fun startScanning() {
        val scanFilter = ScanFilter.Builder().build()
        val scanSettings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        bluetoothLeScanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    private fun stopScanning() {
        bluetoothLeScanner.stopScan(scanCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                return
            } else {
                Toast.makeText(
                    this,
                    "Se necesitan permisos de Bluetooth para usar esta aplicación",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                return
            } else {
                Toast.makeText(
                    this,
                    "El Bluetooth no ha sido habilitado, no se puede escanear dispositivos cercanos",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

}
