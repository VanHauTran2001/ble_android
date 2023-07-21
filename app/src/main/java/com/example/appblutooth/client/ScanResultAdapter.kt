package com.example.appblutooth.client

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appblutooth.databinding.ItemListDeviceBinding

class ScanResultAdapter(
    private val listScanResult : List<ScanResult>,
    private val onClickListener: ((device: ScanResult) -> Unit)
    ) : RecyclerView.Adapter<ScanResultAdapter.Companion.ScanResultViewHolder>() {

    companion object{
        class ScanResultViewHolder(val binding: ItemListDeviceBinding) : RecyclerView.ViewHolder(binding.root)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanResultViewHolder {
        val binding = ItemListDeviceBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ScanResultViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listScanResult.size
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onBindViewHolder(holder: ScanResultViewHolder, position: Int) {
        val item =listScanResult[position]
        holder.binding.deviceName.text = item.device.name ?: "Unnamed"
        holder.binding.macAddress.text = item.device.address
        holder.binding.signalStrength.text = "${item.rssi} dBm"
        holder.itemView.setOnClickListener {
            onClickListener.invoke(item)
        }
    }
}