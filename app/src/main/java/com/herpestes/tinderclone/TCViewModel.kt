package com.herpestes.tinderclone

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.herpestes.tinderclone.data.COLLECTIN_USER
import com.herpestes.tinderclone.data.Event
import com.herpestes.tinderclone.data.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TCViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) :ViewModel() {

    //progress bar state
    val inProgress = mutableStateOf(false)

    val popupNotification = mutableStateOf<Event<String>?>(Event("Test"))

    fun onSignup(username: String, email: String, pass:String){
        if(username.isEmpty() or email.isEmpty() or pass.isEmpty()){
            handleException(customMessage = "Please fill in all fiels")
        }
        inProgress.value = true
        db.collection(COLLECTIN_USER).whereEqualTo("username", username)
            .get()
            .addOnSuccessListener {
                if(it.isEmpty){
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener {  task ->
                            if(task.isSuccessful)
                                createOrUpdateProfile(username = username)
                            else
                                handleException(task.exception, "Signup failed")
                        }

                } else
                    handleException(customMessage = "username already exists")
                inProgress.value = false
            }
            .addOnFailureListener {
                handleException(it)
            }
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null,
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name,
            username = username,
            imageUrl = imageUrl,
            bio = bio
        )

        uid?.let {  uid ->
            inProgress.value = true
            db.collection(COLLECTIN_USER).document(uid)
                .get()
                .addOnSuccessListener {
                    if (it.exists())
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener {
                                inProgress.value = false
                            }
                            .addOnFailureListener {
                                handleException(it, "Cannot update user")
                            }
                    else{
                        db.collection(COLLECTIN_USER).document(uid).set(userData)
                        inProgress.value = false
                    }
                }
                .addOnFailureListener {
                    handleException(it, "Cannot create user")
                }
        }


    }


    private fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("TinderClone", "Tinder Exception", exception)
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isNotEmpty()) errorMsg else "$customMessage: $errorMsg"
        popupNotification.value = Event(message)
        inProgress.value = false
    }

}