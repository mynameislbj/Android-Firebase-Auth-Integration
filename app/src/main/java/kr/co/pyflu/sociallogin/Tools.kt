package kr.co.pyflu.sociallogin

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlin.concurrent.thread

class Tools {
    companion object {

        // 뷰에 포커스를 주고 키보드를 올린다.
        fun showSoftInput(context: Context, view: View) {
            // 뷰에 포커스를 준다.
            view.requestFocus()
            thread {
                // 딜레이
                SystemClock.sleep(200)
                // 키보드 관리 객체를 가져온다.
                val inputMethodManger =
                    context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                // 키보드를 올린다.
                inputMethodManger.showSoftInput(view, 0)
            }
        }

        // 키보드를 내려주고 포커스를 제거한다.
        fun hideSoftInput(activity: Activity) {
            // 포커스를 가지고 있는 뷰가 있다면..
            if (activity.window.currentFocus != null) {
                // 키보드 관리 객체를 가져온다.
                val inputMethodManger =
                    activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                // 키보드를 내려준다.
                inputMethodManger.hideSoftInputFromWindow(
                    activity.window.currentFocus?.windowToken,
                    0
                )
                // 포커스를 제거해준다.
                activity.window.currentFocus?.clearFocus()
            }
        }

        fun showSnackBar(view: View, context: Context, message:String){
            Snackbar.make(view, "사용자 정보 업데이트 완료", Snackbar.LENGTH_SHORT).apply {
                setTextColor(ContextCompat.getColor(context, R.color.white))
                setBackgroundTint(ContextCompat.getColor(context, R.color.dark))
                animationMode = Snackbar.ANIMATION_MODE_FADE
            }.show()
        }
    }
}