package com.example.apkexporter

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apkexporter.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val adapter = AppAdapter { updateButton() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.list.layoutManager = LinearLayoutManager(this)
        b.list.adapter = adapter
        b.search.addTextChangedListener { adapter.filter(it?.toString().orEmpty()) }
        b.exportBtn.setOnClickListener { exportSelected() }

        lifecycleScope.launch {
            val items = withContext(Dispatchers.Default) { queryApps() }
            adapter.submit(items)
            b.progress.visibility = View.GONE
        }
    }

    private fun queryApps(): List<AppItem> {
        val pm = packageManager
        return pm.getInstalledApplications(0)
            .filter {
                // user apps + updated system apps (e.g. Chrome); skip bare system components
                (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                    (it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            }
            .mapNotNull { ai ->
                try {
                    val paths = listOf(ai.sourceDir) + (ai.splitSourceDirs?.toList() ?: emptyList())
                    AppItem(
                        label = ai.loadLabel(pm).toString(),
                        packageName = ai.packageName,
                        versionName = pm.getPackageInfo(ai.packageName, 0).versionName ?: "?",
                        icon = ai.loadIcon(pm),
                        apkPaths = paths,
                        sizeBytes = paths.sumOf { File(it).length() }
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun updateButton() {
        val n = adapter.selected().size
        b.exportBtn.isEnabled = n > 0
        b.exportBtn.text = if (n > 0) "Export $n app${if (n > 1) "s" else ""}" else "Select apps to export"
    }

    private fun exportSelected() {
        val chosen = adapter.selected()
        if (chosen.isEmpty()) return
        b.exportBtn.isEnabled = false
        b.progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            var ok = 0
            val failed = mutableListOf<String>()
            withContext(Dispatchers.IO) {
                chosen.forEach {
                    try { ApkExporter.export(this@MainActivity, it); ok++ }
                    catch (e: Exception) { failed += it.label }
                }
            }
            b.progress.visibility = View.GONE
            chosen.forEach { it.selected = false }
            adapter.filter(b.search.text.toString())
            updateButton()
            val msg = "Saved $ok to Downloads/APK Exports" +
                if (failed.isNotEmpty()) " · failed: ${failed.joinToString()}" else ""
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
        }
    }
}
