@file:Suppress("DEPRECATION")

package com.example.ble

import android.annotation.SuppressLint
import android.app.ListActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Handler

private const val SCAN_PERIOD: Long = 10000

/**
 * Activity for scanning and displaying available BLE devices.
 */

class DeviceScanActivity(
    private val bluetoothAdapter: BluetoothAdapter, private val handler: Handler
) : ListActivity() {
    private var mScanning: Boolean = false
    private val leScanCallback = object : BluetoothAdapter.LeScanCallback {
        var devices = mutableListOf<Triple<String, Int, String>>()

        @SuppressLint("MissingPermission")
        override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
            // Handle discovered devices here
            device?.let { btDevice ->
                val macAddress = btDevice.address // Obtener la dirección MAC del dispositivo
                val deviceName = btDevice.name ?: "desconocido" // Obtener el nombre del dispositivo
                val deviceInfo = Triple(macAddress, rssi, deviceName)
                if (!devices.contains(deviceInfo)) {
                    devices.add(deviceInfo)
                }
            }
        }

    }


    @SuppressLint("MissingPermission")
    fun scanLeDevice(enable: Boolean) {
        when (enable) {
            true -> {
                // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    mScanning = false
                    bluetoothAdapter.stopLeScan(leScanCallback)
                }, SCAN_PERIOD)
                mScanning = true
                bluetoothAdapter.startLeScan(leScanCallback)
            }

            else -> {
                mScanning = false
                bluetoothAdapter.stopLeScan(leScanCallback)
            }
        }
    }

    fun getDiscoveredDevices(): List<Triple<String, Int, String>> {
        return leScanCallback.devices.toList()
    }

    fun resetDiscoveredDevices() {
        leScanCallback.devices.clear()

    }
}