package dev.apktwin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private var selectedApk: Uri? = null
    private lateinit var details: TextView
    private lateinit var packageInput: EditText
    private lateinit var status: TextView

    private val pickApk = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedApk = uri
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            inspect(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        val scroll = ScrollView(this).apply { addView(column) }
        setContentView(scroll)

        column.addView(TextView(this).apply {
            text = "ApkTwin"
            textSize = 28f
        })
        column.addView(TextView(this).apply {
            text = "自分が所有する、または変更許可のあるAPKを別アプリとして扱うためのツール"
            textSize = 14f
        })
        column.addView(Button(this).apply {
            text = "APKを選択"
            setOnClickListener { pickApk.launch(arrayOf("application/vnd.android.package-archive", "*/*")) }
        })
        details = TextView(this).apply {
            text = "まだAPKが選択されていません"
            textSize = 14f
            setPadding(0, pad, 0, pad)
        }
        column.addView(details)
        column.addView(TextView(this).apply {
            text = "新しいパッケージ名（例: com.example.app.copy）"
        })
        packageInput = EditText(this).apply {
            hint = "com.example.app.copy"
            setSingleLine(true)
        }
        column.addView(packageInput)
        column.addView(Button(this).apply {
            text = "別APKとして作成"
            setOnClickListener { startProcessing() }
        })
        status = TextView(this).apply {
            text = "準備完了"
            textSize = 14f
            setPadding(0, pad, 0, 0)
        }
        column.addView(status)
    }

    private fun inspect(uri: Uri) {
        lifecycleScope.launch {
            val info = withContext(Dispatchers.IO) { ApkInspector.inspect(this@MainActivity, uri) }
            details.text = info?.let {
                "アプリ名: ${it.label}\nパッケージ: ${it.packageName}\nバージョン: ${it.versionName ?: "不明"}"
            } ?: "APK情報を読み取れませんでした。APK形式を確認してください。"
            if (info != null && packageInput.text.isNullOrBlank()) {
                packageInput.setText(info.packageName + ".copy")
            }
        }
    }

    private fun startProcessing() {
        val uri = selectedApk ?: run { status.text = "先にAPKを選択してください"; return }
        val newId = packageInput.text.toString().trim()
        if (!newId.matches(Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+"))) {
            status.text = "パッケージ名の形式が正しくありません"
            return
        }
        lifecycleScope.launch {
            status.text = "確認中..."
            val result = withContext(Dispatchers.IO) {
                ClonePipeline.run(this@MainActivity, uri, newId)
            }
            status.text = result
        }
    }
}
