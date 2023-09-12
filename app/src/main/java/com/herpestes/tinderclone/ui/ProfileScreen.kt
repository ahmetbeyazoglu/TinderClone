package com.herpestes.tinderclone.ui


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.herpestes.tinderclone.CommonProgressSpinner
import com.herpestes.tinderclone.TCViewModel


enum class Gender{
    MALE, FEMALE, ANY
}

@Composable
fun ProfileScreen(navController: NavController, vm: TCViewModel) {
    val inProgress = vm.inProgress.value
    if (inProgress)
        CommonProgressSpinner()
    else {
        val userData = vm.userData.value
        var name by rememberSaveable { mutableStateOf(userData?.name ?: "") }
        var username by rememberSaveable { mutableStateOf(userData?.username ?: "") }
        var bio by rememberSaveable { mutableStateOf(userData?.bio ?: "") }
        var gender by rememberSaveable {
            mutableStateOf(Gender.valueOf(userData?.gender?.uppercase() ?: "MALE"))
        }
        var genderPreference by rememberSaveable {
            mutableStateOf(Gender.valueOf(userData?.genderPrefence?.uppercase() ?: "FEMALE"))
        }

        val scrollState = rememberScrollState()

        Column {
            ProfileContent()

            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.PROFILE,
                navController = navController
            )

        }

    }
}

@Composable
fun ProfileContent(
    modifier: Modifier,
    vm: TCViewModel,
    name: String,
    username: String,
    bio: String,
    gender: Gender,
    genderPreference: Gender,
    onNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onGenderPreferenceChange: (Gender) -> Unit,
    onSave:() -> Unit,
    onBack:() -> Unit,
    onLogout:() -> Unit,

) {

}


