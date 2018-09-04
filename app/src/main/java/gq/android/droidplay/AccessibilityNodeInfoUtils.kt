package gq.android.droidplay

import android.view.accessibility.AccessibilityNodeInfo

/**
 * ClassName
 *
 * <p>Description</p>
 *
 * @author <a href="mailto:ogtc890215@gmail.com">guqi</a>
 * @date 2018/1/17
 */
fun AccessibilityNodeInfo.findAccessibilityNodeInfosByClassName(className: String): List<AccessibilityNodeInfo> {
    val list = ArrayList<AccessibilityNodeInfo>()
    val count = this.childCount
    (0 until count)
            .map { this.getChild(it) }
            .forEach { list.addAll(it.findAccessibilityNodeInfosByClassName(className)) }
    if (this.className == className) list.add(this) else this.recycle()
    return list
}