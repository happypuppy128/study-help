package com.example.xuexiengine

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class AppBlockAccessibilityService : AccessibilityService() {
    private var lastBlockedPackage: String? = null
    private var lastLaunchAt: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        if (packageName == this.packageName || packageName == "com.android.systemui") return
        val prefs = getSharedPreferences("engine", MODE_PRIVATE)
        if (!prefs.getBoolean("focus_active", false)) return
        val endAt = prefs.getLong("end_at", 0L)
        if (endAt <= System.currentTimeMillis()) {
            prefs.edit().putBoolean("focus_active", false).putLong("end_at", 0L).apply()
            return
        }
        val blocked = prefs.getStringSet("blocked_packages", emptySet()) ?: emptySet()
        if (!blocked.contains(packageName)) return
        val now = System.currentTimeMillis()
        if (lastBlockedPackage == packageName && now - lastLaunchAt < 1800L) return
        lastBlockedPackage = packageName
        lastLaunchAt = now
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("blocked_app_attempt", true)
        }
        startActivity(intent)
    }

    override fun onInterrupt() = Unit
}
