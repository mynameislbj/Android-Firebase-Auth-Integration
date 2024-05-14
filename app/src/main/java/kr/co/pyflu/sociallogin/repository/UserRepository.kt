package kr.co.pyflu.sociallogin.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kr.co.pyflu.sociallogin.model.User

class UserRepository {

    companion object{

        suspend fun addUser(user: User){
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("users").document(user.id).set(user).await()
            }catch (e: Exception){
                null
            }
        }

        suspend fun getUser(uid: String): User? = withContext(Dispatchers.IO){
            val db = FirebaseFirestore.getInstance()
            try {
                val documentSnapshot = db.collection("users").document(uid).get().await()
                documentSnapshot.toObject<User?>()
            }catch (e: Exception){
                null
            }
        }

        suspend fun updateUser(user: User) {
            val db = FirebaseFirestore.getInstance()
            try {
                val userUpdates = hashMapOf<String, Any>()
                if (user.name.isNotEmpty()) {
                    userUpdates["name"] = user.name
                }
                if (user.email.isNotEmpty()) {
                    userUpdates["email"] = user.email
                }
                if (userUpdates.isNotEmpty()) {
                    db.collection("users").document(user.id).update(userUpdates).await()
                }
            } catch (e: Exception) {
                // 오류 처리
            }
        }


        suspend fun deleteUser(uid: String){
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("users").document(uid).delete().await()
            }catch (e: Exception){
            }
        }

        suspend fun isUserExist(uid: String): Boolean = withContext(Dispatchers.IO){
            val db = FirebaseFirestore.getInstance()
            try {
                val document = db.collection("users").document(uid).get().await()
                document.exists()
            }catch (e: Exception){
                false
            }
        }
    }
}