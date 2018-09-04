package gq.android.droidplay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * ClassName
 *
 * <p>Description</p>
 *
 * @author <a href="mailto:ogtc890215@gmail.com">guqi</a>
 * @date 2018/1/16
 */
interface Action {
    companion object {
        internal const val TAG = "AutoPlayAction"
    }

    fun go()

    fun prepare(event: AccessibilityEvent?): Boolean
    fun test(event: AccessibilityEvent?): Boolean
    fun isEventAcceptable(event: Int): Boolean
}

abstract class BaseAction(private val acceptEventType: Int) : Action {
    abstract override fun go()

    override fun prepare(event: AccessibilityEvent?): Boolean {
        prepareClause?.let { block ->
            Log.d(Action.TAG, "Start action preparing...")
            return block(this, event)
        }
        return true
    }

    override fun test(event: AccessibilityEvent?): Boolean {
        testClause?.let { block ->
            Log.d(Action.TAG, "Start action testing...")
            return block(this, event)
        }

        return true
    }

    override fun isEventAcceptable(event: Int): Boolean = acceptEventType and event > 0

    var prepareClause: ((action: Action, event: AccessibilityEvent?) -> Boolean)? = null
    var testClause: ((action: Action, event: AccessibilityEvent?) -> Boolean)? = null

}

class ErrorStateException : Exception()

class StartActivityAction
private constructor(
        private val context: Context,
        private val intent: Intent,
        eventTypes: Int = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
) : BaseAction(acceptEventType = eventTypes) {

    companion object {
        fun build(context: Context, intent: Intent): StartActivityAction {
            return StartActivityAction(context, intent)
        }

        fun build(context: Context, intent: Intent,
                  prepare: ((action: Action, event: AccessibilityEvent?) -> Boolean)?,
                  test: ((action: Action, event: AccessibilityEvent?) -> Boolean)?): StartActivityAction {
            val action = StartActivityAction(context, intent)
            action.prepareClause = prepare
            action.testClause = test
            return action
        }
    }

    override fun go() {
        context.startActivity(intent)
        Log.d(Action.TAG, "StartActivityAction: intent=$intent")
    }

}

class AccessibilityNodeAction
private constructor(
        private val action: Int,
        private val args: Bundle?,
        eventTypes: Int = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
) : BaseAction(acceptEventType = eventTypes) {
    companion object {
        fun build(action: Int, args: Bundle?, eventTypes: Int,
                  prepare: ((action: Action, event: AccessibilityEvent?) -> Boolean)?,
                  test: ((action: Action, event: AccessibilityEvent?) -> Boolean)?): AccessibilityNodeAction {
            val nodeAction = AccessibilityNodeAction(action, args, eventTypes)
            nodeAction.prepareClause = prepare
            nodeAction.testClause = test
            return nodeAction
        }
    }

    var node: AccessibilityNodeInfo? = null

    override fun go() {
        node?.performAction(action, args)
        Log.d(Action.TAG, "AccessibilityNodeAction: \n\tnode=$node, \n\taction=$action")
    }
}
