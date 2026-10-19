package com.example.xuexiengine

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("engine", MODE_PRIVATE) }
    private lateinit var root: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var xpText: TextView
    private lateinit var timerText: TextView
    private val handler = android.os.Handler(mainLooper)
    private val ticker = object : Runnable {
        override fun run() {
            updateTimer()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(244, 246, 251)
        window.navigationBarColor = Color.rgb(244, 246, 251)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        buildUi()
        handler.post(ticker)
    }

    override fun onResume() {
        super.onResume()
        if (::root.isInitialized) refreshHeader()
    }

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        super.onDestroy()
    }

    private fun buildUi() {
        val scroll = ScrollView(this)
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(30))
            setBackgroundColor(Color.rgb(244, 246, 251))
        }
        scroll.addView(root)
        setContentView(scroll)

        text("⚔️ 学习引擎", 28, true)
        text("把今天的学习，变成看得见的成长。", 14, false, Color.DKGRAY)
        spacer(16)
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundColor(Color.WHITE)
        }
        root.addView(card, matchWrap())
        xpText = TextView(this).apply { textSize = 20f; setTypeface(null, Typeface.BOLD); setTextColor(Color.rgb(38, 57, 110)) }
        card.addView(xpText)
        statusText = TextView(this).apply { textSize = 14f; setTextColor(Color.DKGRAY); setPadding(0, dp(8), 0, dp(4)) }
        card.addView(statusText)
        timerText = TextView(this).apply { textSize = 36f; setTypeface(Typeface.MONOSPACE, Typeface.BOLD); setTextColor(Color.rgb(35, 55, 110)); gravity = Gravity.CENTER; setPadding(0, dp(12), 0, dp(12)) }
        card.addView(timerText, matchWrap())
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        card.addView(row, matchWrap())
        listOf(25, 45, 60).forEach { mins ->
            row.addView(Button(this).apply {
                text = "${mins}分钟"
                setOnClickListener { startFocus(mins) }
            }, LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(4) })
        }
        val stop = Button(this).apply {
            text = "结束专注"
            setOnClickListener {
                prefs.edit().putBoolean("focus_active", false).putLong("end_at", 0L).apply()
                Toast.makeText(this@MainActivity, "本轮专注已结束", Toast.LENGTH_SHORT).show()
                refreshHeader()
            }
        }
        root.addView(stop, matchWrap())

        spacer(18)
        text("🎯 学习任务", 21, true)
        text("完成真实学习行为即可领取经验。", 13, false, Color.DKGRAY)
        taskButton("完成一个知识点", "+10 XP", 10)
        taskButton("完成一道练习题", "+15 XP", 15)
        taskButton("订正一道错题", "+20 XP", 20)
        taskButton("低电量重启：完成 3 分钟小任务", "+3 XP", 3)

        spacer(18)
        text("🔒 应用限制", 21, true)
        text("先勾选你想在专注期间限制的应用，再开启无障碍服务。服务只检查前台应用包名，不读取屏幕内容。", 13, false, Color.DKGRAY)
        root.addView(Button(this).apply {
            text = "选择要限制的应用"
            setOnClickListener { showAppPicker() }
        }, matchWrap())
        root.addView(Button(this).apply {
            text = "打开无障碍服务设置"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }, matchWrap())
        root.addView(Button(this).apply {
            text = "查看 Android 应用信息"
            setOnClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
                startActivity(intent)
            }
        }, matchWrap())
        spacer(12)
        text("提示：这是首个测试版。请先用不重要的应用验证拦截效果，不要限制电话、紧急联系或学习资料应用。", 12, false, Color.GRAY)
        refreshHeader()
    }

    private fun taskButton(label: String, reward: String, amount: Int) {
        root.addView(Button(this).apply {
            text = "$label   $reward"
            setOnClickListener {
                addXp(amount)
                Toast.makeText(this@MainActivity, "获得 $amount XP！", Toast.LENGTH_SHORT).show()
                refreshHeader()
            }
        }, matchWrap())
    }

    private fun startFocus(minutes: Int) {
        val now = System.currentTimeMillis()
        prefs.edit()
            .putBoolean("focus_active", true)
            .putLong("started_at", now)
            .putLong("end_at", now + TimeUnit.MINUTES.toMillis(minutes.toLong()))
            .putInt("focus_minutes", minutes)
            .apply()
        Toast.makeText(this, "专注开始：${minutes} 分钟", Toast.LENGTH_SHORT).show()
        refreshHeader()
    }

    private fun updateTimer() {
        if (!::timerText.isInitialized) return
        val end = prefs.getLong("end_at", 0L)
        val active = prefs.getBoolean("focus_active", false)
        if (!active || end <= 0L) {
            timerText.text = "25:00"
            return
        }
        val left = (end - System.currentTimeMillis()).coerceAtLeast(0L)
        val mins = TimeUnit.MILLISECONDS.toMinutes(left)
        val secs = TimeUnit.MILLISECONDS.toSeconds(left) % 60
        timerText.text = String.format("%02d:%02d", mins, secs)
        if (left == 0L) {
            prefs.edit().putBoolean("focus_active", false).putLong("end_at", 0L).apply()
            addXp(20)
            Toast.makeText(this, "专注完成！获得 20 XP", Toast.LENGTH_LONG).show()
            refreshHeader()
        }
    }

    private fun refreshHeader() {
        if (!::xpText.isInitialized) return
        val xp = prefs.getInt("xp", 0)
        val level = xp / 100 + 1
        val levelXp = xp % 100
        xpText.text = "Lv.$level  学习冒险者"
        statusText.text = "经验值：$xp XP   ·   本级进度：$levelXp / 100 XP\n${if (prefs.getBoolean("focus_active", false)) "专注进行中：已启用所选应用拦截" else "当前待命：选择一个时长开始专注"}"
        updateTimer()
    }

    private fun addXp(amount: Int) {
        prefs.edit().putInt("xp", prefs.getInt("xp", 0) + amount).apply()
    }

    private fun showAppPicker() {
        val pm = packageManager
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(launchIntent, 0)
            .mapNotNull { ri ->
                val packageName = ri.activityInfo?.packageName ?: return@mapNotNull null
                if (packageName == this.packageName) return@mapNotNull null
                val label = ri.loadLabel(pm)?.toString() ?: packageName
                packageName to label
            }
            .distinctBy { it.first }
            .sortedBy { it.second.lowercase() }
        val selected = prefs.getStringSet("blocked_packages", emptySet())?.toMutableSet() ?: mutableSetOf()
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(8), dp(16), dp(8)) }
        val dialogScroll = ScrollView(this)
        dialogScroll.addView(container)
        apps.forEach { (pkg, label) ->
            val cb = CheckBox(this).apply { text = "$label"; isChecked = selected.contains(pkg); tag = pkg }
            container.addView(cb)
        }
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("选择专注期间限制的应用")
            .setView(dialogScroll)
            .setNegativeButton("取消", null)
            .setPositiveButton("保存") { _, _ ->
                val updated = mutableSetOf<String>()
                for (i in 0 until container.childCount) {
                    val v = container.getChildAt(i)
                    if (v is CheckBox && v.isChecked) updated.add(v.tag as String)
                }
                prefs.edit().putStringSet("blocked_packages", updated).apply()
                Toast.makeText(this, "已保存 ${updated.size} 个应用", Toast.LENGTH_SHORT).show()
            }.create()
        dialog.show()
    }

    private fun text(value: String, size: Int, bold: Boolean, color: Int = Color.rgb(25, 32, 50)) {
        root.addView(TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(4), 0, dp(4))
        }, matchWrap())
    }

    private fun spacer(height: Int) {
        root.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun matchWrap() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
