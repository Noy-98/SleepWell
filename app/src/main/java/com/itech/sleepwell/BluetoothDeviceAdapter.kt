package com.itech.sleepwell

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView

class BluetoothDeviceAdapter(
    private val devices: List<BluetoothDevice>,
    private val context: Context
) : RecyclerView.Adapter<BluetoothDeviceAdapter.BluetoothViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bluetooth_item, parent, false)
        return BluetoothViewHolder(view)
    }

    override fun onBindViewHolder(holder: BluetoothViewHolder, position: Int) {
        val device = devices[position]
        holder.nameTextView.text = device.name ?: "Unknown Device"

        if (ActivityCompat.checkSelfPermission(
                context, // Use context here instead of "this"
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider requesting the permission if not granted
            return
        }

        holder.statusTextView.text = if (device.bondState == BluetoothDevice.BOND_BONDED) "Paired" else "Not Paired"
        holder.connectButton.setOnClickListener {
            if (device.bondState == BluetoothDevice.BOND_BONDED) {
                disconnectDevice(device)
                holder.statusTextView.text = "Not Paired"
            } else {
                pairDevice(device)
                holder.statusTextView.text = "Paired"
            }
        }
    }

    private fun disconnectDevice(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Remove bond to effectively disconnect
        try {
            val method = device.javaClass.getMethod("removeBond")
            method.invoke(device)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = devices.size

    private fun pairDevice(device: BluetoothDevice) {
        // Pair with the device
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        device.createBond()
    }

    class BluetoothViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.bluetooth_name)
        val statusTextView: TextView = itemView.findViewById(R.id.status)
        val connectButton: ImageView = itemView.findViewById(R.id.connect_bttn)
    }
}
