package com.example.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {


    private lateinit var texto: TextView
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission", "SetTextI18n")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                val device = it.device
                val deviceName = device.name ?: "Desconocido"
                val address = device.address
                val rssi = it.rssi
                texto.text = "$deviceName ($address) - RSSI: $rssi"
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar BluetoothAdapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Verificar si el dispositivo admite Bluetooth
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Este dispositivo no admite Bluetooth", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Comprobar si el dispositivo es compatible con Bluetooth Low Energy
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(
                this,
                "Bluetooth Low Energy no está soportando en este dispositivo",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }

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
                startScanning()
            }
        } else {
            startScanning()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScanning()
    }

    @SuppressLint("MissingPermission")
    private fun startScanning() {
        val scanner = bluetoothAdapter.bluetoothLeScanner
        val scanFilters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString("0000180D-0000-1000-8000-00805f9b34fb"))
                .build()
        )
        val scanSettings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner.startScan(scanFilters, scanSettings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    private fun stopScanning() {
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startScanning()
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
}
