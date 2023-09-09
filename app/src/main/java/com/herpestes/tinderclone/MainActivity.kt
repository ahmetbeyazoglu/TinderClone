package com.herpestes.tinderclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.herpestes.tinderclone.ui.*
import com.herpestes.tinderclone.ui.theme.TinderCloneTheme


sealed class DestinationScreen(val route: String){
    object Signup : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Profile : DestinationScreen("profile")
    object Swipe : DestinationScreen("swipe")
    object ChatList : DestinationScreen("chatlist")
    object SingleChat: DestinationScreen("singleChat/{chatId}") {
        fun createRoute(id: String) = "SingleChat/$id"
    }



}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TinderCloneTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SwipeAppNavigation()
                }
            }
        }
    }
}

@Composable
fun SwipeAppNavigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = DestinationScreen.Swipe.route){
        composable(DestinationScreen.Signup.route){
            SignupScreen()
        }
        composable(DestinationScreen.Login.route){
            LoginScreen()
        }
        composable(DestinationScreen.Profile.route){
            ProfileScreen(navController)
        }
        composable(DestinationScreen.Swipe.route){
            SwipeCards(navController)
        }
        composable(DestinationScreen.ChatList.route){
            ChatListScreen(navController)
        }
        composable(DestinationScreen.SingleChat.route){
            SingleChatScreen(chatId = "123")
        }
    }
}


