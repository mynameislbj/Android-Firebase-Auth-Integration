package kr.co.pyflu.sociallogin.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import kr.co.pyflu.sociallogin.R
import kr.co.pyflu.sociallogin.databinding.ActivityLoginBinding
import kr.co.pyflu.sociallogin.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        binding = DataBindingUtil.setContentView(this@LoginActivity, R.layout.activity_login)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this@LoginActivity

        googleLogin()
        kakaoLogin()
        naverLogin()
    }

    override fun onStart() {
        super.onStart()
        // 자동 로그인
        viewModel.userAuthenticationState.observe(this@LoginActivity) { isAuthenticated ->
            if (isAuthenticated) {
                navigateToMainActivity()
            }
        }
    }

    private fun googleLogin(){
        binding.googleLoginButton.setOnClickListener {
            viewModel.googleLogin(this@LoginActivity)
        }
    }

    private fun kakaoLogin(){
        binding.kakaoLoginButton.setOnClickListener {
            viewModel.kakaoLogin(this@LoginActivity)
        }
    }

    private fun naverLogin(){
        binding.naverLoginButton.setOnClickListener {
            viewModel.naverLogin(this@LoginActivity)
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }
}