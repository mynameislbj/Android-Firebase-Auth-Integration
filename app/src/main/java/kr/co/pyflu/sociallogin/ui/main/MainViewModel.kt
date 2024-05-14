package kr.co.pyflu.sociallogin.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.pyflu.sociallogin.FirebaseAuthHelper
import kr.co.pyflu.sociallogin.model.User
import kr.co.pyflu.sociallogin.repository.UserRepository

class MainViewModel : ViewModel() {

    val userId = MutableLiveData<String>()
    val userName = MutableLiveData<String>()
    val userEmail = MutableLiveData<String>()

    init {
        getUserInfo()
    }

    fun getUserInfo() = viewModelScope.launch{
        val id = FirebaseAuthHelper.getCurrentUser()?.uid
        val user = UserRepository.getUser(id!!)

        user?.let {
            userId.value = it.id
            userName.value = it.name
            userEmail.value = it.email
        }
    }

    fun updateUserInfo() = viewModelScope.launch{

        val user = User(
            id = userId.value.orEmpty(),
            name = userName.value.orEmpty(),
            email = userEmail.value.orEmpty(),
        )

        UserRepository.updateUser(user)
    }
}