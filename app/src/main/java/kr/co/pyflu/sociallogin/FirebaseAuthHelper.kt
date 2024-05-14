package kr.co.pyflu.sociallogin

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthHelper {

    companion object {
        private var auth: FirebaseAuth? = null

        // Firebase 인증 초기화
        fun initializeFirebaseAuth() {
            if (auth == null) {
                auth = FirebaseAuth.getInstance()
            }
        }

        // 초기화 검사
        private fun checkInitialization() {
            if (auth == null) {
                Log.w("test1234", "FirebaseAuth 초기화 X 오류")
                throw UninitializedPropertyAccessException("FirebaseAuth has not been initialized")
            }
        }

        // 현재 로그인된 사용자 가져오기
        fun getCurrentUser(): FirebaseUser? {
            checkInitialization()
            return auth?.currentUser
        }

        // 로그아웃
        fun signOut() {
            checkInitialization()
            auth?.signOut()
            Log.d("test1234", "사용자 계정 로그아웃 완료")
        }

        // 회원탈퇴
        suspend fun unRegister() {
            checkInitialization()
            withContext(Dispatchers.IO) {
                try {
                    getCurrentUser()?.delete()?.await()
                    Log.d("test1234", "사용자 계정 회원탈퇴 완료")
                } catch (e: Exception) {
                    Log.w("test1234", "사용자 계정 회원탈퇴 실패 ${e.message}")
                }
            }
        }

        // Google 계정으로 Firebase 인증
        fun firebaseAuthWithGoogle(idToken: String, onComplete: (Task<AuthResult>) -> Unit) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
                onComplete(task)
            }
        }

        // Kakao 계정으로 Firebase 인증
        fun firebaseAuthWithKaKao(idToken: String, onComplete: (Task<AuthResult>) -> Unit) {
            val providerId = "oidc.social-login"
            val credential = OAuthProvider.newCredentialBuilder(providerId).setIdToken(idToken).build()
            auth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
                onComplete(task)
            }
        }

        // Naver 계정으로 Firebase 인증
        fun firebaseAuthWithNaver(customToken: String?, onComplete: (Task<AuthResult>) -> Unit) {
            customToken?.let { token ->
                auth?.signInWithCustomToken(token)?.addOnCompleteListener { task ->
                    onComplete(task)
                }
            }
        }
    }
}