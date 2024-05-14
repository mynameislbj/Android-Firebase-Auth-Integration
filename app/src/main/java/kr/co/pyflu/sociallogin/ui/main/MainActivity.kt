package kr.co.pyflu.sociallogin.ui.main

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.pyflu.sociallogin.FirebaseAuthHelper
import kr.co.pyflu.sociallogin.R
import kr.co.pyflu.sociallogin.Tools
import kr.co.pyflu.sociallogin.databinding.ActivityMainBinding
import kr.co.pyflu.sociallogin.repository.UserRepository
import kr.co.pyflu.sociallogin.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this@MainActivity

        toolbar()

        signOut()
        unRegister()
    }

    private fun toolbar() {
        binding.mainToolbar.apply {
            inflateMenu(R.menu.menu_profile)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.update_menuItem -> {

                        Tools.hideSoftInput(this@MainActivity)

                        viewModel.updateUserInfo()
                        viewModel.getUserInfo()

                        Tools.showSnackBar(this, this@MainActivity, "사용자 정보 업데이트 완료")
                    }
                }
                true
            }
        }
    }

    // 로그아웃
    private fun signOut() {
        binding.signoutButton.setOnClickListener {

            FirebaseAuthHelper.signOut()
            navigateToLoginActivity()
        }
    }

    // 회원탈퇴
    private fun unRegister() {
        binding.unregisterButton.setOnClickListener {

            MaterialAlertDialogBuilder(this@MainActivity).apply {
                setTitle("회원탈퇴")
                setMessage("회원을 탈퇴하시겠습니까?")
                setNegativeButton("취소", null)
                setPositiveButton("회원탈퇴") { dialogInterface: DialogInterface, i: Int ->
                    CoroutineScope(Dispatchers.IO).launch {
                        UserRepository.deleteUser(FirebaseAuthHelper.getCurrentUser()?.uid!!)
                        FirebaseAuthHelper.unRegister()
                        withContext(Dispatchers.Main) {
                            navigateToLoginActivity()
                        }
                    }
                }
            }.show().apply {
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this@MainActivity, R.color.orange))
                getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
            }
        }
    }

    private fun navigateToLoginActivity() {
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }
}