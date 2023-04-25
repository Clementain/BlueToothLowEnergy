@file:Suppress("DEPRECATION")

package com.example.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 100
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_ENABLE_LOCATION = 2
    private lateinit var boton: Button
    private lateinit var dispositivos: TextView
    private lateinit var deviceScanActivity: DeviceScanActivity
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_ADMIN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Los permisos no están concedidos
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        } else {
            // Los permisos ya están concedidos
            return
        }

        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(enableLocationIntent, REQUEST_ENABLE_LOCATION)
        }
        boton = findViewById(R.id.btnScan)
        dispositivos = findViewById(R.id.Dispositivos)

        deviceScanActivity = DeviceScanActivity(bluetoothAdapter!!, Handler())

        boton.setOnClickListener {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE).not()) {
                dispositivos.text = "No admite BLE este dispositivo"
            } else {
                deviceScanActivity.scanLeDevice(true)
                val discoveredDevices = deviceScanActivity.getDiscoveredDevices()
                val deviceText =
                    discoveredDevices.joinToString(separator = "\n") { (macAddress, rssi, name) ->
                        "MAC: $macAddress, RSSI: $rssi, Nombre: $name"
                    }
                dispositivos.text = deviceText
                deviceScanActivity.resetDiscoveredDevices()

            }


        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (resultCode == RESULT_OK) {
                    // Bluetooth was successfully enabled
                    return
                } else {
                    // The user declined to enable Bluetooth
                    Toast.makeText(
                        this, "Bluetooth must be enabled to use this app", Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            REQUEST_ENABLE_LOCATION -> {
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    return
                } else {
                    // The user declined to enable Location
                    Toast.makeText(
                        this, "Location must be enabled to use this app", Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                // Los permisos fueron concedidos
                return
            } else {
                // Los permisos fueron denegados
                Toast.makeText(
                    this,
                    "Necesitas proporcionar los permisos solicitados para usar la app",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

}
