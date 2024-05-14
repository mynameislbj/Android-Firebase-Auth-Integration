package kr.co.pyflu.sociallogin.ui.login

import android.content.Context
import android.credentials.GetCredentialException
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.pyflu.sociallogin.FirebaseAuthHelper
import kr.co.pyflu.sociallogin.R
import kr.co.pyflu.sociallogin.model.User
import kr.co.pyflu.sociallogin.repository.UserRepository
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

class LoginViewModel : ViewModel() {

    private val _userAuthenticationState = MutableLiveData<Boolean>()
    val userAuthenticationState: LiveData<Boolean> = _userAuthenticationState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        FirebaseAuthHelper.initializeFirebaseAuth()
        checkIfUserIsAuthenticated()
        setLoading(false)
    }

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    private fun checkIfUserIsAuthenticated() {
        _userAuthenticationState.value = FirebaseAuthHelper.getCurrentUser() != null
    }

    fun googleLogin(context: Context) = viewModelScope.launch {

        setLoading(true)

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val credentialManager = CredentialManager.create(context)
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.GoogleLoginServerClientId))
            .setNonce(hashedNonce)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            FirebaseAuthHelper.firebaseAuthWithGoogle(googleIdToken) { task ->
                if (task.isSuccessful) {
                    val isNewUser = task.result.additionalUserInfo?.isNewUser
                    isNewUser(isNewUser, "구글")
                    setLoading(false)
                } else {
                    setLoading(false)
                    _userAuthenticationState.value = false
                    Log.d("test1234", "구글 사용자 로그인 실패1")
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && e is GetCredentialException) {
                    Log.d("test1234", "에러 ${e.message}")
                    setLoading(false)
                } else if (e is GoogleIdTokenParsingException) {
                    Log.d("test1234", "에러 ${e.message}")
                    setLoading(false)
                } else {
                    Log.d("test1234", "에러 ${e.message}")
                    setLoading(false)
                }
            }
        }
    }

    fun kakaoLogin(context: Context) = viewModelScope.launch {

        setLoading(true)

        KakaoSdk.init(context, context.getString(R.string.NATIVE_APP_KEY))

        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("test1234", "카카오계정으로 로그인 실패", error)
                setLoading(false)
            } else if (token != null) {
                Log.i("test1234", "카카오계정으로 로그인 성공 ${token.accessToken}")
                // 로그인한 사용자 정보를 가져온다.
                // 이 때 accessToken 을 카카오 서버로 전달해야 해야하는데 알아서해준다.
                UserApiClient.instance.me { user, error ->
                    if (error != null) {
                        Log.e("test1234", "사용자 정보를 가져오는데 실패하였습니다", error)
                        setLoading(false)

                    } else if (user != null) {

                        FirebaseAuthHelper.firebaseAuthWithKaKao(token.idToken!!) { task ->
                            if (task.isSuccessful) {
                                val isNewUser = task.result.additionalUserInfo?.isNewUser
                                Log.d("test1234", "$isNewUser")
                                isNewUser(isNewUser, "카카오")
                                setLoading(false)
                            } else {
                                try {
                                    throw task.exception!!

                                } catch (e: FirebaseAuthInvalidUserException) {
                                    Log.d("test1234", "잘못된 자격 증명")
                                    setLoading(false)
                                } catch (e: FirebaseAuthUserCollisionException) {
                                    Log.d("test1234", "이미 존재하는 식별자")
                                    Toast.makeText(context, "이미 존재하는 계정이 있습니다",Toast.LENGTH_SHORT).show()
                                    setLoading(false)
                                } catch (e: Exception) {
                                    Log.d("test1234", "기타 오류 $e")
                                    setLoading(false)
                                }

                                _userAuthenticationState.value = false
                                Log.d("test1234", "카카오 사용자 로그인 실패")
                                setLoading(false)
                            }
                        }
                    }
                }
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Log.e("test1234", "카카오톡으로 로그인 실패", error)
                    setLoading(false)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        setLoading(false)
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(
                        context,
                        callback = callback
                    )
                } else if (token != null) {
                    Log.i("test1234", "카카오톡으로 로그인 성공 ${token.accessToken}")
                    // 로그인한 사용자 정보를 가져온다.
                    // 이 때 accessToken 을 카카오 서버로 전달해야 해야하는데 알아서해준다.
                    UserApiClient.instance.me { user, error ->
                        if (error != null) {
                            Log.e("test1234", "사용자 정보를 가져오는데 실패하였습니다", error)
                            setLoading(false)
                        } else if (user != null) {
                            FirebaseAuthHelper.firebaseAuthWithKaKao(token.idToken!!) { task ->

                                if (task.isSuccessful) {
                                    val isNewUser = task.result.additionalUserInfo?.isNewUser
                                    Log.d("test1234", "새로운 사용자 : $isNewUser")
                                    isNewUser(isNewUser, "카카오")
                                    setLoading(false)
                                } else {

                                    try {
                                        throw task.exception!!

                                    } catch (e: FirebaseAuthInvalidUserException) {
                                        Log.d("test1234", "잘못된 자격 증명")
                                        setLoading(false)
                                    } catch (e: FirebaseAuthUserCollisionException) {
                                        Log.d("test1234", "이미 존재하는 식별자")
                                        Toast.makeText(context, "이미 존재하는 계정이 있습니다",Toast.LENGTH_SHORT).show()
                                        setLoading(false)
                                    } catch (e: Exception) {
                                        Log.d("test1234", "기타 오류 $e")
                                        setLoading(false)
                                    }

                                    _userAuthenticationState.value = false
                                    Log.d("test1234", "카카오 사용자 로그인 실패")
                                    setLoading(false)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
            setLoading(false)
        }
    }

    fun naverLogin(context: Context) = viewModelScope.launch {

        setLoading(true)

        // 네이버 로그인 SDK를 초기화합니다.
        NaverIdLoginSDK.initialize(
            context,
            context.getString(R.string.client_id),
            context.getString(R.string.client_secret),
            context.getString(R.string.client_name)
        )

        // OAuth 로그인 콜백을 정의합니다.
        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                // 네이버 로그인 인증이 성공했을 때 수행할 코드
                val accessToken = NaverIdLoginSDK.getAccessToken()
                getNaverCustomToken(context, accessToken!!)
            }

            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.w("test1234", "errorCode:$errorCode, errorDesc:$errorDescription")
                setLoading(false)
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        // 네이버 로그인을 시작합니다.
        NaverIdLoginSDK.authenticate(context, oauthLoginCallback)
    }

    // 네이버 사용자 CustomToken 생성 요청 함수
    private fun getNaverCustomToken(context: Context, accessToken: String) = viewModelScope.launch {

        val requestBody = FormBody.Builder()
            .add("naverAccessToken", accessToken)
            .build()
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(context.getString(R.string.GoogleCloudFunctions_naverLogin))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 네트워크 요청 실패 처리
                Log.e("test1234", "Failed to fetch user profile", e)
                setLoading(false)
            }

            override fun onResponse(call: Call, response: Response){
                if (response.isSuccessful) {

                    // 응답을 로그로 출력합니다.
                    val responseBody = response.body?.string()

                    val token = responseBody?.let { JSONObject(it).getString("firebaseToken") }

                    FirebaseAuthHelper.firebaseAuthWithNaver(token!!) { task ->
                        if (task.isSuccessful) {
                            val user = task.result.user

                            viewModelScope.launch {
                                isNewUser(!UserRepository.isUserExist(user?.uid!!), "네이버")
                            }
                            setLoading(false)
                        } else {
                            _userAuthenticationState.value = false
                            Log.d("test1234", "네이버 사용자 로그인 실패")
                            setLoading(false)
                        }
                    }
                } else {
                    // HTTP 요청 실패 처리
                    Log.e("test1234", "Failed to fetch user profile: ${response.code}")
                    setLoading(false)
                }
            }
        })
    }

    private fun isNewUser(isNewUser: Boolean?, siteName: String) = viewModelScope.launch {

        when (isNewUser) {
            false -> {
                // 기존 사용자이므로 로그인 상태를 true로 설정
                _userAuthenticationState.value = true
                Log.d("test1234", "기존 $siteName 사용자 로그인 완료")
            }

            true -> {
                val user: FirebaseUser? = FirebaseAuthHelper.getCurrentUser()
                UserRepository.addUser(
                    User(
                        id = user?.uid!!,
                        name = user.displayName.orEmpty(),
                        email = user.email.orEmpty()
                    )
                )

                _userAuthenticationState.value = true
                Log.d("test1234", "$siteName 사용자 정보 추가 완료")
            }

            else -> {
                // 로그인 실패 처리
                _userAuthenticationState.value = false
                Log.d("test1234", "$siteName 사용자 로그인 실패2")
            }
        }
    }
}