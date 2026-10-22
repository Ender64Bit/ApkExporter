package com.example.apkexporter

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apkexporter.databinding.ItemAppBinding

class AppAdapter(private val onSelectionChanged: () -> Unit) :
    RecyclerView.Adapter<AppAdapter.VH>() {

    private var all: List<AppItem> = emptyList()
    private var shown: List<AppItem> = emptyList()

    fun submit(items: List<AppItem>) {
        all = items; shown = items; notifyDataSetChanged()
    }

    fun filter(query: String) {
        val q = query.trim().lowercase()
        shown = if (q.isEmpty()) all
        else all.filter { it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
        notifyDataSetChanged()
    }

    fun selected(): List<AppItem> = all.filter { it.selected }

    inner class VH(val b: ItemAppBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = shown.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = shown[position]
        val ctx = holder.b.root.context
        holder.b.icon.setImageDrawable(item.icon)
        holder.b.label.text = item.label
        val size = Formatter.formatShortFileSize(ctx, item.sizeBytes)
        val splitNote = if (item.isSplit) " · ${item.apkPaths.size} splits" else ""
        holder.b.meta.text = "${item.packageName}\nv${item.versionName} · $size$splitNote"
        holder.b.check.isChecked = item.selected
        holder.b.root.setOnClickListener {
            item.selected = !item.selected
            holder.b.check.isChecked = item.selected
            onSelectionChanged()
        }
    }
}
