package gq.android.droidplay

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * ClassName
 *
 * <p>Description</p>
 *
 * @author <a href="mailto:ogtc890215@gmail.com">guqi</a>
 * @date 2018/1/16
 */
interface Droid {
    companion object {
        const val ACTION_DROID_PLAY = "gq.android.action.DROID_PLAY"
        const val ACTION_MESSAGE_WECHAT = "gq.android.action.MESSAGE_WECHAT"
    }

    var accessibilityService: AccessibilityService?

    fun onAccessibilityEvent(event: AccessibilityEvent)
}