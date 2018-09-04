package gq.android.droidplay

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class AutoPlayAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AutoPlayAccessibility"
    }

    private var mDroid: Droid? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        event.source?.let {
            mDroid?.onAccessibilityEvent(event)
        }
    }

    override fun onInterrupt() {
        /* do nothing */
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.i(TAG, "auto play service connected")
        connectDroidServer()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "auto play service unbind: $intent")
        mDroid?.accessibilityService = null
        unbindService(droidServerConnection)
        mDroid = null
        return super.onUnbind(intent)
    }

    private fun connectDroidServer() {
        if (mDroid == null) {
            val droidPlayIntent = Intent(applicationContext, NativeDelegateService::class.java)
            droidPlayIntent.action = Droid.ACTION_DROID_PLAY
            bindService(droidPlayIntent, droidServerConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private val droidServerConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mDroid = null
            Log.w(TAG, "Droid server disconnected")
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder?) {
            if (service is NativeDelegateService.LocalBinder) {
                mDroid = service.delegate
                mDroid!!.accessibilityService = this@AutoPlayAccessibilityService
                Log.i(TAG, "Droid server has connected")
            }
        }
    }


}
