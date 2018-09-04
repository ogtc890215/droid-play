package gq.android.droidplay

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView

/**
 * ClassName
 *
 * <p>Description</p>
 *
 * @author <a href="mailto:ogtc890215@gmail.com">guqi</a>
 * @date 2018/1/16
 */
class WechatActor(private val context: Context) : BaseActor() {
    companion object {
        private const val TAG = "WechatActor"

        private const val PACKAGE_NAME_WECHAT = "com.tencent.mm"
        private const val ACTIVITY_LAUNCH_WECHAT = "$PACKAGE_NAME_WECHAT.ui.LauncherUI"
        private const val UI_FTS_MAIN = "$PACKAGE_NAME_WECHAT.plugin.search.ui.FTSMainUI"
        private const val UI_CHATTING = "$PACKAGE_NAME_WECHAT.ui.chatting.ChattingUI"

        private const val EXTRA_TARGET = "target"
        private const val EXTRA_MESSAGE = "message"

        fun buildMessageInputActor(context: Context, args: Intent): WechatActor {
            val name: String = args.getStringExtra(EXTRA_TARGET)
            val message: String = args.getStringExtra(EXTRA_MESSAGE)
            return buildMessageInputActor(context, name, message)
        }

        private fun buildMessageInputActor(context: Context, name: String, message: String): WechatActor {
            val actor = WechatActor(context)
            actor.addAction(
                    StartActivityAction.build(context, launchIntent, null) { _, event ->
                        event?.className == ACTIVITY_LAUNCH_WECHAT
                    }
            )
            actor.addAction(
                    AccessibilityNodeAction.build(
                            AccessibilityNodeInfo.ACTION_CLICK, null,
                            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                            actor::findSearchFTSMainUI
                    ) { _, event ->
                        event?.className == UI_FTS_MAIN
                    }
            )
            val args = Bundle()
            args.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, name)
            actor.addAction(
                    AccessibilityNodeAction.build(
                            AccessibilityNodeInfo.ACTION_SET_TEXT, args,
                            (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                                    or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED),
                            actor::findSearchInputField, null)
            )
            actor.addAction(
                    AccessibilityNodeAction.build(
                            AccessibilityNodeInfo.ACTION_CLICK, null,
                            (AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                                    or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED),
                            actor::findMatchingContact
                    ) { _, event ->
                        event?.className == UI_CHATTING
                    }
            )
            val msgArgument = Bundle()
            msgArgument.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, message)
            actor.addAction(
                    AccessibilityNodeAction.build(
                            AccessibilityNodeInfo.ACTION_SET_TEXT, msgArgument,
                            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                            actor::findContactMessageInputField, null)
            )
            return actor
        }


        private val launchIntent: Intent
            get() {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.component = ComponentName(PACKAGE_NAME_WECHAT, ACTIVITY_LAUNCH_WECHAT)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                return intent
            }
    }

    private fun findSearchFTSMainUI(action: Action, event: AccessibilityEvent?): Boolean {
        event?.source?.let {
            val childCount = it.childCount
            for (index in 0 until childCount) {
                val child = it.getChild(index)
                Log.d(TAG, "find SearchFTSMainUI: $child")
                if (child.contentDescription == "Search"
                        && child.className == TextView::class.java.name) {
                    (action as AccessibilityNodeAction).node = child
                    return true
                }
                child.recycle()
            }
            clearLastEvent()
        }
        return false
    }

    private fun findSearchInputField(action: Action, event: AccessibilityEvent?): Boolean {
        event?.source?.let {
            val childCount = it.childCount
            for (index in 0 until childCount) {
                val child = it.getChild(index)
                Log.d(TAG, "find SearchInputField: $child")
                if (child.className == EditText::class.java.name && child.text == "Search") {
                    (action as AccessibilityNodeAction).node = child
                    return true
                }
                child.recycle()
            }
        }
        return false
    }

    private fun findMatchingContact(action: Action, event: AccessibilityEvent?): Boolean {
        event?.source?.let {
            if (it.className == ListView::class.java.name) {
                val itemCount = it.childCount
                if (itemCount > 1) {
                    val contactNode = it.getChild(1)
                    if (contactNode.className == RelativeLayout::class.java.name
                            && contactNode.isClickable) {
                        (action as AccessibilityNodeAction).node = contactNode
                        return true
                    }
                    contactNode.recycle()
                }
            }
            it.recycle()
        }
        return false
    }

    private fun findContactMessageInputField(action: Action, event: AccessibilityEvent?): Boolean {
        event?.source?.let {
            val nodes = it.findAccessibilityNodeInfosByClassName(EditText::class.java.name)
            if (nodes.isNotEmpty()) {
                (action as AccessibilityNodeAction).node = nodes[0]
                return true
            }
        }
        return false
    }

    private fun findContactsTabInLauncherUI(action: Action, event: AccessibilityEvent?): Boolean {
        Log.d(TAG, "find contacts in node: ${event?.source}")
        val nodes = event?.source?.findAccessibilityNodeInfosByText(
                context.getString(R.string.label_for_wechat_bottom_contacts))
        if (nodes != null && nodes.isNotEmpty()) {
            var target = nodes[0]
            while (!target.isClickable) {
                val parent = target.parent
                target.recycle()
                target = parent
            }
            (action as AccessibilityNodeAction).node = target
            Log.d(TAG, "Contacts tab has been found")
            clearLastEvent()
            return true
        }
        return false
    }

    override val id: String = PACKAGE_NAME_WECHAT


}