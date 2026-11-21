package com.jarvis.mobile

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class JarvisAccessibilityService : AccessibilityService() {
    companion object { @Volatile var instance: JarvisAccessibilityService? = null }
    override fun onServiceConnected() { super.onServiceConnected(); instance = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() { if (instance === this) instance = null; super.onDestroy() }
    fun performHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)
    fun performBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun performRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)
    fun clickText(target: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val node = findNode(root, target.lowercase()) ?: return false
        return clickNodeOrParent(node)
    }
    private fun findNode(node: AccessibilityNodeInfo, target: String): AccessibilityNodeInfo? {
        val text = node.text?.toString()?.lowercase().orEmpty()
        val desc = node.contentDescription?.toString()?.lowercase().orEmpty()
        if (text.contains(target) || desc.contains(target)) return node
        for (i in 0 until node.childCount) { val child = node.getChild(i) ?: continue; val found = findNode(child, target); if (found != null) return found }
        return null
    }
    private fun clickNodeOrParent(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable) return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        val parent = node.parent ?: return false
        return clickNodeOrParent(parent)
    }
    fun scrollDown(): Boolean = swipe(.5f, .78f, .5f, .30f)
    fun scrollUp(): Boolean = swipe(.5f, .30f, .5f, .78f)
    private fun swipe(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        val m = resources.displayMetrics
        val path = Path().apply { moveTo(m.widthPixels * x1, m.heightPixels * y1); lineTo(m.widthPixels * x2, m.heightPixels * y2) }
        val gesture = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, 450)).build()
        return dispatchGesture(gesture, null, null)
    }
}
