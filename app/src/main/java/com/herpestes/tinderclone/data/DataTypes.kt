package com.herpestes.tinderclone.data

data class UserData(
    var userId: String? = "",
    var name: String? = "",
    var username: String? = "",
    var imageUrl: String? = "",
    var bio: String? = "",
    var gender: String? = "",
    var genderPrefence: String? = "",
    var swipesLeft: List<String> = listOf(),
    var swipesRight: List<String> = listOf(),
    var matches: List<String> = listOf(),

){
    fun toMap() = mapOf(
        "userId" to userId,
        "name" to name,
        "username" to username,
        "imageurl" to imageUrl,
        "bio" to bio,
        "gender" to gender,
        "genderPrefence" to genderPrefence,
        "swipesLeft" to swipesLeft,
        "swipesRight" to swipesRight,
        "matches" to matches
    )
}
data class ChatData(
    var chatId: String? = "",
    var user1: ChatUser = ChatUser(),
    var user2: ChatUser = ChatUser()
)
data class ChatUser(
    var userId: String? = "",
    var name: String? = "",
    var imageUrl: String? = ""

)

data class Messages(
    val sentBy: String? = null,
    val message: String? = null,
    val timestamp: String? = null
)