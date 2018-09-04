package gq.android.droidplay

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.util.*
import kotlin.properties.Delegates

/**
 * ClassName
 *
 * <p>Description</p>
 *
 * @author <a href="mailto:ogtc890215@gmail.com">guqi</a>
 * @date 2018/1/16
 */
interface Actor {
    companion object {
        const val STATE_IDLE = 0
        const val STATE_RUNNING = 1
        const val STATE_REPLAY = 2
        const val STATE_NEXT = 3
        const val STATE_PREPARING = 4
        const val STATE_TESTING = 5
        const val STATE_CANCEL = 6
        const val STATE_ERR = -1
    }

    val id: String

    fun play()

    fun cancel()

    fun updateEvent(event: AccessibilityEvent?)
}

abstract class BaseActor : Actor {
    companion object {
        private const val TAG = "AutoPlayActor"
    }

    private val playList = LinkedList<Action>()

    private var currentAction: Action? = null

    private var lastEvent: AccessibilityEvent? = null

    private var state by Delegates.observable(Actor.STATE_IDLE) { _, old, new ->
        Log.d(TAG, "Actor state changed: old=$old, new=$new")
    }


    private fun playAction(action: Action) {
        if (!action.prepare(lastEvent)) {
            state = Actor.STATE_PREPARING
            return
        }
        action.go()
        state = Actor.STATE_RUNNING
    }

    private fun playNext() {
        currentAction = playList.pollFirst()
        currentAction?.let {
            try {
                playAction(it)
            } catch (e: ErrorStateException) {
                state = Actor.STATE_ERR
            }
        }
    }

    override fun cancel() {
        state = Actor.STATE_CANCEL
        Log.i("AutoPlayActor", "Playing actor has been cancelled.")
    }

    override fun play() {
        loop@ do {
            when (state) {
                Actor.STATE_IDLE -> {
                    if (playList.isNotEmpty())
                        state = Actor.STATE_NEXT
                    else
                        break@loop
                }
                Actor.STATE_TESTING -> {
                    currentAction!!.let { action ->
                        state = try {
                            if (action.test(lastEvent)) Actor.STATE_IDLE else Actor.STATE_RUNNING
                        } catch (e: ErrorStateException) {
                            Actor.STATE_ERR
                        }
                    }
                }
                Actor.STATE_NEXT -> playNext()
                Actor.STATE_REPLAY -> playAction(currentAction!!)
                else -> break@loop
            }
        } while (true)
    }


    override fun updateEvent(event: AccessibilityEvent?) {
        if (event != null) {
            if (currentAction == null || currentAction!!.isEventAcceptable(event.eventType)) {
                Log.d(TAG, "Receive a accessibility: \n$event")
                lastEvent = event
                if (state == Actor.STATE_RUNNING) {
                    state = Actor.STATE_TESTING
                    play()
                } else if (state == Actor.STATE_PREPARING) {
                    state = Actor.STATE_REPLAY
                    play()
                }
            }
        } else {
            lastEvent = null
        }
    }

    protected fun clearLastEvent() {
        lastEvent = null
    }

    protected fun addAction(action: Action) {
        playList.addLast(action)
    }
}