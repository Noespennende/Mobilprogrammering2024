package com.movielist.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.movielist.Screen
import com.movielist.controller.ControllerViewModel
import com.movielist.ui.theme.LocalColor
import com.movielist.ui.theme.LocalConstraints
import com.movielist.ui.theme.LocalTextFieldColors
import com.movielist.ui.theme.fontFamily
import com.movielist.ui.theme.headerSize
import com.movielist.ui.theme.textFieldColors
import com.movielist.ui.theme.weightBold
import com.movielist.ui.theme.weightRegular

@Composable
fun CreateUserScreen (controllerViewModel: ControllerViewModel, navController: NavController){

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }

    val checkForNoInputErrors: () -> Boolean = {
       if(username.length < 3){
           errorText = "The username must be at least 3 characters long"
           false
       } else if (email.length < 3 || !email.contains("@") || !email.contains(".")){
           errorText= "Please provide a valid email adress."
           false
       } else if (password.length < 5) {
           errorText= "The password must be at least 5 characters long"
           false
       } else {
           true
       }
    }

    val handleCreateUserClick: () -> Unit = {
        if (checkForNoInputErrors()){
            controllerViewModel.createUserWithEmailAndPassword(username, email, password,
                {
                    Log.d("CreateUser", "Success")
                    controllerViewModel.checkUserStatus()
                },
                { Log.d("CreateUser", "Failure") })
        }
    }

    var handleBackToLoginScreenClick: () -> Unit = {
        navController.navigate(Screen.LoginScreen.withArguments())
    }




    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
    ){
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .background(
                    color = LocalColor.current.backgroundLight,
                    shape = RoundedCornerShape(5.dp)
                )
                .fillMaxWidth(.9f)
                .padding(20.dp)
        ) {
            if (errorText.length > 0){
                //Error text
                Text(
                    text = errorText,
                    fontSize = headerSize,
                    fontFamily = fontFamily,
                    fontWeight = weightBold,
                    textAlign = TextAlign.Center,
                    color = LocalColor.current.complimentaryThree,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )
            }

            //Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it},
                singleLine = true,
                colors = LocalTextFieldColors.current.textFieldColors,
                textStyle = TextStyle(
                    fontSize = headerSize,
                    fontFamily = fontFamily,
                    fontWeight = weightRegular,
                    color = LocalColor.current.secondary,
                ),
                label = {Text(
                    "Username",
                    fontSize = headerSize,
                    fontFamily = fontFamily,
                    fontWeight = weightBold,
                    color = LocalColor.current.secondary,
                )},
                modifier = Modifier
                    .fillMaxWidth()
            )

            //Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it},
                singleLine = true,
                colors = LocalTextFieldColors.current.textFieldColors,
                textStyle = TextStyle(
                    fontSize = headerSize,
                    fontFamily = fontFamily,
                    fontWeight = weightRegular,
                    color = LocalColor.current.secondary,
                ),
                label = {Text(
                    "email",
                    fontSize = headerSize,
                    fontFamily = fontFamily,
                    fontWeight = weightBold,
                    color = LocalColor.current.secondary,
                )},
                modifier = Modifier
                    .fillMaxWidth()
            )

            //Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it},
                singleLine = true,
                colors = LocalTextFieldColors.current.textFieldColors,
                textStyle = TextStyle(
                    fontSize = headerSize,
                    fontFamily = fontFamily,
                    fontWeight = weightRegular,
                    color = LocalColor.current.secondary,
                ),
                label = {Text(
                    "Password",
                    fontSize = headerSize,
                    fontFamily = fontFamily,
                    fontWeight = weightBold,
                    color = LocalColor.current.secondary,
                )},
                modifier = Modifier
                    .fillMaxWidth()
            )

            //Create user button
            Box(
                modifier = Modifier
                    .clickable {
                        handleCreateUserClick()
                    }
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        color = LocalColor.current.primary,
                        shape = RoundedCornerShape(5.dp))
            ){
                Text(
                    "Create account",
                    fontSize = headerSize,
                    fontFamily = fontFamily,
                    fontWeight = weightBold,
                    color = LocalColor.current.background,
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                )

            }

            Text(
                "Already have an account? Login instead.",
                fontSize = headerSize,
                fontFamily = fontFamily,
                fontWeight = weightBold,
                textAlign = TextAlign.Center,
                color = LocalColor.current.primary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        handleBackToLoginScreenClick()
                    }
            )
        }
    }


}
