package xyz.mpv.rex.ui.player

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast

class FloatTestActivity : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (!Settings.canDrawOverlays(this)) {
      Toast.makeText(
        this,
        "Allow 'Display over other apps', then open Float Test again",
        Toast.LENGTH_LONG,
      ).show()
      startActivity(
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
      )
      finish()
      return
    }
    showOverlay()
    finish()
  }

  private fun showOverlay() {
    val density = resources.displayMetrics.density
    val dp = { v: Int -> (v * density).toInt() }
    val minW = dp(120)
    val minH = dp(80)

    val ctx = applicationContext
    val wm = ctx.getSystemService(WINDOW_SERVICE) as WindowManager
    val params = WindowManager.LayoutParams(
      dp(260),
      dp(160),
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
      PixelFormat.TRANSLUCENT,
    )
    params.gravity = Gravity.TOP or Gravity.START
    params.x = dp(40)
    params.y = dp(200)

    val root = FrameLayout(ctx)
    root.setBackgroundColor(Color.parseColor("#222222"))

    val label = TextView(ctx)
    label.text = "Float test - drag me, resize from the corner"
    label.setTextColor(Color.WHITE)
    label.gravity = Gravity.CENTER
    root.addView(
      label,
      FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
      ),
    )

    var startX = 0
    var startY = 0
    var touchX = 0f
    var touchY = 0f
    root.setOnTouchListener { _, e ->
      when (e.action) {
        MotionEvent.ACTION_DOWN -> {
          startX = params.x
          startY = params.y
          touchX = e.rawX
          touchY = e.rawY
        }
        MotionEvent.ACTION_MOVE -> {
          params.x = startX + (e.rawX - touchX).toInt()
          params.y = startY + (e.rawY - touchY).toInt()
          wm.updateViewLayout(root, params)
        }
      }
      true
    }

    val handle = View(ctx)
    handle.setBackgroundColor(Color.parseColor("#4FC3F7"))
    root.addView(handle, FrameLayout.LayoutParams(dp(36), dp(36), Gravity.BOTTOM or Gravity.END))
    var startW = 0
    var startH = 0
    var handleX = 0f
    var handleY = 0f
    handle.setOnTouchListener { _, e ->
      when (e.action) {
        MotionEvent.ACTION_DOWN -> {
          startW = params.width
          startH = params.height
          handleX = e.rawX
          handleY = e.rawY
        }
        MotionEvent.ACTION_MOVE -> {
          params.width = maxOf(minW, startW + (e.rawX - handleX).toInt())
          params.height = maxOf(minH, startH + (e.rawY - handleY).toInt())
          wm.updateViewLayout(root, params)
        }
      }
      true
    }

    val close = TextView(ctx)
    close.text = "X"
    close.textSize = 18f
    close.setTextColor(Color.WHITE)
    close.setPadding(dp(14), dp(6), dp(14), dp(6))
    close.setOnClickListener { runCatching { wm.removeView(root) } }
    root.addView(
      close,
      FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        Gravity.TOP or Gravity.END,
      ),
    )

    wm.addView(root, params)
  }
}
