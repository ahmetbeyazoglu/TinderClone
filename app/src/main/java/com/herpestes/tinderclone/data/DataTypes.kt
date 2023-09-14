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