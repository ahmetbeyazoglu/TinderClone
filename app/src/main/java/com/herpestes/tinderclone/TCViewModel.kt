package com.herpestes.tinderclone

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.herpestes.tinderclone.data.*
import com.herpestes.tinderclone.ui.Gender
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TCViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) :ViewModel() {

    //progress bar state
    val inProgress = mutableStateOf(false)

    val popupNotification = mutableStateOf<Event<String>?>(null)
    val signedIn = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)

    val matchProfiles = mutableStateOf<List<UserData>>(listOf())
    val inProgressProfile = mutableStateOf(false)


    val chats = mutableStateOf<List<ChatData>>(listOf())
    val inProgressChats = mutableStateOf(false)

    val inProgressChatMessages = mutableStateOf(false)
    val chatMessages = mutableStateOf<List<Messages>>(listOf())
    var currentChatMessagesListener: ListenerRegistration? = null


    init {
        auth.signOut()
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid)
        }

    }



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
                            if(task.isSuccessful) {
                                signedIn.value = true
                                createOrUpdateProfile(username = username)
                            }

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

    fun onLogin(email: String, pass: String){
        if(email.isEmpty() or pass.isEmpty()){
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true
        auth.signInWithEmailAndPassword(email,pass)
            .addOnCompleteListener { task ->
            if(task.isSuccessful){
                signedIn.value = true
                inProgress.value = false
                auth.currentUser?.uid?.let {
                    getUserData(it)
                }
            }else
                handleException(task.exception, "Login failed")
            }
            .addOnFailureListener {
                handleException(it, "Login failed")
            }
    }
    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null,
        gender: Gender? = null,
        genderPrefence: Gender? = null,
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?:userData.value?.name,
            username = username?:userData.value?.username,
            imageUrl = imageUrl ?:userData.value?.imageUrl,
            bio = bio ?:userData.value?.bio,
            gender = gender ?.toString() ?: userData.value?.gender,
            genderPrefence = genderPrefence ?.toString() ?: userData.value?.genderPrefence,

        )

        uid?.let {  uid ->
            inProgress.value = true
            db.collection(COLLECTIN_USER).document(uid)
                .get()
                .addOnSuccessListener {
                    if (it.exists())
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener {
                                this.userData.value = userData
                                inProgress.value = false
                                populateCards()
                            }
                            .addOnFailureListener {
                                handleException(it, "Cannot update user")
                            }
                    else{
                        db.collection(COLLECTIN_USER).document(uid).set(userData)
                        inProgress.value = false
                        getUserData(uid)
                    }
                }
                .addOnFailureListener {
                    handleException(it, "Cannot create user")
                }
        }


    }

    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(COLLECTIN_USER).document(uid)
            .addSnapshotListener { value, error ->
                if (error != null)
                    handleException(error, "Cannot retrieve user data")
                if (value != null) {
                    val user = value.toObject<UserData>()
                    userData.value = user
                    inProgress.value = false
                    populateCards()
                    populateChats()
                }
            }
    }

    fun onLogout(){
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event("Logged out")
    }

    fun updateProfileData(
        name:String,
        username:String,
        bio:String,
        gender: Gender,
        genderPrefence: Gender
    ){
        createOrUpdateProfile(
            name = name,
            username = username,
            bio = bio,
            gender = gender,
            genderPrefence = genderPrefence
        )
    }

    private fun uploadImage(uri: Uri, onSucces: (Uri) -> Unit){
        inProgress.value = true

        val storageRef = storage.reference
        val uuid =UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask
            .addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener(onSucces)
        }
            .addOnFailureListener {
                handleException(it)
                inProgress.value = false
            }
    }

    fun uploadProfileImage(uri: Uri){
        uploadImage(uri){
            createOrUpdateProfile(imageUrl = it.toString())
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

    private fun populateCards() {
        inProgressProfile.value = true

        val g = if (userData.value?.gender.isNullOrEmpty()) "ANY"
        else userData.value!!.gender!!.uppercase()
        val gPref = if (userData.value?.genderPrefence.isNullOrEmpty()) "ANY"
        else userData.value!!.genderPrefence!!.uppercase()

        val cardsQuery =
            when (Gender.valueOf(gPref)) {
                Gender.MALE -> db.collection(COLLECTIN_USER)
                    .whereEqualTo("gender", Gender.MALE)
                Gender.FEMALE -> db.collection(COLLECTIN_USER)
                    .whereEqualTo("gender", Gender.FEMALE)
                Gender.ANY -> db.collection(COLLECTIN_USER)
            }
        val userGender = Gender.valueOf(g)

        cardsQuery.where(
            Filter.and(
                Filter.notEqualTo("userId", userData.value?.userId),
                Filter.or(
                    Filter.equalTo("genderPrefence", userGender),
                    Filter.equalTo("genderPrefence", Gender.ANY)
                )
            )
        )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    inProgressProfile.value = false
                    handleException(error)
                }
                if (value != null) {
                    val potentials = mutableListOf<UserData>()
                    value.documents.forEach {
                        it.toObject<UserData>()?.let { potential ->
                            var showUser = true
                            if (userData.value?.swipesLeft?.contains(potential.userId) == true ||
                                userData.value?.swipesRight?.contains(potential.userId) == true ||
                                userData.value?.matches?.contains(potential.userId) == true
                            )
                                showUser = false
                            if (showUser)
                                potentials.add(potential)
                        }
                    }

                    matchProfiles.value = potentials
                    inProgressProfile.value = false


                }

            }
    }

    fun onDislike(selectedUser: UserData) {
        db.collection(COLLECTIN_USER).document(userData.value?.userId ?: "")
            .update("swipesLeft", FieldValue.arrayUnion(selectedUser.userId))
    }

    fun onLike(selectedUser: UserData) {
        val reciprocalMatch = selectedUser.swipesRight.contains(userData.value?.userId)
        if (!reciprocalMatch) {
            db.collection(COLLECTIN_USER).document(userData.value?.userId ?: "")
                .update("swipesRight", FieldValue.arrayUnion(selectedUser.userId))
        } else {
            popupNotification.value = Event("Match!")
            db.collection(COLLECTIN_USER).document(selectedUser.userId ?: "")
                .update("swipesRight", FieldValue.arrayRemove(userData.value?.userId))
            db.collection(COLLECTIN_USER).document(selectedUser.userId ?: "")
                .update("matches", FieldValue.arrayUnion(userData.value?.userId))
            db.collection(COLLECTIN_USER).document(userData.value?.userId ?: "")
                .update("matches", FieldValue.arrayUnion(selectedUser.userId))

            val chatKey = db.collection(COLLECTION_CHAT).document().id
            val chatData = ChatData(
                chatKey,
                ChatUser(
                    userData.value?.userId,
                    if(userData.value?.name.isNullOrEmpty()) userData.value?.username
                    else userData.value?.name,

                    userData.value?.imageUrl
                ),
                ChatUser(
                    selectedUser.userId,
                    if(selectedUser.name.isNullOrEmpty()) selectedUser.username
                        else selectedUser.name,

                    selectedUser.imageUrl
                ),

                )
            db.collection(COLLECTION_CHAT).document(chatKey).set(chatData)

        }
    }

    private fun populateChats(){
        inProgress.value = true
        db.collection(COLLECTION_CHAT).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId),
            )
        )
            .addSnapshotListener{ value, error ->
                inProgressChats.value = false
                if (error != null)
                    handleException(error)
                if (value != null)
                    chats.value = value.documents.mapNotNull { it.toObject<ChatData>() }
                inProgressChats.value = false


            }
    }

    fun onSendReply(chatId: String, message: String) {
        val time = Calendar.getInstance().time.toString()
        val message = Messages(userData.value?.userId, message, time)
        db.collection(COLLECTION_CHAT).document(chatId).collection(COLLECTION_MESSAGES).document()
            .set(message)
    }

    fun populateChat(chatId: String){
        inProgressChatMessages.value = true
        currentChatMessagesListener = db.collection(COLLECTION_CHAT)
            .document(chatId)
            .collection(COLLECTION_MESSAGES)
            .addSnapshotListener{ value, error ->
                if (error != null)
                    handleException(error)
                if(value != null)
                    chatMessages.value = value.documents
                        .mapNotNull { it.toObject<Messages>() }
                        .sortedBy { it.timestamp }
                inProgressChatMessages.value = false

            }
    }

    fun depopulateChat(){
        currentChatMessagesListener = null
        chatMessages.value = listOf()
    }


}