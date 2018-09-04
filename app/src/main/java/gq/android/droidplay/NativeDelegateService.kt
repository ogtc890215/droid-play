package gq.android.droidplay

import android.accessibilityservice.AccessibilityService
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class NativeDelegateService : Service(), Droid {

    companion object {
        const val TAG = "NativeDelegateService"
    }

    override var accessibilityService: AccessibilityService? = null

    private var actor: Actor? = null
        set(value) {
            field?.cancel()
            field = value
        }

    override fun onBind(intent: Intent): IBinder? {
        if (intent.action == Droid.ACTION_DROID_PLAY) {
            return LocalBinder()
        }
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "droid play intent: $intent")
        when (intent?.action) {
            Droid.ACTION_MESSAGE_WECHAT -> {
                val wechatActor = WechatActor.buildMessageInputActor(applicationContext, intent)
                actor = wechatActor // cancel current running actor first
                wechatActor.play()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    inner class LocalBinder : Binder() {
        val delegate = this@NativeDelegateService
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        actor?.let {
            if (it.id == event.packageName)
                it.updateEvent(event)
        }
    }
}
